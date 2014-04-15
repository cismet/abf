/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.abf.librarysupport.JarHandler;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.librarysupport.project.util.Utils;
import de.cismet.cids.abf.utilities.ModificationStore;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.3
 */
public final class DeployChangedJarsAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DeployChangedJarsAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(DeployChangedJarsAction.class,
                "DeployChangedJarsAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return LibrarySupportProject.IMAGE_FOLDER + "einpflegenChanged_24.png"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final List<DeployInformation> infos = new LinkedList<DeployInformation>();
        for (final Node n : nodes) {
            for (final Node ch : n.getChildren().getNodes()) {
                final DeployInformation info = DeployInformation.getDeployInformation(ch);
                if (ModificationStore.getInstance().anyModifiedInContext(
                                FileUtil.toFile(info.getSourceDir()).getAbsolutePath(),
                                ModificationStore.MOD_CHANGED)) {
                    infos.add(info);
                }
            }
        }
        try {
            JarHandler.deployAllJars(infos, JarHandler.ANT_TARGET_DEPLOY_CHANGED_JARS);
            for (final DeployInformation info : infos) {
                ModificationStore.getInstance()
                        .removeAllModificationsInContext(
                            FileUtil.toFile(info.getSourceDir()).getAbsolutePath(),
                            ModificationStore.MOD_CHANGED);
            }
        } catch (final IOException ex) {
            LOG.warn("could not deploy changed jars", ex); // NOI18N
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        boolean enable = false;
        if ((nodes == null) || (nodes.length < 1)) {
            return false;
        }
        for (final Node n : nodes) {
            final LocalManagementContextCookie l = n.getCookie(
                    LocalManagementContextCookie.class);
            final StarterManagementContextCookie s = n.getCookie(
                    StarterManagementContextCookie.class);
            if ((l == null) && (s == null)) {
                return false;
            }
            for (final Node ch : n.getChildren().getNodes()) {
                final DeployInformation info = DeployInformation.getDeployInformation(ch);
                if (ModificationStore.getInstance().anyModifiedInContext(
                                FileUtil.toFile(info.getSourceDir()).getAbsolutePath(),
                                ModificationStore.MOD_CHANGED)) {
                    enable = true;
                    break;
                }
            }
        }
        if (!enable) {
            return false;
        }
        final LibrarySupportContextCookie lscc = nodes[0].getCookie(
                LibrarySupportContextCookie.class);
        if (lscc == null) {
            return false;
        }
        final PropertyProvider provider = PropertyProvider.getInstance(lscc.getLibrarySupportContext()
                        .getProjectProperties());
        final String ks = Utils.getPath(provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE));
        if (ks == null) {
            return false;
        } else {
            final File f = new File(ks);

            return f.exists() && f.canRead();
        }
    }
}

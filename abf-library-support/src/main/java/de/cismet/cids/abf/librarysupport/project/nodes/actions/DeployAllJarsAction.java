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
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.utilities.ModificationStore;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.5
 */
public final class DeployAllJarsAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DeployAllJarsAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(DeployAllJarsAction.class,
                "DeployAllJarsAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return LibrarySupportProject.IMAGE_FOLDER
                    + "einpflegenAll_24.gif"; // NOI18N
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
                infos.add(DeployInformation.getDeployInformation(ch));
            }
        }
        try {
            JarHandler.deployAllJars(infos, JarHandler.ANT_TARGET_DEPLOY_ALL_JARS);
            for (final Node n : nodes) {
                final SourceContextCookie sourceCookie = (SourceContextCookie)n.getCookie(SourceContextCookie.class);
                ModificationStore.getInstance()
                        .removeAllModificationsInContext(
                            FileUtil.toFile(sourceCookie.getSourceObject()).getAbsolutePath(),
                            ModificationStore.MOD_CHANGED);
            }
        } catch (final IOException ex) {
            LOG.warn("could not deploy all jars", ex); // NOI18N
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length < 1)) {
            return false;
        }
        for (final Node n : nodes) {
            final LocalManagementContextCookie l = (LocalManagementContextCookie)n.getCookie(
                    LocalManagementContextCookie.class);
            final StarterManagementContextCookie s = (StarterManagementContextCookie)n.getCookie(
                    StarterManagementContextCookie.class);
            if ((l == null) && (s == null)) {
                return false;
            }
        }
        final LibrarySupportContextCookie lscc = (LibrarySupportContextCookie)nodes[0].getCookie(
                LibrarySupportContextCookie.class);
        if (lscc == null) {
            return false;
        }
        final PropertyProvider provider = PropertyProvider.getInstance(lscc.getLibrarySupportContext()
                        .getProjectProperties());
        final File f = new File(provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE));
        return f.exists() && f.canRead();
    }
}

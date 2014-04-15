/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

import java.io.File;
import java.io.IOException;

import de.cismet.cids.abf.librarysupport.JarHandler;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.librarysupport.project.util.Utils;
import de.cismet.cids.abf.utilities.ModificationStore;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.7
 */
public final class DeployJarAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DeployJarAction.class);

    //~ Instance fields --------------------------------------------------------

    private transient boolean enableAction;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        final String label = NbBundle.getMessage(DeployJarAction.class,
                "DeployJarAction.getName().label");                            // NOI18N
        if (enableAction) {
            return label;
        } else {
            return label
                        + org.openide.util.NbBundle.getMessage(
                            DeployJarAction.class,
                            "DeployJarAction.getName().label.keystoreNotSet"); // NOI18N
        }
    }

    @Override
    protected String iconResource() {
        return LibrarySupportProject.IMAGE_FOLDER + "einpflegenSingle_24.gif"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  n  DOCUMENT ME!
     */
    public void deploy(final Node n) {
        try {
            JarHandler.deployJar(DeployInformation.getDeployInformation(n));
            // remove all modifications that were created on basis of change of
            // this sourcefoldernode and all its children
            final SourceContextCookie sourceCookie = n.getCookie(SourceContextCookie.class);
            ModificationStore.getInstance()
                    .removeAllModificationsInContext(
                        FileUtil.toFile(sourceCookie.getSourceObject()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
        } catch (final IOException ex) {
            LOG.error("could not deploy jar", ex);                         // NOI18N
            ErrorManager.getDefault()
                    .annotate(
                        ex,
                        org.openide.util.NbBundle.getMessage(
                            DeployJarAction.class,
                            "DeployJarAction.deploy().incorporateError")); // NOI18N
        }
    }

    @Override
    protected void performAction(final Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            deploy(nodes[i]);
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length < 1)) {
            return false;
        }
        for (final Node n : nodes) {
            if ((n.getCookie(LocalManagementContextCookie.class) != null)
                        || (n.getCookie(StarterManagementContextCookie.class) != null)
                        || (n.getCookie(SourceContextCookie.class) == null)) {
                return false;
            }
        }
        final LibrarySupportContextCookie lscc = nodes[0].getCookie(LibrarySupportContextCookie.class);
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
            enableAction = f.exists() && f.canRead();

            return enableAction;
        }
    }
}

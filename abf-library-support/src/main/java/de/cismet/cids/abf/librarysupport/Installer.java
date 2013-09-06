/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport;

import org.apache.log4j.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProjectNode;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.DeployChangedJarsAction;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.utilities.ModificationStore;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.5
 */
public final class Installer extends ModuleInstall {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1986024226648706520L;
    private static final transient Logger LOG = Logger.getLogger(Installer.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean closing() {
        boolean cleanupRequested = false;
        final ArrayList<Node> libNodes = new ArrayList<Node>(2);
        for (final Project p : OpenProjects.getDefault().getOpenProjects()) {
            final LibrarySupportProjectNode lspn = p.getLookup().lookup(LibrarySupportProjectNode.class);
            if (lspn != null) {
                final String path = FileUtil.toFile(p.getProjectDirectory()).getAbsolutePath();
                if (ModificationStore.getInstance().anyModifiedInContext(path, ModificationStore.MOD_CHANGED)) {
                    if (!cleanupRequested) {
                        final int answer = JOptionPane.showConfirmDialog(
                                WindowManager.getDefault().getMainWindow(),
                                NbBundle.getMessage(
                                    this.getClass(),
                                    "Installer.closing().JOptionPane.confirmDialog.message"), // NOI18N
                                NbBundle.getMessage(
                                    this.getClass(),
                                    "Installer.closing().JOptionPane.confirmDialog.title"), // NOI18N
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (answer == JOptionPane.CANCEL_OPTION) {
                            return false;
                        } else if (answer == JOptionPane.NO_OPTION) {
                            return true;
                        } else if (answer == JOptionPane.YES_OPTION) {
                            cleanupRequested = true;
                        }
                    }
                    libNodes.add(lspn);
                }
            }
        }
        // node list will have at least one entry
        return performDeploy(libNodes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   libNodes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean performDeploy(final List<Node> libNodes) {
        final List<DeployInformation> infos = new LinkedList<DeployInformation>();
        final ModificationStore modStore = ModificationStore.getInstance();
        for (final Node libNode : libNodes) {
            for (final Node node : libNode.getChildren().getNodes(true)) {
                for (final Action a : node.getActions(false)) {
                    // if this action is registered it node should be of type
                    // LocalManagement or StarterManagement
                    if (a instanceof DeployChangedJarsAction) {
                        for (final Node ch : node.getChildren().getNodes()) {
                            final DeployInformation info = DeployInformation.getDeployInformation(ch);
                            final String path = FileUtil.toFile(info.getSourceDir()).getAbsolutePath();
                            if (modStore.anyModifiedInContext(path, ModificationStore.MOD_CHANGED)) {
                                infos.add(info);
                            }
                        }
                        // continue with outer loop since only one action of
                        // this type should be registered
                        break;
                    }
                }
            }
        }
        try {
            JarHandler.deployAllJars(infos, JarHandler.ANT_TARGET_DEPLOY_CHANGED_JARS);
            for (final DeployInformation info : infos) {
                final String path = FileUtil.toFile(info.getSourceDir()).getAbsolutePath();
                modStore.removeAllModificationsInContext(path, ModificationStore.MOD_CHANGED);
            }
            return true;
        } catch (final IOException ex) {
            LOG.warn("could not deploy changed jars", ex); // NOI18N
            return false;
        }
    }
}

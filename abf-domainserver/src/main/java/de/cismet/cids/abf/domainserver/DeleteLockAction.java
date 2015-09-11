/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.meta.CsLocksConnection;
import de.cismet.cids.meta.CsLocksConnection.LockEntry;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DeleteLockAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DeleteLockAction.class);

    private static final RequestProcessor proc = new RequestProcessor("DeleteLockActionRP", 7); // NOI18N

    private static final String DELETE_LOCK_STMT = "DELETE FROM cs_locks WHERE id = "; // NOI18N

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] activatedNodes) {
        final DomainserverProject project = activatedNodes[0].getLookup().lookup(DomainserverProject.class);
        final int answer = JOptionPane.showOptionDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class,
                    "DeleteLockAction.performAction(Node[]).JOptionPane.message"),       // NOI18N
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class,
                    "DeleteLockAction.performAction(Node[]).JOptionPane.title"),         // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[] {
                    org.openide.util.NbBundle.getMessage(
                        DeleteLockAction.class,
                        "DeleteLockAction.performAction(Node[]).JOptionPane.yesOption"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        DeleteLockAction.class,
                        "DeleteLockAction.performAction(Node[]).JOptionPane.noOption")   // NOI18N
                },
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class,
                    "DeleteLockAction.performAction(Node[]).JOptionPane.noOption"));     // NOI18N
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        CsLocksConnection con = null;
        try {
            con = new CsLocksConnection(project.getRuntimeProps());
            con.releaseAllLocks(DomainserverProject.LOCK_PREFIX);
        } catch (final Exception e) {
            LOG.error("could not remove lock", e);                                       // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class,
                    "DeleteLockAction.performAction(Node[]).ErrorUtils.atLeastOneLockUndeleteableError.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class,
                    "DeleteLockAction.performAction(Node[]).ErrorUtils.atLeastOneLockUndeleteableError.title"),
                e);                                                                      // NOI18N
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteLockAction.class, "DeleteLockAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "unlock.png"; // NOI18N
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }

        final DomainserverProject project = nodes[0].getLookup().lookup(DomainserverProject.class);
        if ((project == null) || project.isConnected() || project.isConnectionInProgress()) {
            return false;
        }

        if (!nodes[0].getName().equals(project.getProjectDirectory().getName())) {
            return false;
        }

        return lockExists(project);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   project  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean lockExists(final DomainserverProject project) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checking for locks"); // NOI18N
        }
        CsLocksConnection con = null;
        try {
            final FutureTask<CsLocksConnection> task = new FutureTask(new Callable<CsLocksConnection>() {

                        @Override
                        public CsLocksConnection call() throws Exception {
                            return new CsLocksConnection(project.getRuntimeProps());
                        }
                    });
            proc.post(task);
            con = task.get(300, TimeUnit.MILLISECONDS);
            final LockEntry le = con.getLock(DomainserverProject.LOCK_PREFIX);

            return le != null;
        } catch (final Exception e) {
            LOG.warn("could not check for locks", e); // NOI18N
            // possibly notify the user
            return false;
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }
}

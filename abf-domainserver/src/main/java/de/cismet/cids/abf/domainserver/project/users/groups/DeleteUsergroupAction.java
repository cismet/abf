/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users.groups;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.user.UserGroup;

// TODO: why is a separate action used, destroy would probably be sufficient
/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DeleteUsergroupAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1959785591554761172L;
    private static final transient Logger LOG = Logger.getLogger(DeleteUsergroupAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        final int answer = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(DeleteUsergroupAction.class, "Dsc_reallyDeleteUsergroupQuestion"), // NOI18N
                org.openide.util.NbBundle.getMessage(DeleteUsergroupAction.class, "Dsc_deleteUsergroup"), // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            for (final Node n : nodes) {
                UserGroup ug = null;
                try {
                    final DomainserverProject project = n.getCookie(DomainserverContext.class).getDomainserverProject();
                    final Backend backend = project.getCidsDataObjectBackend();
                    ug = n.getCookie(UserGroupContextCookie.class).getUserGroup();
                    backend.delete(ug);
                    project.getLookup().lookup(UserManagement.class).refreshChildren();
                } catch (final Exception e) {
                    LOG.error("could not delete usergroup: " + ug, e);                                    // NOI18N
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteUsergroupAction.class, "CTL_DeleteUsergroupAction"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "delete_group.png"; // NOI18N
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
    protected int mode() {
        return MODE_ALL;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                UserGroupContextCookie.class
            };
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }

        final DomainserverContext first = nodes[0].getCookie(DomainserverContext.class);
        if ((first == null) || !first.getDomainserverProject().isConnected()) {
            return false;
        }

        for (final Node node : nodes) {
            final UserGroupContextCookie ucc = node.getCookie(UserGroupContextCookie.class);
            final DomainserverContext context = node.getCookie(DomainserverContext.class);

            if ((ucc == null) || (context == null)
                        || !context.getDomainserverProject().equals(first.getDomainserverProject())) {
                return false;
            }
        }

        return true;
    }
}

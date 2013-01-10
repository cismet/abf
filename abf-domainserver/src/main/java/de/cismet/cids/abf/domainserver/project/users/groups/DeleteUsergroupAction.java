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

import java.io.IOException;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;

// TODO: why is a separate action used, destroy would probably be sufficient
/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DeleteUsergroupAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DeleteUsergroupAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        final int answer = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(
                    DeleteUsergroupAction.class,
                    "DeleteUsergroupAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    DeleteUsergroupAction.class,
                    "DeleteUsergroupAction.performAction(Node[]).JOptionPane.title"), // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();

            for (final Node n : nodes) {
                try {
                    n.destroy();
                } catch (final IOException ex) {
                    LOG.error("could not delete node: " + n, ex); // NOI18N
                }
            }

            project.getLookup().lookup(UserManagement.class).refresh();
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteUsergroupAction.class, "DeleteUsergroupAction.getName().returnvalue"); // NOI18N
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

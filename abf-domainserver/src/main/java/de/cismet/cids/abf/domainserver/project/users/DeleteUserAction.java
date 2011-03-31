/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

import java.io.IOException;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
// TODO: why not rely on destroy solely?
public final class DeleteUserAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DeleteUserAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteUserAction.class, "DeleteUserAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "delete_user.png"; // NOI18N
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
                UserContextCookie.class
            };
    }

    @Override
    protected void performAction(final Node[] node) {
        final int answer = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(
                    DeleteUserAction.class,
                    "DeleteUserAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                NbBundle.getMessage(
                    DeleteUserAction.class,
                    "DeleteUserAction.performAction(Node[]).JOptionPane.title"), // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            for (final Node n : node) {
                try {
                    n.destroy();
                } catch (final IOException ex) {
                    final String name = n.getCookie(UserContextCookie.class).getUser().getLoginname();
                    LOG.error("could not delete user: " + name);                 // NOI18N
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
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
            final UserContextCookie ucc = node.getCookie(UserContextCookie.class);
            final DomainserverContext context = node.getCookie(DomainserverContext.class);

            if ((ucc == null) || (context == null)
                        || !context.getDomainserverProject().equals(first.getDomainserverProject())) {
                return false;
            }
        }

        return true;
    }
}

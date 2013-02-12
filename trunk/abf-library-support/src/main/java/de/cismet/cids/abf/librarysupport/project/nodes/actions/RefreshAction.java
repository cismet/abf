/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.RefreshCookie;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class RefreshAction extends CallableSystemAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void performAction() {
        final Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        for (final Node node : nodes) {
            final RefreshCookie cookie = (RefreshCookie)node.getCookie(
                    RefreshCookie.class);
            if (cookie != null) {
                cookie.refresh();
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RefreshAction.class,
                "RefreshAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return LibrarySupportProject.IMAGE_FOLDER + "reload_22.png"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

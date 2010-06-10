/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.Refreshable;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class RefreshAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 9029465562426310703L;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(RefreshAction.class, "CTL_RefreshAction"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "refresh.png"; // NOI18N
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
        return new Class[] { Refreshable.class };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        for (final Node n : nodes) {
            n.getCookie(Refreshable.class).refresh();
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }

        for (final Node n : nodes) {
            if (n.getCookie(DomainserverContext.class) != null) {
                final DomainserverContext dc = n.getCookie(DomainserverContext.class);
                if (!dc.getDomainserverProject().isConnected()) {
                    return false;
                }
            }
        }

        return true;
    }
}

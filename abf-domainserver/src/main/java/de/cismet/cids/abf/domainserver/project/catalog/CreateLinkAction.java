/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class CreateLinkAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -8864866125314813604L;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(CreateLinkAction.class, "CreateLinkAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                CatalogNodeContextCookie.class
            };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final CatalogNodeContextCookie[] catNodeCookies = new CatalogNodeContextCookie[nodes.length];
        for (int i = 0; i < nodes.length; ++i) {
            catNodeCookies[i] = nodes[i].getCookie(CatalogNodeContextCookie.class);
        }
        project.setLinkableCatNodeCookies(catNodeCookies);
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        final boolean enable = super.enable(nodes);
        if (!enable) {
            return false;
        }

        // the selected nodes must live within the same project and they may not be dynamically created
        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        for (int i = 0; i < nodes.length; ++i) {
            if (!project.equals(nodes[i].getCookie(DomainserverContext.class).getDomainserverProject())) {
                return false;
            }

            // don't know why we get to this point as the cookie action should handle this...
            final CatalogNodeContextCookie cncc = nodes[i].getCookie(CatalogNodeContextCookie.class);
            if(cncc == null){
                return false;
            }

            final CatNode node = cncc.getCatNode();
            final CatNode parent = cncc.getParent();
            if ((node.getId() < 1) || ((parent != null) && (parent.getDynamicChildren() != null))) {
                return false;
            }
        }

        return true;
    }
}

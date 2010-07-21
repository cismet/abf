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
public class InsertLinkAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2634047435175113521L;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(InsertLinkAction.class, "InsertLinkAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
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
        final CatalogNodeContextCookie[] cookies = project.getLinkableCatNodeCookies();
        for (final CatalogNodeContextCookie cncc : cookies) {
            project.getCidsDataObjectBackend()
                    .linkNode(cncc.getParent(),
                        nodes[0].getCookie(CatalogNodeContextCookie.class).getCatNode(),
                        cncc.getCatNode());
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        final boolean enable = super.enable(nodes);
        if (!enable) {
            return false;
        }

        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final CatalogNodeContextCookie[] linkableNodes = project.getLinkableCatNodeCookies();
        if ((linkableNodes == null) || (linkableNodes.length == 0)) {
            return false;
        }

        final CatalogNodeContextCookie cookie = nodes[0].getCookie(CatalogNodeContextCookie.class);
        if (cookie == null) {
            return false;
        }
        final CatNode newParent = cookie.getCatNode();
        // one cannot add children to object nodes
        if (newParent.getNodeType().equals(CatNode.Type.OBJECT.getType())) {
            return false;
        }

        for (final CatalogNodeContextCookie cncc : linkableNodes) {
            final CatNode oldParent = cncc.getParent();
            final CatNode node = cncc.getCatNode();
            // one cannot paste a node in itself
            if (cncc.getCatNode().equals(newParent)) {
                return false;
            }
            // one cannot paste a node where it already is
            else if ((oldParent != null) && oldParent.equals(newParent)) {
                return false;
            }
            // one cannot insert if this is dynamic node
            else if (newParent.getDynamicChildren() != null) {
                return false;
            }
            // one cannot insert dynamically created nodes
            else if ((node.getId() < 1) || ((oldParent != null) && (oldParent.getDynamicChildren() != null))) {
                return false;
            }
        }

        return true;
    }
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.netbeans.spi.project.ui.LogicalViewProvider;

import org.openide.nodes.Node;

import de.cismet.cids.abf.domainserver.project.catalog.CatalogNodeContextCookie;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.9
 */
public final class DomainserverLogicalView implements LogicalViewProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DomainserverLogicalView.class);

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;
    private transient int skippedCounter;
    private transient volatile DomainserverProjectNode view;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainserverLogicalView object.
     *
     * @param  project  DOCUMENT ME!
     */
    public DomainserverLogicalView(final DomainserverProject project) {
        this.project = project;
        skippedCounter = 0;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Node findPath(final Node node, final Object object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searching for '" + object + "' from '" + node + "'"); // NOI18N
        }

        if ((node == null) || (object == null)) {
            return null;
        }

        if (object instanceof Class) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("object is instanceof class");                            // NOI18N
            }
            for (final Node n : node.getChildren().getNodes()) {
                if (n.getCookie((Class)object) != null) {
                    return n;
                }
                final Node ret = findPath(n, object);
                if (ret != null) {
                    skippedCounter = 0;
                    return ret;
                }
            }
        } else if (object instanceof Object[]) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("object is instanceof Object[]");                         // NOI18N
            }
            final Object[] ccc = (Object[])object;
            if (ccc.length != 3) {
                throw new IllegalStateException("object array must have length 3"); // NOI18N
            }
            if (ccc[0] instanceof Class) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ccc[0] is instanceof class");                        // NOI18N
                }
                final Class cookie = (Class)ccc[0];
                for (final Node n : node.getChildren().getNodes()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("processing node: " + n);                         // NOI18N
                    }
                    final Object o = n.getCookie(cookie);
                    if (o instanceof CatalogNodeContextCookie) {
                        final CatNode cn = ((CatalogNodeContextCookie)o).getCatNode();
                        if (cn.getId().equals(((CatNode)ccc[1]).getId())) {
                            if (skippedCounter == (Integer)ccc[2]) {
                                skippedCounter = 0;
                                return n;
                            } else {
                                ++skippedCounter;
                            }
                        }
                        final Node ret = findPath(n, object);
                        if (ret != null) {
                            skippedCounter = 0;
                            return ret;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Node createLogicalView() {
        if (view == null) {
            synchronized (this) {
                if (view == null) {
                    view = new DomainserverProjectNode(project);
                }
            }
        }

        return view;
    }
}

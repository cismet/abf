/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import org.apache.log4j.Logger;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.KeyContainer;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.catalog.CatalogNode;
import de.cismet.cids.abf.domainserver.project.catalog.NavigatorNodeManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.catalog.NewCatalogNodeWizardAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.12
 */
public final class CatalogManagement extends ProjectNode implements NavigatorNodeManagementContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CatalogManagement.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;
    private final transient Map<CatNode, HashSet<CatalogNode>> openNodesCache;
    private final transient ConnectionListener connL;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CatalogManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public CatalogManagement(final DomainserverProject project) {
        super(new CatalogNodeManagementChildren(project), project);
        openNodesCache = new HashMap<CatNode, HashSet<CatalogNode>>();
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "tree.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                CatalogManagement.class,
                "CatalogManagement.CatalogManagement(DomainserverProject).displayName"));    // NOI18N
        connL = new ConnectionListenerImpl();
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, connL, project));
        getCookieSet().add(new RefreshableImpl());
        getCookieSet().add(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  node  DOCUMENT ME!
     * @param  open  DOCUMENT ME!
     */
    public void addOpenNode(final CatNode node, final CatalogNode open) {
        if ((node == null) || (node.getId() == null)) {
            return;
        }
        synchronized (openNodesCache) {
            HashSet<CatalogNode> set = openNodesCache.get(node);
            if (set == null) {
                set = new HashSet<CatalogNode>(5);
            }
            set.add(open);
            openNodesCache.put(node, set);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  node  DOCUMENT ME!
     */
    public void addedNode(final CatNode node) {
        if (node == null) {
            return;
        }
        final Set<CatalogNode> nodes = openNodesCache.get(node);
        if (nodes != null) {
            for (final CatalogNode cn : nodes) {
                cn.refresh();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  node     DOCUMENT ME!
     * @param  removed  DOCUMENT ME!
     */
    public void removedNode(final CatNode node, final CatalogNode removed) {
        if ((node == null) || (removed == null)) {
            return;
        }
        synchronized (openNodesCache) {
            final Set<CatalogNode> nodes = openNodesCache.get(node);
            if (nodes != null) {
                nodes.remove(removed);
                if (nodes.isEmpty()) {
                    openNodesCache.remove(node);
                }
            }
        }
        for (final Node n : removed.getChildren().getNodes()) {
            if (n instanceof CatalogNode) {
                final CatalogNode cn = (CatalogNode)n;
                removedNode(cn.getCatNode(), cn);
            } else {
                LOG.warn("error during node removal, node was not instanceof CatalogNode: " + n); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  node       DOCUMENT ME!
     * @param  destroyed  DOCUMENT ME!
     */
    public void destroyedNode(final CatNode node, final CatalogNode destroyed) {
        if ((node == null) || (destroyed == null)) {
            return;
        }
        final Set<CatalogNode> nodes;
        synchronized (openNodesCache) {
            nodes = openNodesCache.remove(node);
        }
        if (nodes != null) {
            for (final CatalogNode cn : nodes) {
                if (!cn.equals(destroyed)) {
                    final Node parent = cn.getParentNode();
                    if (parent == null) {
                        LOG.warn("parent node not present"); // NOI18N
                    } else {
                        ((Refreshable)parent).refresh();
                    }
                }
                removedNode(node, cn);
            }
        }
    }

    @Override
    public Image getIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(NewCatalogNodeWizardAction.class),
                null,
                CallableSystemAction.get(RefreshAction.class)
            };
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class RefreshableImpl implements Refreshable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void refresh() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("refresh requested", new Throwable("cause")); // NOI18N
            }

            if (project.isConnected()) {
                ((ProjectChildren)getChildren()).refreshByNotify();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ConnectionListenerImpl implements ConnectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void connectionStatusChanged(final ConnectionEvent event) {
            if (!event.isIndeterminate()) {
                if (event.isConnected()) {
                    setChildrenEDT(new CatalogNodeManagementChildren(project));
                } else {
                    synchronized (openNodesCache) {
                        openNodesCache.clear();
                    }
                    setChildrenEDT(Children.LEAF);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class CatalogNodeManagementChildren extends ProjectChildren {

        //~ Instance fields ----------------------------------------------------

        private final transient CatalogManagement catalogManagement;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NavigatorNodeManagementChildren object.
         *
         * @param  project  DOCUMENT ME!
         */
        public CatalogNodeManagementChildren(final DomainserverProject project) {
            super(project);
            catalogManagement = project.getLookup().lookup(CatalogManagement.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createUserNodes(final Object o) {
            if (o instanceof KeyContainer) {
                final CatNode catNode = (CatNode)((KeyContainer)o).getObject();
                final CatalogNode cn = new CatalogNode(catNode, project, getNode().getCookie(Refreshable.class));
                catalogManagement.addOpenNode(catNode, cn);

                return new Node[] { cn };
            } else {
                return new Node[] {};
            }
        }

        @Override
        protected void threadedNotify() throws IOException {
            try {
                final List<CatNode> rootNodes = project.getCidsDataObjectBackend().getRootNodes();
                final ListIterator<CatNode> it = rootNodes.listIterator();
                while (it.hasNext()) {
                    if (CatNode.Type.CLASS.getType().equals(it.next().getNodeType())) {
                        it.remove();
                    }
                }

                Collections.sort(rootNodes, new Comparators.CatNodes());
                setKeysEDT(KeyContainer.convertCollection(CatNode.class, rootNodes));
            } catch (final Exception ex) {
                LOG.error("NavNodeManChildren: catnode creation failed", ex);       // NOI18N
                ErrorUtils.showErrorMessage(
                    NbBundle.getMessage(
                        CatalogNodeManagementChildren.class,
                        "NavigatorNodeManagementChildren.addNotify().t.run().exc"), // NOI18N
                    ex);
                setKeysEDT(ex);
            }
        }
    }
}

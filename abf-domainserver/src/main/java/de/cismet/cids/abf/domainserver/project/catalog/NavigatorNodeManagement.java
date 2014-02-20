/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

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
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.KeyContainer;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.12
 */
public final class NavigatorNodeManagement extends ProjectNode implements NavigatorNodeManagementContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(NavigatorNodeManagement.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    private final transient ConnectionListener connL;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NavigatorNodeManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public NavigatorNodeManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "tree.png"); // NOI18N
        setDisplayName(NbBundle.getMessage(
                NavigatorNodeManagement.class,
                "NavigatorNodeManagement.<init>(DomainserverProject).nodeDisplayName"));     // NOI18N
        connL = new ConnectionListenerImpl();
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, connL, project));
        getCookieSet().add(new RefreshableImpl());
        getCookieSet().add((NavigatorNodeManagementContextCookie)this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getIcon(final int i) {
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
                    setChildrenEDT(new NavigatorNodeManagementChildren(project));
                } else {
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
    private static final class NavigatorNodeManagementChildren extends ProjectChildren {

        //~ Instance fields ----------------------------------------------------

        private final transient CatalogManagement catalogManagement;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NavigatorNodeManagementChildren object.
         *
         * @param  project  DOCUMENT ME!
         */
        public NavigatorNodeManagementChildren(final DomainserverProject project) {
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
                        NavigatorNodeManagement.class,
                        "NavigatorNodeManagementChildren.addNotify().t.run().exc"), // NOI18N
                    ex);
                setKeysEDT(ex);
            }
        }
    }
}

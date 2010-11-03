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
import org.openide.util.actions.CallableSystemAction;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.12
 */
public final class NavigatorNodeManagement extends ProjectNode implements NavigatorNodeManagementContextCookie,
    ConnectionListener,
    Refreshable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(NavigatorNodeManagement.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

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
        project.addConnectionListener(this);
        getCookieSet().add(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void connectionStatusChanged(final boolean isConnected) {
        if (isConnected) {
            setChildren(new NavigatorNodeManagementChildren());
        } else {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusIndeterminate() {
        // not needed
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getIcon(final int i) {
        return nodeImage;
    }

    @Override
    public void refresh() {
        if (project.isConnected()) {
            ((NavigatorNodeManagementChildren)getChildren()).refreshAll();
        } else {
            setChildren(Children.LEAF);
        }
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
    private final class NavigatorNodeManagementChildren extends Children.Keys {

        //~ Instance fields ----------------------------------------------------

        private final transient CatalogManagement catalogManagement;
        private transient LoadingNode loadingNode;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NavigatorNodeManagementChildren object.
         */
        public NavigatorNodeManagementChildren() {
            catalogManagement = project.getLookup().lookup(CatalogManagement.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object key) {
            if (key instanceof LoadingNode) {
                return new Node[] { (LoadingNode)key };
            }
            final CatNode catNode = (CatNode)key;
            final CatalogNode cn = new CatalogNode(catNode, project, (Refreshable)getNode());
            catalogManagement.addOpenNode(catNode, cn);

            return new Node[] { cn };
        }

        /**
         * DOCUMENT ME!
         */
        void refreshAll() {
            addNotify();
        }

        @Override
        protected void addNotify() {
            loadingNode = new LoadingNode();
            setKeys(new Object[] { loadingNode });
            refresh();
            final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final List<CatNode> rootNodes = project.getCidsDataObjectBackend().getRootNodes();
                                final ListIterator<CatNode> it = rootNodes.listIterator();
                                while (it.hasNext()) {
                                    if (CatNode.Type.CLASS.getType().equals(it.next().getNodeType())) {
                                        it.remove();
                                    }
                                }

                                Collections.sort(rootNodes, new Comparators.CatNodes());
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            setKeys(rootNodes);
                                        }
                                    });
                            } catch (final Exception ex) {
                                LOG.error("NavNodeManChildren: catnode creation failed", ex);       // NOI18N
                                ErrorUtils.showErrorMessage(
                                    NbBundle.getMessage(
                                        NavigatorNodeManagement.class,
                                        "NavigatorNodeManagementChildren.addNotify().t.run().exc"), // NOI18N
                                    ex);
                            } finally {
                                if (loadingNode != null) {
                                    loadingNode.dispose();
                                    loadingNode = null;
                                }
                            }
                        }
                    }, getClass().getSimpleName() + "::addNotifyRunner");                           // NOI18N
            t.start();
        }
    }
}

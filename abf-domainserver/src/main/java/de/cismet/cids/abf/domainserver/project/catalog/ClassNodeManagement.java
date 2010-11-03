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
import org.openide.util.actions.CallableSystemAction;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.11
 */
public final class ClassNodeManagement extends ProjectNode implements ClassNodeManagementContextCookie,
    ConnectionListener,
    Refreshable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ClassNodeManagement.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image defaultImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassNodeManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClassNodeManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        defaultImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "tutorials.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                ClassNodeManagement.class,
                "ClassNodeManagement.displayName"));
        project.addConnectionListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void connectionStatusChanged(final boolean isConnected) {
        if (isConnected) {
            setChildren(new ClassNodeManagementChildren());
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
        return defaultImage;
    }

    @Override
    public Image getIcon(final int i) {
        return defaultImage;
    }

    @Override
    public void refresh() {
        ((ClassNodeManagementChildren)getChildren()).refreshAll();
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
    private final class ClassNodeManagementChildren extends Children.Keys {

        //~ Instance fields ----------------------------------------------------

        private final transient CatalogManagement catalogManagement;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ClassNodeManagementChildren object.
         */
        public ClassNodeManagementChildren() {
            catalogManagement = project.getLookup().lookup(CatalogManagement.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object key) {
            final CatNode catNode = (CatNode)key;
            final CatalogNode cn = new CatalogNode(
                    catNode,
                    project,
                    (Refreshable)getNode());
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
            final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final List<CatNode> roots = project.getCidsDataObjectBackend()
                                            .getRootNodes(CatNode.Type.CLASS);
                                Collections.sort(roots, new Comparators.CatNodes());
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            setKeys(roots);
                                        }
                                    });
                            } catch (final Exception ex) {
                                LOG.error("ClassNodeManChildren: " // NOI18N
                                            + "catnode creation failed", ex); // NOI18N
                                ErrorUtils.showErrorMessage(
                                    org.openide.util.NbBundle.getMessage(
                                        ClassNodeManagement.class,
                                        "ClassNodeManagement.addNotify().ErrorUtils.loadingClassNodes.message"),
                                    ex);                     // NOI18N
                            }
                        }
                    }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
            t.start();
        }
    }
}

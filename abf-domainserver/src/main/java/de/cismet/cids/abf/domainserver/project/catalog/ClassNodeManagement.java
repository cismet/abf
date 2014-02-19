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

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
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
 * @version  1.11
 */
public final class ClassNodeManagement extends ProjectNode implements ClassNodeManagementContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ClassNodeManagement.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image defaultImage;

    private final transient ConnectionListener connL;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassNodeManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClassNodeManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        defaultImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "tutorials.png");       // NOI18N
        setDisplayName(NbBundle.getMessage(ClassNodeManagement.class, "ClassNodeManagement.displayName")); // NOI18N

        connL = new ConnectionListenerImpl();
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, connL, project));

        getCookieSet().add(new RefreshableImpl());
        getCookieSet().add((ClassNodeManagementContextCookie)this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getOpenedIcon(final int i) {
        return defaultImage;
    }

    @Override
    public Image getIcon(final int i) {
        return defaultImage;
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
    private final class ConnectionListenerImpl implements ConnectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void connectionStatusChanged(final ConnectionEvent event) {
            if (!event.isIndeterminate()) {
                if (event.isConnected()) {
                    setChildrenEDT(new ClassNodeManagementChildren(project));
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
    private final class RefreshableImpl implements Refreshable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void refresh() {
            ((ProjectChildren)getChildren()).refreshByNotify();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class ClassNodeManagementChildren extends ProjectChildren {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ClassNodeManagementChildren object.
         *
         * @param  project  DOCUMENT ME!
         */
        public ClassNodeManagementChildren(final DomainserverProject project) {
            super(project);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createUserNodes(final Object key) {
            if (key instanceof CatNode) {
                final CatNode catNode = (CatNode)key;
                final CatalogNode cn = new CatalogNode(catNode, project, getNode().getCookie(Refreshable.class));
                project.getLookup().lookup(CatalogManagement.class).addOpenNode(catNode, cn);
                return new Node[] { cn };
            } else {
                return new Node[] {};
            }
        }

        @Override
        protected void threadedNotify() throws IOException {
            try {
                final List<CatNode> roots = project.getCidsDataObjectBackend().getRootNodes(CatNode.Type.CLASS);
                Collections.sort(roots, new Comparators.CatNodes());
                setKeysEDT(roots);
            } catch (final Exception ex) {
                LOG.error("ClassNodeManChildren: catnode creation failed", ex);                  // NOI18N
                setKeysEDT(ex);
                ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        ClassNodeManagement.class,
                        "ClassNodeManagement.addNotify().ErrorUtils.loadingClassNodes.message"), // NOI18N
                    ex);
            }
        }
    }
}

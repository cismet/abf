/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.query.Query;

import de.cismet.cids.maintenance.InspectionResult;
import de.cismet.cids.maintenance.MaintenanceBackend;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class QueriesNode extends ProjectNode implements ConnectionListener, Refreshable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(QueriesNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    private transient boolean refreshActionAllowed;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueriesNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public QueriesNode(final DomainserverProject project) {
        super(Children.LEAF, project);
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        getCookieSet().add(this);
        refreshActionAllowed = false;
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "search.png"); // NOI18N
        setName(org.openide.util.NbBundle.getMessage(
                QueriesNode.class,
                "QueriesNode.QueriesNode(DomainserverProject).name"));                         // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Action[] getActions(final boolean b) {
        if (refreshActionAllowed) {
            return new Action[] {
                    CallableSystemAction.get(NewQueryWizardAction.class),
                    null,
                    CallableSystemAction.get(RefreshAction.class)
                };
        } else {
            return new Action[] { CallableSystemAction.get(NewQueryWizardAction.class) };
        }
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        if (event.isConnected() && !event.isIndeterminate()) {
            setChildrenEDT(new QueriesNodeChildren());
        } else {
            setChildrenEDT(Children.LEAF);
        }
    }

    @Override
    public void refresh() {
        ((QueriesNodeChildren)getChildren()).refreshAll();
    }

    @Override
    public Image getIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class QueriesNodeChildren extends Children.Keys {

        //~ Instance fields ----------------------------------------------------

        private final transient Backend backend;
        private transient List<Query> currentQueries;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new QueriesNodeChildren object.
         */
        public QueriesNodeChildren() {
            this.backend = project.getCidsDataObjectBackend();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object object) {
            if (object instanceof Query) {
                refreshActionAllowed = false;
                final Query q = (Query)object;
                return new Node[] { new QueryNode(project, q, QueriesNode.this) };
            } else if (object instanceof String) {
                final Runnable run = new Runnable() {

                        @Override
                        public void run() {
                            final ProgressHandle handle = ProgressHandleFactory.createHandle(org.openide.util.NbBundle
                                            .getMessage(
                                                QueriesNode.class,
                                                "QueriesNode.createNodes(Object).run().handle.message"));      // NOI18N
                            try {
                                handle.start();
                                final MaintenanceBackend b = new MaintenanceBackend(
                                        project.getRuntimeProps());
                                final List<InspectionResult> results = new ArrayList<InspectionResult>();
                                results.add(b.checkTable("cs_query"));                                         // NOI18N
                                results.add(b.checkTable(
                                        "cs_query_class_assoc"));                                              // NOI18N
                                results.add(b.checkTable(
                                        "cs_query_link"));                                                     // NOI18N
                                results.add(b.checkTable(
                                        "cs_query_parameter"));                                                // NOI18N
                                results.add(b.checkTable(
                                        "cs_query_store"));                                                    // NOI18N
                                results.add(b.checkTable(
                                        "cs_query_store_ug_assoc"));                                           // NOI18N
                                results.add(b.checkTable(
                                        "cs_query_ug_assoc"));                                                 // NOI18N
                                final InconsistencyPanel panel = new InconsistencyPanel(results.toArray(
                                            new InspectionResult[results.size()]));
                                final DialogDescriptor desc = new DialogDescriptor(
                                        panel,
                                        org.openide.util.NbBundle.getMessage(
                                            QueriesNode.class,
                                            "QueriesNode.createNodes(Object).run().DialogDescriptor.message"), // NOI18N
                                        false,
                                        DialogDescriptor.WARNING_MESSAGE,
                                        null,
                                        null);
                                final Dialog d = DialogDisplayer.getDefault().createDialog(desc);
                                handle.finish();
                                d.setVisible(true);
                                d.toFront();
                            } catch (final Exception ex) {
                                LOG.error(
                                    "could not show inspection results",                                       // NOI18N
                                    ex);
                                handle.finish();
                            } finally {
                                refreshActionAllowed = true;
                            }
                        }
                    };
//                RequestProcessor.getDefault().post(run, 100, Thread.NORM_PRIORITY);

                return new Node[] {
                        new AbstractNode(Children.LEAF) {

                            @Override
                            public String getDisplayName() {
                                return (String)object;
                            }
                        }
                    };
            }
            return new Node[] {};
        }

        @Override
        protected void addNotify() {
            final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                currentQueries = backend.getAllEntities(Query.class);
                            } catch (final Exception ex) {
                                LOG.error("could not retrieve queries", ex);                    // NOI18N
                                setKeys(
                                    new Object[] {
                                        org.openide.util.NbBundle.getMessage(
                                            QueriesNode.class,
                                            "QueriesNode.addNotify().run().dbInconsistencyKey") // NOI18N
                                    });
                                currentQueries = null;
                                return;
                            }
                            if (currentQueries == null) {
                                throw new IllegalStateException(
                                    "currentQueries must not be null");                         // NOI18N
                            }
                            Collections.sort(currentQueries, new Comparator<Query>() {

                                    @Override
                                    public int compare(final Query q1, final Query q2) {
                                        return q1.getName().compareToIgnoreCase(q2.getName());
                                    }
                                });
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        setKeys(new Object[] {});
                                        setKeys(currentQueries);
                                    }
                                });
                        }
                    }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
            t.start();
        }

        /**
         * DOCUMENT ME!
         */
        void refreshCurrent() {
            if (currentQueries != null) {
                for (final Query q : currentQueries) {
                    refreshKey(q);
                }
            }
        }

        /**
         * DOCUMENT ME!
         */
        void refreshAll() {
            addNotify();
            refreshCurrent();
        }
    }
}

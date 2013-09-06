/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.netbeans.spi.project.ui.support.CommonProjectActions;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;

import java.text.MessageFormat;

import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.DeleteLockAction;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.ConfigAttrManagement;
import de.cismet.cids.abf.domainserver.project.nodes.IconManagement;
import de.cismet.cids.abf.domainserver.project.nodes.JavaClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.QueryManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import de.cismet.cids.abf.domainserver.project.nodes.TypeManagement;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.nodes.ConnectAction;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DomainserverProjectNode extends AbstractNode implements ConnectionListener, DomainserverContext {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DomainserverProjectNode.class);

    private static final String NODE_NAME_PATTERN =
        "<font color=\"!textText\">{0}</font><font color=\"!controlShadow\"> [cidsDomainserver] {1}</font>"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;
    private final transient Image domainserverImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainserverProjectNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public DomainserverProjectNode(final DomainserverProject project) {
        super(Children.create(new DomainserverProjectNodeChildrenFactory(project), false), Lookups.singleton(project));

        this.project = project;
        final UserManagement userManagement = new UserManagement(project);
        final ViewManagement viewManagement = new ViewManagement(project);
        final ClassManagement classManagement = new ClassManagement(project);
        final TypeManagement typeManagement = new TypeManagement(project);
        final JavaClassManagement javaClassManagement = new JavaClassManagement(project);
        final IconManagement iconManagement = new IconManagement(project);
        final CatalogManagement catManagement = new CatalogManagement(project);
        final QueryManagement queryManagement = new QueryManagement(project);
        final SyncManagement syncManagement = new SyncManagement(project);
        final ConfigAttrManagement configAttrManagement = new ConfigAttrManagement(project);
        project.addLookup(new ProxyLookup(
                new Lookup[] {
                    getLookup(),
                    Lookups.fixed(
                        new Object[] {
                            this,
                            userManagement,
                            viewManagement,
                            classManagement,
                            typeManagement,
                            configAttrManagement,
                            javaClassManagement,
                            iconManagement,
                            catManagement,
                            queryManagement,
                            syncManagement
                        })
                }));
        domainserverImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "domainserver.png"); // NOI18N
        setName(project.getProjectDirectory().getName());
        setShortDescription(FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath());
        project.setDomainserverProjectNode(this);
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int type) {
        return domainserverImage;
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return domainserverImage;
    }

    @Override
    public String getDisplayName() {
        return getName() + " [" + getStatus() + "]"; // NOI18N
    }

    // NOTE: it seems that html display names are not shown for a project's root node if the project is under version
    // control. so the display name is a fallback
    @Override
    public String getHtmlDisplayName() {
        return MessageFormat.format(NODE_NAME_PATTERN, getName(), getStatus());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getStatus() {
        final String status;

        // TODO: maybe the status messages could be made better
        if (project.isConnectionInProgress()) {
            if (project.isConnected()) {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class,
                        "DomainserverProjectNode.getHtmlDisplayName().status.disconnect");   // NOI18N
            } else {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class,
                        "DomainserverProjectNode.getHtmlDisplayName().status.connect");      // NOI18N
            }
        } else {
            if (project.isConnected()) {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class,
                        "DomainserverProjectNode.getHtmlDisplayName().status.connected");    // NOI18N
            } else {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class,
                        "DomainserverProjectNode.getHtmlDisplayName().status.disconnected"); // NOI18N
            }
        }

        return status;
    }

    @Override
    public Action[] getActions(final boolean b) {
        final Action closeAction = new ProjectCloseHookAction(CommonProjectActions.closeProjectAction());
        final Action deleteLockAction;
        if (project.isConnected() || project.isConnectionInProgress()) {
            deleteLockAction = null;
        } else {
            deleteLockAction = CallableSystemAction.get(DeleteLockAction.class);
        }

        return new Action[] {
                CallableSystemAction.get(ConnectAction.class),
                deleteLockAction,
                null,
                closeAction,
                CommonProjectActions.setAsMainProjectAction(),
                null,
                CommonProjectActions.customizeProjectAction(),
            };
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    fireDisplayNameChange(null, null);
                }
            });
    }

    @Override
    public DomainserverProject getDomainserverProject() {
        return project;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class DomainserverProjectNodeChildrenFactory extends ChildFactory<Node> {

        //~ Instance fields ----------------------------------------------------

        private final transient DomainserverProject project;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DomainserverProjectNodeChildren object.
         *
         * @param  project  DOCUMENT ME!
         */
        public DomainserverProjectNodeChildrenFactory(final DomainserverProject project) {
            this.project = project;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node createNodeForKey(final Node key) {
            return key;
        }

        @Override
        protected boolean createKeys(final List<Node> toPopulate) {
            final Lookup lkp = project.getLookup();
            final UserManagement um = lkp.lookup(UserManagement.class);
            final ClassManagement cm = lkp.lookup(ClassManagement.class);
            final ViewManagement vm = lkp.lookup(ViewManagement.class);
            final CatalogManagement catm = lkp.lookup(CatalogManagement.class);
            final TypeManagement tm = lkp.lookup(TypeManagement.class);
            final ConfigAttrManagement cam = lkp.lookup(ConfigAttrManagement.class);
            final JavaClassManagement jcm = lkp.lookup(JavaClassManagement.class);
            final IconManagement im = lkp.lookup(IconManagement.class);
            final QueryManagement qm = lkp.lookup(QueryManagement.class);
            final SyncManagement sm = lkp.lookup(SyncManagement.class);

            final Node runtimeNode;
            try {
                final FileObject runtimeFO = project.getProjectDirectory().getFileObject("runtime.properties"); // NOI18N
                final DataObject runtimeDO = DataObject.find(runtimeFO);
                runtimeNode = runtimeDO.getNodeDelegate();
            } catch (final Exception e) {
                final String message = "cannot create project view";                                            // NOI18N
                LOG.error(message, e);
                throw new IllegalStateException(message, e);
            }

            toPopulate.addAll(Arrays.asList(new Node[] { runtimeNode, um, cm, vm, catm, tm, cam, jcm, im, qm, sm }));

            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ProjectCloseHookAction implements Action {

        //~ Instance fields ----------------------------------------------------

        private final transient Action delegate;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ProjectCloseHookAction object.
         *
         * @param  delegate  DOCUMENT ME!
         */
        public ProjectCloseHookAction(final Action delegate) {
            this.delegate = delegate;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Object getValue(final String arg0) {
            return delegate.getValue(arg0);
        }

        @Override
        public void putValue(final String arg0, final Object arg1) {
            delegate.putValue(arg0, arg1);
        }

        @Override
        public void setEnabled(final boolean arg0) {
            delegate.setEnabled(arg0);
        }

        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p) {
            delegate.addPropertyChangeListener(p);
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p) {
            delegate.removePropertyChangeListener(p);
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            project.setConnected(false);
            delegate.actionPerformed(arg0);
        }
    }
}

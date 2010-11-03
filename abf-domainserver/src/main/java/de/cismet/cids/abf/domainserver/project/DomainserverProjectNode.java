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

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.awt.Image;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;

import java.text.MessageFormat;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.ConnectAction;
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
import de.cismet.cids.abf.utilities.ConnectionListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DomainserverProjectNode extends FilterNode implements ConnectionListener, DomainserverContext {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DomainserverProjectNode.class);

    private static final String NODE_NAME_PATTERN =
        "<font color=''!textText''>{0}</font><font color=''!controlShadow''> [cidsDomainserver] {1}</font>"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;
    private final transient Image domainserverImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainserverProjectNode object.
     *
     * @param   node     DOCUMENT ME!
     * @param   project  DOCUMENT ME!
     *
     * @throws  DataObjectNotFoundException  DOCUMENT ME!
     */
    public DomainserverProjectNode(final Node node, final DomainserverProject project)
            throws DataObjectNotFoundException {
        super(node, new Children(node), new ProxyLookup(
                new Lookup[] { Lookups.singleton(project), node.getLookup() }));
        this.project = project;
        project.setDomainserverProjectNode(this);
        project.addConnectionListener(this);
        final UserManagement userManagement = new UserManagement(project);
        final ViewManagement viewManagement = new ViewManagement(project);
        final ClassManagement classManagement = new ClassManagement(project);
        final TypeManagement typeManagement = new TypeManagement(project);
        final JavaClassManagement javaClassManagement = new JavaClassManagement(
                project);
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
        getChildren().add(
            new Node[] {
                userManagement,
                classManagement,
                viewManagement,
                catManagement,
                typeManagement,
                configAttrManagement,
                javaClassManagement,
                iconManagement,
                queryManagement,
                syncManagement
            });
        domainserverImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER
                        + "domainserver.png"); // NOI18N
        setName(project.getProjectDirectory().getName());
        setShortDescription(FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath());
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
    public String getHtmlDisplayName() {
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
        return MessageFormat.format(NODE_NAME_PATTERN, getName(), status);
    }

    @Override
    public Action[] getActions(final boolean b) {
        final Action closeAction = new ProjectCloseHookAction(
                CommonProjectActions.closeProjectAction());
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
    public void connectionStatusChanged(final boolean isConnected) {
        fireDisplayNameChange(null, null);
    }

    @Override
    public void connectionStatusIndeterminate() {
        fireDisplayNameChange(null, null);
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

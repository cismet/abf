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
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;

import java.awt.EventQueue;
import java.awt.Image;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.users.AllUsersNode;
import de.cismet.cids.abf.domainserver.project.users.NewUserWizardAction;
import de.cismet.cids.abf.domainserver.project.users.UserManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.users.groups.NewUsergroupWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupContextCookie;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.PropertyRefresh;

import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.19
 */
public final class UserManagement extends ProjectNode implements ConnectionListener,
    UserManagementContextCookie,
    Refreshable,
    PropertyRefresh {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(UserManagement.class);

    public static final RequestProcessor REFRESH_PROCESSOR = new RequestProcessor("user-refresh-processor", 10); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public UserManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "usermanagement.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                UserManagement.class,
                "UserManagement.UserManagement(DomainserverProject).displayName"));                    // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        if (event.isConnected() && !event.isIndeterminate()) {
            setChildrenEDT(new UserGroupChildren(project));
        } else {
            setChildrenEDT(Children.LEAF);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  affectedGroups  DOCUMENT ME!
     */
    public void refreshGroups(final Collection<UserGroup> affectedGroups) {
        final UserManagement um = project.getLookup().lookup(UserManagement.class);
        final Node[] ugNodes = um.getChildren().getNodes(true);
        for (final Node n : ugNodes) {
            final UserGroup ug = n.getCookie(UserGroupContextCookie.class).getUserGroup();
            if (AllUsersNode.ALL_GROUP.equals(ug)) {
                n.getCookie(Refreshable.class).refresh();
            } else {
                for (final UserGroup affected : affectedGroups) {
                    if (affected.equals(ug)) {
                        n.getCookie(Refreshable.class).refresh();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(NewUserWizardAction.class),
                CallableSystemAction.get(NewUsergroupWizardAction.class),
            };
    }

    @Override
    public void refresh() {
        final Children children = getChildren();
        if (children instanceof ProjectChildren) {
            REFRESH_PROCESSOR.execute(new Runnable() {

                    @Override
                    public void run() {
                        final Future<?> future = ((ProjectChildren)children).refreshByNotify();

                        try {
                            future.get(30, TimeUnit.SECONDS);

                            // access the children nodes in the EDT
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        for (final Node n : children.getNodes()) {
                                            final Refreshable r = n.getCookie(Refreshable.class);

                                            if (r != null) {
                                                r.refresh();
                                            }
                                        }
                                    }
                                });
                        } catch (final Exception e) {
                            LOG.warn("refresh unsuccessful: " + this, e); // NOI18N
                        }
                    }
                });
        }
    }

    @Override
    public void refreshProperties(final boolean forceInit) {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    final Children children = getChildren();
                    for (final Node n : children.getNodes()) {
                        final PropertyRefresh pr = n.getCookie(PropertyRefresh.class);

                        if (pr != null) {
                            pr.refreshProperties(forceInit);
                        }
                    }
                }
            };
        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class UserGroupChildren extends ProjectChildren {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UserGroupChildren object.
         *
         * @param  project  DOCUMENT ME!
         */
        public UserGroupChildren(final DomainserverProject project) {
            super(project);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createUserNodes(final Object o) {
            if (o instanceof String) {
                return new Node[] { new AllUsersNode(project) };
            } else if (o instanceof UserGroup) {
                return new Node[] { new UserGroupNode((UserGroup)o, project) };
            } else {
                throw new IllegalArgumentException("unsupported user object: " + o); // NOI18N
            }
        }

        @Override
        protected void threadedNotify() throws IOException {
            final List<UserGroup> ugs = project.getCidsDataObjectBackend().getAllEntities(UserGroup.class);
            Collections.sort(ugs, new Comparators.UserGroups());
            final List groupsAndMore = new ArrayList();
            groupsAndMore.add(
                NbBundle.getMessage(
                    UserManagement.class,
                    "UserManagement.UserGroupChildren.addNotify().allUsers")); // NOI18N
            groupsAndMore.addAll(ugs);
            setKeysEDT(groupsAndMore);
        }
    }
}

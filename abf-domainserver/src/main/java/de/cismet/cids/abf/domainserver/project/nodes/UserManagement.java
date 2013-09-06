/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProgressIndicatingExecutor;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.RefreshableNode;
import de.cismet.cids.abf.domainserver.project.users.AllUsersNode;
import de.cismet.cids.abf.domainserver.project.users.NewUserWizardAction;
import de.cismet.cids.abf.domainserver.project.users.NoGroupUsersNode;
import de.cismet.cids.abf.domainserver.project.users.UserManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.users.groups.NewUsergroupWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupContextCookie;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.19
 */
public final class UserManagement extends RefreshableNode implements ConnectionListener, UserManagementContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    public static final ProgressIndicatingExecutor REFRESH_DISPATCHER;

    static {
        REFRESH_DISPATCHER = new ProgressIndicatingExecutor(
                NbBundle.getMessage(UserManagement.class, "UserManagement.REFRESH_DISPATCHER.displayName"), // NOI18N
                "user-refresh-dispatcher", // NOI18N
                10);
    }

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public UserManagement(final DomainserverProject project) {
        super(Children.LEAF, project, REFRESH_DISPATCHER);
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
        REFRESH_DISPATCHER.execute(new Runnable() {

                @Override
                public void run() {
                    final Node[] ugNodes = Children.MUTEX.readAccess(new org.openide.util.Mutex.Action<Node[]>() {

                                @Override
                                public Node[] run() {
                                    return getChildren().getNodes(true);
                                }
                            });

                    for (final Node n : ugNodes) {
                        final UserGroup ug = n.getCookie(UserGroupContextCookie.class).getUserGroup();
                        final Refreshable r = n.getCookie(Refreshable.class);
                        if ((AllUsersNode.ALL_GROUP == ug) || (NoGroupUsersNode.NO_GROUP == ug)) {
                            if (r != null) {
                                r.refresh();
                            }
                        } else {
                            for (final UserGroup affected : affectedGroups) {
                                if (affected.equals(ug)) {
                                    if (r != null) {
                                        r.refresh();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            });
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(NewUserWizardAction.class),
                CallableSystemAction.get(NewUsergroupWizardAction.class),
            };
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
            if (o instanceof UserGroup) {
                if (AllUsersNode.ALL_GROUP == o) {
                    return new Node[] { new AllUsersNode(project) };
                } else if (NoGroupUsersNode.NO_GROUP == o) {
                    return new Node[] { new NoGroupUsersNode(project) };
                } else {
                    return new Node[] { new UserGroupNode((UserGroup)o, project) };
                }
            } else {
                throw new IllegalArgumentException("unsupported user object: " + o); // NOI18N
            }
        }

        @Override
        protected void threadedNotify() throws IOException {
            final List<UserGroup> ugs = project.getCidsDataObjectBackend().getAllEntities(UserGroup.class);
            Collections.sort(ugs, new Comparators.UserGroups());
            final List<UserGroup> groupsAndMore = new ArrayList<UserGroup>();
            groupsAndMore.add(AllUsersNode.ALL_GROUP);
            groupsAndMore.add(NoGroupUsersNode.NO_GROUP);

            groupsAndMore.addAll(ugs);
            setKeysEDT(groupsAndMore);
        }
    }
}

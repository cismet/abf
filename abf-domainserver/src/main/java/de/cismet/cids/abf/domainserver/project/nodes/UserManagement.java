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
import org.openide.util.actions.CallableSystemAction;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.users.AllUsersNode;
import de.cismet.cids.abf.domainserver.project.users.NewUserWizardAction;
import de.cismet.cids.abf.domainserver.project.users.UserManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.users.UserNode;
import de.cismet.cids.abf.domainserver.project.users.groups.NewUsergroupWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;

import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.19
 */
public final class UserManagement extends ProjectNode implements ConnectionListener, UserManagementContextCookie {

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
        project.addConnectionListener(this);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "usermanagement.png");                                    // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                UserManagement.class,
                "UserManagement.UserManagement(DomainserverProject).displayName")); // NOI18N
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
    public void connectionStatusChanged(final boolean isConnected) {
        if (project.isConnected()) {
            setChildren(new UserGroupChildren());
        } else {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusIndeterminate() {
        // not needed
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshChildren() {
        final UserGroupChildren ch = (UserGroupChildren)getChildren();
        ch.refreshAll();
        for (final Node node : ch.getNodes()) {
            if (node instanceof Refreshable) {
                ((Refreshable)node).refresh();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  user  DOCUMENT ME!
     */
    public void refreshUser(final User user) {
        final UserGroupChildren ugch = (UserGroupChildren)getChildren();
        for (final Node node : ugch.getNodes()) {
            final Children ch = node.getChildren();
            for (final Node n : ch.getNodes()) {
                if (n instanceof UserNode) {
                    final UserNode un = (UserNode)n;
                    if (un.getUser().getId().equals(user.getId())) {
                        un.refresh();
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class UserGroupChildren extends Children.Keys {

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object obj) {
            if (obj instanceof LoadingNode) {
                return new Node[] { (LoadingNode)obj };
            } else if (obj instanceof String) {
                return new Node[] { new AllUsersNode(project) };
            } else if (obj instanceof UserGroup) {
                return new Node[] { new UserGroupNode((UserGroup)obj, project) };
            } else {
                return new Node[] {};
            }
        }

        /**
         * DOCUMENT ME!
         */
        void refreshAll() {
            addNotify();
        }

        @Override
        protected void addNotify() {
            final LoadingNode loadingNode = new LoadingNode();
            setKeys(new Object[] { loadingNode });
            refresh();
            final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final List<UserGroup> ugs = project.getCidsDataObjectBackend()
                                            .getAllEntities(
                                                UserGroup.class);
                                Collections.sort(ugs, new Comparators.UserGroups());
                                final List groupsAndMore = new ArrayList();
                                groupsAndMore.add(
                                    org.openide.util.NbBundle.getMessage(
                                        UserManagement.class,
                                        "UserManagement.UserGroupChildren.addNotify().allUsers")); // NOI18N
                                groupsAndMore.addAll(ugs);
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            setKeys(groupsAndMore);
                                        }
                                    });
                            } finally {
                                if (loadingNode != null) {
                                    loadingNode.dispose();
                                }
                            }
                        }
                    }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
            t.start();
        }
    }
}

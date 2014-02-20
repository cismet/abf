/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.apache.log4j.Logger;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.KeyContainer;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.RefreshIndicatorAction;
import de.cismet.cids.abf.domainserver.project.RefreshableNode;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupContextCookie;
import de.cismet.cids.abf.utilities.Comparators;

import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.11
 */
public final class NoGroupUsersNode extends RefreshableNode implements UserGroupContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    public static final UserGroup NO_GROUP;

    static {
        NO_GROUP = new UserGroup();
        NO_GROUP.setName("nogroup"); // NOI18N
        NO_GROUP.setId(Integer.MIN_VALUE + 1);
        NO_GROUP.setUsers(null);
        NO_GROUP.setDescription(null);
        NO_GROUP.setDomain(null);
    }

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(NoGroupUsersNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AllUsersNode object.
     *
     * @param  project  DOCUMENT ME!
     */
    public NoGroupUsersNode(final DomainserverProject project) {
        super(new NoGroupUsersChildren(project), project, UserManagement.REFRESH_DISPATCHER);

        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "all_users.png"); // NOI18N

        setDisplayName(NbBundle.getMessage(NoGroupUsersNode.class, "NoGroupUsersNode.displayName"));           // NOI18N
        setShortDescription(NbBundle.getMessage(NoGroupUsersNode.class, "NoGroupUsersNode.shortDescription")); // NOI18N
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
    public Action[] getActions(final boolean b) {
        if (UserManagement.REFRESH_DISPATCHER.tasksInProgress()) {
            return new Action[] { CallableSystemAction.get(RefreshIndicatorAction.class) };
        } else {
            return new Action[] { CallableSystemAction.get(NewUserWizardAction.class) };
        }
    }

    @Override
    protected Sheet createSheet() {
        setSheetInitialised(true);

        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set set = Sheet.createPropertiesSet();

        try {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: UserCount ">
            final Property<Integer> countUser = new PropertySupport<Integer>(
                    "usercount",                                                // NOI18N
                    Integer.class,
                    NbBundle.getMessage(
                        NoGroupUsersNode.class,
                        "NoGroupUsersNode.createSheet().countUser.usercount"),  // NOI18N
                    NbBundle.getMessage(
                        NoGroupUsersNode.class,
                        "NoGroupUsersNode.createSheet().countUser.totalusercount"), // NOI18N
                    true,
                    false) {

                    @Override
                    public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                        // this initialises the child nodes!
                        return NoGroupUsersNode.this.getChildren().getNodesCount(true);
                    }

                    @Override
                    public void setValue(final Integer object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        // not needed
                    }
                }; //</editor-fold>

            set.setName("nogroupusersinfo");                                 // NOI18N
            set.setDisplayName(NbBundle.getMessage(
                    NoGroupUsersNode.class,
                    "NoGroupUsersNode.createSheet().set.displayName"));      // NOI18N
            set.setShortDescription(NbBundle.getMessage(
                    NoGroupUsersNode.class,
                    "NoGroupUsersNode.createSheet().set.shortDescription")); // NOI18N

            set.put(countUser);

            sheet.put(set);
        } catch (final Exception ex) {
            LOG.error("could not create property sheet", ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        }

        return sheet;
    }

    @Override
    public UserGroup getUserGroup() {
        return NO_GROUP;
    }
}
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class NoGroupUsersChildren extends ProjectChildren {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AllUsersChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public NoGroupUsersChildren(final DomainserverProject project) {
        super(project);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createUserNodes(final Object o) {
        return new Node[] { new UserNode((User)((KeyContainer)o).getObject(), project) };
    }

    @Override
    protected void threadedNotify() throws IOException {
        final List<User> users = project.getCidsDataObjectBackend().getAllEntities(User.class);
        final List<User> noGroupUsers = new ArrayList<User>(users.size() / 2);
        for (final User user : users) {
            if (user.getUserGroups().isEmpty()) {
                noGroupUsers.add(user);
            }
        }

        Collections.sort(noGroupUsers, new Comparators.Users());
        setKeysEDT(KeyContainer.convertCollection(User.class, noGroupUsers));
    }
}

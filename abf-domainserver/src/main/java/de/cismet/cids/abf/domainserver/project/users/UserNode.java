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
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.users.groups.ChangeGroupBelongingWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.RemoveGroupMembershipAction;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.entity.user.User;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.12
 */
public final class UserNode extends ProjectNode implements UserContextCookie, Refreshable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            UserNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image adminImage;
    private final transient Image userImage;
    private transient User user;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserNode object.
     *
     * @param  user     DOCUMENT ME!
     * @param  project  DOCUMENT ME!
     */
    public UserNode(final User user, final DomainserverProject project) {
        super(Children.LEAF, project);
        this.user = user;
        userImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "user.png");  // NOI18N
        adminImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "admin.png"); // NOI18N
        getCookieSet().add(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        if (user.isAdmin()) {
            return adminImage;
        } else {
            return userImage;
        }
    }

    @Override
    public String getDisplayName() {
        return user.getLoginname();
    }

    @Override
    protected Sheet createSheet() {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set set = Sheet.createPropertiesSet();
        final Sheet.Set setAdditionalInfo = Sheet.createPropertiesSet();
        try {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ID ">
            final Property idProp = new PropertySupport.Reflection(user,
                    Integer.class, "getId", null);          // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    UserNode.class,
                    "UserNode.createSheet().idProp.name")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: PasswordChange ">
            final Property pwchangeProp = new PropertySupport(
                    "lastPWChange",                                               // NOI18N
                    Date.class,
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().pwchangeProp.lastPWChange"),      // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().pwchangeProp.timestampLastPWChange"), // NOI18N
                    true,
                    false) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return user.getLastPwdChange();
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        // not needed
                    }
                }; // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
            final Property nameProp = new PropertySupport(
                    "name",                                       // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().nameProp.name"),  // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().nameProp.userLogin"), // NOI18N
                    true,
                    true) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return user.getLoginname();
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        final User old = user;
                        try {
                            user.setLoginname(object.toString());
                            user = (User)project.getCidsDataObjectBackend().store(
                                    user);
                        } catch (final Exception ex) {
                            LOG.error("could not store user", ex); // NOI18N
                            user = old;
                            ErrorManager.getDefault().notify(ex);
                        }
                        fireDisplayNameChange(null, object.toString());
                    }
                };                                                 // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: Password ">
            final Property passProp = new PropertySupport(
                    "password",                                        // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().passProp.password"),   // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().passProp.passwordOfUser"), // NOI18N
                    true,
                    true) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return user.getPassword();
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        final User old = user;
                        try {
                            user.setPassword(object.toString());
                            user.setLastPwdChange(Calendar.getInstance().getTime());
                            user = (User)project.getCidsDataObjectBackend().store(
                                    user);
                        } catch (final Exception ex) {
                            LOG.error("could not store user", ex); // NOI18N
                            user = old;
                            ErrorManager.getDefault().notify(ex);
                        }
                        firePropertyChange("lastPWChange",         // NOI18N
                            null, user.getLastPwdChange());
                    }
                };                                                 // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: Administrator ">
            final Property adminProp = new PropertySupport(
                    "admin",                                         // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().adminProp.admin"),   // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().adminProp.isUserAdmin"), // NOI18N
                    true,
                    true) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return user.isAdmin();
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        final User old = user;
                        try {
                            user.setAdmin((Boolean)object);
                            user = (User)project.getCidsDataObjectBackend().store(
                                    user);
                        } catch (final Exception ex) {
                            LOG.error("could not store user", ex); // NOI18N
                            user = old;
                            ErrorManager.getDefault().notify(ex);
                        }
                        fireIconChange();
                    }
                };                                                 // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: GroupInfo ">
            final Property groupsInfo = new PropertySupport(
                    "groupsInfo",                                                   // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().groupsInfo.usergroups"),            // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserNode.class,
                        "UserNode.createSheet().groupsInfo.userIsMemberOfTheseGroups"), // NOI18N
                    true,
                    false) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        final Set set = user.getUserGroups();
                        if (set == null) {
                            return org.openide.util.NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().groupsInfo.getValue().userNotAssignedToAnyGroup"); // NOI18N
                        } else {
                            return set.toString();
                        }
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        // not needed
                    }
                };                                                         // </editor-fold>
            setAdditionalInfo.setName("zusatz");                           // NOI18N
            setAdditionalInfo.setDisplayName(org.openide.util.NbBundle.getMessage(
                    UserNode.class,
                    "UserNode.createSheet().additionalInfo.displayName")); // NOI18N
            set.put(nameProp);
            set.put(passProp);
            set.put(pwchangeProp);
            set.put(adminProp);
            set.put(idProp);
            setAdditionalInfo.put(groupsInfo);
            sheet.put(set);
            sheet.put(setAdditionalInfo);
        } catch (final Exception ex) {
            LOG.error("could not create property sheet", ex);              // NOI18N
            ErrorManager.getDefault().notify(ex);
        }
        return sheet;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof UserNode)) {
            return false;
        }
        final UserNode un = (UserNode)object;
        return user.getId().equals(un.user.getId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (59 * hash) + ((this.user == null) ? 0 : this.user.hashCode());
        return hash;
    }

    @Override
    public Action[] getActions(final boolean b) {
        Action removeGrpMbrShip = null;
        if (!(getParentNode() instanceof AllUsersNode)) {
            removeGrpMbrShip = CallableSystemAction.get(
                    RemoveGroupMembershipAction.class);
        }
        return new Action[] {
                CallableSystemAction.get(ChangeGroupBelongingWizardAction.class),
                removeGrpMbrShip,
                null,
                CallableSystemAction.get(DeleteUserAction.class),
            };
    }

    @Override
    public void destroy() throws IOException {
        try {
            project.getCidsDataObjectBackend().delete(user);
            super.destroy();
        } catch (final Exception ex) {
            LOG.error("could not delete user", ex);         // NOI18N
            throw new IOException("could not delete user: " // NOI18N
                        + ex.getMessage(), ex);
        }
    }

    // returns false to ensure a user can only be deleted by deleteuseraction
    // maybe the default deleteaction should be used here...
    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public void refresh() {
        user = project.getCidsDataObjectBackend().getEntity(
                User.class,
                user.getId());
        setSheet(createSheet());
    }
}

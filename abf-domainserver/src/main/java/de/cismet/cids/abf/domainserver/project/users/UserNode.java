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
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.configattr.ConfigAttrEntryNode;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.groups.ChangeGroupBelongingWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.RemoveGroupMembershipAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.12
 */
public final class UserNode extends ProjectNode implements UserContextCookie, Refreshable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserNode.class);

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
        userImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "user.png");   // NOI18N
        adminImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "admin.png"); // NOI18N
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
        final Sheet.Set setUG = Sheet.createPropertiesSet();
        final Sheet.Set setCAttr = Sheet.createPropertiesSet();
        final Sheet.Set setAAttr = Sheet.createPropertiesSet();
        final Sheet.Set setXAttr = Sheet.createPropertiesSet();

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
                            user = project.getCidsDataObjectBackend().store(user);
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
                            user = project.getCidsDataObjectBackend().store(user);
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
                            user = project.getCidsDataObjectBackend().store(user);
                        } catch (final Exception ex) {
                            LOG.error("could not store user", ex); // NOI18N
                            user = old;
                            ErrorManager.getDefault().notify(ex);
                        }
                        fireIconChange();
                    }
                };                                                 // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: GroupInfo ">
            final List<UserGroup> ugs = new ArrayList<UserGroup>(user.getUserGroups());
            Collections.sort(ugs, new Comparators.UserGroups());
            for (final UserGroup ug : user.getUserGroups()) {
                setUG.put(new PropertySupport.ReadOnly<Integer>(
                        "ug"
                                + ug.getId(), // NOI18N
                        Integer.class,
                        ug.getName(),
                        ug.getDescription()) {

                        @Override
                        public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                            return ug.getId();
                        }
                    });
            } // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: ConfigAttrs ">
            final List<ConfigAttrEntry> caes = project.getCidsDataObjectBackend().getEntries(user);
            Collections.sort(caes, new Comparators.ConfigAttrEntries());

            // hidden as long as there is no entry added
            setCAttr.setHidden(true);
            setAAttr.setHidden(true);
            setXAttr.setHidden(true);

            final Map<String, List<ReadOnly<String>>> propMapC = new TreeMap<String, List<ReadOnly<String>>>();
            final Map<String, List<ReadOnly<String>>> propMapA = new TreeMap<String, List<ReadOnly<String>>>();
            final Map<String, List<ReadOnly<String>>> propMapX = new TreeMap<String, List<ReadOnly<String>>>();

            for (final ConfigAttrEntry cae : caes) {
                final String entryOwner = ConfigAttrEntryNode.createEntryOwnerString(cae);

                final PropertySupport.ReadOnly<String> p = new PropertySupport.ReadOnly<String>(cae.getKey().getKey()
                                + cae.getId(),
                        String.class,
                        cae.getKey().getKey(),
                        null) {

                        @Override
                        public String getValue() throws IllegalAccessException, InvocationTargetException {
                            return cae.getValue().getValue();
                        }
                    };

                final Map<String, List<ReadOnly<String>>> map;
                switch (cae.getType().getAttrType()) {
                    case CONFIG_ATTR: {
                        map = propMapC;

                        break;
                    }
                    case ACTION_TAG: {
                        map = propMapA;

                        break;
                    }
                    case XML_ATTR: {
                        map = propMapX;

                        break;
                    }
                    default: {
                        throw new IllegalStateException("unknown type: " + cae.getType().getAttrType()); // NOI18N
                    }
                }

                List<ReadOnly<String>> attrList = map.get(entryOwner);
                if (attrList == null) {
                    attrList = new ArrayList<ReadOnly<String>>();
                }

                attrList.add(p);
                map.put(entryOwner, attrList);
            }

            populateAttrSet(setCAttr, propMapC);
            populateAttrSet(setAAttr, propMapA);
            populateAttrSet(setXAttr, propMapX);

            // </editor-fold>

            setUG.setName("usergroups");                          // NOI18N
            setUG.setDisplayName(NbBundle.getMessage(
                    UserNode.class,
                    "UserNode.createSheet().setUG.displayName")); // NOI18N
            setCAttr.setName("configattrs");                      // NOI18N
            setCAttr.setDisplayName("Configuration Attributes");
            setAAttr.setName("actionattrs");                      // NOI18N
            setAAttr.setDisplayName("Action-Tag Attributes");
            setXAttr.setName("xmlattrs");                         // NOI18N
            setXAttr.setDisplayName("XML Configuration Attributes");

            set.put(nameProp);
            set.put(passProp);
            set.put(pwchangeProp);
            set.put(adminProp);
            set.put(idProp);

            sheet.put(set);
            sheet.put(setUG);
            sheet.put(setCAttr);
            sheet.put(setAAttr);
            sheet.put(setXAttr);
        } catch (final Exception ex) {
            LOG.error("could not create property sheet", ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        }
        return sheet;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  set  DOCUMENT ME!
     * @param  map  DOCUMENT ME!
     */
    public static void populateAttrSet(final Sheet.Set set, final Map<String, List<ReadOnly<String>>> map) {
        for (final String key : map.keySet()) {
            set.put(new PropertySupport.ReadOnly<String>(
                    "attr-"
                            + key
                            + System.currentTimeMillis(),
                    String.class,
                    key,
                    null) {

                    @Override
                    public String getValue() throws IllegalAccessException, InvocationTargetException {
                        return "";
                    }
                });
            for (final ReadOnly<String> ro : map.get(key)) {
                set.put(ro);
            }

            set.setHidden(false);
        }
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
            removeGrpMbrShip = CallableSystemAction.get(RemoveGroupMembershipAction.class);
        }

        return new Action[] {
                CallableSystemAction.get(ChangeGroupBelongingWizardAction.class),
                removeGrpMbrShip,
                null,
                CallableSystemAction.get(CopyUserWizardAction.class),
                CallableSystemAction.get(DeleteUserAction.class),
            };
    }

    @Override
    public void destroy() throws IOException {
        try {
            project.getCidsDataObjectBackend().delete(user);
        } catch (final Exception ex) {
            final String message = "could not delete user: " + user; // NOI18N
            LOG.error(message, ex);
            throw new IOException(message, ex);
        }
        project.getLookup().lookup(UserManagement.class).refreshGroups(user.getUserGroups());
    }

    // returns false to ensure a user can only be deleted by deleteuseraction
    // maybe the default deleteaction should be used here...
    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public void refresh() {
        user = project.getCidsDataObjectBackend().getEntity(User.class, user.getId());

        setSheet(createSheet());
    }
}

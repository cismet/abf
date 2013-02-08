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

import java.awt.EventQueue;
import java.awt.Image;

import java.beans.PropertyEditor;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.customizer.DomainserverProjectCustomizer;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.groups.ChangeGroupBelongingWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.RemoveGroupMembershipAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.PropertyRefresh;

import de.cismet.cids.jpa.backend.service.Backend;
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
public final class UserNode extends ProjectNode implements UserContextCookie, Refreshable, PropertyRefresh {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Image adminImage;
    private final transient Image userImage;
    private transient User user;

    // accessed in syncronised methods
    private transient boolean sheetInitialised;

    private transient volatile boolean deleted;

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
        deleted = false;
        sheetInitialised = false;
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
        sheetInitialised = true;
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set set = Sheet.createPropertiesSet();
        final Sheet.Set setUG = Sheet.createPropertiesSet();

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
            final PasswordPropertyEditor pwEditor = new PasswordPropertyEditor();
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
                        return "****"; // NOI18N
                    }

                    @Override
                    public PropertyEditor getPropertyEditor() {
                        return pwEditor;
                    }

                    @Override
                    public void setValue(final Object object) throws IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException {
                        if ((object == null) || object.toString().equals("****")) { // NOI18N
                            // ignore
                            return;
                        }

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
                };
            passProp.setValue("canEditAsText", Boolean.FALSE);     // NOI18N
            // </editor-fold>

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
                        "ug" // NOI18N
                                + ug.getId(),
                        Integer.class,
                        ug.getName(),
                        ug.getDescription()) {

                        @Override
                        public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                            return ug.getId();
                        }
                    });
            } // </editor-fold>

            setUG.setName("usergroups");                          // NOI18N
            setUG.setDisplayName(NbBundle.getMessage(
                    UserNode.class,
                    "UserNode.createSheet().setUG.displayName")); // NOI18N

            set.put(nameProp);
            set.put(passProp);
            set.put(pwchangeProp);
            set.put(adminProp);
            set.put(idProp);

            sheet.put(set);
            sheet.put(setUG);

            // <editor-fold defaultstate="collapsed" desc=" Create Property: ConfigAttrs legacy">
            if (Boolean.valueOf(
                            project.getProperties().getProperty(
                                DomainserverProjectCustomizer.PROP_USER_SHOW_LEGACY_CFGATTR_PROPS,
                                "false"))) { // NOI18N
                populateLegacyConfigAttrSet(project, sheet, null, user);
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Create Property: ConfigAttrs">
            if (Boolean.valueOf(
                            project.getProperties().getProperty(
                                DomainserverProjectCustomizer.PROP_USER_SHOW_CFGATTR_PROPS,
                                "false"))) {                  // NOI18N
                populateConfigAttrSet(sheet);
            }
            // </editor-fold>
        } catch (final Exception ex) {
            LOG.error("could not create property sheet", ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        }

        return sheet;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sheet  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void populateConfigAttrSet(final Sheet sheet) {
        final ReadOnly<String> propCAttr = new ReadOnly<String>(
                "configattrs",                                                                                 // NOI18N
                String.class,
                NbBundle.getMessage(UserNode.class, "UserNode.createSheet().propCAttr.displayName"),           // NOI18N
                null) {

                @Override
                public String getHtmlDisplayName() {
                    return "<html><b>"                                                               // NOI18N
                                + NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().propCAttr.displayName") + "</b></html>"; // NOI18N
                }

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return ""; // NOI18N
                }
            };

        final ReadOnly<String> propAAttr = new ReadOnly<String>(
                "actionattrs",                                                                                 // NOI18N
                String.class,
                NbBundle.getMessage(UserNode.class, "UserNode.createSheet().propAAttr.displayName"),           // NOI18N
                null) {

                @Override
                public String getHtmlDisplayName() {
                    return "<html><b>"                                                               // NOI18N
                                + NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().propAAttr.displayName") + "</b></html>"; // NOI18N
                }

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return ""; // NOI18N
                }
            };

        final ReadOnly<String> propXAttr = new ReadOnly<String>(
                "xmlattrs",                                                                                    // NOI18N
                String.class,
                NbBundle.getMessage(UserNode.class, "UserNode.createSheet().propXAttr.displayName"),           // NOI18N
                null) {

                @Override
                public String getHtmlDisplayName() {
                    return "<html><b>"                                                               // NOI18N
                                + NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().propXAttr.displayName") + "</b></html>"; // NOI18N
                }

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return ""; // NOI18N
                }
            };

        final List<Object[]> entries = project.getCidsDataObjectBackend().getEntriesNewCollect(user, true);
        // entries sorted by key id, thus we create the attr entries right away and sort later
        final Map<ReadOnly<String>, List<ConflictAwareCfgAttrProperty>> map =
            new TreeMap<ReadOnly<String>, List<ConflictAwareCfgAttrProperty>>(new Comparator<ReadOnly<String>>() {

                    @Override
                    public int compare(final ReadOnly<String> o1, final ReadOnly<String> o2) {
                        return o1.getDisplayName().compareTo(o2.getDisplayName());
                    }
                });
        for (final Object[] obj : entries) {
            final ConfigAttrEntry cae = (ConfigAttrEntry)obj[0];
            final ReadOnly<String> prop;
            switch (cae.getType().getAttrType()) {
                case CONFIG_ATTR: {
                    prop = propCAttr;
                    break;
                }
                case ACTION_TAG: {
                    prop = propAAttr;
                    break;
                }
                case XML_ATTR: {
                    prop = propXAttr;
                    break;
                }
                default: {
                    throw new IllegalStateException("unknown type: " + cae.getType().getAttrType()); // NOI18N
                }
            }

            List<ConflictAwareCfgAttrProperty> props = map.get(prop);
            if (props == null) {
                props = new ArrayList<ConflictAwareCfgAttrProperty>();
                map.put(prop, props);
            }

            if (props.isEmpty() || !props.get(props.size() - 1).getMainEntry().getKey().equals(cae.getKey())) {
                props.add(new ConflictAwareCfgAttrProperty(cae, (String)obj[1]));
            } else {
                props.get(props.size() - 1).putConflictEntry(cae, (String)obj[1]);
            }
        }

        for (final List<ConflictAwareCfgAttrProperty> props : map.values()) {
            Collections.sort(props, new Comparator<ConflictAwareCfgAttrProperty>() {

                    private final transient Comparator<ConfigAttrEntry> comp = new Comparators.ConfigAttrEntries();

                    @Override
                    public int compare(final ConflictAwareCfgAttrProperty o1,
                            final ConflictAwareCfgAttrProperty o2) {
                        return comp.compare(o1.mainEntry, o2.getMainEntry());
                    }
                });
        }

        final Sheet.Set setConflictAwareCfgAttr = Sheet.createPropertiesSet();

        for (final ReadOnly<String> prop : map.keySet()) {
            setConflictAwareCfgAttr.put(prop);
            for (final ConflictAwareCfgAttrProperty p : map.get(prop)) {
                setConflictAwareCfgAttr.put(p);
            }
        }

        setConflictAwareCfgAttr.setName("conflictAwareConfigAttrProperties"); // NOI18N
        setConflictAwareCfgAttr.setDisplayName(NbBundle.getMessage(
                UserNode.class,
                "UserNode.populateConfigAttrSet(Sheet).setConflictAwareCfgAttr.displayName"));

        sheet.put(setConflictAwareCfgAttr);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   project  backend map DOCUMENT ME!
     * @param   sheet    DOCUMENT ME!
     * @param   group    DOCUMENT ME!
     * @param   user     DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     * @throws  IllegalStateException     DOCUMENT ME!
     */
    public static void populateLegacyConfigAttrSet(final DomainserverProject project,
            final Sheet sheet,
            final UserGroup group,
            final User user) {
        if ((user == null) && (group == null)) {
            throw new IllegalArgumentException("group and user must not be null");                   // NOI18N
        }
        final ReadOnly<String> propCAttr = new ReadOnly<String>(
                "configattrs",                                                                       // NOI18N
                String.class,
                NbBundle.getMessage(UserNode.class, "UserNode.createSheet().propCAttr.displayName"), // NOI18N
                null) {

                @Override
                public String getHtmlDisplayName() {
                    return "<html><b>"                                                               // NOI18N
                                + NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().propCAttr.displayName") + "</b></html>"; // NOI18N
                }

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return ""; // NOI18N
                }
            };

        final ReadOnly<String> propAAttr = new ReadOnly<String>(
                "actionattrs",                                                                                 // NOI18N
                String.class,
                NbBundle.getMessage(UserNode.class, "UserNode.createSheet().propAAttr.displayName"),           // NOI18N
                null) {

                @Override
                public String getHtmlDisplayName() {
                    return "<html><b>"                                                               // NOI18N
                                + NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().propAAttr.displayName") + "</b></html>"; // NOI18N
                }

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return ""; // NOI18N
                }
            };

        final ReadOnly<String> propXAttr = new ReadOnly<String>(
                "xmlattrs",                                                                                    // NOI18N
                String.class,
                NbBundle.getMessage(UserNode.class, "UserNode.createSheet().propXAttr.displayName"),           // NOI18N
                null) {

                @Override
                public String getHtmlDisplayName() {
                    return "<html><b>"                                                               // NOI18N
                                + NbBundle.getMessage(
                                    UserNode.class,
                                    "UserNode.createSheet().propXAttr.displayName") + "</b></html>"; // NOI18N
                }

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return ""; // NOI18N
                }
            };

        final List<UserGroup> ugs;
        if (user == null) {
            ugs = new ArrayList(1);
            ugs.add(group);
        } else {
            ugs = new ArrayList(user.getUserGroups());
            Collections.sort(ugs, new Comparators.UserGroups());
        }

        for (final UserGroup ug : ugs) {
            final List<ConfigAttrEntry> caes = project.getCidsDataObjectBackend()
                        .getEntries(ug.getDomain(),
                            ug,
                            user,
                            project.getRuntimeProps().getProperty("serverName"), // NOI18N
                            true);
            Collections.sort(caes, new Comparators.ConfigAttrEntries());

            if (!caes.isEmpty()) {
                final Sheet.Set ugSet = Sheet.createPropertiesSet();
                ugSet.setName(ug.toString());
                ugSet.setDisplayName(ug.getName() + "@" + ug.getDomain().getName()); // NOI18N

                final Map<ReadOnly<String>, List<ReadOnly<String>>> map =
                    new TreeMap<ReadOnly<String>, List<ReadOnly<String>>>(new Comparator<ReadOnly<String>>() {

                            @Override
                            public int compare(final ReadOnly<String> o1, final ReadOnly<String> o2) {
                                return o1.getDisplayName().compareTo(o2.getDisplayName());
                            }
                        });

                for (final ConfigAttrEntry cae : caes) {
                    final PropertySupport.ReadOnly<String> p = new PropertySupport.ReadOnly<String>(cae.getKey()
                                    .getKey()
                                    + cae.getId(),
                            String.class,
                            cae.getKey().getKey()
                                    + " ["                                                                        // NOI18N
                                    + ((cae.getUser() == null)
                                        ? ((cae.getUsergroup() == null)
                                            ? NbBundle.getMessage(
                                                UserNode.class,
                                                "UserNode.createSheet().caentryProp.displayName.category.domain") // NOI18N
                                            : NbBundle.getMessage(
                                                UserNode.class,
                                                "UserNode.createSheet().caentryProp.displayName.category.ug"))    // NOI18N
                                        : NbBundle.getMessage(
                                            UserNode.class,
                                            "UserNode.createSheet().caentryProp.displayName.category.user"))      // NOI18N
                                    + "]",                                                                        // NOI18N
                            null) {

                            @Override
                            public String getHtmlDisplayName() {
                                return "<html>" + cae.getKey().getKey()
                                            + " <font color=\"!controlShadow\"> ["                                            // NOI18N
                                            + ((cae.getUser() == null)
                                                ? ((cae.getUsergroup() == null)
                                                    ? NbBundle.getMessage(
                                                        UserNode.class,
                                                        "UserNode.createSheet().caentryProp.displayName.category.domain")     // NOI18N
                                                    : NbBundle.getMessage(
                                                        UserNode.class,
                                                        "UserNode.createSheet().caentryProp.displayName.category.ug"))        // NOI18N
                                                : NbBundle.getMessage(
                                                    UserNode.class,
                                                    "UserNode.createSheet().caentryProp.displayName.category.user"))          // NOI18N
                                            + "]</font></html>";                                                              // NOI18N
                            }

                            @Override
                            public String getValue() throws IllegalAccessException, InvocationTargetException {
                                return cae.getValue().getValue();
                            }
                        };

                    List<ReadOnly<String>> attrList;
                    switch (cae.getType().getAttrType()) {
                        case CONFIG_ATTR: {
                            attrList = map.get(propCAttr);
                            if (attrList == null) {
                                attrList = new ArrayList<ReadOnly<String>>();
                                map.put(propCAttr, attrList);
                            }

                            break;
                        }
                        case ACTION_TAG: {
                            attrList = map.get(propAAttr);
                            if (attrList == null) {
                                attrList = new ArrayList<ReadOnly<String>>();
                                map.put(propAAttr, attrList);
                            }

                            break;
                        }
                        case XML_ATTR: {
                            attrList = map.get(propXAttr);
                            if (attrList == null) {
                                attrList = new ArrayList<ReadOnly<String>>();
                                map.put(propXAttr, attrList);
                            }

                            break;
                        }
                        default: {
                            throw new IllegalStateException("unknown type: " + cae.getType().getAttrType()); // NOI18N
                        }
                    }

                    attrList.add(p);
                }

                for (final ReadOnly<String> typeProp : map.keySet()) {
                    ugSet.put(typeProp);
                    for (final ReadOnly<String> entryProp : map.get(typeProp)) {
                        ugSet.put(entryProp);
                    }
                }

                sheet.put(ugSet);
            }
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
        final Set<UserGroup> ugs;

        try {
            final Backend backend = project.getCidsDataObjectBackend();
            for (final UserGroup ug : user.getUserGroups()) {
                ug.getUsers().remove(user);
                backend.store(ug);
            }
            ugs = new HashSet<UserGroup>(user.getUserGroups());
            user.getUserGroups().clear();
            
            backend.delete(user);
            
            deleted = true;
        } catch (final Exception ex) {
            final String message = "could not delete user: " + user; // NOI18N
            LOG.error(message, ex);
            throw new IOException(message, ex);
        }
        project.getLookup().lookup(UserManagement.class).refreshGroups(ugs);
    }

    // returns false to ensure a user can only be deleted by deleteuseraction
    // maybe the default deleteaction should be used here...
    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public void refresh() {
        // has the user been deleted?
        if (!deleted) {
            UserManagement.REFRESH_PROCESSOR.execute(new Runnable() {

                    @Override
                    public void run() {
                        user = project.getCidsDataObjectBackend().getEntity(User.class, user.getId());

                        refreshProperties(false);
                    }
                });
        }
    }

    @Override
    public void refreshProperties(final boolean forceInit) {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    if (sheetInitialised || forceInit) {
                        setSheet(createSheet());
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
    private final class ConflictAwareCfgAttrProperty extends PropertySupport<String> {

        //~ Instance fields ----------------------------------------------------

        private final transient ConfigAttrEntry mainEntry;
        private final transient String mainEntryOriginUgName;
        private final transient List<Object[]> conflictingEntries;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ConflictAwareCfgAttrProperty object.
         *
         * @param  cae           DOCUMENT ME!
         * @param  originUgName  DOCUMENT ME!
         */
        public ConflictAwareCfgAttrProperty(final ConfigAttrEntry cae, final String originUgName) {
            super(cae.getKey().getKey() + cae.getId() + "-conflictaware", // NOI18N
                String.class,
                cae.getKey().getKey(),
                null,
                true,
                false);

            this.mainEntry = cae;
            this.mainEntryOriginUgName = originUgName;
            this.conflictingEntries = new ArrayList<Object[]>(0);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  cae           DOCUMENT ME!
         * @param  originUgName  DOCUMENT ME!
         */
        public void putConflictEntry(final ConfigAttrEntry cae, final String originUgName) {
            conflictingEntries.add(new Object[] { cae, originUgName });
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public ConfigAttrEntry getMainEntry() {
            return mainEntry;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getMainEntryOriginUgName() {
            return mainEntryOriginUgName;
        }

        @Override
        public String getHtmlDisplayName() {
            final int size = conflictingEntries.size();
            return "<html>"                                                                                       // NOI18N
                        + mainEntry.getKey().getKey()
                        + ((size > 0) ? (" <font color=\"!controlShadow\"> [" + size + " more ...]</font>") : "") // NOI18N
                        + "</html>";                                                                              // NOI18N
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new ConflictingCfgAttrPropertyEditor(mainEntry, mainEntryOriginUgName, conflictingEntries);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return mainEntry.getValue().getValue();
        }

        @Override
        public void setValue(final String t) throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException {
            // ignore, this property is actually read only
        }
    }
}

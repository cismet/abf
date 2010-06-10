/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
import de.cismet.cids.abf.utilities.Comparators;

import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.common.PermissionAwareEntity;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.user.UserGroup;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectInformation;

import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class CheckRightsAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -5950608700166813558L;

    //~ Instance fields --------------------------------------------------------

    private final transient Map<DomainserverProject, Map<CidsClass, List<String>>> runningProjects;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CheckRightsAction object.
     */
    public CheckRightsAction() {
        runningProjects = new Hashtable<DomainserverProject, Map<CidsClass, List<String>>>(2);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final List<CidsClass> allClasses = project.getCidsDataObjectBackend().getAllEntities(CidsClass.class);
        final List<CidsClass> classes;
        if (nodes[0].getCookie(ClassManagementContextCookie.class) == null) {
            // assume action is performed on class nodes
            classes = new ArrayList<CidsClass>(nodes.length);
            for (final Node node : nodes) {
                classes.add(node.getCookie(CidsClassContextCookie.class).getCidsClass());
            }
        } else {
            // process all classes as the action was requested via classmanagement context
            classes = allClasses;
        }
        Collections.sort(classes, new Comparators.CidsClasses());
        final List<UserGroup> groups = project.getCidsDataObjectBackend().getAllEntities(UserGroup.class);
        final List<Permission> permissions = project.getCidsDataObjectBackend().getAllEntities(Permission.class);
        final Map<Integer, CidsClass> classCache = new Hashtable<Integer, CidsClass>();
        for (final CidsClass c : allClasses) {
            classCache.put(c.getId(), c);
        }
        final String actionName = NbBundle.getMessage(
                CheckRightsAction.class,
                "CheckRightsAction.io.checkRights", // NOI18N
                project.getLookup().lookup(ProjectInformation.class).getDisplayName());
        final CancelAction cancelAction = new CancelAction();
        final InputOutput io = IOProvider.getDefault().getIO(actionName, new Action[] { cancelAction });
        final FocusAction focusAction = new FocusAction(io);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(actionName, cancelAction, focusAction);
        try {
            synchronized (runningProjects) {
                if (runningProjects.containsKey(project)) {
                    return;
                }
                runningProjects.put(project, new Hashtable<CidsClass, List<String>>());
            }
            EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        handle.start(classes.size());
                        try {
                            io.getOut().reset();
                        } catch (final IOException ex) {
                            // not cleared just put some separators in between
                            io.getOut().println();
                            io.getOut().println("================================================"); // NOI18N
                            io.getOut().println();
                        }
                        io.select();
                    }
                });

            final Runnable progress = new Runnable() {

                    private int i = 1;

                    @Override
                    public void run() {
                        handle.progress(i++);
                    }
                };

            final PermissionResolver resolver = PermissionResolver.getInstance(project);
            for (final CidsClass c : classes) {
                if (cancelAction.isCancelled()) {
                    break;
                }
                checkCidsClass(project, c, groups, permissions, resolver, classCache, io);
                EventQueue.invokeLater(progress);
            }
            final Map<CidsClass, List<String>> issues = runningProjects.get(project);
            if (!issues.isEmpty()) {
                io.getErr().println();
                io.getErr().println(NbBundle.getMessage(CheckRightsAction.class, "CheckRightsAction.io.issueSummary")); // NOI18N
                io.getErr().println();
                for (Entry<CidsClass, List<String>> entry : issues.entrySet()) {
                    io.getErr().println(entry.getKey().getName());
                    for (final String issue : entry.getValue()) {
                        io.getErr().println(issue);
                    }
                    io.getErr().println();
                }
            }
        } finally {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        handle.finish();
                    }
                });
            synchronized (runningProjects) {
                runningProjects.remove(project);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  project      DOCUMENT ME!
     * @param  cidsClass    DOCUMENT ME!
     * @param  groups       DOCUMENT ME!
     * @param  permissions  DOCUMENT ME!
     * @param  resolver     DOCUMENT ME!
     * @param  classCache   DOCUMENT ME!
     * @param  io           DOCUMENT ME!
     */
    private void checkCidsClass(
            final DomainserverProject project,
            final CidsClass cidsClass,
            final List<UserGroup> groups,
            final List<Permission> permissions,
            final PermissionResolver resolver,
            final Map<Integer, CidsClass> classCache,
            final InputOutput io) {
        io.getOut()
                .println(
                    NbBundle.getMessage(CheckRightsAction.class, "CheckRightsAction.io.checking", cidsClass.getName())); // NOI18N
        final String indent = "\t";                                                                                      // NOI18N
        final Set<CidsClass> relatedClasses = getRelatedClasses(cidsClass, classCache, new HashSet<CidsClass>(3));
        if (relatedClasses.isEmpty()) {
            // nothing to check, so it's ok
            io.getOut().println(indent + NbBundle.getMessage(CheckRightsAction.class, "CheckRightsAction.io.ok"));     // NOI18N
        } else {
            final Map<UserGroup, Map<Permission, Boolean>> effectivePermissions = getPermissions(
                    groups,
                    permissions,
                    cidsClass,
                    resolver);
            boolean good = true;
            for (final CidsClass relatedClass : relatedClasses) {
                if (
                    !comparePermissions(
                                project,
                                cidsClass,
                                effectivePermissions,
                                relatedClass,
                                getPermissions(groups, permissions, relatedClass, resolver),
                                io)) {
                    good = false;
                }
            }
            if (good) {
                io.getOut().println(indent + NbBundle.getMessage(CheckRightsAction.class, "CheckRightsAction.io.ok")); // NOI18N
            } else {
                io.getErr()
                        .println(
                            indent
                            + NbBundle.getMessage(
                                CheckRightsAction.class,
                                "CheckRightsAction.io.issuesIdentified"));                                             // NOI18N
            }
        }
    }

    /**
     * The effectivePermissions are the "root" permissions. All the relatedEffectivePermissions shall be equal or
     * superior to ensure that in case of e.g. write access all sub-objects are guaranteed to be writable also.
     *
     * @param   project                      DOCUMENT ME!
     * @param   cidsClass                    the class in context
     * @param   effectivePermissions         the permissions of the class in context
     * @param   relatedClass                 the class related to class in context
     * @param   relatedEffectivePermissions  the permissions of the class related to the class in context
     * @param   io                           the output provider
     *
     * @return  true, if the comparison did not find an issue, false otherwise
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private boolean comparePermissions(
            final DomainserverProject project,
            final CidsClass cidsClass,
            final Map<UserGroup, Map<Permission, Boolean>> effectivePermissions,
            final CidsClass relatedClass,
            final Map<UserGroup, Map<Permission, Boolean>> relatedEffectivePermissions,
            final InputOutput io) {
        boolean good = true;
        for (final UserGroup usergroup : effectivePermissions.keySet()) {
            final Map<Permission, Boolean> effective = effectivePermissions.get(usergroup);
            final Map<Permission, Boolean> related = relatedEffectivePermissions.get(usergroup);
            // if there are no permissions for this usergroup at all an issue is assumed
            if (related == null) {
                // this will never occur here because all usergroups of the domainserver are in both maps
                throw new IllegalStateException(
                    "erroneous relatedEffectivePermission map: missing usergroup: " + usergroup); // NOI18N
            }
            for (final Permission permission : effective.keySet()) {
                if (effective.get(permission)) {
                    // there is a certain effective permission so the related class should have it also
                    final Boolean relatedPerm = related.get(permission);
                    if (relatedPerm == null) {
                        // this will never occur here because all permissions for all usergroups are in both maps
                        throw new IllegalStateException(
                            "erroneous relatedEffectivePermission map: missing permission for usergroup: " // NOI18N
                            + usergroup
                            + " :: "                                                                       // NOI18N
                            + permission);
                    }
                    if (!relatedPerm) {
                        // we found an issue and we will store it for later summary
                        List<String> issues = runningProjects.get(project).get(cidsClass);
                        if (issues == null) {
                            issues = new ArrayList<String>();
                            runningProjects.get(project).put(cidsClass, issues);
                        }
                        issues.add(
                            "\t"                                                      // NOI18N
                            + NbBundle.getMessage(
                                CheckRightsAction.class,
                                "CheckRightsAction.io.issueUGRightClassRelatedClass", // NOI18N
                                new Object[] { usergroup, permission, cidsClass, relatedClass }));
                        good = false;
                    }
                }
            }
        }
        return good;
    }

    /**
     * Builds a permission map: which usergroup may do what for the given entity
     *
     * @param   groups       all the usergroups to be checked
     * @param   permissions  all the permissions to check for
     * @param   entity       the entity in context
     * @param   resolver     the permission resolver for this domainserver
     *
     * @return  a permission map: which usergroup may do what
     */
    private Map<UserGroup, Map<Permission, Boolean>> getPermissions(
            final List<UserGroup> groups,
            final List<Permission> permissions,
            final PermissionAwareEntity entity,
            final PermissionResolver resolver) {
        final Map<UserGroup, Map<Permission, Boolean>> ugPermMap = new Hashtable<UserGroup, Map<Permission, Boolean>>();
        for (final UserGroup group : groups) {
            final Map<Permission, Boolean> permMap = new Hashtable<Permission, Boolean>();
            for (final Permission permission : permissions) {
                permMap.put(permission, resolver.hasPerm(group, entity, permission));
            }
            ugPermMap.put(group, permMap);
        }
        return ugPermMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsClass   DOCUMENT ME!
     * @param   classCache  DOCUMENT ME!
     * @param   related     processed DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Set<CidsClass> getRelatedClasses(
            final CidsClass cidsClass,
            final Map<Integer, CidsClass> classCache,
            final Set<CidsClass> related) {
        for (final Attribute attr : cidsClass.getAttributes()) {
            if (attr.isForeignKey()) {
                final CidsClass relatedClass = classCache.get(attr.getForeignKeyClass());
                if (related.add(relatedClass)) {
                    related.addAll(getRelatedClasses(relatedClass, classCache, related));
                }
            }
        }
        return related;
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CheckRightsAction.class, "CTL_CheckRightsAction"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { DomainserverContext.class };
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(final Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }
        final DomainserverProject project = activatedNodes[0].getCookie(DomainserverContext.class)
                    .getDomainserverProject();
        if (activatedNodes.length == 1) {
            if (
                (activatedNodes[0].getCookie(ClassManagementContextCookie.class) == null)
                        && (activatedNodes[0].getCookie(CidsClassContextCookie.class) == null)) {
                return false;
            }
        } else {
            for (final Node node : activatedNodes) {
                if (
                    !node.getCookie(DomainserverContext.class).getDomainserverProject().equals(project)
                            || (node.getCookie(CidsClassContextCookie.class) == null)) {
                    return false;
                }
            }
        }

        synchronized (runningProjects) {
            if (runningProjects.containsKey(project)) {
                return false;
            }
            return project.isConnected();
        }
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class FocusAction implements Action {

        //~ Instance fields ----------------------------------------------------

        private final transient InputOutput io;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CancelAction object.
         *
         * @param  io  DOCUMENT ME!
         */
        public FocusAction(final InputOutput io) {
            this.io = io;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Object getValue(final String key) {
            // not needed
            return null;
        }

        @Override
        public void putValue(final String key, final Object value) {
            // not needed
        }

        @Override
        public void setEnabled(final boolean b) {
            // not needed
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            // not needed
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            // not needed
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            io.select();
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class CancelAction implements Action, Cancellable {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon icon;
        private transient boolean cancelled;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CancelAction object.
         */
        public CancelAction() {
            icon = new ImageIcon(Utilities.loadImage("de/cismet/cids/abf/abfcore/res/images/button_cancel.png")); // NOI18N
            cancelled = false;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public Object getValue(final String key) {
            if (SMALL_ICON.equals(key)) {
                return icon;
            }
            return null;
        }

        @Override
        public void putValue(final String key, final Object value) {
            // not needed
        }

        @Override
        public void setEnabled(final boolean b) {
            // not needed
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            // not needed
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            // not needed
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            cancelled = true;
        }

        @Override
        public boolean cancel() {
            cancelled = true;
            return cancelled;
        }
    }
}

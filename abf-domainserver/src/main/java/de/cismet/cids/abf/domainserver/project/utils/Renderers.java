/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.utils;

import org.apache.log4j.Logger;

import org.openide.util.ImageUtilities;

import java.awt.Component;
import java.awt.Image;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.common.PermissionAwareEntity;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class Renderers {

    //~ Static fields/initializers ---------------------------------------------

    // TODO: be aware of performance issues
    private static final DefaultListCellRenderer LIST_R = new DefaultListCellRenderer();
    private static final DefaultTableCellRenderer TABLE_R = new DefaultTableCellRenderer();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Renderers object.
     */
    private Renderers() {
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class UnifiedCellRenderer implements ListCellRenderer, TableCellRenderer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient Logger LOG = Logger.getLogger(UnifiedCellRenderer.class);

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon groupIcon = new ImageIcon(
                ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "group.png"));       // NOI18N
        private final transient ImageIcon remotegroupIcon = new ImageIcon(
                ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "remotegroup.png")); // NOI18N

        private final transient PermissionResolver resolver;
        private final transient PermissionAwareEntity entity;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UnifiedCellRenderer object.
         */
        public UnifiedCellRenderer() {
            this(null, null);
        }

        /**
         * Creates a new UnifiedCellRenderer object.
         *
         * @param  project  DOCUMENT ME!
         * @param  entity   DOCUMENT ME!
         */
        public UnifiedCellRenderer(final DomainserverProject project, final PermissionAwareEntity entity) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating new UnifiedCellRenderer: project: " + project + " :: entity: " + entity); // NOI18N
            }

            this.entity = entity;
            if ((project == null) || (entity == null)) {
                resolver = null;
            } else {
                resolver = PermissionResolver.getInstance(project);
            }
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel label = (JLabel)LIST_R.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);
            modifyLabel(label, value);

            return label;
        }

        @Override
        public Component getTableCellRendererComponent(final JTable t,
                final Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column) {
            final JLabel label = (JLabel)TABLE_R.getTableCellRendererComponent(
                    t,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column);
            modifyLabel(label, value);

            return label;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   label  DOCUMENT ME!
         * @param   value  DOCUMENT ME!
         *
         * @throws  UnsupportedOperationException  DOCUMENT ME!
         */
        private void modifyLabel(final JLabel label, final Object value) {
            if (value != null) {
                if (value instanceof Permission) {
                    final Permission perm = (Permission)value;
                    String s;
                    if (resolver == null) {
                        s = perm.getDescription();
                    } else {
                        s = resolver.getPermString(entity, perm).getPermissionString();
                    }
                    if (s == null) {
                        s = perm.getKey();
                    }
                    label.setText(s);
                    label.setIcon(null);
                } else if (value instanceof UserGroup) {
                    final UserGroup ug = (UserGroup)value;
                    final StringBuilder name = new StringBuilder(ug.getName());
                    Icon icon = groupIcon;
                    if (!"local".equalsIgnoreCase(                  // NOI18N
                                    ug.getDomain().getName())) {
                        name.append('@')                            // NOI18N
                        .append(ug.getDomain().getName());
                        icon = remotegroupIcon;
                    }
                    label.setText(name.toString());
                    label.setIcon(icon);
                } else if (value instanceof Domain) {
                    label.setText(((Domain)value).getName());
                    label.setIcon(null);
                } else if (value instanceof CidsClass) {
                    label.setText(((CidsClass)value).getTableName());
                    label.setIcon(null);
                } else if (value instanceof Policy) {
                    throw new UnsupportedOperationException("tbd"); // NOI18N
                } else if (value instanceof JLabel) {
                    final JLabel lbl = (JLabel)value;
                    label.setText(lbl.getText());
                    label.setIcon(lbl.getIcon());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class JavaClassTypeRenderer implements ListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                return l;
            }

            final String type = value.toString();
            if (type.equals(JavaClass.Type.TO_STRING.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.JavaClassTypeRenderer.getListCellRendererComponent().toStringClass")); // NOI18N
            } else if (type.equals(JavaClass.Type.RENDERER.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.JavaClassTypeRenderer.getListCellRendererComponent().renderer"));      // NOI18N
            } else if (type.equals(JavaClass.Type.SIMPLE_EDITOR.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.JavaClassTypeRenderer.getListCellRendererComponent().simpleEditor"));  // NOI18N
            } else if (type.equals(JavaClass.Type.COMPLEX_EDITOR.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.JavaClassTypeRenderer.getListCellRendererComponent().complexEditor")); // NOI18N
            } else if (type.equals(JavaClass.Type.UNKNOWN.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.JavaClassTypeRenderer.getListCellRendererComponent().unknownClass"));  // NOI18N
            }

            return l;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class NodeTypeRenderer implements ListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            if (value == null) {
                return l;
            }

            final String type = value.toString();
            if (type.equals(CatNode.Type.CLASS.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.NodeTypeRenderer.getListCellRendererComponent().classNode"));          // NOI18N
            } else if (type.equals(CatNode.Type.OBJECT.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.NodeTypeRenderer.getListCellRendererComponent().objectNode"));         // NOI18N
            } else if (type.equals(CatNode.Type.ORG.getType())) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.NodeTypeRenderer.getListCellRendererComponent().organisationalNode")); // NOI18N
            } else {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.NodeTypeRenderer.getListCellRendererComponent().unknownType"));        // NOI18N
            }

            return l;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class DomainListRenderer implements ListCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon domainIcon;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UserGroupListRenderer object.
         */
        public DomainListRenderer() {
            domainIcon = ImageUtilities.loadImageIcon(DomainserverProject.IMAGE_FOLDER + "domainserver.png", false); // NOI18N
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Domain) {
                final Domain domain = ((Domain)value);

                l.setText(domain.getName());
                l.setIcon(domainIcon);
            }

            return l;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class UserGroupListRenderer implements ListCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon groupIcon;
        private final transient ImageIcon remotegroupIcon;

        private final transient DomainserverProject project;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UserGroupListRenderer object.
         *
         * @param  project  DOCUMENT ME!
         */
        public UserGroupListRenderer(final DomainserverProject project) {
            groupIcon = new ImageIcon(ImageUtilities.loadImage(
                        DomainserverProject.IMAGE_FOLDER
                                + "group.png"));       // NOI18N
            remotegroupIcon = new ImageIcon(ImageUtilities.loadImage(
                        DomainserverProject.IMAGE_FOLDER
                                + "remotegroup.png")); // NOI18N
            this.project = project;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            final UserGroup ug = ((UserGroup)value);
            final StringBuilder name = new StringBuilder(ug.getName());

            Icon icon = groupIcon;
            if (ProjectUtils.isRemoteGroup(ug, project)) {
                icon = remotegroupIcon;
                name.append('@').append(ug.getDomain()); // NOI18N
            }
            l.setText(name.toString());
            l.setIcon(icon);

            return l;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class UserListRenderer implements ListCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon userIcon;
        private final transient ImageIcon adminIcon;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UserGroupListRenderer object.
         */
        public UserListRenderer() {
            userIcon = new ImageIcon(ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "user.png"));   // NOI18N
            adminIcon = new ImageIcon(ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "admin.png")); // NOI18N
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            final User user = ((User)value);
            l.setText(user.getLoginname());

            if (user.isAdmin()) {
                l.setIcon(adminIcon);
            } else {
                l.setIcon(userIcon);
            }

            return l;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class IconCellRenderer implements ListCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private final transient DomainserverProject project;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new IconCellRenderer object.
         *
         * @param  project  DOCUMENT ME!
         */
        public IconCellRenderer(final DomainserverProject project) {
            this.project = project;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final de.cismet.cids.jpa.entity.cidsclass.Icon i = (de.cismet.cids.jpa.entity.cidsclass.Icon)value;
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (i == null) {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class,
                        "Renderers.IconCellRenderer.getListCellRendererComponent().noIcon")); // NOI18N
                l.setIcon(null);
            } else {
                final Image ii = ProjectUtils.getImageForIconAndProject(
                        i,
                        project);
                l.setText(i.getName());
                if (ii != null) {
                    l.setIcon(new ImageIcon(ii));
                }
            }

            return l;
        }
    }
}

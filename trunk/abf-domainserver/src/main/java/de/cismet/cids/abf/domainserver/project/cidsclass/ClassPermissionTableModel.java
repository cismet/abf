/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import javax.swing.table.AbstractTableModel;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;

import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.permission.ClassPermission;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ClassPermissionTableModel extends AbstractTableModel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ClassPermissionTableModel.class);

    public static final int GROUP = 0;
    public static final int PERMISSION = 1;

    //~ Instance fields --------------------------------------------------------

    private final transient CidsClass cidsClass;
    private final transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassPermissionTableModel object.
     *
     * @param  project    DOCUMENT ME!
     * @param  cidsClass  DOCUMENT ME!
     */
    public ClassPermissionTableModel(final DomainserverProject project,
            final CidsClass cidsClass) {
        this.cidsClass = cidsClass;
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   rowIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ClassPermission getClassPermission(final int rowIndex) {
        if (cidsClass.getClassPermissions().size() > rowIndex) {
            return (ClassPermission)cidsClass.getClassPermissions().toArray()[rowIndex];
        } else {
            return null;
        }
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (cidsClass.getClassPermissions().size() > rowIndex) {
            final ClassPermission perm = (ClassPermission)cidsClass.getClassPermissions().toArray()[rowIndex];
            if (GROUP == columnIndex) {
                final UserGroup ug = perm.getUserGroup();
                if (ug == null) {
                    return "";                // NOI18N
                }
                if (ProjectUtils.isRemoteGroup(ug, project)) {
                    return ug.getName() + "@" // NOI18N
                                + ug.getDomain().getName();
                } else {
                    return ug.getName();
                }
            } else if (PERMISSION == columnIndex) {
                return perm.getPermission().getDescription();
            }
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clazz.getPermissions().size():" // NOI18N
                        + cidsClass.getClassPermissions().size());
        }
        return cidsClass.getClassPermissions().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(final int column) {
        if (GROUP == column) {
            return org.openide.util.NbBundle.getMessage(
                    ClassPermissionTableModel.class,
                    "ClassPermissionTableModel.getColumnName(int).groupColumn");      // NOI18N
        } else if (PERMISSION == column) {
            return org.openide.util.NbBundle.getMessage(
                    ClassPermissionTableModel.class,
                    "ClassPermissionTableModel.getColumnName(int).permissionColumn"); // NOI18N
        } else {
            throw new IllegalArgumentException(
                "invalid column: "
                        + column);                                                    // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        if (GROUP == column) {
            return String.class;
        } else if (PERMISSION == column) {
            return String.class;
        } else {
            throw new IllegalArgumentException(
                "invalid column: "
                        + column); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  perm  DOCUMENT ME!
     */
    public void addPermission(final ClassPermission perm) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addPermission: " + perm); // NOI18N
        }
        cidsClass.getClassPermissions().add(perm);
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        // read-only
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }
}

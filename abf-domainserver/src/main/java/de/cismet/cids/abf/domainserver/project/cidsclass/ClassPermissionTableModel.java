/*
 * ClassPermissionTableModel.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 14. März 2007, 14:40
 *
 */

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.permission.ClassPermission;
import de.cismet.cids.jpa.entity.user.UserGroup;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class ClassPermissionTableModel extends AbstractTableModel
{
    private static final transient Logger LOG = Logger.getLogger(
            ClassPermissionTableModel.class);

    public final static int GROUP = 0;
    public final static int PERMISSION = 1;

    private final transient CidsClass cidsClass;
    private final transient DomainserverProject project;

    public ClassPermissionTableModel(final DomainserverProject project,
            final CidsClass cidsClass)
    {
        this.cidsClass = cidsClass;
        this.project = project;
    }


    public ClassPermission getClassPermission(final int rowIndex)
    {
        if(cidsClass.getClassPermissions().size() > rowIndex)
        {
            return (ClassPermission)cidsClass.getClassPermissions()
                    .toArray()[rowIndex];
        }else
        {
            return null;
        }
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {

        if(cidsClass.getClassPermissions().size() > rowIndex)
        {
            final ClassPermission perm = (ClassPermission)cidsClass
                    .getClassPermissions().toArray()[rowIndex];
            if(GROUP == columnIndex)
            {
                final UserGroup ug = perm.getUserGroup();
                if(ug == null)
                {
                    return ""; // NOI18N
                }
                if(ProjectUtils.isRemoteGroup(ug, project))
                {
                    return ug.getName() + "@" // NOI18N
                            + ug.getDomain().getName();
                }else
                {
                    return ug.getName();
                }
            }else if(PERMISSION == columnIndex)
            {
                return perm.getPermission().getDescription();
            }
        }
        return null;
    }

    @Override
    public int getRowCount()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("clazz.getPermissions().size():" // NOI18N
                    + cidsClass.getClassPermissions().size());
        }
        return cidsClass.getClassPermissions().size();
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public String getColumnName(final int column)
    {
        if(GROUP == column)
        {
            return org.openide.util.NbBundle.getMessage(
                    ClassPermissionTableModel.class, "Dsc_group"); // NOI18N
        }else if(PERMISSION == column)
        {
            return org.openide.util.NbBundle.getMessage(
                    ClassPermissionTableModel.class, "Dsc_right"); // NOI18N
        }else
        {
            throw new IllegalArgumentException(
                    "invalid column: " + column); // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(final int column)
    {
        if(GROUP == column)
        {
            return String.class;
        }else if(PERMISSION == column)
        {
            return String.class;
        }else
        {
            throw new IllegalArgumentException(
                    "invalid column: " + column); // NOI18N
        }
    }

    public void addPermission(final ClassPermission perm)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("addPermission: " + perm); // NOI18N
        }
        cidsClass.getClassPermissions().add(perm);
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(final Object aValue, final int row, final int column)
    {
        // read-only
    }

    @Override
    public boolean isCellEditable(final int row, final int column)
    {
        return false;
    }
}
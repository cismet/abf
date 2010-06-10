/*
 * ClassTableModel.java, encoding: UTF-8
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
 * Created on 8. März 2007, 16:22
 *
 */

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import javax.persistence.NoResultException;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class ClassTableModel extends AbstractTableModel
{
    private static final transient Logger LOG = Logger.getLogger(
            ClassTableModel.class);

    public static final int POS = 0;
    public static final int NAME = 1;
    public static final int FIELD_NAME = 2;
    public static final int TYPE = 3;
    public static final int DEFAULTVALUE = 4;
    public static final int INDEXED = 5;
    public static final int OPTIONAL = 6;

    private final transient CidsClass cidsClass;
    
    public ClassTableModel(final DomainserverProject project, final
            CidsClass cidsClass)
    {
        if(cidsClass == null)
        {
            this.cidsClass = new CidsClass();
            final Attribute primaryKey = new Attribute();
            primaryKey.setName("id"); // NOI18N
            primaryKey.setFieldName("ID"); // NOI18N
            primaryKey.setDescription(org.openide.util.NbBundle.getMessage(
                    ClassTableModel.class, "Dsc_primaryKey")); // NOI18N
            primaryKey.setIndexed(Boolean.FALSE);
            primaryKey.setOptional(Boolean.FALSE);
            try
            {
                primaryKey.setType(project.getCidsDataObjectBackend()
                        .getEntity(Type.class, "INTEGER")); // NOI18N
            }catch(final NoResultException e)
            {
                LOG.warn("could not find type INTEGER, " // NOI18N
                        + "trying lower case", e); // NOI18N
                try
                {
                    primaryKey.setType(project.getCidsDataObjectBackend()
                            .getEntity(Type.class, "integer")); // NOI18N
                }catch(final NoResultException ex)
                {
                    throw new IllegalStateException(
                            "cannot set type for primary key", ex); // NOI18N
                }
            }
            primaryKey.setPosition(0);
            primaryKey.setVisible(Boolean.FALSE);
            primaryKey.setCidsClass(cidsClass);
            cidsClass.getAttributes().add(primaryKey);
        }else
        {
            this.cidsClass = cidsClass;
        }
    }
    
    Attribute getAttributeAt(final int rowIndex)
    {
        if(cidsClass.getAttributes().size() > rowIndex)
        {
            return (Attribute)cidsClass.getAttributes().toArray()[rowIndex];
        }else
        {
            return null;
        }
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        if(cidsClass.getAttributes().size() > rowIndex)
        {
            final Attribute attr = getAttributeAt(rowIndex);
            switch(columnIndex)
            {
                case POS:
                    return attr.getPosition();
                case NAME:
                    return attr.getName();
                case FIELD_NAME:
                    return attr.getFieldName();
                case TYPE:
                    if(attr.getType() == null)
                    {
                        return null;
                    }else
                    {
                        final StringBuffer sb = 
                                new StringBuffer(attr.getType().getName());
                        if(attr.getPrecision() != null)
                        {
                            sb.append('(') // NOI18N
                                    .append(attr.getPrecision().toString());
                            if(attr.getScale() != null)
                            {
                                sb.append(", ") // NOI18N
                                        .append(attr.getScale().toString());
                            }
                            sb.append(')'); // NOI18N
                        }
                        return sb.toString();
                    }
                case DEFAULTVALUE:
                    return attr.getDefaultValue();
                case INDEXED:
                    return attr.isIndexed();
                case OPTIONAL:
                    return attr.isOptional();
                default:
                    break;
            }
        }
        return null;
    }
    
    @Override
    public int getRowCount()
    {
        return cidsClass.getAttributes().size();
    }
    
    @Override
    public int getColumnCount()
    {
        return 7;
    }
    
    @Override
    public String getColumnName(final int column)
    {
        switch(column)
        {
            case POS:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_positionAbbrev"); // NOI18N
            case NAME:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_name"); // NOI18N
            case FIELD_NAME:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_fieldname"); // NOI18N
            case TYPE:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_type"); // NOI18N
            case DEFAULTVALUE:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_defaultValue"); // NOI18N
            case INDEXED:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_indexed"); // NOI18N
            case OPTIONAL:
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class, "Dsc_optional"); // NOI18N
            default:
                break;
        }
        throw new IllegalArgumentException("invalid column: " + column);//NOI18N
    }
    
    @Override
    public Class<?> getColumnClass(final int column)
    {
        switch(column)
        {
            case POS:
                return Integer.class;
            case NAME:
                return String.class;
            case FIELD_NAME:
                return String.class;
            case TYPE:
                return String.class;
            case DEFAULTVALUE:
                return String.class;
            case INDEXED:
                return Boolean.class;
            case OPTIONAL:
                return Boolean.class;
            default:
                break;
        }
        throw new IllegalArgumentException("invalid column: " + column);//NOI18N
    }
    
    CidsClass getCidsClass()
    {
        return cidsClass;
    }
    
    void addAttribute(final Attribute attr)
    {
        attr.setCidsClass(cidsClass);
        cidsClass.getAttributes().add(attr);
        fireTableDataChanged();
    }
    
    @Override
    public void setValueAt(final Object aValue, final int row, final int column)
    {
        if(cidsClass.getAttributes().size() > row)
        {
            final Attribute attr = getAttributeAt(row);
            switch(column)
            {
                case POS:
                    attr.setPosition((Integer)aValue);
                    break;
                case NAME:
                    attr.setName((String)aValue);
                    break;
                case FIELD_NAME:
                    attr.setFieldName((String)aValue);
                    break;
                case TYPE:
                    // read-only
                    break;
                case DEFAULTVALUE:
                    attr.setDefaultValue((String)aValue);
                    break;
                case INDEXED:
                    attr.setIndexed((Boolean)aValue);
                    break;
                case OPTIONAL:
                    attr.setOptional((Boolean)aValue);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "invalid column: " + column); // NOI18N
            }
            fireTableDataChanged();
        }
    }
    
    @Override
    public boolean isCellEditable(final int row, final int column)
    {
        switch(column)
        {
            case POS:
                return true;
            case NAME:
                return true;
            case FIELD_NAME:
                return true;
            case TYPE:
                return false;
            case DEFAULTVALUE:
                return true;
            case INDEXED:
                return true;
            case OPTIONAL:
                return !"id".equalsIgnoreCase( // NOI18N
                        getValueAt(row, NAME).toString());
            default:
                break;
        }
        return false;
    }
}
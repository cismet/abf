/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import javax.persistence.NoResultException;

import javax.swing.table.AbstractTableModel;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;

import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ClassTableModel extends AbstractTableModel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ClassTableModel.class);

    public static final int POS = 0;
    public static final int NAME = 1;
    public static final int FIELD_NAME = 2;
    public static final int TYPE = 3;
    public static final int DEFAULTVALUE = 4;
    public static final int INDEXED = 5;
    public static final int OPTIONAL = 6;

    //~ Instance fields --------------------------------------------------------

    private final transient CidsClass cidsClass;
    private transient boolean sync;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassTableModel object.
     *
     * @param   project    DOCUMENT ME!
     * @param   cidsClass  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public ClassTableModel(final DomainserverProject project, final CidsClass cidsClass) {
        if (cidsClass == null) {
            this.cidsClass = new CidsClass();
            final Attribute primaryKey = new Attribute();
            primaryKey.setName("id");                                                                          // NOI18N
            primaryKey.setFieldName("ID");                                                                     // NOI18N
            primaryKey.setDescription(org.openide.util.NbBundle.getMessage(
                    ClassTableModel.class,
                    "ClassTableModel.ClassTableModel(DomainserverProject,CidsClass).primaryKey.description")); // NOI18N
            primaryKey.setIndexed(Boolean.FALSE);
            primaryKey.setOptional(Boolean.FALSE);
            try {
                primaryKey.setType(project.getCidsDataObjectBackend().getEntity(Type.class, "INTEGER"));       // NOI18N
            } catch (final NoResultException e) {
                LOG.warn("could not find type INTEGER, trying lower case", e);                                 // NOI18N
                try {
                    primaryKey.setType(project.getCidsDataObjectBackend().getEntity(Type.class, "integer"));   // NOI18N
                } catch (final NoResultException ex) {
                    throw new IllegalStateException("cannot set type for primary key", ex);                    // NOI18N                                                                            // NOI18N
                }
            }
            primaryKey.setPosition(0);
            primaryKey.setVisible(Boolean.FALSE);
            primaryKey.setCidsClass(this.cidsClass);
            this.cidsClass.getAttributes().add(primaryKey);
        } else {
            this.cidsClass = cidsClass;
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   rowIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Attribute getAttributeAt(final int rowIndex) {
        if (cidsClass.getAttributes().size() > rowIndex) {
            return (Attribute)cidsClass.getAttributes().toArray()[rowIndex];
        } else {
            return null;
        }
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (cidsClass.getAttributes().size() > rowIndex) {
            final Attribute attr = getAttributeAt(rowIndex);
            switch (columnIndex) {
                case POS: {
                    return attr.getPosition();
                }
                case NAME: {
                    return attr.getName();
                }
                case FIELD_NAME: {
                    return attr.getFieldName();
                }
                case TYPE: {
                    if (attr.getType() == null) {
                        return null;
                    } else {
                        final StringBuffer sb = new StringBuffer(attr.getType().getName());
                        if (attr.getPrecision() != null) {
                            sb.append('(').append(attr.getPrecision().toString());
                            if (attr.getScale() != null) {
                                sb.append(", ").append(attr.getScale().toString()); // NOI18N
                            }
                            sb.append(')');
                        }
                        return sb.toString();
                    }
                }
                case DEFAULTVALUE: {
                    return attr.getDefaultValue();
                }
                case INDEXED: {
                    return attr.isIndexed();
                }
                case OPTIONAL: {
                    return attr.isOptional();
                }
                default: {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return cidsClass.getAttributes().size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case POS: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).posColumn");          // NOI18N
            }
            case NAME: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).nameColumn");         // NOI18N
            }
            case FIELD_NAME: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).fieldnameColumn");    // NOI18N
            }
            case TYPE: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).typeColumn");         // NOI18N
            }
            case DEFAULTVALUE: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).defaultValueColumn"); // NOI18N
            }
            case INDEXED: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).indexedColumn");      // NOI18N
            }
            case OPTIONAL: {
                return org.openide.util.NbBundle.getMessage(
                        ClassTableModel.class,
                        "ClassTableModel.getColumnName(int).optionalColumn");     // NOI18N
            }
            default: {
                throw new IllegalArgumentException("invalid column: " + column);  // NOI18N
            }
        }
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        switch (column) {
            case POS: {
                return Integer.class;
            }
            case NAME: {
                return String.class;
            }
            case FIELD_NAME: {
                return String.class;
            }
            case TYPE: {
                return String.class;
            }
            case DEFAULTVALUE: {
                return String.class;
            }
            case INDEXED: {
                return Boolean.class;
            }
            case OPTIONAL: {
                return Boolean.class;
            }
            default: {
                throw new IllegalArgumentException("invalid column: " + column); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsClass getCidsClass() {
        return cidsClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  attr  DOCUMENT ME!
     */
    void addAttribute(final Attribute attr) {
        attr.setCidsClass(cidsClass);
        cidsClass.getAttributes().add(attr);
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        if (cidsClass.getAttributes().size() > row) {
            final Attribute attr = getAttributeAt(row);
            switch (column) {
                case POS: {
                    attr.setPosition((Integer)aValue);
                    break;
                }
                case NAME: {
                    attr.setName((String)aValue);

                    // sync fieldname if it is not set already
                    final String fieldname = (String)getValueAt(row, FIELD_NAME);
                    if ((fieldname == null) || fieldname.isEmpty() || sync) {
                        attr.setFieldName(ProjectUtils.toDBCompatibleString(((String)aValue).toLowerCase()));
                    }
                    break;
                }
                case FIELD_NAME: {
                    attr.setFieldName((String)aValue);
                    break;
                }
                case TYPE: {
                    // read-only
                    break;
                }
                case DEFAULTVALUE: {
                    if ("<null>".equals(aValue)) {
                        attr.setDefaultValue(null);
                    } else {
                        attr.setDefaultValue((String)aValue);
                    }
                    break;
                }
                case INDEXED: {
                    attr.setIndexed((Boolean)aValue);
                    break;
                }
                case OPTIONAL: {
                    attr.setOptional((Boolean)aValue);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("invalid column: " + column); // NOI18N
                }
            }
            fireTableDataChanged();
        }
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        switch (column) {
            case POS: {
                return true;
            }
            case NAME: {
                return true;
            }
            case FIELD_NAME: {
                return true;
            }
            case TYPE: {
                return false;
            }
            case DEFAULTVALUE: {
                return true;
            }
            case INDEXED: {
                return true;
            }
            case OPTIONAL: {
                return !"id".equalsIgnoreCase(getValueAt(row, NAME).toString()); // NOI18N
            }
            default: {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sync  DOCUMENT ME!
     */
    void setAttrSync(final boolean sync) {
        this.sync = sync;
    }
}

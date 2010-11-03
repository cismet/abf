/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.utils;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ROResultSetTableModel extends AbstractTableModel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ROResultSetTableModel.class);

    //~ Instance fields --------------------------------------------------------

    private transient List<String> columnNames;
    private transient List<List> rows;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ROResultSetTableModel object.
     */
    public ROResultSetTableModel() {
        rows = new ArrayList<List>();
        columnNames = new ArrayList<String>();
    }

    /**
     * Creates a new ROResultSetTableModel object.
     *
     * @param  set  DOCUMENT ME!
     */
    public ROResultSetTableModel(final ResultSet set) {
        setNewResultSet(set);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  set  DOCUMENT ME!
     */
    public synchronized void setNewResultSet(final ResultSet set) {
        try {
            rows.clear();
            final ResultSetMetaData metadata = set.getMetaData();
            final int columns = metadata.getColumnCount();
            columnNames = new ArrayList(columns);
            for (int i = 1; i <= columns; i++) {
                columnNames.add(metadata.getColumnName(i));
            }
            while (set.next()) {
                final List rowData = new ArrayList(columns);
                for (int i = 1; i <= columns; i++) {
                    rowData.add(set.getObject(i));
                }
                rows.add(rowData);
            }
            // table empty ?
            if (rows.isEmpty()) {
                final List rowData = new ArrayList(columns);
                rowData.add(org.openide.util.NbBundle.getMessage(
                        ROResultSetTableModel.class,
                        "ROResultSetTableModel.setNewResultSet(ResultSet).tableEmpty")); // NOI18N
                for (int i = 2; i <= columns; i++) {
                    rowData.add(" ");                                                    // NOI18N
                }
                rows.add(rowData);
            }
            fireTableStructureChanged();
        } catch (final SQLException ex) {
            LOG.error("could not retrieve table data", ex);                              // NOI18N
            setErrorState(org.openide.util.NbBundle.getMessage(
                    ROResultSetTableModel.class,
                    "ROResultSetTableModel.setNewResultSet(ResultSet).ErrorState.coultNotRetrieveTableDataColon"),
                ex);                                                                     // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  columnName  DOCUMENT ME!
     * @param  content     DOCUMENT ME!
     */
    public synchronized void setSingleColumn(
            final String columnName,
            final Object content) {
        rows.clear();
        columnNames.clear();
        final List column = new ArrayList(1);
        column.add(content);
        rows.add(column);
        columnNames.add(columnName);
        fireTableStructureChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  message  DOCUMENT ME!
     * @param  t        DOCUMENT ME!
     */
    protected void setErrorState(final String message, final Throwable t) {
        setSingleColumn(org.openide.util.NbBundle.getMessage(
                ROResultSetTableModel.class,
                "ROResultSetTableModel.setErrorState(String,Throwable).defaultMessage"), // NOI18N
            message
                    + "\n"
                    + t); // NOI18N
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return (rows.isEmpty() || (rows.get(0) == null)) ? 0 : rows.get(0).size();
    }

    @Override
    public String getColumnName(final int column) {
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }
}

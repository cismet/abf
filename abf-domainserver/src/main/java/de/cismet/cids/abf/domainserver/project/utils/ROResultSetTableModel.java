/*
 * ROResultSetTableModel.java, encoding: UTF-8
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
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 7. September 2007, 17:24
 *
 */

package de.cismet.cids.abf.domainserver.project.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class ROResultSetTableModel extends AbstractTableModel
{
    private static final transient Logger LOG = Logger.getLogger(
            ROResultSetTableModel.class);
    
    private transient List<String> columnNames;
    private transient List<List> rows;
    
    public ROResultSetTableModel()
    {
        rows = new ArrayList<List>();
        columnNames = new ArrayList<String>();
    }
    
    public ROResultSetTableModel(final ResultSet set)
    {
        setNewResultSet(set);
    }

    public synchronized void setNewResultSet(final ResultSet set)
    {
        try
        {
            rows.clear();
            final ResultSetMetaData metadata = set.getMetaData();
            final int columns = metadata.getColumnCount();
            columnNames = new ArrayList(columns);
            for(int i = 1; i <= columns; i++)
            {
                columnNames.add(metadata.getColumnName(i));
            }
            while(set.next())
            {
                final List rowData = new ArrayList(columns);
                for(int i = 1; i <= columns; i++)
                {
                    rowData.add(set.getObject(i));
                }
                rows.add(rowData);
            }
            // table empty ?
            if(rows.isEmpty())
            {
                final List rowData = new ArrayList(columns);
                rowData.add(org.openide.util.NbBundle.getMessage(
                        ROResultSetTableModel.class, "ROResultSetTableModel.setNewResultSet(ResultSet).tableEmpty"));//NOI18N
                for(int i = 2; i <= columns; i++)
                {
                    rowData.add(" "); // NOI18N
                }
                rows.add(rowData);
            }       
            fireTableStructureChanged();
        }catch(final SQLException ex)
        {
            LOG.error("could not retrieve table data", ex); // NOI18N
            setErrorState(org.openide.util.NbBundle.getMessage(
                    ROResultSetTableModel.class,
                    "ROResultSetTableModel.setNewResultSet(ResultSet).ErrorState.coultNotRetrieveTableDataColon"), ex); // NOI18N
        }
    }
    
    public synchronized void setSingleColumn(
            final String columnName, final Object content)
    {
        rows.clear();
        columnNames.clear();
        final List column = new ArrayList(1);
        column.add(content);
        rows.add(column);
        columnNames.add(columnName);
        fireTableStructureChanged();
    }
    
    protected void setErrorState(final String message, final Throwable t)
    {
        setSingleColumn(org.openide.util.NbBundle.getMessage(
                ROResultSetTableModel.class, "ROResultSetTableModel.setErrorState(String,Throwable).defaultMessage"), // NOI18N
                message + "\n" + t); // NOI18N
    }
    
    @Override
    public int getRowCount()
    {
        return rows.size();
    }

    @Override
    public int getColumnCount()
    {
        return rows.isEmpty() || rows.get(0) == null ? 0 : rows.get(0).size();
    }

    @Override
    public String getColumnName(final int column)
    {
        return columnNames.get(column);
    }
    
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        return rows.get(rowIndex).get(columnIndex);
    }
}
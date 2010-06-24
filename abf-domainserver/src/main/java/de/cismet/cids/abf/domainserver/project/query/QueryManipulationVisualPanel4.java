/*
 * QueryManipulationVisualPanel4.java, encoding: UTF-8
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
 * Created on ???
 *
 */

package de.cismet.cids.abf.domainserver.project.query;

import de.cismet.cids.abf.domainserver.project.utils.ROResultSetTableModel;
import de.cismet.cids.jpa.entity.query.QueryParameter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.text.EditorKit;
import org.apache.log4j.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

public final class QueryManipulationVisualPanel4 extends JPanel implements
        Observer, Cancellable
{
    private static final transient Logger LOG = Logger.getLogger(
            QueryManipulationVisualPanel4.class);

    private static RequestProcessor reqProc = new RequestProcessor(
            "StatementExecutionProcessor", 1, true); // NOI18N
    
    private final transient QueryManipulationWizardPanel4 model;
    private transient ROResultSetTableModel resultModel;
    private final transient RequestProcessor.Task execTask;
    private transient ProgressHandle progressHandle;
    
    public QueryManipulationVisualPanel4(
            final QueryManipulationWizardPanel4 model)
    {
        this.model = model;
        execTask = reqProc.create(new ExecStatementRunnable(), true);
        initComponents();
    }
    
    private void init()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init"); // NOI18N
        }
        final DefaultTableModel dtm = (DefaultTableModel)tblParam.getModel();
        while(dtm.getRowCount() > 0)
        {
            dtm.removeRow(0);
        }
        final String columnParam = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.init().columnParam"); // NOI18N
        final String columnParamName = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.init().columnParamName"); // NOI18N
        final String columnDesc = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.init().columnDesc");//NOI18N
        final String columnIsResult = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.init().columnIsResult"); // NOI18N
        final String columnParamValue = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.init().columnParamValue");// NOI18N
        dtm.setColumnIdentifiers(new Object[]
        {
            columnParam,
            columnParamName,
            columnDesc,
            columnIsResult,
            columnParamValue
        });
        tblParam.setTableHeader(new JTableHeader(tblParam.getColumnModel(
                )));
        tblParam.putClientProperty(
                "terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        final DefaultTableCellRenderer defRenderer =
                new DefaultTableCellRenderer();
        defRenderer.setHorizontalAlignment(JTextField.CENTER);
        tblParam.getColumn(columnParam).setMaxWidth(105);
        tblParam.getColumn(columnParam).setResizable(false);
        tblParam.getColumn(columnParam).setCellRenderer(defRenderer);
        tblParam.getColumn(columnParamName).sizeWidthToFit();
        tblParam.getColumn(columnDesc).sizeWidthToFit();
        tblParam.getColumn(columnIsResult).setMaxWidth(105);
        tblParam.getColumn(columnParamValue).setPreferredWidth(150);
        tblParam.getColumn(columnParamValue).setResizable(false);
        edpStmt.setContentType("text/x-sql"); // NOI18N
        final EditorKit kit =
                JEditorPane.createEditorKitForContentType("text/x-sql");//NOI18N
        kit.install(edpStmt);
        edpStmt.setEditorKit(kit);
        edpStmt.setText(model.getQuery().getStatement());
        edpStmt.setEditable(false);
        edpStmt.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(final FocusEvent e)
            {
                tblParam.requestFocusInWindow();
            }

            @Override
            public void focusLost(final FocusEvent e)
            {
                // not needed
            }
        });
        btnExecute.setEnabled(false);
        final Set<QueryParameter> paramsSet =
                model.getQuery().getQueryParameters();
        final List<QueryParameter> params =
                new LinkedList<QueryParameter>(paramsSet);
        Collections.sort(params, new Comparator<QueryParameter>()
        {
            @Override
            public int compare(final QueryParameter p1, final QueryParameter p2)
            {
                return p1.getPosition().compareTo(p2.getPosition());
            }
        });
        final List row = new ArrayList(5);
        for(final QueryParameter param : params)
        {
            row.clear();
            row.add(param.getPosition());
            row.add(param.getKey());
            row.add(param.getDescription());
            row.add(param.getIsQueryResult());
            row.add(""); // NOI18N
            dtm.addRow(row.toArray());
        }
        final TableCellEditor defCellEdit = tblParam
                .getDefaultEditor(String.class);
        defCellEdit.addCellEditorListener(new CellEditorListener()
        {
            @Override
            public void editingCanceled(final ChangeEvent e)
            {
                checkExecutionAbility();
            }

            @Override
            public void editingStopped(final ChangeEvent e)
            {
                checkExecutionAbility();
            }
        });
        tblParam.getColumn(columnParamValue).setCellEditor(defCellEdit);
        tblParam.getSelectionModel().addListSelectionListener(
                new SelectionListener());
        resultModel = new ROResultSetTableModel();
        tblResult.setAutoCreateColumnsFromModel(true);
        tblResult.setModel(resultModel);
        resultModel.setSingleColumn(
                org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel4.class,
                    "QueryManipulationVisualPanel4.resultModel.columnName"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel4.class,
                    "QueryManipulationVisualPanel4.resultModel.content")); // NOI18N
        checkExecutionAbility();
        btnCancel.setEnabled(false);
        tblParam.requestFocusInWindow();
    }
    
    public void checkExecutionAbility()
    {
        for(int i = 0; i < tblParam.getRowCount(); i++)
        {
            final String content = (String)tblParam.getValueAt(i, 4);
            if(content == null || content.length() == 0)
            {
                btnExecute.setEnabled(false);
                return;
            }
        }
        btnExecute.setEnabled(true);
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.getName().returnvalue"); // NOI18N
    }
   
    @Override
    public void update(final Observable o, final Object arg)
    {
        if("readSettings".equals(arg)) // NOI18N
        {
            init();
        }
    }
    
    private String buildExecutableStatement()
    {
        String statement = edpStmt.getText();
        synchronized(tblParam)
        {
            for(int i = 0; i < tblParam.getRowCount(); i++)
            {
                final String param = (String)tblParam.getValueAt(i, 4);
                statement = statement.replaceFirst("\\?", " " + param);// NOI18N
            }
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("built statement: " + statement); // NOI18N
        }
        return statement;
    }
        
    private void setSingleProgressColumn()
    {
        resultModel.setSingleColumn(
                org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel4.class,
                    "QueryManipulationVisualPanel4.setSingleProgressColumn().resultModel.columnName"), // NOI18N
                ""); // NOI18N
        progressHandle = ProgressHandleFactory.createHandle(
                org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel4.class,
                    "QueryManipulationVisualPanel4.setSingleProgressColumn().progressHandle.message")); // NOI18N
    }
    
    @Override
    public boolean cancel()
    {
        if(!execTask.isFinished())
        {
            return execTask.cancel();
        }
        return false;
    }
    
    class ExecStatementRunnable implements Runnable
    {
        @Override
        public void run()
        {
            progressHandle.start();
            try
            {
                exec();
            } finally
            {
                progressHandle.finish();
            }
        }
        
        private void exec()
        {
            final Properties props = model.getProperties();
            final String url =
                    props.getProperty("connection.url"); // NOI18N
            final String user =
                    props.getProperty("connection.username"); // NOI18N
            final String pass =
                    props.getProperty("connection.password"); // NOI18N
            final String driver =
                    props.getProperty("connection.driver_class"); // NOI18N
            Connection con = null;
            try
            {
                Class.forName(driver);
                con = DriverManager.getConnection(url, user, pass);
            }catch(final SQLException ex)
            {
                if(Thread.interrupted())
                {
                    return;
                }
                LOG.error("could not create connection to db", ex); // NOI18N
                resultModel.setSingleColumn(
                        org.openide.util.NbBundle.getMessage(
                            QueryManipulationVisualPanel4.class,
                            "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().SQLException.resultModel.columnName"), // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryManipulationVisualPanel4.class,
                            "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().SQLException.resultModel.content") + ex); // NOI18N
                return;
            }catch(final ClassNotFoundException ex)
            {
                if(Thread.interrupted())
                {
                    return;
                }
                LOG.error("driver not found: " + driver, ex); // NOI18N
                resultModel.setSingleColumn(
                        org.openide.util.NbBundle.getMessage(
                            QueryManipulationVisualPanel4.class,
                            "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().ClassNotFoundException.resultModel.columnName"), // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryManipulationVisualPanel4.class,
                            "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().ClassNotFoundException.resultModel.content") + ex); // NOI18N
                return;
            }
            if(con == null)
            {
                throw new IllegalStateException(
                        "connection cannot be null"); // NOI18N
            }
            final String statement = buildExecutableStatement();
            if(statement.toLowerCase(Locale.UK).startsWith("select")) // NOI18N
            {
                ResultSet set = null;
                Statement s = null;
                try
                {
                    if(Thread.interrupted())
                    {
                        return;
                    }
                    s = con.createStatement();
                    s.setMaxRows(50000);
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("fetching resultset: " + s); // NOI18N
                    }
                    set = s.executeQuery(statement);
                    if(Thread.interrupted())
                    {
                        return;
                    }
                    resultModel.setNewResultSet(set);
                } catch (final SQLException ex)
                {
                    if(Thread.interrupted())
                    {
                        return;
                    }
                    LOG.error("error during query execution", ex); // NOI18N
                    resultModel.setSingleColumn(
                            org.openide.util.NbBundle.getMessage(
                                QueryManipulationVisualPanel4.class,
                                "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().SQLException.resultModel.columnName.execution"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                QueryManipulationVisualPanel4.class,
                                "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().SQLException.resultModel.content.execution") + ex); // NOI18N
                    return;
                }finally
                {
                    try
                    {
                        if(set != null)
                        {
                            set.close();
                        }
                        if(s != null)
                        {
                            s.close();
                        }
                        con.close();
                    } catch (SQLException ex)
                    {
                        LOG.error("could not close connection", ex); // NOI18N
                    }
                }
            }else
            {
                try
                {
                    if(Thread.interrupted())
                    {
                        return;
                    }
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("fetching return value"); // NOI18N
                    }
                    final int result = con.createStatement().executeUpdate(
                            statement);
                    if(Thread.interrupted())
                    {
                        return;
                    }
                    resultModel.setSingleColumn(
                            org.openide.util.NbBundle.getMessage(
                                QueryManipulationVisualPanel4.class, 
                                "QueryManipulationVisualPanel4.resultModel.columnName"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                QueryManipulationVisualPanel4.class, 
                                "QueryManipulationVisualPanel4.resultModel.content.stmtDeliveredResult") + result); // NOI18N
                }catch(final SQLException ex)
                {
                    if(Thread.interrupted())
                    {
                        return;
                    }
                    tblResult.getColumn(org.openide.util.NbBundle.getMessage(
                            QueryManipulationVisualPanel4.class, 
                            "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().tblResult.columnName")).setCellRenderer(null); // NOI18N
                    LOG.error("error during query execution", ex); // NOI18N
                    resultModel.setSingleColumn(
                            org.openide.util.NbBundle.getMessage(
                                QueryManipulationVisualPanel4.class, 
                                "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().SQLException.resultModel.columnName.execution2"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                QueryManipulationVisualPanel4.class, 
                                "QueryManipulationVisualPanel4.ExecStatementRunnable.exec().SQLException.resultModel.content.execution2") + ex); // NOI18N
                    return;
                }finally
                {
                    try
                    {
                        con.close();
                    }catch(final SQLException ex)
                    {
                        LOG.error("could not close connection", ex); // NOI18N
                    }
                }
            }
        }
    }
    
    class SelectionListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(final ListSelectionEvent e)
        {
            final int[] selected = tblParam.getSelectedRows();
            if(selected.length != 1)
            {
                return;
            }
            int index = 0;
            for(int i = 0; i <= selected[0]; i++)
            {
                index = edpStmt.getText().indexOf("?", index) + 1; // NOI18N
            }
            edpStmt.setSelectionStart(index - 1);
            edpStmt.setSelectionEnd(index);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.openide.awt.Mnemonics.setLocalizedText(lblStmt, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.lblStmt.text")); // NOI18N

        tblParam.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblParam);

        tblResult.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ergebnis"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblResult);

        org.openide.awt.Mnemonics.setLocalizedText(lblParam, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.lblParam.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblResult, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.lblResult.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnExecute, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.btnExecute.text")); // NOI18N
        btnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecuteActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.btnCancel.text")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(edpStmt);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .add(lblStmt)
                    .add(lblParam)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 512, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btnExecute, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                            .add(btnCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)))
                    .add(lblResult))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblStmt)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblParam)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(33, 33, 33)
                        .add(lblResult))
                    .add(layout.createSequentialGroup()
                        .add(btnExecute)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnCancel)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnCancelActionPerformed
    {//GEN-HEADEREND:event_btnCancelActionPerformed
        btnCancel.setEnabled(false);
        cancel();
        resultModel.setSingleColumn(org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel4.class, "QueryManipulationVisualPanel4.btnCancelActionPerformed(ActionEvent).resultModel.columnName"),// NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel4.class, 
                    "QueryManipulationVisualPanel4.btnCancelActionPerformed(ActionEvent).resultModel.content")); // NOI18N
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnExecuteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnExecuteActionPerformed
    {//GEN-HEADEREND:event_btnExecuteActionPerformed
        setSingleProgressColumn();
        execTask.schedule(0);
        btnCancel.setEnabled(true);
    }//GEN-LAST:event_btnExecuteActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton btnCancel = new javax.swing.JButton();
    private final transient javax.swing.JButton btnExecute = new javax.swing.JButton();
    private final transient javax.swing.JEditorPane edpStmt = new javax.swing.JEditorPane();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblParam = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblResult = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStmt = new javax.swing.JLabel();
    private final transient javax.swing.JTable tblParam = new javax.swing.JTable();
    private final transient javax.swing.JTable tblResult = new javax.swing.JTable();
    // End of variables declaration//GEN-END:variables
}
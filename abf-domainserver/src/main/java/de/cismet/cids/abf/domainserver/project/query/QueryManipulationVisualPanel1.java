/*
 * QueryManipulationVisualPanel1.java, encoding: UTF-8
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

import de.cismet.cids.jpa.entity.query.Query;
import de.cismet.cids.jpa.entity.query.QueryParameter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.EditorKit;
import org.apache.log4j.Logger;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class QueryManipulationVisualPanel1 extends JPanel implements
        Observer
{
    private static final transient Logger LOG = Logger.getLogger(
            QueryManipulationVisualPanel1.class);

    private static RequestProcessor reqProc = new RequestProcessor(
            "ParamTableUpdateRunnable"); // NOI18N

    private static final String TOKEN_INIT = 
            "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ"; // NOI18N

    private final transient QueryManipulationWizardPanel1 model;
    private final transient Set<ChangeListener> listeners;
    private final transient DocumentListener sqlL;
    private final transient DocumentListener nameL;
    private final transient RequestProcessor.Task reqTask;
    private transient Query query;
    private transient volatile List<String> oldTokens;
    
    public QueryManipulationVisualPanel1(
            final QueryManipulationWizardPanel1 model)
    {
        this.model = model;
        listeners = new HashSet<ChangeListener>(1);
        reqTask = reqProc.create(new ParamTableUpdateRunnable(), true);
        sqlL = new DocumentListenerImpl();
        nameL = new DocumentListenerImpl()
        {
            @Override
            public void changedUpdate(final DocumentEvent e)
            {
                fireChangeEvent();
            }
        };
        initComponents();
    }
    
    private void init()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init"); // NOI18N
        }
        oldTokens = new ArrayList<String>();
        oldTokens.add(TOKEN_INIT);
        final DefaultTableModel dtm = (DefaultTableModel)tblParam.getModel();
        final String columnParam = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class, "Dsc_param"); // NOI18N
        final String columnParamName = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class, "Dsc_paramName"); // NOI18N
        final String columnDesc = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class, "Dsc_description");//NOI18N
        final String columnResult = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class, "Dsc_isResult"); // NOI18N
        dtm.setColumnIdentifiers(new Object[]
        {
            columnParam, columnParamName, columnDesc, columnResult
        });
        tblParam.setTableHeader(new JTableHeader(tblParam.getColumnModel(
                )));
        while(dtm.getRowCount() > 0)
        {
            dtm.removeRow(0);
        }
        final DefaultTableCellRenderer defRenderer =
                new DefaultTableCellRenderer();
        defRenderer.setHorizontalAlignment(JTextField.CENTER);
        tblParam.getColumn(columnParam).sizeWidthToFit();
        tblParam.getColumn(columnParam).setResizable(false);
        tblParam.getColumn(columnParam).setCellRenderer(defRenderer);
        tblParam.getColumn(columnParamName).setPreferredWidth(220);
        tblParam.getColumn(columnDesc).setPreferredWidth(350);
        tblParam.getColumn(columnDesc).setResizable(false);
        tblParam.getColumn(columnResult).setMaxWidth(105);
        tblParam.getColumn(columnResult).setResizable(false);
        tblParam.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(final FocusEvent e)
            {
                // not needed
            }

            @Override
            public void focusLost(final FocusEvent e)
            {
                tblParam.clearSelection();
            }
        });
        tblParam.putClientProperty(
                "terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        edpStatement.setContentType("text/x-sql"); // NOI18N
        final EditorKit kit = JEditorPane
                .createEditorKitForContentType("text/x-sql"); // NOI18N
        kit.install(edpStatement);
        edpStatement.setEditorKit(kit);
        tblParam.getSelectionModel().addListSelectionListener(
                new SelectionListener());
        tblParam.getSelectionModel().setSelectionMode(ListSelectionModel.
                SINGLE_SELECTION);
        if(model.getQuery() == null)
        {
            initDefaults();
        }else
        {
            initQuery();
        }
        txtName.getDocument().addDocumentListener(WeakListeners.document(
                nameL, txtName.getDocument()));
        edpStatement.getDocument().addDocumentListener(WeakListeners.document(
                sqlL, edpStatement.getDocument()));
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init finished"); // NOI18N
        }
    }
    
    private void initDefaults()
    {
        txtName.setText(org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class,
                "Dsc_newQueryOneWord")); // NOI18N
        edpStatement.setText(org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class,
                "Dsc_insertQueryHere")); // NOI18N
    }
    
    private void initQuery()
    {
        query = model.getQuery();
        txtName.setText(query.getName());
        edpStatement.setText(query.getStatement());
        fill(oldTokens, edpStatement.getText().split("\\?")); // NOI18N
        oldTokens.add(";"); // NOI18N
        final Set<QueryParameter> paramsSet = query.getQueryParameters();
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
        final ArrayList row = new ArrayList(4);
        for(final QueryParameter param : params)
        {
            row.clear();
            row.add(param.getPosition());
            row.add(param.getKey());
            row.add(param.getDescription());
            row.add(param.getIsQueryResult());
            ((DefaultTableModel)tblParam.getModel()).addRow(row.toArray());
        }
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel1.class, "Dsc_nameAndStmt");//NOI18N
    }
    
    public String getQueryName()
    {
        return txtName.getText();
    }
    
    public Query getQuery()
    {
        final Set<QueryParameter> params = getParameters();
        if(query == null)
        {
            query = new Query();
        }
        final String name = txtName.getText();
        String statement = edpStatement.getText();
        statement = statement.replace(";", ""); // NOI18N
        query.setName(name);
        query.setStatement(statement);
        query.setQueryParameters(params);
        return query;
    }
    
    public Set<QueryParameter> getParameters()
    {
        final DefaultTableModel tModel = (DefaultTableModel)tblParam.getModel(
                );
        final Set<QueryParameter> ret = new HashSet<QueryParameter>(tModel.
                getRowCount());
        for(int i = 0; i < tModel.getRowCount(); i++)
        {
            final QueryParameter param = new QueryParameter();
            final Integer paramNo = (Integer)tModel.getValueAt(i, 0);
            final String paramName = (String)tModel.getValueAt(i, 1);
            final String paramDesc = (String)tModel.getValueAt(i, 2);
            final Boolean isResult = (Boolean)tModel.getValueAt(i, 3);
            param.setPosition(paramNo);
            param.setKey(paramName);
            param.setDescription(paramDesc);
            param.setIsQueryResult(isResult);
            // type id is not used leave it 'null'
            param.setTypeID(null);
            param.setQuery(query);
            ret.add(param);
        }
        return ret;
    }
    
    public void addChangeListener(final ChangeListener l)
    {
        synchronized(listeners)
        {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(final ChangeListener l)
    {
        synchronized(listeners)
        {
            listeners.remove(l);
        }
    }
    
    protected void fireChangeEvent()
    {
        final Iterator<ChangeListener> it;
        synchronized (listeners)
        {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while(it.hasNext())
        {
            it.next().stateChanged(ev);
        }
    }
    
    @Override
    public void update(final Observable o, final Object arg)
    {
        if("readSettings".equals(arg)) // NOI18N
        {
            init();
        }
    }
    
    private void fill(final List<String> v, final String[] toAdd)
    {
        v.clear();
        for(final String s : toAdd)
        {
            v.add(s);
        }
    }
    
    class DocumentListenerImpl implements DocumentListener
    {
        @Override
        public void changedUpdate(final DocumentEvent e)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("schedule document change event"); // NOI18N
            }
            reqTask.schedule(0);
        }

        @Override
        public void removeUpdate(final DocumentEvent e)
        {
            changedUpdate(e);
        }

        @Override
        public void insertUpdate(final DocumentEvent e)
        {
            changedUpdate(e);
        }
    }
    
    // should be ok
    class ParamTableUpdateRunnable implements Runnable
    {
        @Override
        public void run()
        {
            final String content = edpStatement.getText() + ";"; // NOI18N
            final String[] tokenArray = content.split("\\?"); // NOI18N
            final List<String> tokens = new ArrayList<String>();
            fill(tokens, tokenArray);
            if(tokens.size() == oldTokens.size())
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("param count equal, oldTokens: " // NOI18N
                            + oldTokens.toString());
                }
                oldTokens = tokens;
                // do nothing
            }
            else
            {
                int i;
                for(i = 0; i < tokens.size() && i < oldTokens.size(); i++)
                {
                    final int comp =
                            tokens.get(i).length() - oldTokens.get(i).length();
                    if(comp < 0 
                            && !(tokens.get(i).equals(";") // NOI18N
                            && tokens.size() < oldTokens.size()))
                    {
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("add param before"); // NOI18N
                            LOG.debug("newTokens: " // NOI18N
                                    + tokens.toString());
                            LOG.debug("oldTokens before [" // NOI18N
                                    + i
                                    + "] : " // NOI18N
                                    + oldTokens.toString());
                        }
                        oldTokens.set(i, tokens.get(i));
                        if(i < tokens.size() - 1)
                        {
                            oldTokens.add(i + 1, tokens.get(i + 1));
                        }
                        modifyTable(i, true);
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("oldTokens after: " // NOI18N
                                    + oldTokens.toString());
                        }
                    }else if(comp > 0 
                            && !(oldTokens.get(i).equals(";") // NOI18N
                            && tokens.size() > oldTokens.size()))
                    {
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("remove param before"); // NOI18N
                            LOG.debug("newTokens: " // NOI18N
                                    + tokens.toString());
                            LOG.debug("oldTokens before [" // NOI18N
                                    + i 
                                    + "] : " // NOI18N
                                    + oldTokens.toString());
                        }
                        oldTokens.set(i, tokens.get(i));
                        oldTokens.remove(i + 1);
                        modifyTable(i, false);
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("oldTokens after: " // NOI18N
                                    + oldTokens.toString());
                        }
                    }else
                    {
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("skip, check next"); // NOI18N
                        }
                    }
                }
                if(i < tokens.size())
                {
                    // many '?' were inserted
                    for(; i < tokens.size(); i++)
                    {
                        modifyTable(i - 1, true);
                    }
                }else if(i < oldTokens.size())
                {
                    // many '?' were removed, count backwards
                    for(int j = oldTokens.size() - 2; j > i - 2; j--)
                    {
                        modifyTable(j, false);
                    }
                }
                // this has not necessarily to be done, if 
                // abs(delta(paramCount)) = 1, but we'll do because of the 
                // possibility that one inserted more than one '?'
                oldTokens = tokens;
            }
        }
        
        private void modifyTable(final int position, final boolean add)
        {
            final DefaultTableModel tModel =
                    (DefaultTableModel)tblParam.getModel();
            final String param = org.openide.util.NbBundle.getMessage(
                    QueryManipulationVisualPanel1.class, "Dsc_param") // NOI18N
                    + " "; // NOI18N
            int i;
            if(add)
            {    
                tModel.insertRow(position, new Object[] 
                {
                    position + 1,
                    param + (position + 1),
                    "", // NOI18N
                    false
                });
                i = position + 1;
            }else
            {
                if(position >= tModel.getRowCount())
                {
                    return;
                }
                tModel.removeRow(position);
                i = position;
            }
            for(; i < tModel.getRowCount(); i++)
            {
                tModel.setValueAt(i + 1, i, 0);
                if(tModel.getValueAt(i, 1) != null)
                {
                    final String paramName = (String)tModel.getValueAt(i, 1);
                    if(paramName.startsWith(param))
                    {
                        tModel.setValueAt(param + (i + 1), i, 1);
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
                index = edpStatement.getText().indexOf("?", index) + 1;//NOI18N
            }
            edpStatement.setSelectionStart(index - 1);
            edpStatement.setSelectionEnd(index);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel1.class, "Lbl_name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblStatement, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel1.class, "Lbl_stmt")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblParam, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel1.class, "Lbl_param")); // NOI18N

        jScrollPane3.setAutoscrolls(true);
        jScrollPane3.setFocusCycleRoot(true);

        tblParam.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "", "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblParam.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        jScrollPane3.setViewportView(tblParam);

        jScrollPane1.setViewportView(edpStatement);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(lblName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblStatement)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblParam)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(22, 22, 22)
                .add(lblStatement)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 181, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblParam)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JEditorPane edpStatement = new javax.swing.JEditorPane();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblName = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblParam = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStatement = new javax.swing.JLabel();
    private final transient javax.swing.JTable tblParam = new javax.swing.JTable();
    private final transient javax.swing.JTextField txtName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
    
}
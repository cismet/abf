/*
 * NewCidsClassVisualPanel3.java, encoding: UTF-8
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
 * Created on ???
 *
 */

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.openide.util.RequestProcessor;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class NewCidsClassVisualPanel3 extends JPanel
{
    private static final transient Logger LOG = Logger.getLogger(
            NewCidsClassVisualPanel3.class);
    
    private final static int KEY = 0;
    private final static int VALUE = 1;

    private final transient NewCidsClassWizardPanel3 model;
    private transient CidsClass cidsClass;
    private transient ClassAttrTableModel tableModel;
    private transient FutureTask<Type> typeCache;
    
    public NewCidsClassVisualPanel3(final NewCidsClassWizardPanel3 model)
    {
        this.model = model;
        initComponents();
        final JXTable classAttrTable = (JXTable)tblClassAttr;
        classAttrTable.setAutoStartEditOnKeyStroke(true);
        classAttrTable.setTerminateEditOnFocusLost(true);
        classAttrTable.getDefaultEditor(String.class).addCellEditorListener(new
                CellEditorListener()
        {
            @Override
            public void editingCanceled(final ChangeEvent e)
            {
                model.fireChangeEvent();
            }
            
            @Override
            public void editingStopped(final ChangeEvent e)
            {
                model.fireChangeEvent();
            }
        });
    }
    
    void init()
    {
        cidsClass = model.getCidsClass();
        tableModel = new ClassAttrTableModel(cidsClass.getClassAttributes());
        tblClassAttr.setModel(tableModel);
        typeCache = new FutureTask(new Callable<Type>()
        {
            @Override
            public Type call() throws Exception
            {
                final Backend b = model.getProject().getCidsDataObjectBackend();
                final List<Type> types = b.getAllEntities(Type.class);
                return types.get(0);
            }
        });
        RequestProcessor.getDefault().post(typeCache);
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewCidsClassVisualPanel3.class, "Dsc_assignClassAttr");// NOI18N
    }
    
    CidsClass getCidsClass()
    {
        final Set<ClassAttribute> set = cidsClass.getClassAttributes();
        set.clear();
        for(final ClassAttribute ca : tableModel.cas)
        {
            set.add(ca);
        }
        return cidsClass;
    }
    
    private final class ClassAttrTableModel extends AbstractTableModel
    {
        private final List<ClassAttribute> cas;
        
        ClassAttrTableModel(final Collection<ClassAttribute> cas)
        {
            this.cas = new ArrayList<ClassAttribute>();
            this.cas.addAll(cas);
        }
        
        @Override
        public Object getValueAt(final int row, final int column)
        {
            switch(column)
            {
                case KEY:
                    return cas.get(row).getAttrKey();
                case VALUE:
                    return cas.get(row).getAttrValue();
                default:
                    throw new IllegalArgumentException(
                            "unknown column: " + column); // NOI18N
            }
        }
        
        @Override
        public int getRowCount()
        {
            return cas.size();
        }
        
        @Override
        public int getColumnCount()
        {
            return 2;
        }
        
        @Override
        public String getColumnName(final int column)
        {
            switch(column)
            {
                case KEY:
                    return org.openide.util.NbBundle.getMessage(
                            NewCidsClassVisualPanel3.class, "Dsc_key");// NOI18N
                case VALUE:
                    return org.openide.util.NbBundle.getMessage(
                            NewCidsClassVisualPanel3.class, "Dsc_val");// NOI18N
                default:
                    throw new IllegalArgumentException(
                            "unknown column: " + column); // NOI18N
            }
        }
        
        @Override
        public Class<?> getColumnClass(final int columnIndex)
        {
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            return true;
        }
        
        @Override
        public void setValueAt(final Object aValue, final int row,
                final int column)
        {
            if(aValue instanceof String)
            {
                switch(column)
                {
                    case KEY:
                        cas.get(row).setAttrKey(((String)aValue).trim());
                        break;
                    case VALUE:
                        cas.get(row).setAttrValue(((String)aValue).trim());
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "unknown column: " + column); // NOI18N
                }
            }
        }
        
        public void addClassAttribute(final ClassAttribute ca)
        {
            cas.add(ca);
            fireTableDataChanged();
        }
        
        public void removeClassAttribute(final int rowIndex)
        {
            cas.remove(rowIndex);
            fireTableDataChanged();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scpClassAttr.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NewCidsClassVisualPanel3.class, "Dsc_classAttrs"))); // NOI18N
        scpClassAttr.setPreferredSize(new java.awt.Dimension(130, 296));
        scpClassAttr.setViewportView(tblClassAttr);

        tboClassAttr.setFloatable(false);

        cmdNewClassAttr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/add_row.png"))); // NOI18N
        cmdNewClassAttr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdNewClassAttrActionPerformed(evt);
            }
        });
        tboClassAttr.add(cmdNewClassAttr);

        cmdRemoveClassAttr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/remove_row.png"))); // NOI18N
        cmdRemoveClassAttr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveClassAttrActionPerformed(evt);
            }
        });
        tboClassAttr.add(cmdRemoveClassAttr);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tboClassAttr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, scpClassAttr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tboClassAttr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scpClassAttr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 281, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void cmdNewClassAttrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewClassAttrActionPerformed
        final ClassAttribute ca = new ClassAttribute();
        ca.setCidsClass(cidsClass);
        ca.setAttrKey(""); // NOI18N
        try
        {
            ca.setType(typeCache.get());
        }catch(final InterruptedException ex)
        {
            LOG.error("type retrieval was interrupted", ex); // NOI18N
            ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassVisualPanel3.class, 
                        "Err_couldNotLoadType"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassVisualPanel3.class,
                        "Err_loadingInterrupt"), // NOI18N
                    ex);
            return;
        }catch(final ExecutionException ex)
        {
            LOG.error("error during type loading", ex); // NOI18N
            ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassVisualPanel3.class, 
                        "Err_couldNotLoadType"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassVisualPanel3.class, 
                        "Err_loading"), // NOI18N
                    ex);
            return;
        }
        ca.setAttrValue(""); // NOI18N
        tableModel.addClassAttribute(ca);
        model.fireChangeEvent();
        final JXTable jxt = (JXTable)tblClassAttr;
        jxt.setSortOrder(0, SortOrder.ASCENDING);
        for(int i = 0; i < tableModel.getRowCount(); ++i)
        {
            if("".equals(tableModel.getValueAt(i, 0))) // NOI18N
            {
                tblClassAttr.requestFocus();
                tblClassAttr.editCellAt(jxt.convertRowIndexToView(i), 0);
                break;
            }
        }
    }//GEN-LAST:event_cmdNewClassAttrActionPerformed
    
    private void cmdRemoveClassAttrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveClassAttrActionPerformed
        final int selectedRow = tblClassAttr.getSelectedRow();
        final JXTable table = (JXTable)tblClassAttr;
        if(selectedRow >= 0)
        {
            final int modelRow = table.convertRowIndexToModel(selectedRow);
            tableModel.removeClassAttribute(modelRow);
            final int rc = tblClassAttr.getRowCount();
            if(rc > 0)
            {
                if((rc - 1) >= selectedRow)
                {
                    table.getSelectionModel().setSelectionInterval(
                            selectedRow, selectedRow);
                }else
                {
                    table.getSelectionModel().setSelectionInterval(
                            rc - 1, rc - 1);
                }
            }
        }
        model.fireChangeEvent();
    }//GEN-LAST:event_cmdRemoveClassAttrActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton cmdNewClassAttr = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdRemoveClassAttr = new javax.swing.JButton();
    private final transient javax.swing.JScrollPane scpClassAttr = new javax.swing.JScrollPane();
    private final transient javax.swing.JTable tblClassAttr = new JXTable();
    private final transient javax.swing.JToolBar tboClassAttr = new javax.swing.JToolBar();
    // End of variables declaration//GEN-END:variables
}
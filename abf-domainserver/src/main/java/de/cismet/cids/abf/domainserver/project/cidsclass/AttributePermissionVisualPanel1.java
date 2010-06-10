/*
 * AttributePermissionVisualPanel1.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.permission.AttributePermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class AttributePermissionVisualPanel1 extends JPanel
{
    private static final transient Logger LOG = Logger.getLogger(
            AttributePermissionVisualPanel1.class);
    
    private final transient AttributePermissionWizardPanel1 model;
    private transient AttrPermissionTableModel tableModel;
    private transient Attribute cidsAttribute;
    
    public AttributePermissionVisualPanel1(
            final AttributePermissionWizardPanel1 model)
    {
        this.model = model;
        initComponents();
        cboPermissionSelector.setRenderer(new Renderers.UnifiedCellRenderer());
        tblPermissions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstUserGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    void init()
    {
        cidsAttribute = model.getCidsAttribute();
        final Backend backend = model.getBackend();
        lstUserGroups.setCellRenderer(new Renderers.UserGroupListRenderer(model.
                getProject()));
        final List<UserGroup> groups = new ArrayList<UserGroup>(backend.
                getAllEntities(UserGroup.class));
        final List<Permission> allPerms = new ArrayList<Permission>(backend.
                getAllEntities(Permission.class));
        cboPermissionSelector.setModel(
                new DefaultComboBoxModel(allPerms.toArray()));
        lstUserGroups.setListData(groups.toArray());
        tableModel = new AttrPermissionTableModel();
        tblPermissions.setModel(tableModel);
        ((JXTable)tblPermissions).setSortOrder(0, SortOrder.ASCENDING);
        ((JXTable)tblPermissions).setSortable(true);
    }
    
    Attribute getCidsAttribute()
    {
        return cidsAttribute;
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(AttributePermissionVisualPanel1.class, "Dsc_attrRight");
    }
    
    class DropAwareJXTable extends JXTable implements DropTargetListener
    {
        private final transient int acceptableActions;
        private final transient DropTarget dt;
        
        public DropAwareJXTable()
        {
            acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
            dt = new DropTarget(this, acceptableActions, this);
        }
        
        @Override
        public void drop(final DropTargetDropEvent dtde)
        {
            try
            {
                final Object o = dtde.getTransferable().getTransferData(
                        CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if(o instanceof UserGroup)
                {
                    final UserGroup ug = (UserGroup)o;
                    if(tableModel != null)
                    {
                        final AttributePermission p = new AttributePermission();
                        p.setUserGroup(ug);
                        p.setPermission((Permission)cboPermissionSelector.
                                getSelectedItem());
                        tableModel.addPermission(p);
                    }
                }
            }catch(final Exception e)
            {
                LOG.error("could not perform drop", e); // NOI18N
                // TODO: shall I inform the user...
            }finally
            {
                dtde.dropComplete(true);
            }
        }
        
        @Override
        public DropTarget getDropTarget()
        {
            return dt;
        }
        
        // <editor-fold defaultstate="collapsed" desc=" Not needed Listener Impls ">
        @Override
        public void dragExit(final DropTargetEvent dte)
        {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde)
        {
            // not needed
        }
        // </editor-fold>
    }
    
    class DragJList extends JList implements 
            DragGestureListener,
            DragSourceListener
    {
        private final transient DragSource dragSource;
        
        public DragJList()
        {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                    this, // component where drag originates
                    DnDConstants.ACTION_COPY_OR_MOVE, // actions
                    this); // drag gesture recognizer
        }
        
        @Override
        public void dragGestureRecognized(final DragGestureEvent e)
        {
            final Object o = this.getSelectedValue();
            final Transferable trans = new CidsUserGroupTransferable(o);
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }
        
        // <editor-fold defaultstate="collapsed" desc=" Not needed Listener Impls ">
        @Override
        public void dragDropEnd(final DragSourceDropEvent e)
        {
            // not needed
        }

        @Override
        public void dragEnter(final DragSourceDragEvent e)
        {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent e)
        {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent e)
        {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent e)
        {
            // not needed
        }
        // </editor-fold>
    }

    // TODO: refactor the
    class AttrPermissionTableModel extends AbstractTableModel
    {
        private final transient Logger log = Logger.getLogger(
                AttrPermissionTableModel.class);
        
        public static final int GROUP = 0;
        public static final int PERMISSION = 1;
        
        public AttributePermission getAttributePermission(final int rowIndex)
        {
            if(cidsAttribute.getAttributePermissions().size() <= rowIndex)
            {
                return null;
            }else
            {
                return (AttributePermission)cidsAttribute
                        .getAttributePermissions().toArray()[rowIndex];
            }
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex)
        {
            if(cidsAttribute.getAttributePermissions().size() > rowIndex)
            {
                final AttributePermission p = (AttributePermission)cidsAttribute
                        .getAttributePermissions().toArray()[rowIndex];
                if(GROUP == columnIndex)
                {
                    final UserGroup ug = p.getUserGroup();
                    if(ug == null)
                    {
                        return ""; // NOI18N
                    }
                    final StringBuffer sb = new StringBuffer(ug.getName());
                    if(ProjectUtils.isRemoteGroup(ug, model.getProject()))
                    {
                        sb.append('@') // NOI18N
                                .append(ug.getDomain().getName());
                    }
                    return sb.toString();
                }else if(PERMISSION == columnIndex)
                {
                    return p.getPermission().getDescription();
                }
            }
            return null;
        }
        
        @Override
        public int getRowCount()
        {
            return cidsAttribute.getAttributePermissions().size();
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
                case GROUP:
                    return org.openide.util.NbBundle.getMessage(
                            AttributePermissionVisualPanel1.class, 
                            "Dsc_group"); // NOI18N
                case PERMISSION:
                    return org.openide.util.NbBundle.getMessage(
                            AttributePermissionVisualPanel1.class, 
                            "Dsc_right"); // NOI18N
                default:
                    return ""; // NOI18N
            }
        }
        
        @Override
        public Class<?> getColumnClass(final int columnIndex)
        {
            switch(columnIndex)
            {
                case GROUP:
                    return String.class;
                case PERMISSION:
                    return String.class;
                default:
                    return null;
            }
        }
        
        public void addPermission(final AttributePermission perm)
        {
            if(LOG.isDebugEnabled())
            {
                log.debug("addPermission:" + perm); // NOI18N
            }
            perm.setAttribute(cidsAttribute);
            cidsAttribute.getAttributePermissions().add(perm);
            fireTableDataChanged();
        }
        
        @Override
        public void setValueAt(final Object aValue, final int rowIndex,
                final int columnIndex)
        {
            // read-only
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            return false;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1.setFloatable(false);

        cmdRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/remove_row.png"))); // NOI18N
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdRemove);

        scpPermissions.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AttributePermissionVisualPanel1.class, "Brd_rights"))); // NOI18N
        scpPermissions.setPreferredSize(new java.awt.Dimension(130, 296));
        scpPermissions.setViewportView(tblPermissions);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AttributePermissionVisualPanel1.class, "Lbl_right")); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AttributePermissionVisualPanel1.class, "Brd_groups"))); // NOI18N
        jScrollPane1.setViewportView(lstUserGroups);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 162, Short.MAX_VALUE)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cboPermissionSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(scpPermissions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 266, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 32, Short.MAX_VALUE)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 254, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scpPermissions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 278, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cboPermissionSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 278, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveActionPerformed
        final int selectedRow = tblPermissions.getSelectedRow();
        if(selectedRow >= 0)
        {
            final JXTable permTable = (JXTable)tblPermissions;
            final int modelRow = permTable.convertRowIndexToModel(selectedRow);
            final AttributePermission a =
                    tableModel.getAttributePermission(modelRow);
            cidsAttribute.getAttributePermissions().remove(a);
            tableModel.fireTableDataChanged();
            final int rc = tblPermissions.getRowCount();
            if(rc > 0)
            {
                if(rc - 1 >= selectedRow)
                {
                    permTable.getSelectionModel().setSelectionInterval(
                            selectedRow, selectedRow);
                }else
                {
                    permTable.getSelectionModel().setSelectionInterval(
                            rc - 1, rc - 1);
                }
            }
        }
    }//GEN-LAST:event_cmdRemoveActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboPermissionSelector = new javax.swing.JComboBox();
    private final transient javax.swing.JButton cmdRemove = new javax.swing.JButton();
    private final transient javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JToolBar jToolBar1 = new javax.swing.JToolBar();
    private final transient javax.swing.JList lstUserGroups = new DragJList();
    private final transient javax.swing.JScrollPane scpPermissions = new javax.swing.JScrollPane();
    private final transient javax.swing.JTable tblPermissions = new DropAwareJXTable();
    // End of variables declaration//GEN-END:variables
}
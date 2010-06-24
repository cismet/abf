/*
 * QueryManipulationVisualPanel3.java, encoding: UTF-8
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

import de.cismet.cids.abf.domainserver.project.utils.Renderers.UnifiedCellRenderer;
import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.QueryPermission;
import de.cismet.cids.jpa.entity.query.Query;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Cursor;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.EditorKit;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class QueryManipulationVisualPanel3 extends JPanel implements 
        Observer
{
    private static final transient Logger LOG = Logger.getLogger(
            QueryManipulationVisualPanel3.class);
    
    private final transient QueryManipulationWizardPanel3 model;
    private transient Query query;
    private transient Domain defaultDomain;
    
    public QueryManipulationVisualPanel3(
            final QueryManipulationWizardPanel3 model)
    {
        this.model = model;
        initComponents();
    }
    
    private void init()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init"); // NOI18N
        }
        query = model.getQuery();
        edpStmt.setContentType("text/x-sql"); // NOI18N
        final EditorKit kit =
                JEditorPane.createEditorKitForContentType("text/x-sql");//NOI18N
        kit.install(edpStmt);
        edpStmt.setEditorKit(kit);
        edpStmt.setText(query.getStatement());
        edpStmt.setEditable(false);
        edpStmt.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(final FocusEvent e)
            {
                lstGroup.requestFocusInWindow();
            }

            @Override
            public void focusLost(final FocusEvent e)
            {
                // not needed
            }
        });
        final UnifiedCellRenderer unifiedRenderer = new UnifiedCellRenderer();
        final Comparators.UserGroups ugComp = new Comparators.UserGroups();
        final JComboBox cboDomain = new JComboBox(new DefaultComboBoxModel());
        final JComboBox cboRightRenderer = new JComboBox(
                new DefaultComboBoxModel());
        lstGroup.setModel(new DefaultListModel());
        final List<Permission> perms = model.getBackend().getAllEntities(
                Permission.class);
        final List<Domain> domains = model.getBackend().getAllEntities(
                Domain.class);
        Collections.sort(perms, new Comparators.Permissions());
        Collections.sort(domains, new Comparators.Domains());
        for(final Permission perm : perms)
        {
            cboRight.addItem(perm);
            cboRightRenderer.addItem(perm);
        }
        for(final Domain domain : domains)
        {
            cboDomain.addItem(domain);
        }
        defaultDomain = domains.get(0);
        cboRight.setRenderer(unifiedRenderer);
        cboDomain.setRenderer(unifiedRenderer);
        cboRightRenderer.setRenderer(unifiedRenderer);
        final DefaultTableModel tModel =
                (DefaultTableModel)tblRights.getModel();
        final String group = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.init().group"); // NOI18N
        final String right = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.init().right"); // NOI18N
        final String domain = org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.init().domain"); // NOI18N
        tModel.setColumnIdentifiers(new Object[] {group, right, domain});
        while(tModel.getRowCount() > 0)
        {
            tModel.removeRow(0);
        }
        tblRights.setTableHeader(new JTableHeader(tblRights.getColumnModel(
                )));
        tblRights.getColumn(right).setCellEditor(new DefaultCellEditor(
                cboRightRenderer));
        tblRights.getColumn(group).setCellRenderer(unifiedRenderer);
        tblRights.getColumn(right).setCellRenderer(unifiedRenderer);
        tblRights.getColumn(domain).setCellRenderer(unifiedRenderer);
        TableColumnExt tc = ((JXTable)tblRights).getColumnExt(group);
        tc.setComparator(ugComp);
        tc.setSortable(true);
        tc = ((JXTable)tblRights).getColumnExt(right);
        tc.setComparator(null);
        tc.setSortable(false);
        tc = ((JXTable)tblRights).getColumnExt(domain);
        tc.setComparator(null);
        tc.setSortable(false);
        ((JXTable)tblRights).setSortOrder(group, SortOrder.ASCENDING);
        final List<UserGroup> groups = model.getBackend().getAllEntities(
                UserGroup.class);
        ((JXList)lstGroup).setComparator(ugComp);
        ((JXList)lstGroup).setSortOrder(SortOrder.ASCENDING);
        //((JXList)lstGroup).setFilterEnabled(true);
        ((JXList)lstGroup).setCellRenderer(unifiedRenderer);
        final Set<QueryPermission> qperms = query.getQueryPermissions();
        if(qperms != null)
        {
            final Iterator<QueryPermission> it = qperms.iterator();
            while(it.hasNext())
            {
                final QueryPermission qperm = it.next();
                groups.remove(qperm.getUserGroup());
                tModel.addRow(new Object[] 
                {
                    qperm.getUserGroup(),
                    qperm.getPermission()
                });
            }
        }
        for(final UserGroup ug : groups)
        {
            ((DefaultListModel)((JXList)lstGroup).getWrappedModel()).
                    addElement(ug);
        }
        btnAddAll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                addAllPushed();
            }
        });
        btnAdd.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                addPushed();
            }
        });
        btnRemove.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                removePushed();
            }
        });
        btnRemoveAll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                removeAllPushed();
            }
        });
        lstGroup.requestFocusInWindow();
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel3.class,
                "QueryManipulationVisualPanel3.getName().returnvalue"); // NOI18N
    }
    
    public Query getQuery()
    {
        final DefaultTableModel tModel = (DefaultTableModel)tblRights.
                getModel();
        final Set<QueryPermission> perms = query.getQueryPermissions();
        final Set<QueryPermission> newPerms = new HashSet<QueryPermission>();
        boolean contained = false;
        for(int i = 0; i < tModel.getRowCount(); i++)
        {
            final QueryPermission qperm = new QueryPermission();
            final UserGroup ug = (UserGroup)tModel.getValueAt(i, 0);
            final Permission perm = (Permission)tModel.getValueAt(i, 1);
            //Domain domain = (Domain)tModel.getValueAt(i, 2);
            for(final QueryPermission qp : perms)
            {
                if(qp.getUserGroup().equals(ug))
                {
                    qp.setPermission(perm);
                    newPerms.add(qp);
                    contained = true;
                }
            }
            if(!contained)
            {
                qperm.setQuery(query);
                qperm.setUserGroup(ug);
                qperm.setPermission(perm);
                //qperm.setDomain(domain);
                newPerms.add(qperm);
            }
            contained = false;
        }
        query.setQueryPermissions(newPerms);
        return query;
    }
    
    private void addAllPushed()
    {
        final DefaultListModel lModel = (DefaultListModel)((JXList)lstGroup).
                getWrappedModel();
        final DefaultTableModel tModel =
                (DefaultTableModel)tblRights.getModel();
        while(lModel.size() > 0)
        {
            tModel.addRow(new Object[]
            {
                lModel.remove(0),
                cboRight.getSelectedItem(),
                defaultDomain
            });
        }
    }
    
    private void addPushed()
    {
        final DefaultListModel lModel = (DefaultListModel)((JXList)lstGroup).
                getWrappedModel();
        final DefaultTableModel tModel =
                (DefaultTableModel)tblRights.getModel();
        final JXList jxList = (JXList)lstGroup;
        final int[] selected = lstGroup.getSelectedIndices();
        Arrays.sort(selected);
        // start from behind to ensure the indices are still valid!
        for(int i = selected.length - 1; i >= 0; i--)
        {
            final int index = jxList.convertIndexToModel(selected[i]);
            tModel.addRow(new Object[] 
            {
                lModel.remove(index),
                cboRight.getSelectedItem(), 
                defaultDomain
            });
        }
    }
    
    private void removePushed()
    {
        final DefaultListModel lModel = (DefaultListModel)((JXList)lstGroup).
                getWrappedModel();
        final DefaultTableModel tModel =
                (DefaultTableModel)tblRights.getModel();
        final JXTable jxTable = (JXTable)tblRights;
        final int[] selected = tblRights.getSelectedRows();
        Arrays.sort(selected);
        // start from behind to ensure the indices are still valid!
        for(int i = selected.length - 1; i >= 0; i--)
        {
            final int index = jxTable.convertRowIndexToModel(selected[i]);
            lModel.addElement(tModel.getValueAt(index, 0));
            tModel.removeRow(index);
        }
    }
    
    private void removeAllPushed()
    {
        final DefaultListModel lModel = (DefaultListModel)((JXList)lstGroup).
                getWrappedModel();
        final DefaultTableModel tModel =
                (DefaultTableModel)tblRights.getModel();
        while(tModel.getRowCount() > 0)
        {
            final Object content = tModel.getValueAt(0, 0);
            tModel.removeRow(0);
            lModel.addElement(content);
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
    
    class DnDJXTable extends JXTable implements 
            DropTargetListener,
            DragGestureListener,
            DragSourceListener
    {
        // field is needed for drag and drop (why?)
        private final transient DropTarget dropTarget;
        private final transient DragSource dragSource;
        
        public DnDJXTable()
        {
            dropTarget = new DropTarget(this, DnDConstants.ACTION_MOVE, this);
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                    this, // component where drag originates
                    DnDConstants.ACTION_MOVE, // actions
                    this); // drag gesture recognizer
        }

        @Override
        public void drop(final DropTargetDropEvent dtde)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug(((DropTarget)dtde.getSource()).getComponent());
                LOG.debug(((DropTarget)dtde.getSource()).getComponent()
                        .equals(this));
            }
            try
            {
                final Object o = dtde.getTransferable().getTransferData(
                        CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if(o instanceof Object[])
                {
                    final Object[] dropped = (Object[])o;
                    if(dropped[0].equals(this))
                    {
                        return;
                    }
                    final Integer[] indices = (Integer[])dropped[1];
                    final UserGroup[] ugs = (UserGroup[])dropped[2];
                    final DefaultTableModel tModel =
                            (DefaultTableModel)getModel();
                    final DefaultListModel lModel = (DefaultListModel)((
                            JXList)lstGroup).getWrappedModel();
                    for(final int index : indices)
                    {
                        lModel.remove(index);
                    }
                    for(final UserGroup ug : ugs)
                    {
                        tModel.addRow(new Object[]
                        {
                           ug, cboRight.getSelectedItem(), defaultDomain
                        });
                    }
                }
            }catch(final Exception ex)
            {
                LOG.error("could not perform drop action", ex); // NOI18N
            }finally
            {
                dtde.dropComplete(true);
            }
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge)
        {
            final List ugs = new ArrayList(getSelectedRowCount());
            final List indices = new ArrayList(getSelectedRowCount());
            for(final int row : getSelectedRows())
            {
                ugs.add(getValueAt(row, 0));
                indices.add(convertRowIndexToModel(row));
            }
            final Object object = new Object[]
            {
                this,
                indices.toArray(new Integer[indices.size()]),
                ugs.toArray(new UserGroup[ugs.size()])
            };
            final Transferable t = new CidsUserGroupTransferable(object);
            dragSource.startDrag(dge, Cursor.getPredefinedCursor(
                    Cursor.MOVE_CURSOR), t, this);
        }
        
        // <editor-fold defaultstate="collapsed" desc=" Not needed Listener impls ">
        @Override
        public void dragEnter(final DragSourceDragEvent dsde)
        {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent dsde)
        {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent dsde)
        {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent dse)
        {
            // not needed
        }

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde)
        {
            // not needed
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dragExit(final DropTargetEvent dte)
        {
            // not needed
        }// </editor-fold>
    }
    
    class DnDJXList extends JXList implements 
            DropTargetListener,
            DragGestureListener,
            DragSourceListener
    {
        // field is needed for drag and drop (why?)
        private final transient DropTarget dropTarget;
        private final transient DragSource dragSource;
        
        public DnDJXList()
        {
            dropTarget = new DropTarget(this, DnDConstants.ACTION_MOVE, this);
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                    this, // component where drag originates
                    DnDConstants.ACTION_MOVE, // actions
                    this); // drag gesture recognizer
        }

        @Override
        public void drop(final DropTargetDropEvent dtde)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug(((DropTarget)dtde.getSource()).getComponent());
                LOG.debug(((DropTarget)dtde.getSource()).getComponent()
                        .equals(this));
            }
            try
            {
                final Object o = dtde.getTransferable().getTransferData(
                        CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if(o instanceof Object[])
                {
                    final Object[] dropped = (Object[])o;
                    if(dropped[0].equals(this))
                    {
                        return;
                    }
                    final Integer[] indices = (Integer[])dropped[1];
                    final UserGroup[] ugs = (UserGroup[])dropped[2];
                    final DefaultListModel lModel =
                            (DefaultListModel)getWrappedModel();
                    final DefaultTableModel tModel =
                            (DefaultTableModel)tblRights.getModel();
                    for(final int index : indices)
                    {
                        tModel.removeRow(index);
                    }
                    for(final UserGroup ug : ugs)
                    {
                        lModel.addElement(ug);
                    }
                }
            }catch(final Exception ex)
            {
                LOG.error("could not perform drop action", ex); // NOI18N
            } finally
            {
                dtde.dropComplete(true);
            }
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge)
        {
            final List indices = new ArrayList(getSelectedIndices().length);
            for(final int index : getSelectedIndices())
            {
                indices.add(convertIndexToModel(index));
            }
            final Object[] o = getSelectedValues();
            final UserGroup[] ugs = new UserGroup[o.length];
            for(int i = 0; i < o.length; i++)
            {
                ugs[i] = (UserGroup)o[i];
            }
            final Object object = new Object[]
            {
                this,
                indices.toArray(new Integer[indices.size()]),
                ugs
            };
            final Transferable t = new CidsUserGroupTransferable(object);
            dragSource.startDrag(dge, Cursor.getPredefinedCursor(
                    Cursor.MOVE_CURSOR), t, this);
        }

        // <editor-fold defaultstate="collapsed" desc=" Not needed Listener impls ">
        @Override
        public void dragEnter(final DragSourceDragEvent dsde)
        {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent dsde)
        {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent dsde)
        {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent dse)
        {
            // not needed
        }

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde)
        {
            // not needed
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde)
        {
            // not needed
        }

        @Override
        public void dragExit(final DropTargetEvent dte)
        {
            // not needed
        }// </editor-fold>
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMaximumSize(new java.awt.Dimension(661, 460));

        org.openide.awt.Mnemonics.setLocalizedText(lblRight, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.lblRight.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblGroups, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.lblGroups.text")); // NOI18N

        jScrollPane1.setViewportView(lstGroup);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddAll, "<<");

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, "<");

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, ">");

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveAll, ">>");

        org.openide.awt.Mnemonics.setLocalizedText(lblStmt, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.lblStmt.text")); // NOI18N

        tblRights.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblRights);

        org.openide.awt.Mnemonics.setLocalizedText(lblRights, org.openide.util.NbBundle.getMessage(QueryManipulationVisualPanel3.class, "QueryManipulationVisualPanel3.lblRights.text")); // NOI18N

        jScrollPane3.setPreferredSize(new java.awt.Dimension(104, 20));

        edpStmt.setMinimumSize(new java.awt.Dimension(90, 16));
        edpStmt.setPreferredSize(new java.awt.Dimension(90, 16));
        jScrollPane3.setViewportView(edpStmt);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                            .add(27, 27, 27)
                                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 293, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 315, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(lblRights)))
                                    .add(lblStmt))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 93, Short.MAX_VALUE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(lblRight)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(cboRight, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(layout.createSequentialGroup()
                                        .add(lblGroups)
                                        .add(155, 155, 155))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 208, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(layout.createSequentialGroup()
                        .add(349, 349, 349)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(btnAdd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnAddAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnRemoveAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblStmt)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(45, 45, 45)
                        .add(lblRights))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblRight)
                            .add(cboRight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(lblGroups)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(44, 44, 44)
                        .add(btnAddAll)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnAdd)
                        .add(20, 20, 20)
                        .add(btnRemove)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemoveAll))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1)
                            .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 248, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton btnAdd = new javax.swing.JButton();
    private final transient javax.swing.JButton btnAddAll = new javax.swing.JButton();
    private final transient javax.swing.JButton btnRemove = new javax.swing.JButton();
    private final transient javax.swing.JButton btnRemoveAll = new javax.swing.JButton();
    private final transient javax.swing.JComboBox cboRight = new javax.swing.JComboBox();
    private final transient javax.swing.JEditorPane edpStmt = new javax.swing.JEditorPane();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane4 = new javax.swing.JScrollPane();
    private final transient javax.swing.JTable jTable1 = new javax.swing.JTable();
    private final transient javax.swing.JLabel lblGroups = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblRight = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblRights = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStmt = new javax.swing.JLabel();
    private final transient javax.swing.JList lstGroup = new DnDJXList();
    private final transient javax.swing.JTable tblRights = new DnDJXTable();
    // End of variables declaration//GEN-END:variables
}
/*
 * NodeRightPropertyVisualPanel1.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
import de.cismet.cids.abf.domainserver.project.utils.Renderers.UnifiedCellRenderer;
import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.permission.NodePermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

public final class NodeRightPropertyVisualPanel1 extends JPanel implements 
        ChangeListener
{
    private static final transient Logger LOG = Logger.getLogger(
            NodeRightPropertyVisualPanel1.class);
    
    private final transient NodeRightPropertyWizardPanel1 model;
    private transient Permission[] validPermissions;
    private transient int differentRightCount;
    private transient List<UserGroup> allUserGroups;
    private final transient ActionListener addL;
    private final transient ActionListener addAllL;
    private final transient ActionListener remL;
    private final transient ActionListener remAllL;
    private final transient ActionListener cboL;
    
    public NodeRightPropertyVisualPanel1(final NodeRightPropertyWizardPanel1 m)
    {
        this.model = m;
        initComponents();
        addAllL = new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                addAllPushed();
            }
        };
        addL = new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                addPushed();
            }
        };
        remL = new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                removePushed();
            }
        };
        remAllL = new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                removeAllPushed();
            }
        };
        cboL = new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                if(tblRights.isEditing())
                {
                    return;
                }
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("new right selected, updating"); // NOI18N
                }
                final DefaultTableModel tModel = (DefaultTableModel)tblRights.
                        getModel();
                final JXTable tblRights = getRightsTable();
                final int[] selRows = tblRights.getSelectedRows();
                if(selRows.length != 1)
                {
                    return;
                }
                final int selIndex = tblRights.convertRowIndexToModel(selRows[
                        0]);
                final UserGroup ug = (UserGroup)tModel.getValueAt(selIndex, 0);
                final Permission selPerm = (Permission)tModel.getValueAt(
                        selIndex, 1);
                final List rows = tModel.getDataVector();
                for(int i = 0; i < rows.size(); ++i)
                {
                    final List row = (List)rows.get(i);
                    if(ug.equals(row.get(0))
                            && selPerm.equals(row.get(1))
                            && i != selIndex)
                    {
                        final int toRem = i;
                        EventQueue.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tModel.removeRow(toRem);
                            }
                        });
                        return;
                    }
                }
            }
        };
    }
    
    // needed for immediate listener init
    private JXTable getRightsTable()
    {
        return (JXTable)tblRights;
    }

    private void init()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init"); // NOI18N
        }
        final UnifiedCellRenderer unifiedRenderer = new UnifiedCellRenderer(
                model.getProject(), model.getCatNode());
        ((DefaultComboBoxModel)cboPolicy.getModel()).removeAllElements();
        cboPolicy.setRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(final JList list,
                    final Object value, final int index, final boolean
                    isSelected, final boolean cellHasFocus)
            {
                final JLabel label = (JLabel)super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                final Policy policy = (Policy)value;
                String s;
                if(policy.equals(Policy.NO_POLICY))
                {
                    final CatNode node = model.getCatNode();
                    final Policy p = node.getPolicy();
                    node.setPolicy(null);
                    try
                    {
                        s = "<" + PermissionResolver.getInstance(model.// NOI18N
                                getProject()).getPermString(node, null).
                                getInheritanceString() + ">"; // NOI18N
                    }finally
                    {
                        node.setPolicy(p);
                    }
                }else
                {
                    s = policy.getName();
                }
                label.setText(s);
                label.setIcon(null);
                return label;
            }
        });
        cboPolicy.addItem(Policy.NO_POLICY);
        final List<Policy> policies = model.getBackend().getAllEntities(Policy.
                class);
        Collections.sort(policies, new Comparators.Policies());
        for(final Policy p : policies)
        {
            cboPolicy.addItem(p);
        }
        if(model.getCatNode().getPolicy() == null)
        {
            cboPolicy.setSelectedIndex(0);
        }else
        {
            cboPolicy.setSelectedItem(model.getCatNode().getPolicy());
        }
        cboPolicy.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    final Policy policy = (Policy)e.getItem();
                    model.getCatNode().setPolicy(policy.getId() == null ? null :
                            policy);
                }
                NodeRightPropertyVisualPanel1.this.repaint();
            }
        });
        final Comparators.UserGroups ugComp = new Comparators.UserGroups();
        final JComboBox rightComboboxTableCellRenderer = new JComboBox(new
                DefaultComboBoxModel());
        rightComboboxTableCellRenderer.addActionListener(cboL);
        final List<Permission> perms = model.getBackend().getAllEntities(
                Permission.class);
        final List<Domain> domains = model.getBackend().getAllEntities(Domain.
                class);
        Collections.sort(perms, new Comparators.Permissions());
        Collections.sort(domains, new Comparators.Domains());
        validPermissions = new Permission[perms.size()];
        for(int i = 0; i < perms.size(); ++i)
        {
            rightComboboxTableCellRenderer.addItem(perms.get(i));
            cboRights.addItem(perms.get(i));
            validPermissions[i] = perms.get(i);
        }
        differentRightCount = perms.size();
        rightComboboxTableCellRenderer.setRenderer(unifiedRenderer);
        cboRights.setRenderer(unifiedRenderer);
        cboRights.setSelectedItem(perms.get(0));
        final DefaultTableModel tModel = (DefaultTableModel)tblRights.
                getModel();
        final String group = org.openide.util.NbBundle.getMessage(
                NodeRightPropertyVisualPanel1.class, "Dsc_group"); // NOI18N
        final String right = org.openide.util.NbBundle.getMessage(
                NodeRightPropertyVisualPanel1.class, "Dsc_right"); // NOI18N
        final String domain = org.openide.util.NbBundle.getMessage(
                NodeRightPropertyVisualPanel1.class, "Dsc_domain"); // NOI18N
        tModel.setColumnIdentifiers(new Object[] {group, right, domain});
        while(tModel.getRowCount() > 0)
        {
            tModel.removeRow(0);
        }
        tblRights.setTableHeader(new JTableHeader(tblRights.getColumnModel(
                )));
        tblRights.getColumn(right).setCellEditor(new DefaultCellEditor(
                rightComboboxTableCellRenderer));
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
        //((JXList)groupList).setFilterEnabled(true);
        ((JXList)lstGroup).setCellRenderer(unifiedRenderer);
        final List<NodePermission> nperms = model.getPermissions();
        if(nperms != null)
        {
            for(final NodePermission np : nperms)
            {
                final UserGroup ug = np.getUserGroup();
                groups.remove(ug);
                tModel.addRow(new Object[] {ug, np.getPermission(), ug.
                        getDomain()});
            }
        }
        for(final UserGroup ug : groups)
        {
            ((DefaultListModel)((JXList)lstGroup).getWrappedModel()).
                addElement(ug);
        }
        allUserGroups = groups;
        btnAddAll.addActionListener(addAllL);
        btnAdd.addActionListener(addL);
        btnRemove.addActionListener(remL);
        btnRemoveAll.addActionListener(remAllL);
        lstGroup.requestFocusInWindow();
        updateGroupList();
    }

    private void clear()
    {
        // when reaching this segment for the first time the listmodel is not
        // initialized yet
        if(lstGroup.getModel() instanceof DefaultListModel)
        {
            ((DefaultListModel)lstGroup.getModel()).clear();
        }
        while(tblRights.getModel().getRowCount() > 0)
        {
            ((DefaultTableModel)tblRights.getModel()).removeRow(0);
        }
    }
    
    private void addAllPushed()
    {
        final DefaultListModel lModel = (DefaultListModel)((JXList)lstGroup).
                getWrappedModel();
        for(int i = 0; i < lModel.size(); ++i)
        {
            addIndex(i);
        }
        updateGroupList();
    }

    private void addPushed()
    {
        final JXList jxList = (JXList)lstGroup;
        final int[] selected = lstGroup.getSelectedIndices();
        Arrays.sort(selected);
        // start from behind to ensure the indices are still valid!
        for(int i = selected.length - 1; i >= 0; i--)
        {
            final int index = jxList.convertIndexToModel(selected[i]);
            addIndex(index);
            updateGroupList();
        }
    }

    private void removePushed()
    {
        final DefaultTableModel tModel = (DefaultTableModel)tblRights.
                getModel();
        final JXTable jxTable = (JXTable)tblRights;
        final int[] selected = tblRights.getSelectedRows();
        Arrays.sort(selected);
        // start from behind to ensure the indices are still valid!
        for(int i = selected.length - 1; i >= 0; i--)
        {
            final int index = jxTable.convertRowIndexToModel(selected[i]);
            tModel.removeRow(index);
        }
        updateGroupList();
    }

    private void removeAllPushed()
    {
        final DefaultTableModel tModel = (DefaultTableModel)tblRights.
                getModel();
        while(tModel.getRowCount() > 0)
        {
            tModel.removeRow(0);
        }
        updateGroupList();
    }

    private void addIndex(final int index)
    {
        final UserGroup ug = (UserGroup)((DefaultListModel)((JXList)lstGroup).
                getWrappedModel()).getElementAt(index);
        Permission permittedPerm = (Permission)cboRights.getSelectedItem();
        if(!addPermitted(ug, permittedPerm))
        {
            permittedPerm = null;
            for(int i = 0; i < validPermissions.length; ++i)
            {
                if(addPermitted(ug, validPermissions[i]))
                {
                    permittedPerm = validPermissions[i];
                    break;
                }
            }
            if(permittedPerm == null)
            {
                throw new IllegalStateException(
                        "could not find a valid permission"); // NOI18N
            }
        }
        ((DefaultTableModel)tblRights.getModel()).addRow(new Object[]
        {
            ug, permittedPerm, ug.getDomain()
        });
    }

    private boolean addPermitted(final UserGroup ug, final Permission perm)
    {
        final List data = ((DefaultTableModel)tblRights.getModel()).
                getDataVector();
        for(final Object row : data)
        {
            if(ug.equals(((List)row).get(0)) && perm.equals(((List)row).get(1)))
            {
                return false;
            }
        }
        return true;
    }

    private void updateGroupList()
    {
        final JXList list = (JXList)lstGroup;
        final DefaultListModel lModel = (DefaultListModel)list.getWrappedModel(
                );
        final Object[] selUgs = list.getSelectedValues();
        lModel.clear();
        final Set<UserGroup> ugs = getSelectedUGsWithMaxUsage();
        for(final UserGroup ug : allUserGroups)
        {
            if(!ugs.contains(ug))
            {
                lModel.addElement(ug);
            }
        }
        list.setSortOrder(SortOrder.ASCENDING);
        list.setComparator(new Comparators.UserGroups());
        setSelection(selUgs);
    }

    private void setSelection(final Object[] ugs)
    {
        final JXList list = (JXList)lstGroup;
        final ArrayList<Integer> indices = new ArrayList<Integer>(ugs.length);
        for(final Object o : ugs)
        {
            for(int i = 0; i < list.getElementCount(); ++i)
            {
                final Object el = list.getElementAt(i);
                if(o.equals(el))
                {
                    indices.add(i);
                    break;
                }
            }
        }
        final int[] selection = new int[indices.size()];
        for(int i = 0; i < indices.size(); ++i)
        {
            selection[i] = indices.get(i);
        }
        list.setSelectedIndices(selection);
    }

    private Set<UserGroup> getSelectedUGsWithMaxUsage()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("searching for usergroups with max usage"); // NOI18N
        }
        final List data = ((DefaultTableModel)tblRights.getModel()).
                getDataVector();
        final Set<UserGroup> ugs = new HashSet(data.size());
        for(int i = 0; i < data.size(); ++i)
        {
            int usageCount = 1;
            final UserGroup ug = (UserGroup)((List)data.get(i)).get(0);
            for(int j = i + 1; j < data.size(); ++j)
            {
                final UserGroup copy = (UserGroup)((List)data.get(j)).get(0);
                if(copy.equals(ug) && ++usageCount == differentRightCount)
                {
                    ugs.add(ug);
                    break;
                }
            }
        }
        return ugs;
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NodeRightPropertyVisualPanel1.class, 
                "Dsc_changeRights"); // NOI18N
    }
    
    List<NodePermission> getPermissions()
    {
        final List<NodePermission> perms = new LinkedList<NodePermission>();
        final DefaultTableModel tModel = (DefaultTableModel)tblRights.
                getModel();
        for(int i = 0; i < tModel.getRowCount(); i++)
        {
            final NodePermission nperm = new NodePermission();
            final UserGroup ug = (UserGroup)tModel.getValueAt(i, 0);
            final Permission perm = (Permission)tModel.getValueAt(i, 1);
            //Domain domain = (Domain)tModel.getValueAt(i, 2);
            nperm.setNode(model.getCatNode());
            nperm.setUserGroup(ug);
            nperm.setPermission(perm);
            //nperm.setDomain(domain);
            perms.add(nperm);
        }
        return perms;
    }
    
    @Override
    public void stateChanged(final ChangeEvent e)
    {
        if(e.getSource() instanceof NodeRightPropertyWizardPanel1)
        {    
            clear();
            init();
        }
    }
    
    class DnDJXTable extends JXTable implements
            DropTargetListener,
            DragGestureListener,
            DragSourceListener
    {
        private final transient DragSource dragSource;
        private final transient DropTarget dropTarget;
        
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
                    for(final int index : indices)
                    {
                        addIndex(index);
                    }
                    updateGroupList();
                }
            }catch(final Exception ex)
            {
                LOG.error("could not perform drop action", ex); // NOI18N
                // TODO: shall I inform the user...
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
        private final transient DragSource dragSource;
        private final transient DropTarget dropTarget;
        
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
                    final DefaultTableModel tModel = (DefaultTableModel)
                            tblRights.getModel();
                    for(final int index : indices)
                    {
                        tModel.removeRow(index);
                    }
                    updateGroupList();
                }
            }catch(final Exception ex)
            {
                LOG.error("could not perform drop action", ex); // NOI18N
                // TODO: shall I inform the user...
            }
            finally
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

        tblRights.setModel(new javax.swing.table.DefaultTableModel(
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
        scpRights.setViewportView(tblRights);

        org.openide.awt.Mnemonics.setLocalizedText(lblRights, org.openide.util.NbBundle.getMessage(NodeRightPropertyVisualPanel1.class, "Lbl_rights")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAddAll, "<<");

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, "<");

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, ">");

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveAll, ">>");

        lstGroup.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(lstGroup);

        org.openide.awt.Mnemonics.setLocalizedText(lblGroup, org.openide.util.NbBundle.getMessage(NodeRightPropertyVisualPanel1.class, "Lbl_groups")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblDefRights, org.openide.util.NbBundle.getMessage(NodeRightPropertyVisualPanel1.class, "Lbl_defaultRight")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblPolicy, org.openide.util.NbBundle.getMessage(NodeRightPropertyVisualPanel1.class, "Lbl_nodePolicy")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scpRights, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 405, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRights))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btnRemoveAll)
                    .add(btnRemove)
                    .add(btnAdd)
                    .add(btnAddAll))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cboRights, 0, 205, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .add(lblGroup)
                    .add(lblDefRights)
                    .add(lblPolicy)
                    .add(cboPolicy, 0, 205, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblRights)
                            .add(lblPolicy))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(scpRights, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(cboPolicy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(5, 5, 5)
                                .add(lblDefRights)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cboRights, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblGroup)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(btnAddAll)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnAdd)
                        .add(35, 35, 35)
                        .add(btnRemove)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemoveAll)
                        .add(152, 152, 152))))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton btnAdd = new javax.swing.JButton();
    private final transient javax.swing.JButton btnAddAll = new javax.swing.JButton();
    private final transient javax.swing.JButton btnRemove = new javax.swing.JButton();
    private final transient javax.swing.JButton btnRemoveAll = new javax.swing.JButton();
    private final transient javax.swing.JComboBox cboPolicy = new javax.swing.JComboBox();
    private final transient javax.swing.JComboBox cboRights = new javax.swing.JComboBox();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblDefRights = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblGroup = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPolicy = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblRights = new javax.swing.JLabel();
    private final transient javax.swing.JList lstGroup = new DnDJXList();
    private final transient javax.swing.JScrollPane scpRights = new javax.swing.JScrollPane();
    private final transient javax.swing.JTable tblRights = new DnDJXTable();
    // End of variables declaration//GEN-END:variables
    
}

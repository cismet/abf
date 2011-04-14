/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXTable;

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

import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.permission.AttributePermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class AttributePermissionVisualPanel1 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            AttributePermissionVisualPanel1.class);

    //~ Instance fields --------------------------------------------------------

    private final transient AttributePermissionWizardPanel1 model;
    private transient AttrPermissionTableModel tableModel;
    private transient Attribute cidsAttribute;

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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AttributePermissionVisualPanel1 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public AttributePermissionVisualPanel1(
            final AttributePermissionWizardPanel1 model) {
        this.model = model;
        initComponents();
        cboPermissionSelector.setRenderer(new Renderers.UnifiedCellRenderer());
        tblPermissions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstUserGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        cidsAttribute = model.getCidsAttribute();
        final Backend backend = model.getBackend();
        lstUserGroups.setCellRenderer(new Renderers.UserGroupListRenderer(model.getProject()));
        final List<UserGroup> groups = new ArrayList<UserGroup>(backend.getAllEntities(UserGroup.class));
        final List<Permission> allPerms = new ArrayList<Permission>(backend.getAllEntities(Permission.class));
        cboPermissionSelector.setModel(
            new DefaultComboBoxModel(allPerms.toArray()));
        lstUserGroups.setListData(groups.toArray());
        tableModel = new AttrPermissionTableModel();
        tblPermissions.setModel(tableModel);
        ((JXTable)tblPermissions).setSortOrder(0, SortOrder.ASCENDING);
        ((JXTable)tblPermissions).setSortable(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Attribute getCidsAttribute() {
        return cidsAttribute;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                AttributePermissionVisualPanel1.class,
                "AttributePermissionVisualPanel1.getName().returnvalue");
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jToolBar1.setFloatable(false);

        cmdRemove.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cids/abf/domainserver/images/remove_row.png"))); // NOI18N
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRemove);

        scpPermissions.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    AttributePermissionVisualPanel1.class,
                    "AttributePermissionVisualPanel1.scpPermission.border.title"))); // NOI18N
        scpPermissions.setPreferredSize(new java.awt.Dimension(130, 296));
        scpPermissions.setViewportView(tblPermissions);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(
                AttributePermissionVisualPanel1.class,
                "AttributePermissionVisualPanel1.jlabel1.text")); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    AttributePermissionVisualPanel1.class,
                    "AttributePermissionVisualPanel1.jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setViewportView(lstUserGroups);

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(
                            jToolBar1,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            140,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED,
                            162,
                            Short.MAX_VALUE).add(jLabel1).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                            cboPermissionSelector,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            210,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        layout.createSequentialGroup().add(
                            scpPermissions,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            266,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED,
                            36,
                            Short.MAX_VALUE).add(
                            jScrollPane1,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            254,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(
                            jToolBar1,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            scpPermissions,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            278,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                                cboPermissionSelector,
                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel1)).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            jScrollPane1,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            278,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveActionPerformed
        final int selectedRow = tblPermissions.getSelectedRow();
        if (selectedRow >= 0) {
            final JXTable permTable = (JXTable)tblPermissions;
            final int modelRow = permTable.convertRowIndexToModel(selectedRow);
            final AttributePermission a = tableModel.getAttributePermission(modelRow);
            cidsAttribute.getAttributePermissions().remove(a);
            tableModel.fireTableDataChanged();
            final int rc = tblPermissions.getRowCount();
            if (rc > 0) {
                if ((rc - 1) >= selectedRow) {
                    permTable.getSelectionModel().setSelectionInterval(
                        selectedRow,
                        selectedRow);
                } else {
                    permTable.getSelectionModel().setSelectionInterval(
                        rc
                                - 1,
                        rc
                                - 1);
                }
            }
        }
    }                                                                             //GEN-LAST:event_cmdRemoveActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class DropAwareJXTable extends JXTable implements DropTargetListener {

        //~ Methods ------------------------------------------------------------

        // <editor-fold defaultstate="collapsed" desc=" Not needed Listener Impls ">
        @Override
        public void dragExit(final DropTargetEvent dte) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
            // not needed
        }
        // </editor-fold>

        //~ Instance fields ----------------------------------------------------

        private final transient int acceptableActions;
        private final transient DropTarget dt;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DropAwareJXTable object.
         */
        public DropAwareJXTable() {
            acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
            dt = new DropTarget(this, acceptableActions, this);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void drop(final DropTargetDropEvent dtde) {
            try {
                final Object o = dtde.getTransferable().getTransferData(
                        CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if (o instanceof UserGroup) {
                    final UserGroup ug = (UserGroup)o;
                    if (tableModel != null) {
                        final AttributePermission p = new AttributePermission();
                        p.setUserGroup(ug);
                        p.setPermission((Permission)cboPermissionSelector.getSelectedItem());
                        tableModel.addPermission(p);
                    }
                }
            } catch (final Exception e) {
                LOG.error("could not perform drop", e); // NOI18N
                // TODO: shall I inform the user...
            } finally {
                dtde.dropComplete(true);
            }
        }

        @Override
        public DropTarget getDropTarget() {
            return dt;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class DragJList extends JList implements DragGestureListener, DragSourceListener {

        //~ Methods ------------------------------------------------------------

        // <editor-fold defaultstate="collapsed" desc=" Not needed Listener Impls ">
        @Override
        public void dragDropEnd(final DragSourceDropEvent e) {
            // not needed
        }

        @Override
        public void dragEnter(final DragSourceDragEvent e) {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent e) {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent e) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent e) {
            // not needed
        }
        // </editor-fold>

        //~ Instance fields ----------------------------------------------------

        private final transient DragSource dragSource;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DragJList object.
         */
        public DragJList() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this);                            // drag gesture recognizer
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void dragGestureRecognized(final DragGestureEvent e) {
            final Object o = this.getSelectedValue();
            final Transferable trans = new CidsUserGroupTransferable(o);
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }
    }
    /**
     * TODO: refactor the
     *
     * @version  $Revision$, $Date$
     */
    class AttrPermissionTableModel extends AbstractTableModel {

        //~ Static fields/initializers -----------------------------------------

        public static final int GROUP = 0;
        public static final int PERMISSION = 1;

        //~ Instance fields ----------------------------------------------------

        private final transient Logger log = Logger.getLogger(
                AttrPermissionTableModel.class);

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   rowIndex  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public AttributePermission getAttributePermission(final int rowIndex) {
            if (cidsAttribute.getAttributePermissions().size() <= rowIndex) {
                return null;
            } else {
                return (AttributePermission)cidsAttribute.getAttributePermissions().toArray()[rowIndex];
            }
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if (cidsAttribute.getAttributePermissions().size() > rowIndex) {
                final AttributePermission p = (AttributePermission)
                    cidsAttribute.getAttributePermissions().toArray()[rowIndex];
                if (GROUP == columnIndex) {
                    final UserGroup ug = p.getUserGroup();
                    if (ug == null) {
                        return "";     // NOI18N
                    }
                    final StringBuffer sb = new StringBuffer(ug.getName());
                    if (ProjectUtils.isRemoteGroup(ug, model.getProject())) {
                        sb.append('@') // NOI18N
                        .append(ug.getDomain().getName());
                    }
                    return sb.toString();
                } else if (PERMISSION == columnIndex) {
                    return p.getPermission().getDescription();
                }
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return cidsAttribute.getAttributePermissions().size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(final int column) {
            switch (column) {
                case GROUP: {
                    return org.openide.util.NbBundle.getMessage(
                            AttributePermissionVisualPanel1.class,
                            "AttributePermissionVisualPanel1.getColumnName(int).groupColumn");      // NOI18N
                }
                case PERMISSION: {
                    return org.openide.util.NbBundle.getMessage(
                            AttributePermissionVisualPanel1.class,
                            "AttributePermissionVisualPanel1.getColumnName(int).permissionColumn"); // NOI18N
                }
                default: {
                    return "";                                                                      // NOI18N
                }
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
                case GROUP: {
                    return String.class;
                }
                case PERMISSION: {
                    return String.class;
                }
                default: {
                    return null;
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  perm  DOCUMENT ME!
         */
        public void addPermission(final AttributePermission perm) {
            if (LOG.isDebugEnabled()) {
                log.debug("addPermission:" + perm); // NOI18N
            }
            perm.setAttribute(cidsAttribute);
            cidsAttribute.getAttributePermissions().add(perm);
            fireTableDataChanged();
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex,
                final int columnIndex) {
            // read-only
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return false;
        }
    }
}

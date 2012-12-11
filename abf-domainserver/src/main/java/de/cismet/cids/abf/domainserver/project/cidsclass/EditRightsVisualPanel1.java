/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;

import java.awt.Component;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;
import de.cismet.cids.abf.utilities.Comparators;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.permission.ClassPermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class EditRightsVisualPanel1 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            EditRightsVisualPanel1.class);

    private static final Policy VARIOUS_POLICIES;
    private static final Permission VARIOUS_PERMS;

    // TODO: improve implementation (low prio)
    // TODO: select newly added usergroups only
    // TODO: maybe add cbobox for preselecting ug right when adding
    // TODO: maybe do not reflect changes into classes, use copies instead
    // TODO: more logging (mid prio)
    // TODO: document implementation (high prio)
    // TODO: javadoc (mid prio)
    static {
        VARIOUS_POLICIES = new Policy();
        VARIOUS_POLICIES.setName(org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.VARIOUS_POLICIES.name")); // NOI18N
        VARIOUS_PERMS = new Permission();
        VARIOUS_PERMS.setKey(org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.VARIOUS_PERMS.name"));    // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    private final transient EditRightsWizardPanel1 model;
    private final transient PolicySelectionListener cboPolicyItemListener;
    private final transient ClassTableSelectionListener tblClassSelListener;
    private final transient ButtonSelectionListener rdbSelectionListener;
    private final transient RightSelectionListener cboRightItemListener;
    private final transient RightsTableSelectionListener tblRightsSelListener;

    private final transient List tblRightsColumnIdentifiers;
    private final transient List<Integer> unmarkedRightRows;
    private transient List<CidsClass> selectedClasses;
    private transient List<Integer> preserveSelection;
    private transient List<UserGroup> allUserGroups;
    private transient int differentRightCount;
    private transient Permission defaultPermission;
    private transient Permission[] validPermissions;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboPolicy = new javax.swing.JComboBox();
    private final transient javax.swing.JComboBox cboRight = new javax.swing.JComboBox();
    private final transient javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    private final transient javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
    private final transient javax.swing.JList lstGroups = new DragDropJList();
    private final transient javax.swing.JPanel pnlClasses = new javax.swing.JPanel();
    private final transient javax.swing.JPanel pnlRights = new javax.swing.JPanel();
    private final transient javax.swing.JRadioButton rdbAttrPolicy = new javax.swing.JRadioButton();
    private final transient javax.swing.JRadioButton rdbClassPolicy = new javax.swing.JRadioButton();
    private final transient javax.swing.JTable tblClasses = new JXTable();
    private final transient javax.swing.JTable tblRights = new DragDropJXTable();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form EditRightsVisualPanel1.
     *
     * @param  model  DOCUMENT ME!
     */
    public EditRightsVisualPanel1(final EditRightsWizardPanel1 model) {
        this.model = model;
        cboPolicyItemListener = new PolicySelectionListener();
        tblClassSelListener = new ClassTableSelectionListener();
        rdbSelectionListener = new ButtonSelectionListener();
        cboRightItemListener = new RightSelectionListener();
        tblRightsSelListener = new RightsTableSelectionListener();
        tblRightsColumnIdentifiers = new ArrayList(2);
        tblRightsColumnIdentifiers.add(org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.EditRightsVisualPanel1(EditRightsWizardPanel1).tblRightsColumnIdentifiers.element1")); // NOI18N
        tblRightsColumnIdentifiers.add(org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.EditRightsVisualPanel1(EditRightsWizardPanel1).tblRightsColumnIdentifiers.element2")); // NOI18N
        unmarkedRightRows = new ArrayList();
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performing init()"); // NOI18N
        }
        initClasstable();
        initGrouptable();
        initGrouplist();
        // selecting all classes, will perform an update
        tblClasses.getSelectionModel().setSelectionInterval(0, model.getCidsClasses().length - 1);
    }

    /**
     * DOCUMENT ME!
     */
    private void initClasstable() {
        final CidsClass[] classes = model.getCidsClasses();
        final Object[][] data = new Object[classes.length][3];
        final String[] columns = new String[] {
                org.openide.util.NbBundle.getMessage(
                    EditRightsVisualPanel1.class,
                    "EditRightsVisualPanel1.initClasstable().column1.name"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    EditRightsVisualPanel1.class,
                    "EditRightsVisualPanel1.initClasstable().column2.name"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    EditRightsVisualPanel1.class,
                    "EditRightsVisualPanel1.initClasstable().column3.name")  // NOI18N
            };
        for (int i = 0; i < classes.length; ++i) {
            data[i][0] = classes[i];
            data[i][1] = (classes[i].getPolicy() == null) ? Policy.NO_POLICY : classes[i].getPolicy();
            data[i][2] = (classes[i].getAttributePolicy() == null) ? Policy.NO_POLICY : classes[i].getAttributePolicy();
        }
        final DefaultTableModel tblModel = new DefaultTableModel(data, columns) {

                @Override
                public boolean isCellEditable(final int row, final int column) {
                    return false;
                }
            };
        tblClasses.setModel(tblModel);
        final Backend someBackend = model.getBackend();
        final LinkedList<Policy> policies = new LinkedList(someBackend.getAllEntities(Policy.class));
        Collections.sort(policies, new Comparators.Policies());
        policies.addFirst(Policy.NO_POLICY);
        final DefaultComboBoxModel cboPolicyModel = new DefaultComboBoxModel(
                policies.toArray());
        cboPolicy.setModel(cboPolicyModel);
        rdbAttrPolicy.setSelected(false);
        rdbClassPolicy.setSelected(true);
        final JXTable classesTable = (JXTable)tblClasses;
        classesTable.getColumnExt(0).setComparator(new Comparators.CidsClasses());
        classesTable.setSortOrder(0, SortOrder.ASCENDING);
        classesTable.setSortable(true);
        final ListSelectionModel sModel = classesTable.getSelectionModel();
        sModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sModel.addListSelectionListener(tblClassSelListener);
        rdbAttrPolicy.addItemListener(rdbSelectionListener);
        rdbClassPolicy.addItemListener(rdbSelectionListener);
        cboPolicy.addItemListener(cboPolicyItemListener);
        tblClasses.setColumnSelectionAllowed(false);
        tblClasses.setRowSelectionAllowed(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void initGrouptable() {
        final Backend someBackend = model.getBackend();
        final List<Permission> perms = new ArrayList(someBackend.getAllEntities(
                    Permission.class));
        if (perms.isEmpty()) {
            throw new IllegalStateException(
                "no permissions in database");   // NOI18N
        }
        Collections.sort(perms, new Comparators.Permissions());
        differentRightCount = perms.size();
        validPermissions = new Permission[perms.size()];
        for (int i = 0; i < perms.size(); ++i) {
            validPermissions[i] = perms.get(i);
        }
        defaultPermission = validPermissions[0];
        if (defaultPermission == null) {
            throw new IllegalStateException(
                "default permission not found"); // NOI18N
        }
        final DefaultComboBoxModel cModel = new DefaultComboBoxModel(
                perms.toArray());
        cboRight.setModel(cModel);
        final DefaultTableModel tModel = new DefaultTableModel() {

                @Override
                public boolean isCellEditable(final int row, final int column) {
                    return false;
                }
            };
        tblRights.setModel(tModel);
        cboRight.addItemListener(cboRightItemListener);
        final ListSelectionModel sModel = tblRights.getSelectionModel();
        sModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sModel.addListSelectionListener(tblRightsSelListener);
        tblRights.setColumnSelectionAllowed(false);
        tblRights.setRowSelectionAllowed(true);
    }

    /**
     * DOCUMENT ME!
     */
    private void initGrouplist() {
        final Backend backend = model.getBackend();
        allUserGroups = backend.getAllEntities(UserGroup.class);
        Collections.sort(allUserGroups, new Comparators.UserGroups());

        final DefaultListModel lModel = new DefaultListModel();
        final JXList list = (JXList)lstGroups;
        list.setModel(lModel);
        list.setSortable(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedPolicy  DOCUMENT ME!
     */
    private void updatePolicies(final Policy selectedPolicy) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating selected rows: " + selectedPolicy); // NOI18N
        }
        final JXTable classesTable = (JXTable)tblClasses;
        final DefaultTableModel tModel = (DefaultTableModel)classesTable.getModel();
        final int[] rows = classesTable.getSelectedRows();
        for (int i = 0; i < rows.length; ++i) {
            final int selRow = classesTable.convertRowIndexToModel(rows[i]);
            if (rdbClassPolicy.isSelected()) {
                tModel.setValueAt(selectedPolicy, selRow, 1);
                selectedClasses.get(i).setPolicy(selectedPolicy.equals(Policy.NO_POLICY) ? null : selectedPolicy);
            }
            if (rdbAttrPolicy.isSelected()) {
                tModel.setValueAt(selectedPolicy, selRow, 2);
                selectedClasses.get(i)
                        .setAttributePolicy(selectedPolicy.equals(
                                Policy.NO_POLICY) ? null : selectedPolicy);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedPerm  DOCUMENT ME!
     */
    private void updateRights(final Permission selectedPerm) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating permission: " + selectedPerm);           // NOI18N
        }
        final JXTable rightsTable = (JXTable)tblRights;
        final DefaultTableModel tModel = (DefaultTableModel)rightsTable.getModel();
        final int[] rows = rightsTable.getSelectedRows();
        preserveSelection = new ArrayList(rows.length);
        for (final int row : rows) {
            final int selRow = rightsTable.convertRowIndexToModel(row);
            preserveSelection.add(selRow);
            final UserGroup ug = (UserGroup)tModel.getValueAt(selRow, 0);
            final Permission pBefore = (Permission)tModel.getValueAt(selRow, 1);
            for (final CidsClass clazz : selectedClasses) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("examining cidsclass: " + clazz);          // NOI18N
                }
                boolean permPresent = false;
                ClassPermission duplicate = null;
                for (final ClassPermission perm : clazz.getClassPermissions()) {
                    if (perm.getUserGroup().equals(ug)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("usergroup present in cidsclass: " // NOI18N
                                        + ug);
                        }
                        permPresent = true;
                        if (perm.getPermission().equals(pBefore)) {
                            perm.setPermission(selectedPerm);
                            for (final ClassPermission cPerm : clazz.getClassPermissions()) {
                                if (cPerm.getUserGroup().equals(perm.getUserGroup())
                                            && cPerm.getPermission().equals(perm.getPermission())) {
                                    final Integer id1 = perm.getId();
                                    final Integer id2 = cPerm.getId();
                                    if ((id1 == null) && (id2 != null)) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug(
                                                "found duplicate: "      // NOI18N
                                                        + perm);
                                        }
                                        duplicate = perm;
                                    } else if ((id1 != null) && (id2 == null)) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug(
                                                "found duplicate: "      // NOI18N
                                                        + cPerm);
                                        }
                                        duplicate = cPerm;
                                    } else if ((id1 != null)
                                                && (id2 != null)
                                                && !id1.equals(id2)) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug(
                                                "found duplicate: "      // NOI18N
                                                        + cPerm);
                                        }
                                        duplicate = cPerm;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!permPresent) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("permission was not found, "           // NOI18N
                                    + "adding classperm");               // NOI18N
                    }
                    final ClassPermission cPerm = new ClassPermission();
                    cPerm.setCidsClass(clazz);
                    cPerm.setPermission(selectedPerm);
                    cPerm.setUserGroup(ug);
                    clazz.getClassPermissions().add(cPerm);
                }
                if (duplicate != null) {
                    clazz.getClassPermissions().remove(duplicate);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updatePolicyBox() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating policybox policy selection");         // NOI18N
        }
        cboPolicy.removeItemListener(cboPolicyItemListener);
        final JXTable classesTable = (JXTable)tblClasses;
        final DefaultTableModel tModel = (DefaultTableModel)classesTable.getModel();
        final int[] rows = classesTable.getSelectedRows();
        Policy firstPolicy = null;
        boolean equalPolicies = true;
        final Comparator<Policy> pComp = new Comparators.Policies();
        for (final int row : rows) {
            final int selRow = classesTable.convertRowIndexToModel(row);
            if (rdbClassPolicy.isSelected()) {
                Policy toCheck = (Policy)tModel.getValueAt(selRow, 1);
                if (toCheck == null) {
                    toCheck = Policy.NO_POLICY;
                }
                if (firstPolicy == null) {
                    firstPolicy = toCheck;
                } else {
                    if (pComp.compare(firstPolicy, toCheck) != 0) {
                        equalPolicies = false;
                        break;
                    }
                }
            }
            if (rdbAttrPolicy.isSelected()) {
                Policy toCheck = (Policy)tModel.getValueAt(selRow, 2);
                if (toCheck == null) {
                    toCheck = Policy.NO_POLICY;
                }
                if (firstPolicy == null) {
                    firstPolicy = toCheck;
                } else {
                    if (pComp.compare(firstPolicy, toCheck) != 0) {
                        equalPolicies = false;
                        break;
                    }
                }
            }
        }
        if (equalPolicies) {
            if (cboPolicy.getItemAt(0).equals(VARIOUS_POLICIES)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("index at 0 is various policies, "      // NOI18N
                                + "removing it");                     // NOI18N
                }
                cboPolicy.removeItemAt(0);
            }
            cboPolicy.setSelectedItem(firstPolicy);
        } else {
            if (!cboPolicy.getItemAt(0).equals(VARIOUS_POLICIES)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("index at 0 was not various policies, " // NOI18N
                                + "inserting it");                    // NOI18N
                }
                cboPolicy.insertItemAt(VARIOUS_POLICIES, 0);
            }
            cboPolicy.setSelectedIndex(0);
        }
        cboPolicy.addItemListener(cboPolicyItemListener);
    }

    /**
     * DOCUMENT ME!
     */
    private void updateRightBox() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating right box");                       // NOI18N
        }
        cboRight.removeItemListener(cboRightItemListener);
        final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
        final int[] rows = tblRights.getSelectedRows();
        Permission firstPerm = null;
        boolean equalRights = true;
        final Comparator<Permission> pComp = new Comparators.Permissions();
        for (final int row : rows) {
            final int selRow = ((JXTable)tblRights).convertRowIndexToModel(row);
            if (!unmarkedRightRows.contains(selRow)) {
                equalRights = false;
                break;
            }
            final Permission toCheck = (Permission)tModel.getValueAt(selRow, 1);
            if (firstPerm == null) {
                firstPerm = toCheck;
            } else {
                if (pComp.compare(firstPerm, toCheck) != 0) {
                    equalRights = false;
                    break;
                }
            }
        }
        if (equalRights) {
            if (cboRight.getItemAt(0).equals(VARIOUS_PERMS)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("index at 0 is various perms, "      // NOI18N
                                + "removing it");                  // NOI18N
                }
                cboRight.removeItemAt(0);
            }
            cboRight.setSelectedItem(firstPerm);
        } else {
            if (!cboRight.getItemAt(0).equals(VARIOUS_PERMS)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("index at 0 was not various perms, " // NOI18N
                                + "inserting it");                 // NOI18N
                }
                cboRight.insertItemAt(VARIOUS_PERMS, 0);
            }
            cboRight.setSelectedIndex(0);
        }
        cboRight.addItemListener(cboRightItemListener);
    }

    /**
     * DOCUMENT ME!
     */
    private void updateRightsTable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating rights table");         // NOI18N
        }
        final DefaultTableModel rtModel = (DefaultTableModel)tblRights.getModel();
        final ListSelectionModel sModel = tblRights.getSelectionModel();
        sModel.removeListSelectionListener(tblRightsSelListener);
        final List<List> data = new ArrayList();
        final List<List> unmarked = new ArrayList();
        boolean firstClass = true;
        for (final CidsClass clazz : selectedClasses) {
            final Set<ClassPermission> cperms = clazz.getClassPermissions();
            final List miniData;
            if (unmarked.isEmpty()) {
                miniData = null;
            } else {
                miniData = new ArrayList(cperms.size());
            }
            for (final ClassPermission cperm : cperms) {
                final List list = new ArrayList(2);
                list.add(cperm.getUserGroup());
                list.add(cperm.getPermission());
                if (!data.contains(list)) {
                    data.add(list);
                    if (firstClass) {
                        unmarked.add(list);
                    }
                } else if (miniData != null) {
                    miniData.add(list);
                }
            }
            if (miniData != null) {
                final List toRemove = new ArrayList();
                for (final Object o : unmarked) {
                    if (!miniData.contains(o)) {
                        toRemove.add(o);
                    }
                }
                unmarked.removeAll(toRemove);
            }
            firstClass = false;
        }
        unmarkedRightRows.clear();
        for (final List l : unmarked) {
            unmarkedRightRows.add(data.indexOf(l));
        }
        final Object[][] listData = new Object[data.size()][];
        int i = -1;
        for (final List l : data) {
            listData[++i] = l.toArray();
        }
        rtModel.setDataVector(listData, tblRightsColumnIdentifiers.toArray());
        sModel.addListSelectionListener(tblRightsSelListener);
        final JXTable rightsTable = (JXTable)tblRights;
        rightsTable.setSortOrder(0, SortOrder.ASCENDING);
        rightsTable.setSortable(true);
        if (preserveSelection == null) {
            sModel.setSelectionInterval(0, data.size() - 1);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("using preserved selection: " // NOI18N
                            + preserveSelection);
            }
            rightsTable.clearSelection();
            for (final Integer row : preserveSelection) {
                final int selRow = rightsTable.convertRowIndexToView(row);
                sModel.addSelectionInterval(selRow, selRow);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updateGroupList() {
        final JXList list = (JXList)lstGroups;
        final DefaultListModel lModel = (DefaultListModel)list.getModel();
        lModel.clear();
        final Set<UserGroup> ugs = getSelectedUGsWithMaxUsage();
        for (final UserGroup ug : allUserGroups) {
            if (!ugs.contains(ug)) {
                lModel.addElement(ug);
            }
        }
        list.setSortOrder(SortOrder.ASCENDING);
        list.setComparator(new Comparators.UserGroups());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ugs    DOCUMENT ME!
     * @param  perms  DOCUMENT ME!
     */
    private void removeGroups(final List ugs, final List perms) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing groups"); // NOI18N
        }
        for (final CidsClass clazz : selectedClasses) {
            final List<ClassPermission> toRemove = new ArrayList(perms.size());
            for (final ClassPermission cperm : clazz.getClassPermissions()) {
                for (int i = 0; i < ugs.size(); ++i) {
                    final UserGroup ug = (UserGroup)ugs.get(i);
                    final Permission perm = (Permission)perms.get(i);
                    if (cperm.getUserGroup().equals(ug)
                                && cperm.getPermission().equals(perm)) {
                        toRemove.add(cperm);
                    }
                }
            }
            clazz.getClassPermissions().removeAll(toRemove);
        }
        preserveSelection = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ugs   DOCUMENT ME!
     * @param   perm  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void addGroups(final Object[] ugs, final Permission perm) {
        for (final Object o : ugs) {
            final UserGroup ug = (UserGroup)o;
            Permission permittedPerm = perm;
            if (!addPermitted(ug, perm)) {
                permittedPerm = null;
                for (int i = 0; i < validPermissions.length; ++i) {
                    if (addPermitted(ug, validPermissions[i])) {
                        permittedPerm = validPermissions[i];
                        break;
                    }
                }
                if (permittedPerm == null) {
                    throw new IllegalStateException(
                        "could not find a valid permission"); // NOI18N
                }
            }
            for (final CidsClass clazz : selectedClasses) {
                final ClassPermission cperm = new ClassPermission();
                cperm.setCidsClass(clazz);
                cperm.setPermission(permittedPerm);
                cperm.setUserGroup(ug);
                clazz.getClassPermissions().add(cperm);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug    DOCUMENT ME!
     * @param   perm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean addPermitted(final UserGroup ug, final Permission perm) {
        final List data = ((DefaultTableModel)tblRights.getModel()).getDataVector();
        for (final Object row : data) {
            if (ug.equals(((List)row).get(0))
                        && perm.equals(((List)row).get(1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<CidsClass> getSelectedClasses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetching selected classes"); // NOI18N
        }
        final JXTable classesTable = (JXTable)tblClasses;
        final DefaultTableModel ctModel = (DefaultTableModel)classesTable.getModel();
        final int[] rows = classesTable.getSelectedRows();
        final List<CidsClass> selClasses = new ArrayList<CidsClass>(rows.length);
        for (int i = 0; i < rows.length; ++i) {
            final int selRow = classesTable.convertRowIndexToModel(rows[i]);
            selClasses.add((CidsClass)ctModel.getValueAt(selRow, 0));
        }
        return selClasses;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Set<UserGroup> getSelectedUGsWithMaxUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searching for usergroups with max usage"); // NOI18N
        }
        final List data = ((DefaultTableModel)tblRights.getModel()).getDataVector();
        final Set<UserGroup> ugs = new HashSet(data.size());
        for (int i = 0; i < data.size(); ++i) {
            int usageCount = 1;
            final UserGroup ug = (UserGroup)((List)data.get(i)).get(0);
            for (int j = i + 1; j < data.size(); ++j) {
                final UserGroup copy = (UserGroup)((List)data.get(j)).get(0);
                if (copy.equals(ug) && (++usageCount == differentRightCount)) {
                    ugs.add(ug);
                    break;
                }
            }
        }
        return ugs;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.getName().returnvalue"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    EditRightsVisualPanel1.class,
                    "EditRightsVisualPanel1.jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setViewportView(tblClasses);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    EditRightsVisualPanel1.class,
                    "EditRightsVisualPanel1.jScrollPane2.border.title"))); // NOI18N
        jScrollPane2.setViewportView(tblRights);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    EditRightsVisualPanel1.class,
                    "EditRightsVisualPanel1.jScrollPane3.border.title"))); // NOI18N

        jScrollPane3.setViewportView(lstGroups);

        org.openide.awt.Mnemonics.setLocalizedText(
            rdbClassPolicy,
            org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.rdbClassPolicy.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            rdbAttrPolicy,
            org.openide.util.NbBundle.getMessage(
                EditRightsVisualPanel1.class,
                "EditRightsVisualPanel1.rdbAttrPolicy.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(EditRightsVisualPanel1.class, "EditRightsVisualPanel1.jLabel1.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout pnlClassesLayout = new org.jdesktop.layout.GroupLayout(pnlClasses);
        pnlClasses.setLayout(pnlClassesLayout);
        pnlClassesLayout.setHorizontalGroup(
            pnlClassesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                pnlClassesLayout.createSequentialGroup().add(
                    jLabel1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    77,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    pnlClassesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        rdbClassPolicy).add(rdbAttrPolicy).add(cboPolicy, 0, 320, Short.MAX_VALUE)).add(0, 0, 0)));
        pnlClassesLayout.setVerticalGroup(
            pnlClassesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                pnlClassesLayout.createSequentialGroup().addContainerGap(20, Short.MAX_VALUE).add(rdbClassPolicy)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(rdbAttrPolicy)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    pnlClassesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                        cboPolicy,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel1))));

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(EditRightsVisualPanel1.class, "EditRightsVisualPanel1.jLabel2.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout pnlRightsLayout = new org.jdesktop.layout.GroupLayout(pnlRights);
        pnlRights.setLayout(pnlRightsLayout);
        pnlRightsLayout.setHorizontalGroup(
            pnlRightsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                pnlRightsLayout.createSequentialGroup().add(
                    jLabel2,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    75,
                    Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(
                    cboRight,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    194,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
        pnlRightsLayout.setVerticalGroup(
            pnlRightsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                pnlRightsLayout.createSequentialGroup().addContainerGap(69, Short.MAX_VALUE).add(
                    pnlRightsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                        cboRight,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel2))));

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(
                        pnlClasses,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        292,
                        Short.MAX_VALUE)).add(32, 32, 32).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(
                        pnlRights,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        jScrollPane2,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        280,
                        Short.MAX_VALUE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    31,
                    Short.MAX_VALUE).add(
                    jScrollPane3,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    239,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        pnlClasses,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        pnlRights,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        jScrollPane3,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        431,
                        Short.MAX_VALUE).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        jScrollPane2,
                        0,
                        0,
                        Short.MAX_VALUE).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        431,
                        Short.MAX_VALUE)).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DragDropJXTable extends JXTable implements DragGestureListener,
        DragSourceListener,
        DropTargetListener {

        //~ Methods ------------------------------------------------------------

        // <editor-fold defaultstate="collapsed" desc=" Not needed ListenerImpls ">
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

        @Override
        public void dragEnter(final DragSourceDragEvent dsde) {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent dsde) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent dsde) {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent dse) {
            // not needed
        }

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde) {
            // not needed
        }
        // </editor-fold>

        //~ Instance fields ----------------------------------------------------

        private final transient DragSource dragSource;
        private final transient int acceptableActions;
        private final transient DropTarget dt;
        private final transient RightRowRenderer rightRowRenderer;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DragDropJXTable object.
         */
        public DragDropJXTable() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this);                            // drag gesture recognizer
            acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
            dt = new DropTarget(this, acceptableActions, this);
            rightRowRenderer = new RightRowRenderer();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void dragGestureRecognized(final DragGestureEvent e) {
            final List ugs = new ArrayList(getSelectedRowCount());
            final List perms = new ArrayList(getSelectedRowCount());
            for (final int row : getSelectedRows()) {
                ugs.add(getValueAt(row, 0));
                perms.add(getValueAt(row, 1));
            }
            final Object o = new Object[] { this, ugs, perms };
            final Transferable trans = new CidsUserGroupTransferable(o);
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }

        @Override
        public void drop(final DropTargetDropEvent dtde) {
            try {
                final Object o = dtde.getTransferable().getTransferData(
                        CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if (o instanceof Object[]) {
                    final Object[] ugs = (Object[])o;
                    if (ugs[0].equals(this)) {
                        return;
                    }
                    final Permission perm;
                    if (cboRight.getSelectedItem().equals(VARIOUS_PERMS)) {
                        perm = defaultPermission;
                    } else {
                        perm = (Permission)cboRight.getSelectedItem();
                    }
                    addGroups((Object[])ugs[1], perm);
                    updateRightsTable();
                    updateGroupList();
                }
            } catch (final Exception e) {
                LOG.warn("exception during drop action", e); // NOI18N
            } finally {
                dtde.dropComplete(true);
            }
        }

        @Override
        public TableCellRenderer getCellRenderer(final int row,
                final int column) {
            return rightRowRenderer;
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
    private final class DragDropJList extends JXList implements DragGestureListener,
        DragSourceListener,
        DropTargetListener {

        //~ Methods ------------------------------------------------------------

        // <editor-fold defaultstate="collapsed" desc=" Not needed ListenerImpls ">
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

        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragExit(final DropTargetEvent dte) {
            // not needed
        }
        // </editor-fold>

        //~ Instance fields ----------------------------------------------------

        private final transient int acceptableActions;
        private final transient DropTarget dt;
        private final transient DragSource dragSource;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DragDropJList object.
         */
        public DragDropJList() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this);                            // drag gesture recognizer
            acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
            dt = new DropTarget(this, acceptableActions, this);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void dragGestureRecognized(final DragGestureEvent e) {
            final Object o = new Object[] { this, getSelectedValues() };
            final Transferable trans = new CidsUserGroupTransferable(o);
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }

        @Override
        public DropTarget getDropTarget() {
            return dt;
        }

        @Override
        public void drop(final DropTargetDropEvent dtde) {
            try {
                final Object o = dtde.getTransferable().getTransferData(
                        CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if (o instanceof Object[]) {
                    final Object[] ob = (Object[])o;
                    if (ob[0].equals(this)) {
                        return;
                    }
                    removeGroups((List)ob[1], (List)ob[2]);
                    updateRightsTable();
                    updateGroupList();
                }
            } catch (final Exception e) {
                LOG.warn("exception during drop action", e); // NOI18N
            } finally {
                dtde.dropComplete(true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class PolicySelectionListener implements ItemListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if ((ItemEvent.SELECTED != e.getStateChange())
                        || VARIOUS_POLICIES.equals(e.getItem())) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("new policy selected, updating");    // NOI18N
            }
            final ListSelectionModel sModel = tblClasses.getSelectionModel();
            sModel.removeListSelectionListener(tblClassSelListener);
            cboPolicy.removeItemListener(cboPolicyItemListener);
            if (VARIOUS_POLICIES.equals(cboPolicy.getItemAt(0))) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("index at 0 is various policies" // NOI18N
                                + ", removing it");            // NOI18N
                }
                cboPolicy.removeItemAt(0);
            }
            updatePolicies((Policy)e.getItem());
            cboPolicy.addItemListener(cboPolicyItemListener);
            sModel.addListSelectionListener(tblClassSelListener);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ClassTableSelectionListener implements ListSelectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("class table selection changed"); // NOI18N
            }
            preserveSelection = null;
            selectedClasses = getSelectedClasses();
            updatePolicyBox();
            updateRightsTable();
            updateGroupList();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class RightsTableSelectionListener implements ListSelectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("rights table selection changed"); // NOI18N
            }
            updateRightBox();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ButtonSelectionListener implements ItemListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (rdbClassPolicy.equals(e.getSource())
                            && !rdbAttrPolicy.isSelected()) {
                    rdbAttrPolicy.setSelected(true);
                }
                if (rdbAttrPolicy.equals(e.getSource())
                            && !rdbClassPolicy.isSelected()) {
                    rdbClassPolicy.setSelected(true);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("button selection changed: " // NOI18N
                            + rdbClassPolicy.isSelected()
                            + " | " + rdbAttrPolicy.isSelected()); // NOI18N
            }
            updatePolicyBox();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class RightSelectionListener implements ItemListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if ((ItemEvent.SELECTED != e.getStateChange())
                        || VARIOUS_PERMS.equals(e.getItem())) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("new right selected, updating");    // NOI18N
            }
            final ListSelectionModel sModel = tblRights.getSelectionModel();
            sModel.removeListSelectionListener(tblRightsSelListener);
            cboRight.removeItemListener(cboRightItemListener);
            if (cboRight.getItemAt(0).equals(VARIOUS_PERMS)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("index at 0 is various perms, " // NOI18N
                                + "removing it");             // NOI18N
                }
                cboRight.removeItemAt(0);
            }
            updateRights((Permission)e.getItem());
            cboRight.addItemListener(cboRightItemListener);
            sModel.addListSelectionListener(tblRightsSelListener);
            updateRightsTable();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class RightRowRenderer extends DefaultTableCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getTableCellRendererComponent(
                final JTable table,
                final Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column) {
            final Component c = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            c.setEnabled(unmarkedRightRows.contains(((JXTable)tblRights).convertRowIndexToModel(row)));
            return c;
        }
    }
}

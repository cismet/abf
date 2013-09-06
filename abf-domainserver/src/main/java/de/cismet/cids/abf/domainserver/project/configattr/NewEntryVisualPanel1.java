/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.domainserver.project.utils.Renderers.DomainListRenderer;
import de.cismet.cids.abf.domainserver.project.utils.Renderers.UserGroupListRenderer;
import de.cismet.cids.abf.domainserver.project.utils.Renderers.UserListRenderer;
import de.cismet.cids.abf.utilities.Comparators;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrValue;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class NewEntryVisualPanel1 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final User UG_FIRST_USER;

    static {
        UG_FIRST_USER = new User();
        UG_FIRST_USER.setId(-1);
        UG_FIRST_USER.setLoginname(NbBundle.getMessage(
                NewEntryVisualPanel1.class,
                "NewEntryVisualPanel1.<clinit>.UG_FIRST_USER.loginName")); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    private final transient NewEntryWizardPanel1 model;

    private final transient ItemListener uGItemL;
    private final transient ItemListener userItemL;
    private final transient ItemListener domainItemL;
    private final transient ChangeListener addButtonCL;
    private final transient ActionListener addButtonAL;
    private final transient ActionListener remButtonAL;
    private final transient ListSelectionListener remButtonLSL;

    private transient List<ConfigAttrType> typeCache;
    private transient List<UserGroup> allUserGroups;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddEntry;
    private javax.swing.JButton btnRemoveEntry;
    private javax.swing.JComboBox cboDomain;
    private javax.swing.JComboBox cboUser;
    private javax.swing.JComboBox cboUsergroup;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXList jxlEntries;
    private javax.swing.JLabel lblDomain;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblUsergroup;
    private javax.swing.JPanel pnlEntries;
    private javax.swing.JToolBar tlbEntry;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewEntryVisualPanel1 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public NewEntryVisualPanel1(final NewEntryWizardPanel1 model) {
        this.model = model;
        uGItemL = new UGItemListenerImpl();
        userItemL = new UserItemListenerImpl();
        domainItemL = new DomainItemListenerImpl();
        addButtonCL = new AddButtonChangeListener();
        addButtonAL = new AddButtonActionListener();
        remButtonAL = new RemoveButtonActionListener();
        remButtonLSL = new JXEntriesSelectionListener();

        model.addChangeListener(WeakListeners.change(addButtonCL, model));

        initComponents();

        btnAddEntry.addActionListener(WeakListeners.create(ActionListener.class, addButtonAL, btnAddEntry));
        btnRemoveEntry.addActionListener(WeakListeners.create(ActionListener.class, remButtonAL, btnRemoveEntry));
        jxlEntries.addListSelectionListener(WeakListeners.create(
                ListSelectionListener.class,
                remButtonLSL,
                jxlEntries));

        btnAddEntry.setIcon(ImageUtilities.loadImageIcon(DomainserverProject.IMAGE_FOLDER + "plus.png", false));     // NOI18N
        btnRemoveEntry.setIcon(ImageUtilities.loadImageIcon(DomainserverProject.IMAGE_FOLDER + "minus.png", false)); // NOI18N
        btnAddEntry.setText(null);
        btnRemoveEntry.setText(null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ConfigAttrEntry getEntry() {
        final ConfigAttrEntry entry = new ConfigAttrEntry();
        entry.setDomain((Domain)cboDomain.getSelectedItem());

        final UserGroup ug = (UserGroup)cboUsergroup.getSelectedItem();
        if (UserGroup.NO_GROUP.equals(ug)) {
            entry.setUsergroup(null);
        } else {
            entry.setUsergroup(ug);
        }

        final User user = (User)cboUser.getSelectedItem();
        if (User.NO_USER.equals(user)) {
            entry.setUser(null);
        } else {
            entry.setUser(user);
        }

        entry.setKey(model.getKey());
        entry.setValue(getDefaultValue(model.getType()));
        entry.setType(getType(model.getType()));

        return entry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    ConfigAttrType getType(final Types type) {
        for (final ConfigAttrType cat : typeCache) {
            if (cat.getAttrType().equals(type)) {
                return cat;
            }
        }

        throw new IllegalStateException("no ConfigAttrType for type: " + type); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ConfigAttrValue getDefaultValue(final Types type) {
        final ConfigAttrValue value = new ConfigAttrValue();

        if (Types.ACTION_TAG.equals(type)) {
            value.setValue("true");          // NOI18N
        } else if (Types.CONFIG_ATTR.equals(type)) {
            value.setValue("");              // NOI18N
        } else if (Types.XML_ATTR.equals(type)) {
            value.setValue("<root></root>"); // NOI18N
        }

        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    void init() {
        final Backend backend = model.getProject().getCidsDataObjectBackend();
        final List<Domain> domains = backend.getAllEntities(Domain.class);
        allUserGroups = backend.getAllEntities(UserGroup.class);
        typeCache = backend.getAllEntities(ConfigAttrType.class);
        Collections.sort(allUserGroups, new Comparators.UserGroups());

        Domain localDomain = null;
        for (final Domain domain : domains) {
            if (ProjectUtils.LOCAL_DOMAIN_NAME.equals(domain.getName())) { // NOI18N
                localDomain = domain;
            }

            cboDomain.addItem(domain);
        }

        if (localDomain == null) {
            throw new IllegalStateException("could not find local domain"); // NOI18N
        }

        cboDomain.setSelectedItem(localDomain);
        cboDomain.addItemListener(WeakListeners.create(ItemListener.class, domainItemL, cboDomain));
        cboDomain.setRenderer(new DomainListRenderer());

        cboUsergroup.addItem(UserGroup.NO_GROUP);
        for (final UserGroup ug : allUserGroups) {
            if (!ProjectUtils.isRemoteGroup(ug, model.getProject())) {
                cboUsergroup.addItem(ug);
            }
        }
        cboUsergroup.setSelectedItem(UserGroup.NO_GROUP);
        cboUsergroup.addItemListener(WeakListeners.create(ItemListener.class, uGItemL, cboUsergroup));
        cboUsergroup.setRenderer(new UserGroupListRenderer(model.getProject()));

        cboUser.addItem(UG_FIRST_USER);
        cboUser.setSelectedItem(UG_FIRST_USER);
        cboUser.setEnabled(false);
        cboUser.addItemListener(WeakListeners.create(ItemListener.class, userItemL, cboUser));
        cboUser.setRenderer(new UserListRenderer());

        jxlEntries.setModel(model.getEntryListModel());
        jxlEntries.setComparator(new JXEntriesComparator());
        jxlEntries.setCellRenderer(new JXEntriesRenderer());
        jxlEntries.setSortable(true);
        jxlEntries.setSortsOnUpdates(true);
        jxlEntries.setAutoCreateRowSorter(true);
        jxlEntries.setSortOrder(SortOrder.ASCENDING);

        btnRemoveEntry.setEnabled(false);

        model.setCurrentEntry(getEntry());
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.getName().returnValue"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cboDomain = new javax.swing.JComboBox();
        lblDomain = new javax.swing.JLabel();
        cboUsergroup = new javax.swing.JComboBox();
        cboUser = new javax.swing.JComboBox();
        lblUsergroup = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        pnlEntries = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jxlEntries = new org.jdesktop.swingx.JXList();
        tlbEntry = new javax.swing.JToolBar();
        btnAddEntry = new javax.swing.JButton();
        btnRemoveEntry = new javax.swing.JButton();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(cboDomain, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblDomain,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.lblDomain.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblDomain, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(cboUsergroup, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(cboUser, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblUsergroup,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.lblUsergroup.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblUsergroup, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblUser,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.lblUser.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblUser, gridBagConstraints);

        pnlEntries.setOpaque(false);
        pnlEntries.setLayout(new java.awt.GridBagLayout());

        jxlEntries.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jxlEntries);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        pnlEntries.add(jScrollPane1, gridBagConstraints);

        tlbEntry.setRollover(true);
        tlbEntry.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(
            btnAddEntry,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.btnAddEntry.text")); // NOI18N
        btnAddEntry.setFocusable(false);
        btnAddEntry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddEntry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tlbEntry.add(btnAddEntry);

        org.openide.awt.Mnemonics.setLocalizedText(
            btnRemoveEntry,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.btnRemoveEntry.text")); // NOI18N
        btnRemoveEntry.setFocusable(false);
        btnRemoveEntry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveEntry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tlbEntry.add(btnRemoveEntry);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        pnlEntries.add(tlbEntry, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlEntries, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class AddButtonActionListener implements ActionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final ConfigAttrEntry cae = model.getCurrentEntry();
            model.addEntry(cae);

            // setselectedvalue currently does not convert the index
            final ListModel lm = jxlEntries.getModel();
            int index = -1;
            for (int i = 0; i < lm.getSize(); ++i) {
                if (lm.getElementAt(i).equals(cae)) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                index = jxlEntries.convertIndexToView(index);
                jxlEntries.setSelectedIndex(index);
                jxlEntries.ensureIndexIsVisible(index);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class RemoveButtonActionListener implements ActionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            // getselectedvalue currently does not convert the index
            final int viewIndex = jxlEntries.getSelectedIndex();
            final int modelIndex = jxlEntries.convertIndexToModel(viewIndex);
            final ListModel lm = jxlEntries.getModel();
            model.removeEntry((ConfigAttrEntry)lm.getElementAt(modelIndex));

            if (viewIndex < lm.getSize()) {
                jxlEntries.setSelectedIndex(viewIndex);
            } else {
                jxlEntries.setSelectedIndex(viewIndex - 1);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class JXEntriesSelectionListener implements ListSelectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                final int viewIndex = jxlEntries.getSelectedIndex();
                ConfigAttrEntry cae = null;

                if (viewIndex >= 0) {
                    final int modelIndex = jxlEntries.convertIndexToModel(viewIndex);
                    final ListModel lm = jxlEntries.getModel();
                    cae = (ConfigAttrEntry)lm.getElementAt(modelIndex);
                }

                btnRemoveEntry.setEnabled(cae != null);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class AddButtonChangeListener implements ChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void stateChanged(final ChangeEvent e) {
            btnAddEntry.setEnabled(!model.entryContained(getEntry(), model.getEntries()));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class JXEntriesComparator implements Comparator<ConfigAttrEntry> {

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final ConfigAttrEntry o1, final ConfigAttrEntry o2) {
            return ConfigAttrEntryNode.createEntryOwnerString(o1)
                        .compareTo(ConfigAttrEntryNode.createEntryOwnerString(o2));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class JXEntriesRenderer extends DefaultListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel comp = (JLabel)super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);

            final ConfigAttrEntry cae = (ConfigAttrEntry)value;
            comp.setText(ConfigAttrEntryNode.createEntryOwnerString(cae));
            comp.setIcon(new ImageIcon(ConfigAttrEntryNode.getIcon(cae)));

            return comp;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DomainItemListenerImpl implements ItemListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                final Domain selected = (Domain)e.getItem();

                cboUsergroup.removeAllItems();

                cboUsergroup.addItem(UserGroup.NO_GROUP);
                for (final UserGroup ug : allUserGroups) {
                    if (selected.equals(ug.getDomain())) {
                        cboUsergroup.addItem(ug);
                    }
                }

                cboUsergroup.setSelectedItem(UserGroup.NO_GROUP);

                model.setCurrentEntry(getEntry());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class UserItemListenerImpl implements ItemListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                model.setCurrentEntry(getEntry());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class UGItemListenerImpl implements ItemListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                final UserGroup selected = (UserGroup)e.getItem();

                cboUser.removeAllItems();
                if (UserGroup.NO_GROUP.equals(selected)) {
                    cboUser.addItem(UG_FIRST_USER);
                    cboUser.setSelectedItem(UG_FIRST_USER);
                    cboUser.setEnabled(false);
                    // UserItemListener fires change event for us
                } else {
                    final ArrayList<User> newUsers = new ArrayList<User>(selected.getUsers());
                    Collections.sort(newUsers, new Comparators.Users());
                    cboUser.addItem(User.NO_USER);
                    for (final User user : newUsers) {
                        cboUser.addItem(user);
                    }
                    cboUser.setEnabled(true);

                    cboUser.setSelectedItem(User.NO_USER);
                }

                model.setCurrentEntry(getEntry());
            }
        }
    }
}

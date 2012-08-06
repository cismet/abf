/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 mscholl
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cids.abf.domainserver.project.configattr;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
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

    private transient List<ConfigAttrType> typeCache;

    private transient List<UserGroup> allUserGroups;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboDomain;
    private javax.swing.JComboBox cboUser;
    private javax.swing.JComboBox cboUsergroup;
    private javax.swing.JLabel lblDomain;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblUsergroup;
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

        initComponents();
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

        model.fireChangeEvent();
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
        cboDomain = new javax.swing.JComboBox();
        lblDomain = new javax.swing.JLabel();
        cboUsergroup = new javax.swing.JComboBox();
        cboUser = new javax.swing.JComboBox();
        lblUsergroup = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(
            lblDomain,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.lblDomain.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            lblUsergroup,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.lblUsergroup.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            lblUser,
            NbBundle.getMessage(NewEntryVisualPanel1.class, "NewEntryVisualPanel1.lblUser.text")); // NOI18N

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addContainerGap().addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblDomain)
                                .addComponent(
                                    cboDomain,
                                    javax.swing.GroupLayout.PREFERRED_SIZE,
                                    109,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblUsergroup)
                                .addComponent(
                                    cboUsergroup,
                                    javax.swing.GroupLayout.PREFERRED_SIZE,
                                    156,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblUser)
                                .addComponent(cboUser, 0, 234, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addGap(29, 29, 29).addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        layout.createSequentialGroup().addComponent(lblUsergroup).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(
                            cboUsergroup,
                            javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
                        layout.createSequentialGroup().addGroup(
                            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                                lblDomain).addComponent(lblUser)).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                                cboDomain,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                                cboUser,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)))).addContainerGap(
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)));
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

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
                model.fireChangeEvent();
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
            }
        }
    }
}

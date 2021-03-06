/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users.groups;

import org.openide.util.WeakListeners;

import java.awt.EventQueue;

import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.domainserver.project.utils.Renderers;

import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class NewUsergroupVisualPanel1 extends JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient NewUsergroupWizardPanel1 model;
    // needed because of weaklistener
    private final transient DocumentListener docL;
    // EDT access only
    private transient boolean init;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboDomain = new javax.swing.JComboBox();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblDesc = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblDomain = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblDomainserverName = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblGoal = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblName = new javax.swing.JLabel();
    private final transient javax.swing.JTextArea txtDescription = new javax.swing.JTextArea();
    private final transient javax.swing.JTextField txtName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewUsergroupVisualPanel1 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public NewUsergroupVisualPanel1(final NewUsergroupWizardPanel1 model) {
        this.model = model;
        initComponents();
        docL = new DocumentListenerImpl();
        cboDomain.setRenderer(new Renderers.UnifiedCellRenderer());
        txtName.getDocument().addDocumentListener(WeakListeners.document(docL, txtName.getDocument()));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        assert EventQueue.isDispatchThread() : "not called from EDT"; // NOI18N

        init = true;
        final Properties props = model.getProject().getRuntimeProps();
        final String destination = props.getProperty("serverName") // NOI18N
                    + " ("                                         // NOI18N
                    + props.getProperty("connection.url") + ")";   // NOI18N
        lblDomainserverName.setText(destination);
        final List<Domain> domains = model.getProject().getCidsDataObjectBackend().getAllEntities(Domain.class);
        cboDomain.setModel(new DefaultComboBoxModel(domains.toArray()));
        if (model.getUserGroup() == null) {
            txtName.setText("");                                   // NOI18N
            txtDescription.setText("");                            // NOI18N
        } else {
            final UserGroup ug = model.getUserGroup();
            txtName.setText(ug.getName());
            txtDescription.setText(ug.getDescription());
            cboDomain.setSelectedItem(ug.getDomain());
        }

        init = false;
        model.fireChangeEvent();
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel1.class,
                "NewUsergroupVisualPanel1.getName().returnvalue");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    UserGroup getUserGroup() {
        final UserGroup ug;
        if (model.getUserGroup() == null) {
            ug = new UserGroup();
        } else {
            ug = model.getUserGroup();
        }

        ug.setName(txtName.getText());
        final Object o = cboDomain.getSelectedItem();
        if (o instanceof Domain) {
            ug.setDomain((Domain)o);
        }
        ug.setDescription(txtDescription.getText());
        return ug;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(
            lblName,
            org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel1.class,
                "NewUsergroupVisualPanel1.lblName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            lblDesc,
            org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel1.class,
                "NewUsergroupVisualPanel1.lblDesc.text")); // NOI18N

        txtDescription.setColumns(20);
        txtDescription.setRows(5);
        jScrollPane1.setViewportView(txtDescription);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblGoal,
            org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel1.class,
                "NewUsergroupVisualPanel1.lblGoal.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblDomainserverName, " ");

        org.openide.awt.Mnemonics.setLocalizedText(
            lblDomain,
            org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel1.class,
                "NewUsergroupVisualPanel1.lblDomain.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lblGoal).add(
                                lblDomain)).add(79, 79, 79).add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                cboDomain,
                                0,
                                270,
                                Short.MAX_VALUE).add(
                                lblDomainserverName,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                270,
                                Short.MAX_VALUE))).add(
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lblName).add(
                                lblDesc)).add(50, 50, 50).add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
                                jScrollPane1,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                277,
                                Short.MAX_VALUE).add(
                                org.jdesktop.layout.GroupLayout.LEADING,
                                txtName,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                277,
                                Short.MAX_VALUE)))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblGoal).add(
                        lblDomainserverName)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblDomain).add(
                        cboDomain,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblName).add(
                        txtName,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lblDesc).add(
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)));

        lblName.getAccessibleContext().setAccessibleName("");
        lblGoal.getAccessibleContext().setAccessibleName("");
        lblDomain.getAccessibleContext().setAccessibleName("");
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            if (!init) {
                model.fireChangeEvent();
            }
        }
    }
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.util.Utils;

import de.cismet.tools.PasswordEncrypter;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.4
 */
public final class KeystoreVisualPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient PropertyProvider provider;
    private final transient DocumentListener docL;
    private final transient ImageIcon warning;
    private final transient Category category;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton btnChoose = new javax.swing.JButton();
    private final transient javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
            new java.awt.Dimension(0, 0),
            new java.awt.Dimension(0, 32767));
    private final transient javax.swing.JLabel lblAlias = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblError = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblKeystore = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPassword = new javax.swing.JLabel();
    private final transient javax.swing.JPasswordField pwdKeystore = new javax.swing.JPasswordField();
    private final transient javax.swing.JTextField txtAlias = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtKeystore = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form KeystoreVisualPanel.
     *
     * @param  project   DOCUMENT ME!
     * @param  category  DOCUMENT ME!
     */
    public KeystoreVisualPanel(final ClientProject project, final Category category) {
        provider = PropertyProvider.getInstance(project.getProjectProperties());
        assert provider != null;
        this.category = category;

        docL = new DocumentListenerImpl();
        warning = new ImageIcon(ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "warning_16.gif")); // NOI18N
        initComponents();
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void init() {
        final String keystorePath = provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE);

        final String kspw = provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE_PW);
        if (kspw == null) {
            pwdKeystore.setText(null);
        } else {
            pwdKeystore.setText(String.valueOf(PasswordEncrypter.decrypt(kspw.toCharArray(), true)));
        }

        final String keystoreAlias = provider.get(PropertyProvider.KEY_KEYSTORE_ALIAS);

        if (keystorePath == null) {
            txtKeystore.setText(org.openide.util.NbBundle.getMessage(
                    KeystoreVisualPanel.class,
                    "KeystoreVisualPanel.init().txtKeystore.text.noKeystore")); // NOI18N
        } else {
            txtKeystore.setText(keystorePath);
        }
        txtAlias.setText(keystoreAlias);

        txtKeystore.getDocument().addDocumentListener(WeakListeners.document(docL, txtKeystore.getDocument()));
        txtAlias.getDocument().addDocumentListener(WeakListeners.document(docL, txtAlias.getDocument()));

        validateEntry();
    }

    /**
     * DOCUMENT ME!
     */
    public void validateEntry() {
        final String ksPath = Utils.getPath(txtKeystore.getText());
        final File file = new File(ksPath);
        if (file.exists() && file.isFile()) {
            if (file.canRead()) {
                if (txtAlias.getText().isEmpty()) {
                    lblError.setIcon(warning);
                    lblError.setText(NbBundle.getMessage(
                            KeystoreVisualPanel.class,
                            "KeystoreVisualPanel.validateEntry().lblError.text.noAlias"));
                    category.setValid(false);
                } else {
                    lblError.setIcon(null);
                    lblError.setText("");                                                         // NOI18N
                    category.setValid(true);
                }
            } else {
                lblError.setIcon(warning);
                lblError.setText(NbBundle.getMessage(
                        KeystoreVisualPanel.class,
                        "KeystoreVisualPanel.validateEntry().lblError.text.keystoreUnreadable")); // NOI18N
                category.setValid(false);
            }
        } else {
            lblError.setIcon(warning);
            lblError.setText(NbBundle.getMessage(
                    KeystoreVisualPanel.class,
                    "KeystoreVisualPanel.validateEntry().lblError.text.keystoreNotExistent"));    // NOI18N
            category.setValid(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKeystore() {
        return txtKeystore.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public char[] getPassword() {
        return pwdKeystore.getPassword();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAlias() {
        return txtAlias.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        lblKeystore.setText(org.openide.util.NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.mainKeystoreLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblKeystore, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txtKeystore, gridBagConstraints);

        btnChoose.setText(org.openide.util.NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.chooseButton.text")); // NOI18N
        btnChoose.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnChooseActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(btnChoose, gridBagConstraints);

        lblError.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 204, 0));
        lblError.setText(org.openide.util.NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.errorLabel.text"));             // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblError, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(pwdKeystore, gridBagConstraints);

        lblPassword.setText(org.openide.util.NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.passwordLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblPassword, gridBagConstraints);

        lblAlias.setText(NbBundle.getMessage(KeystoreVisualPanel.class, "KeystoreVisualPanel.lblAlias.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblAlias, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txtAlias, gridBagConstraints);

        filler1.setMaximumSize(new java.awt.Dimension(0, 32767));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(filler1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnChooseActionPerformed(final java.awt.event.ActionEvent evt)                       //GEN-FIRST:event_btnChooseActionPerformed
    {                                                                                                 //GEN-HEADEREND:event_btnChooseActionPerformed
        final JFileChooser chooser = new JFileChooser();
        final File userhome = new File(System.getProperty("user.home"));                              // NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(userhome);
        chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.chooseButtonActionPerformed(ActionEvent).chooser.dialogTitle")); // NOI18N
        chooser.setFileHidingEnabled(false);
        final int retVal = chooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            txtKeystore.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }                                                                                                 //GEN-LAST:event_btnChooseActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent documentEvent) {
            validateEntry();
        }

        @Override
        public void removeUpdate(final DocumentEvent documentEvent) {
            validateEntry();
        }

        @Override
        public void changedUpdate(final DocumentEvent documentEvent) {
            validateEntry();
        }
    }
}

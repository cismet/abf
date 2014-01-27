/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;

import de.cismet.tools.PasswordEncrypter;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class KeystoreVisualPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient Project project;
    private final transient Category category;
    private final transient ImageIcon icon;
    private final transient DocumentListener docL;
    private final transient ActionListener actionL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel lblKeystorePath;
    private javax.swing.JLabel lblKeystorePw;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JPasswordField pwKeystorePw;
    private javax.swing.JTextField txtKeystorePath;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KeystoreVisualPanel object.
     *
     * @param   project   DOCUMENT ME!
     * @param   category  DOCUMENT ME!
     *
     * @throws  NullPointerException  DOCUMENT ME!
     */
    public KeystoreVisualPanel(final Project project, final Category category) {
        if (project == null) {
            throw new NullPointerException("project must not be null"); // NOI18N
        }

        icon = ImageUtilities.loadImageIcon(
                KeystoreVisualPanel.class.getPackage().getName().replaceAll("\\.", "/") // NOI18N
                        + "/error.png",                                                 // NOI18N
                false);
        this.project = project;
        this.category = category;
        this.docL = new DocL();
        this.actionL = new ActionL();

        initComponents();

        txtKeystorePath.getDocument().addDocumentListener(WeakListeners.document(docL, txtKeystorePath.getDocument()));
        pwKeystorePw.getDocument().addDocumentListener(WeakListeners.document(docL, pwKeystorePw.getDocument()));
        btnBrowse.addActionListener(WeakListeners.create(ActionListener.class, actionL, btnBrowse));

        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void init() {
        final Properties projectProps = project.getLookup().lookup(Properties.class);
        if (projectProps == null) {
            throw new IllegalStateException("project properties not availabe for project: " + project); // NOI18N
        }

        final String pwProp = projectProps.getProperty(PropertyProvider.KEY_GENERAL_KEYSTORE_PW, "");

        txtKeystorePath.setText(projectProps.getProperty(PropertyProvider.KEY_GENERAL_KEYSTORE));
        pwKeystorePw.setText(String.valueOf(PasswordEncrypter.decrypt(pwProp.toCharArray(), true)));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isValidPanel() {
        final File file = new File(txtKeystorePath.getText());
        if (file.exists() && file.isFile()) {
            lblStatus.setText(null);
            lblStatus.setIcon(null);

            category.setValid(true);
        } else {
            lblStatus.setText("Invalid keystore path");
            lblStatus.setIcon(icon);

            category.setValid(false);
        }

        return category.isValid();
    }

    /**
     * DOCUMENT ME!
     */
    private void updateProperties() {
        if (isValidPanel()) {
            final Properties props = project.getLookup().lookup(Properties.class);
            props.put(PropertyProvider.KEY_GENERAL_KEYSTORE, txtKeystorePath.getText());
            props.put(
                PropertyProvider.KEY_GENERAL_KEYSTORE_PW,
                // TODO: use new password encryption
                PasswordEncrypter.encryptString(String.valueOf(pwKeystorePw.getPassword())));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblKeystorePath = new javax.swing.JLabel();
        txtKeystorePath = new javax.swing.JTextField();
        lblKeystorePw = new javax.swing.JLabel();
        pwKeystorePw = new javax.swing.JPasswordField();
        btnBrowse = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 32767));
        lblStatus = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        lblKeystorePath.setText(NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.lblKeystorePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblKeystorePath, gridBagConstraints);

        txtKeystorePath.setText(NbBundle.getMessage(
                KeystoreVisualPanel.class,
                "KeystoreVisualPanel.txtKeystorePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txtKeystorePath, gridBagConstraints);

        lblKeystorePw.setText(NbBundle.getMessage(KeystoreVisualPanel.class, "KeystoreVisualPanel.lblKeystorePw.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblKeystorePw, gridBagConstraints);

        pwKeystorePw.setText(NbBundle.getMessage(KeystoreVisualPanel.class, "KeystoreVisualPanel.pwKeystorePw.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(pwKeystorePw, gridBagConstraints);

        btnBrowse.setText(NbBundle.getMessage(KeystoreVisualPanel.class, "KeystoreVisualPanel.btnBrowse.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(btnBrowse, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(filler1, gridBagConstraints);

        lblStatus.setText(NbBundle.getMessage(KeystoreVisualPanel.class, "KeystoreVisualPanel.lblStatus.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(lblStatus, gridBagConstraints);
    }                                                                                                            // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DocL implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            updateProperties();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            updateProperties();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            updateProperties();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ActionL implements ActionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chooser keystore");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileHidingEnabled(false);
            chooser.setMultiSelectionEnabled(false);
            final File initPath = new File(txtKeystorePath.getText());
            if (initPath.exists()) {
                chooser.setSelectedFile(initPath);
            }

            final int answer = chooser.showOpenDialog(KeystoreVisualPanel.this);
            if (answer == JFileChooser.APPROVE_OPTION) {
                txtKeystorePath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}

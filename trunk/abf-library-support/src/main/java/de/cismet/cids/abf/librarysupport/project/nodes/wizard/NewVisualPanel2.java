/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.utilities.files.PackageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.7
 */
public final class NewVisualPanel2 extends JPanel implements DocumentListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewVisualPanel2.class);

    //~ Instance fields --------------------------------------------------------

    private final transient NewWizardPanel2 model;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboPackage = new javax.swing.JComboBox();
    private final transient javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
    private final transient javax.swing.JLabel lblNewFolder = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPackage = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPackageName = new javax.swing.JLabel();
    private final transient javax.swing.JTextField txtNewFolder = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtPackageName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form NewPackageVisualPanel1.
     *
     * @param  model  DOCUMENT ME!
     */
    public NewVisualPanel2(final NewWizardPanel2 model) {
        this.model = model;
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        final FileObject root = model.getRootDir();
        final FileObject current = model.getCurrentDir();
        final LinkedList<String> packages = new LinkedList<String>();
        for (final Enumeration<? extends FileObject> e = root.getFolders(true); e.hasMoreElements();) {
            packages.add(PackageUtils.toPackage(root, e.nextElement()));
        }
        Collections.sort(packages);
        packages.addFirst(PackageUtils.ROOT_PACKAGE);
        final DefaultComboBoxModel dcbmodel = new DefaultComboBoxModel(
                packages.toArray());
        dcbmodel.setSelectedItem(PackageUtils.toPackage(root, current));
        cboPackage.setModel(dcbmodel);
        String name = "";                                             // NOI18N
        final String ext = model.getExt();
        lblPackageName.setText(org.openide.util.NbBundle.getMessage(
                NewVisualPanel2.class,
                "NewVisualPanel2.lblPackageName.text.fileName"));     // NOI18N
        lblNewFolder.setText(org.openide.util.NbBundle.getMessage(
                NewVisualPanel2.class,
                "NewVisualPanel2.lblNewFolder.text.newFile"));        // NOI18N
        if (model.isPackage()) {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class,
                    "NewVisualPanel2.init().name.newPackage");        // NOI18N
            lblPackageName.setText(org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class,
                    "NewVisualPanel2.lblPackageName.text"));          // NOI18N
            lblNewFolder.setText(org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class,
                    "NewVisualPanel2.lblNewFolder.text"));            // NOI18N
        } else if ((ext == null) || "".equals(ext))                   // NOI18N
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class,
                    "NewVisualPanel2.init().name.newFile");           // NOI18N
        } else if ("properties".equals(ext))                          // NOI18N
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class,
                    "NewVisualPanel2.init().name.newPropertiesFile"); // NOI18N
        } else if ("txt".equals(ext))                                 // NOI18N
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class,
                    "NewVisualPanel2.init().name.newTextFile");       // NOI18N
        }
        txtPackageName.setText(name);
        txtPackageName.setSelectionStart(
            txtPackageName.getText().indexOf(name));
        txtPackageName.setSelectionEnd(txtPackageName.getText().length());
        txtPackageName.getDocument().addDocumentListener(this);
        cboPackage.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    updateText();
                    model.fireChangeEvent();
                }
            });
        updateText();
        model.fireChangeEvent();
        if (LOG.isDebugEnabled()) {
            LOG.debug("init finished"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updateText() {
        final StringBuffer content = new StringBuffer();
        final FileObject root = model.getRootDir();
        final String ext = model.getExt();
        if (model.isPackage()) {
            content.append(PackageUtils.toAbsolutePath(root, cboPackage.getSelectedItem().toString(), true))
                    .append(System.getProperty("file.separator")) // NOI18N
            .append(PackageUtils.toRelativePath(
                    txtPackageName.getText(),
                    true));
        } else {
            content.append(PackageUtils.toAbsolutePath(root, cboPackage.getSelectedItem().toString(), true))
                    .append(System.getProperty("file.separator")) // NOI18N
            .append(txtPackageName.getText());
            if ((ext != null) && !ext.equals(""))                 // NOI18N
            {
                content.append('.').append(ext);                  // NOI18N
            }
        }
        txtNewFolder.setText(content.toString());
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewVisualPanel2.class,
                "NewVisualPanel2.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getDir() {
        return txtNewFolder.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getPackageName() {
        return txtPackageName.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getSelectedPackage() {
        return cboPackage.getSelectedItem().toString();
    }

    @Override
    public void insertUpdate(final DocumentEvent documentEvent) {
        changedUpdate(documentEvent);
    }

    @Override
    public void removeUpdate(final DocumentEvent documentEvent) {
        changedUpdate(documentEvent);
    }

    @Override
    public void changedUpdate(final DocumentEvent documentEvent) {
        updateText();
        model.fireChangeEvent();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(
            lblPackageName,
            org.openide.util.NbBundle.getMessage(NewVisualPanel2.class, "NewVisualPanel2.lblPackageName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            lblNewFolder,
            org.openide.util.NbBundle.getMessage(NewVisualPanel2.class, "NewVisualPanel2.lblNewFolder.text")); // NOI18N

        txtNewFolder.setBackground(new java.awt.Color(228, 226, 226));
        txtNewFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblPackage,
            org.openide.util.NbBundle.getMessage(NewVisualPanel2.class, "NewVisualPanel2.lblPackage.text")); // NOI18N

        cboPackage.setFocusable(false);

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        jSeparator1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        748,
                        Short.MAX_VALUE).add(
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(
                                lblPackage,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).add(
                                lblNewFolder,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).add(
                                lblPackageName,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                cboPackage,
                                0,
                                637,
                                Short.MAX_VALUE).add(
                                txtPackageName,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                637,
                                Short.MAX_VALUE).add(
                                txtNewFolder,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                637,
                                Short.MAX_VALUE)))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                        cboPackage,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(lblPackage)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                        txtPackageName,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(lblPackageName)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblNewFolder).add(
                        txtNewFolder,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    10,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(245, Short.MAX_VALUE)));
    } // </editor-fold>//GEN-END:initComponents
}

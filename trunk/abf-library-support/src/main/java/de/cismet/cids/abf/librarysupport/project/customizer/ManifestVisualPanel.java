/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.customizer;

import org.apache.log4j.Logger;

import org.openide.util.WeakListeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.4
 */
public final class ManifestVisualPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ManifestVisualPanel.class);

    //~ Instance fields --------------------------------------------------------

    private final transient LibrarySupportProject project;
    private final transient DocumentListener docL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JTextField basicManifestField = new javax.swing.JTextField();
    private final transient javax.swing.JLabel basicManifestLabel = new javax.swing.JLabel();
    private final transient javax.swing.JButton chooseButton = new javax.swing.JButton();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JTextArea previewArea = new javax.swing.JTextArea();
    private final transient javax.swing.JLabel previewLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ManifestVisualPanel object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ManifestVisualPanel(final LibrarySupportProject project) {
        this.project = project;
        docL = new DocumentListenerImpl();
        initComponents();
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void init() {
        basicManifestField.getDocument()
                .addDocumentListener(WeakListeners.document(docL, basicManifestField.getDocument()));
        final PropertyProvider provider = PropertyProvider.getInstance(project.getProjectProperties());
        final String basicManPath = provider.get(PropertyProvider.KEY_GENERAL_MANIFEST);
        basicManifestField.setText(basicManPath);
        previewArea.setEditable(false);
        updatePreview();
    }

    /**
     * DOCUMENT ME!
     */
    private void updatePreview() {
        final File manifest = new File(basicManifestField.getText());
        if (manifest.exists()
                    && manifest.isFile()
                    && manifest.getName().endsWith(".mf"))                  // NOI18N
        {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(manifest));
                final StringBuffer sb = new StringBuffer();
                while (in.ready()) {
                    sb.append(in.readLine()).append('\n');                  // NOI18N
                }
                previewArea.setText(sb.toString());
            } catch (final Exception ex) {
                previewArea.setText(org.openide.util.NbBundle.getMessage(
                        ManifestVisualPanel.class,
                        "ManifestVisualPanel.previewArea.text.noPreview")); // NOI18N
                LOG.warn("could not read manifest file", ex);               // NOI18N
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (final IOException ex) {
                    LOG.warn("could not close bufferedreader", ex);         // NOI18N
                }
            }
        } else {
            previewArea.setText(org.openide.util.NbBundle.getMessage(
                    ManifestVisualPanel.class,
                    "ManifestVisualPanel.previewArea.text.noValidFile"));   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JTextField getBasicManifestField() {
        return basicManifestField;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        basicManifestLabel.setText(org.openide.util.NbBundle.getMessage(
                ManifestVisualPanel.class,
                "ManifestVisualPanel.basicManifestLabel.text")); // NOI18N

        chooseButton.setText(org.openide.util.NbBundle.getMessage(
                ManifestVisualPanel.class,
                "ManifestVisualPanel.chooseButton.text")); // NOI18N
        chooseButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chooseButtonActionPerformed(evt);
                }
            });

        previewArea.setColumns(20);
        previewArea.setRows(5);
        jScrollPane1.setViewportView(previewArea);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(
                ManifestVisualPanel.class,
                "ManifestVisualPanel.previewLabel.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        599,
                        Short.MAX_VALUE).add(
                        layout.createSequentialGroup().add(basicManifestLabel).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            basicManifestField,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            397,
                            Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                            chooseButton)).add(previewLabel)).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(basicManifestLabel).add(
                        chooseButton).add(
                        basicManifestField,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(29, 29, 29).add(previewLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jScrollPane1,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    188,
                    Short.MAX_VALUE).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chooseButtonActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_chooseButtonActionPerformed
    {                                                                              //GEN-HEADEREND:event_chooseButtonActionPerformed
        final String path = basicManifestField.getText();
        File current = null;
        if ((path != null) && !path.equals(""))                                    // NOI18N
        {
            current = new File(path);
            if (current.exists()) {
                if (current.isFile()) {
                    current = current.getParentFile();
                }
            } else {
                current = new File(System.getProperty("user.home"));               // NOI18N
            }
        }
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(current);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

                @Override
                public boolean accept(final File file) {
                    return file.isDirectory()
                                || file.getName().endsWith(".mf"); // NOI18N
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            ManifestVisualPanel.class,
                            "ManifestVisualPanel.getDescription().returnvalue"); // NOI18N
                }
            });
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            basicManifestField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }                                                                            //GEN-LAST:event_chooseButtonActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void changedUpdate(final DocumentEvent documentEvent) {
            updatePreview();
        }

        @Override
        public void insertUpdate(final DocumentEvent documentEvent) {
            changedUpdate(documentEvent);
        }

        @Override
        public void removeUpdate(final DocumentEvent documentEvent) {
            changedUpdate(documentEvent);
        }
    }
}

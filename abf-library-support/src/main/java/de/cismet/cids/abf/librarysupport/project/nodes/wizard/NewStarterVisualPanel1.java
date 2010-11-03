/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.apache.log4j.Logger;

import org.openide.util.WeakListeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.7
 */
public final class NewStarterVisualPanel1 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewStarterVisualPanel1.class);

    //~ Instance fields --------------------------------------------------------

    private final transient NewStarterWizardPanel1 model;
    private final transient DocumentListener docL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JLabel basicManifestLabel = new javax.swing.JLabel();
    private final transient javax.swing.JButton chooseButton = new javax.swing.JButton();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JTextField manifestPathField = new javax.swing.JTextField();
    private final transient javax.swing.JTextField nameField = new javax.swing.JTextField();
    private final transient javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    private final transient javax.swing.JTextArea previewArea = new javax.swing.JTextArea();
    private final transient javax.swing.JLabel previewLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewStarterVisualPanel1 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public NewStarterVisualPanel1(final NewStarterWizardPanel1 model) {
        this.model = model;
        docL = new DocumentListenerImpl();
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        manifestPathField.getDocument()
                .addDocumentListener(WeakListeners.document(docL, manifestPathField.getDocument()));
        nameField.getDocument().addDocumentListener(WeakListeners.document(docL, manifestPathField.getDocument()));
        final PropertyProvider provider = PropertyProvider.getInstance(model.getProject().getProjectProperties());
        final String basicManPath = provider.get(PropertyProvider.KEY_GENERAL_MANIFEST);
        manifestPathField.setText(basicManPath);
        nameField.setText(org.openide.util.NbBundle.getMessage(
                NewStarterVisualPanel1.class,
                "NewStarterVisualPanel1.init().namefield.text")); // NOI18N
        nameField.setSelectionStart(0);
        nameField.setSelectionEnd(nameField.getText().length());
        previewArea.setEditable(false);
        updatePreview();
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewStarterVisualPanel1.class,
                "NewStarterVisualPanel1.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     */
    private void updatePreview() {
        final File manifest = new File(manifestPathField.getText());
        if (manifest.exists()
                    && manifest.isFile()
                    && manifest.getName().endsWith(".mf"))                           // NOI18N
        {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(manifest));
                final StringBuffer sb = new StringBuffer();
                while (in.ready()) {
                    sb.append(in.readLine()).append('\n');
                }
                previewArea.setText(sb.toString());
            } catch (final Exception ex) {
                previewArea.setText(org.openide.util.NbBundle.getMessage(
                        NewStarterVisualPanel1.class,
                        "NewStarterVisualPanel1.previewArea.text.noPreview"));       // NOI18N
                LOG.warn("could not read manifest file", ex);                        // NOI18N
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (final IOException ex) {
                    LOG.warn("could not close bufferedreader", ex);                  // NOI18N
                }
            }
        } else {
            previewArea.setText(org.openide.util.NbBundle.getMessage(
                    NewStarterVisualPanel1.class,
                    "NewStarterVisualPanel1.previewArea.text.noValidFileProvided")); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getManifestPath() {
        return manifestPathField.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getStarterName() {
        return nameField.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(
            basicManifestLabel,
            org.openide.util.NbBundle.getMessage(
                NewStarterVisualPanel1.class,
                "NewStarterVisualPanel1.basicManifestLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            chooseButton,
            org.openide.util.NbBundle.getMessage(
                NewStarterVisualPanel1.class,
                "NewStarterVisualPanel1.chooseButton.text")); // NOI18N
        chooseButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chooseButtonActionPerformed(evt);
                }
            });

        previewArea.setColumns(20);
        previewArea.setRows(5);
        jScrollPane1.setViewportView(previewArea);

        org.openide.awt.Mnemonics.setLocalizedText(
            previewLabel,
            org.openide.util.NbBundle.getMessage(
                NewStarterVisualPanel1.class,
                "NewStarterVisualPanel1.previewLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            nameLabel,
            org.openide.util.NbBundle.getMessage(
                NewStarterVisualPanel1.class,
                "NewStarterVisualPanel1.nameLabel.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                jScrollPane1,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                651,
                                Short.MAX_VALUE).add(previewLabel)).addContainerGap()).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
                                org.jdesktop.layout.GroupLayout.LEADING,
                                layout.createSequentialGroup().add(nameLabel).add(61, 61, 61).add(
                                    nameField,
                                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                    475,
                                    Short.MAX_VALUE)).add(
                                layout.createSequentialGroup().add(basicManifestLabel).addPreferredGap(
                                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                                    manifestPathField,
                                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                    473,
                                    Short.MAX_VALUE))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                            chooseButton).add(20, 20, 20)))));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(nameLabel).add(
                        nameField,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(basicManifestLabel).add(
                        chooseButton).add(
                        manifestPathField,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    26,
                    Short.MAX_VALUE).add(previewLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jScrollPane1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    313,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chooseButtonActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_chooseButtonActionPerformed
    {                                                                              //GEN-HEADEREND:event_chooseButtonActionPerformed
        final String path = manifestPathField.getText();
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
                            NewStarterVisualPanel1.class,
                            "NewStarterVisualPanel1.getDescription().returnvalue"); // NOI18N
                }
            });
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            manifestPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }                                                                               //GEN-LAST:event_chooseButtonActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void changedUpdate(final DocumentEvent documentEvent) {
            updatePreview();
            model.fireChangeEvent();
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

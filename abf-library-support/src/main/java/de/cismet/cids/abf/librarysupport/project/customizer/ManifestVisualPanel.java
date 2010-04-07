/*
 * ManifestVisualPanel.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 25. August 2007, 14:48
 */

package de.cismet.cids.abf.librarysupport.project.customizer;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
import org.openide.util.WeakListeners;

/**
 *
 * @author mscholl
 * @version 1.4
 */
public final class ManifestVisualPanel extends javax.swing.JPanel
{
    private static final transient Logger LOG = Logger.getLogger(
            ManifestVisualPanel.class);
    
    private final transient LibrarySupportProject project;
    private final transient DocumentListener docL;
    
    public ManifestVisualPanel(final LibrarySupportProject project)
    {
        this.project = project;
        docL = new DocumentListenerImpl();
        initComponents();
        init();
    }
    
    public void init()
    {
        basicManifestField.getDocument().addDocumentListener(WeakListeners.
                document(docL, basicManifestField.getDocument()));
        final PropertyProvider provider = PropertyProvider.getInstance(project.
                getProjectProperties());
        final String basicManPath = provider.get(PropertyProvider.
                KEY_GENERAL_MANIFEST);
        basicManifestField.setText(basicManPath);
        previewArea.setEditable(false);
        updatePreview();
    }
    
    private void updatePreview()
    {
        final File manifest = new File(basicManifestField.getText());
        if(manifest.exists()
                && manifest.isFile()
                && manifest.getName().endsWith(".mf")) // NOI18N
        {
            BufferedReader in = null;
            try
            {
                in = new BufferedReader(new FileReader(manifest));
                final StringBuffer sb = new StringBuffer();
                while(in.ready())
                {
                    sb.append(in.readLine()).append('\n'); // NOI18N
                }
                previewArea.setText(sb.toString());
            }catch(final Exception ex)
            {
                previewArea.setText(org.openide.util.NbBundle.getMessage(
                        ManifestVisualPanel.class, "Txt_noPreview")); // NOI18N
                LOG.warn("could not read manifest file", ex); // NOI18N
            } finally
            {
                try
                {
                    if(in != null)
                    {
                        in.close();
                    }
                }catch(final IOException ex)
                {
                    LOG.warn("could not close bufferedreader", ex); // NOI18N
                }
            }
        }else
        {
            previewArea.setText(org.openide.util.NbBundle.getMessage(
                    ManifestVisualPanel.class, "Txt_noValidFile")); // NOI18N
        }
    }

    public JTextField getBasicManifestField()
    {
        return basicManifestField;
    }

    class DocumentListenerImpl implements DocumentListener
    {
        @Override
        public void changedUpdate(final DocumentEvent documentEvent)
        {
            updatePreview();
        }

        @Override
        public void insertUpdate(final DocumentEvent documentEvent)
        {
            changedUpdate(documentEvent);
        }

        @Override
        public void removeUpdate(final DocumentEvent documentEvent)
        {
            changedUpdate(documentEvent);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        basicManifestLabel.setText(org.openide.util.NbBundle.getMessage(ManifestVisualPanel.class, "Lbl_standardManifest")); // NOI18N

        chooseButton.setText(org.openide.util.NbBundle.getMessage(ManifestVisualPanel.class, "Btn_browse")); // NOI18N
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });

        previewArea.setColumns(20);
        previewArea.setRows(5);
        jScrollPane1.setViewportView(previewArea);

        previewLabel.setText(org.openide.util.NbBundle.getMessage(ManifestVisualPanel.class, "Lbl_preview")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(basicManifestLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(basicManifestField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(chooseButton))
                    .add(previewLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(basicManifestLabel)
                    .add(chooseButton)
                    .add(basicManifestField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(29, 29, 29)
                .add(previewLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_chooseButtonActionPerformed
    {//GEN-HEADEREND:event_chooseButtonActionPerformed
        final String path = basicManifestField.getText();
        File current = null;
        if(path != null && !path.equals("")) // NOI18N
        {
            current = new File(path);
            if(current.exists())
            {
                if(current.isFile())
                {
                    current = current.getParentFile();
                }
            }else
            {
                current = new File(System.getProperty("user.home")); // NOI18N
            }
        }   
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(current);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
        {
            @Override
            public boolean accept(final File file)
            {
                return file.isDirectory() 
                        || file.getName().endsWith(".mf"); // NOI18N
            }

            @Override
            public String getDescription()
            {
                return org.openide.util.NbBundle.getMessage(
                        ManifestVisualPanel.class, "ManifestFile"); // NOI18N
            }
        });
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            basicManifestField.setText(chooser.getSelectedFile().
                    getAbsolutePath());
        }
    }//GEN-LAST:event_chooseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JTextField basicManifestField = new javax.swing.JTextField();
    private final transient javax.swing.JLabel basicManifestLabel = new javax.swing.JLabel();
    private final transient javax.swing.JButton chooseButton = new javax.swing.JButton();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JTextArea previewArea = new javax.swing.JTextArea();
    private final transient javax.swing.JLabel previewLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables
}

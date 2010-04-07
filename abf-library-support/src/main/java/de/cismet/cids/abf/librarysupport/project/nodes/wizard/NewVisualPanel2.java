/*
 * NewVisualPanel1.java, encoding: UTF-8
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
 * Created on ???
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import de.cismet.cids.abf.utilities.files.PackageUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mscholl
 * @version 1.7
 */
public final class NewVisualPanel2 extends JPanel implements DocumentListener
{
    private static final transient Logger LOG = Logger.getLogger(
            NewVisualPanel2.class);
    
    private final transient NewWizardPanel2 model;
    
    /** Creates new form NewPackageVisualPanel1 */
    public NewVisualPanel2(final NewWizardPanel2 model)
    {
        this.model = model;
        initComponents();
    }
    
    void init()
    {
        final FileObject root = model.getRootDir();
        final FileObject current = model.getCurrentDir();
        final LinkedList<String> packages = new LinkedList<String>();
        for(final Enumeration<? extends FileObject> e = root.getFolders(true); 
                e.hasMoreElements();)
        {
            packages.add(PackageUtils.toPackage(root, e.nextElement()));
        }
        Collections.sort(packages);
        packages.addFirst(PackageUtils.ROOT_PACKAGE);
        final DefaultComboBoxModel dcbmodel = new DefaultComboBoxModel(
                packages.toArray());
        dcbmodel.setSelectedItem(PackageUtils.toPackage(root, current));
        cboPackage.setModel(dcbmodel);
        String name = "";  // NOI18N
        final String ext = model.getExt();
        lblPackageName.setText(org.openide.util.NbBundle.getMessage(
                NewVisualPanel2.class, "Lbl_fileName")); // NOI18N
        lblNewFolder.setText(org.openide.util.NbBundle.getMessage(
                NewVisualPanel2.class, "Lbl_newFile")); // NOI18N
        if(model.isPackage())
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class, "Txt_newPackage"); // NOI18N
            lblPackageName.setText(org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class, "Lbl_packageName")); // NOI18N
            lblNewFolder.setText(org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class, "Lbl_newFolder")); // NOI18N
        }else if(ext == null || "".equals(ext)) // NOI18N
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class, "Txt_newFile"); // NOI18N
        }else if("properties".equals(ext)) // NOI18N
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class, "Txt_newPropertiesFile"); // NOI18N
        }else if("txt".equals(ext)) // NOI18N
        {
            name = org.openide.util.NbBundle.getMessage(
                    NewVisualPanel2.class, "Txt_newTextFile"); // NOI18N
        }
        txtPackageName.setText(name);
        txtPackageName.setSelectionStart(
                txtPackageName.getText().indexOf(name));
        txtPackageName.setSelectionEnd(txtPackageName.getText().length());
        txtPackageName.getDocument().addDocumentListener(this);
        cboPackage.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                updateText();
                model.fireChangeEvent();
            }
        });
        updateText();
        model.fireChangeEvent();
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init finished"); // NOI18N
        }
    }
    
    private void updateText()
    {
        final StringBuffer content = new StringBuffer();
        final FileObject root = model.getRootDir();
        final String ext = model.getExt();
        if(model.isPackage())
        {
            content.append(PackageUtils.toAbsolutePath(root, cboPackage.
                            getSelectedItem().toString(),true))
                    .append(System.getProperty("file.separator")) // NOI18N
                    .append(PackageUtils.toRelativePath(
                            txtPackageName.getText(), true));
        }
        else
        {
            content.append(PackageUtils.toAbsolutePath(root, cboPackage.
                            getSelectedItem().toString(),true))
                    .append(System.getProperty("file.separator")) // NOI18N
                    .append(txtPackageName.getText());
            if(ext != null && !ext.equals("")) // NOI18N
            {
                content.append('.').append(ext); // NOI18N
            }
        }
        txtNewFolder.setText(content.toString());
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewVisualPanel2.class, "Dsc_namePlace"); // NOI18N
    }
    
    String getDir()
    {
        return txtNewFolder.getText();
    }
    
    String getPackageName()
    {
        return txtPackageName.getText();
    }
    
    String getSelectedPackage()
    {
        return cboPackage.getSelectedItem().toString();
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

    @Override
    public void changedUpdate(final DocumentEvent documentEvent)
    {
        updateText();
        model.fireChangeEvent();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.openide.awt.Mnemonics.setLocalizedText(lblPackageName, org.openide.util.NbBundle.getMessage(NewVisualPanel2.class, "Lbl_packageName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblNewFolder, org.openide.util.NbBundle.getMessage(NewVisualPanel2.class, "Lbl_newFolder")); // NOI18N

        txtNewFolder.setBackground(new java.awt.Color(228, 226, 226));
        txtNewFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblPackage, org.openide.util.NbBundle.getMessage(NewVisualPanel2.class, "Lbl_package")); // NOI18N

        cboPackage.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(lblPackage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblNewFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblPackageName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cboPackage, 0, 619, Short.MAX_VALUE)
                            .add(txtPackageName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                            .add(txtNewFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cboPackage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblPackage))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtPackageName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblPackageName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNewFolder)
                    .add(txtNewFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(245, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboPackage = new javax.swing.JComboBox();
    private final transient javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
    private final transient javax.swing.JLabel lblNewFolder = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPackage = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPackageName = new javax.swing.JLabel();
    private final transient javax.swing.JTextField txtNewFolder = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtPackageName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
}
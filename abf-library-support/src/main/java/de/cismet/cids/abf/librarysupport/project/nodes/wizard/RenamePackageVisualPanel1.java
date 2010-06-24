/*
 * RenamePackageVisualPanel1.java, encoding: UTF-8
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mscholl
 * @version 1.5
 */
public final class RenamePackageVisualPanel1 extends JPanel implements
        DocumentListener
{
    private static final transient Logger LOG = Logger.getLogger(
            RenamePackageVisualPanel1.class);
    
    private final transient FileObject root;
    private final transient FileObject current;

    private final transient Set<ChangeListener> listeners;
    
    public RenamePackageVisualPanel1(final FileObject root, final FileObject 
            current)
    {
        assert root != null;
        assert current != null;
        this.root = root;
        this.current = current;
        listeners = new HashSet<ChangeListener>(1);
        initComponents();
        init();
        if(LOG.isDebugEnabled())
        {
            LOG.debug("init finished"); // NOI18N
        }
    }
    
    private void init()
    {
        txtPackageName.setText(PackageUtils.toPackage(root, current));
        txtPackageName.setSelectionStart(txtPackageName.getText().indexOf(
                current.getName()));
        txtPackageName.setSelectionEnd(txtPackageName.getText().length());
        txtPackageName.getDocument().addDocumentListener(this);
        updateText();
    }
    
    private void updateText()
    {
        txtNewFolder.setText(PackageUtils.toAbsolutePath(root, txtPackageName.
                getText(), true));
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                RenamePackageVisualPanel1.class, "RenamePackageVisualPanel1.getName().returnvalue");
    }
    
    String getPackage()
    {
        return txtPackageName.getText();
    }
    
    public void addChangeListener(final ChangeListener l) 
    {
        synchronized(listeners) 
        {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(final ChangeListener l) 
    {
        synchronized(listeners) 
        {
            listeners.remove(l);
        }
    }
    
    protected void fireChangeEvent()
    {
        final Iterator<ChangeListener> it;
        synchronized(listeners) 
        {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) 
        {
            it.next().stateChanged(ev);
        }
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
        fireChangeEvent();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.openide.awt.Mnemonics.setLocalizedText(lblPackageName, org.openide.util.NbBundle.getMessage(RenamePackageVisualPanel1.class, "RenamePackageVisualPanel1.lblPackageName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblNewFolder, org.openide.util.NbBundle.getMessage(RenamePackageVisualPanel1.class, "RenamePackageVisualPanel1.lblNewFolder.text")); // NOI18N

        txtNewFolder.setBackground(new java.awt.Color(228, 226, 226));
        txtNewFolder.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblNewFolder)
                            .add(lblPackageName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtNewFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE)
                            .add(txtPackageName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPackageName)
                    .add(txtPackageName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNewFolder)
                    .add(txtNewFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(278, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
    private final transient javax.swing.JLabel lblNewFolder = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPackageName = new javax.swing.JLabel();
    private final transient javax.swing.JTextField txtNewFolder = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtPackageName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
    
}


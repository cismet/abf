/*
 * URLEditorPanel.java, encoding: UTF-8
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
 * Created on 27.10.2009, 11:55:23
 */

package de.cismet.cids.abf.domainserver.project;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.common.URL;
import de.cismet.cids.jpa.entity.common.URLBase;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class URLEditorPanel extends javax.swing.JPanel implements
        TableModelListener,
        ListSelectionListener
{
    private final transient Backend backend;
    private final transient URLTableModel tModel;
    private final transient List<Integer> deletedRows;
    private final transient List<Integer> addedRows;
    private final transient List<Integer> editedRows;
    private final transient Map<Integer, URLBase> originalBases;

    private final transient Set<ChangeListener> listeners;

    private transient URL selectedURL;
    private transient boolean processed;


    public URLEditorPanel(final Backend backend)
    {
        initComponents();
        this.backend = backend;
        tModel = (URLTableModel)tblURLs.getModel();
        deletedRows = new ArrayList<Integer>();
        addedRows = new ArrayList<Integer>();
        editedRows = new ArrayList<Integer>();
        originalBases = new Hashtable<Integer, URLBase>();
        listeners = new HashSet<ChangeListener>();
        init();
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                updateTableStatus();
            }
        });
    }

    private void init()
    {
        final URLCellRenderer renderer = new URLCellRenderer();
        final TableColumnModel tcm = tblURLs.getColumnModel();
        for(int i = 0; i < tModel.getColumnCount(); ++i)
        {
            tcm.getColumn(i).setCellRenderer(renderer);
        }
        tblURLs.setShowGrid(true);
        tblURLs.setGridColor(Color.LIGHT_GRAY);
        tblURLs.getSelectionModel().addListSelectionListener(this);
        tModel.addTableModelListener(this);
        cmdSave.setEnabled(saveEnable());
    }

    @Override
    public void tableChanged(final TableModelEvent e)
    {
        if(e != null && TableModelEvent.INSERT == e.getType())
        {
            tblURLs.scrollRectToVisible(
                    tblURLs.getCellRect(e.getFirstRow(), e.getLastRow(), true));
            tblURLs.getSelectionModel()
                    .setSelectionInterval(e.getFirstRow(), e.getFirstRow());
            tblURLs.editCellAt(e.getFirstRow(), 2, new EventObject(tModel));
        }else if(e != null 
                && TableModelEvent.UPDATE == e.getType()
                && chkAutosave.isSelected()
                && !processed)
        {
            final int row = tblURLs.getEditingRow();
            if(row >= 0)
            {
                final URL url = tModel.getURL(row);
                if(url.getId() == null)
                {
                    // assume it is a new url
                    addedRows.add(row);
                }else
                {
                    // it is already present
                    editedRows.add(row);
                }
                processed = true;
                cmdSaveActionPerformed(null);
            }
        }
        updateTableStatus();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e)
    {
        final int selRow = tblURLs.getSelectedRow();
        final String s = org.openide.util.NbBundle.getMessage(
                URLEditorPanel.class, "URLEditorPanel.valueChanged(ListSelectionEvent).s"); // NOI18N
        if(selRow < 0)
        {
            selectedURL = null;
            lblSelectedURL.setText(s + org.openide.util.NbBundle.getMessage(
                    URLEditorPanel.class, "URLEditorPanel.lblSelectedURL.text.noneBrackets")); // NOI18N
        }else
        {
            selectedURL = tModel.getURL(selRow);
            final Integer id = selectedURL.getId();
            if(id == null)
            {
                lblSelectedURL.setText(s + org.openide.util.NbBundle.getMessage(
                        URLEditorPanel.class, "URLEditorPanel.lblSelectedURL.text.newURLBrackets"));// NOI18N
                selectedURL = null;
            }else
            {
                lblSelectedURL.setText(s + id);
            }
        }
        fireChangeEvent();
    }

    public URL getSelectedURL()
    {
        return selectedURL;
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
        final ChangeEvent ev = new ChangeEvent(this);
        synchronized(listeners)
        {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        while(it.hasNext())
        {
            it.next().stateChanged(ev);
        }
    }

    public static URL showURLEditorDialog(final Backend b)
    {
        final URLEditorPanel panel = new URLEditorPanel(b);
        class WizardPanel implements WizardDescriptor.Panel, ChangeListener
        {
            private final transient Set<ChangeListener> listeners =
                    new HashSet<ChangeListener>();

            @Override
            public Component getComponent()
            {
                // Sets step number of a component
                panel.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                        Integer.valueOf(1));
                // Sets steps names for a panel
                panel.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DATA,
                        org.openide.util.NbBundle.getMessage(
                            URLEditorPanel.class, "URLEditorPanel.showURLEditorDialog(Backend).WizardPanel.panel.PROP_CONTENT_DATA")); // NOI18N
                // Turn on subtitle creation on each step
                panel.putClientProperty(
                        WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.FALSE);
                // Show steps on the left side
                panel.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.FALSE);
                // Turn on numbering of all steps
                panel.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.FALSE);
                return panel;
            }

            @Override
            public HelpCtx getHelp()
            {
                return HelpCtx.DEFAULT_HELP;
            }

            @Override
            public void readSettings(final Object settings)
            {
            }

            @Override
            public void storeSettings(final Object settings)
            {
            }

            @Override
            public boolean isValid()
            {
                return panel.getSelectedURL() != null;
            }

            @Override
            public void addChangeListener(final ChangeListener l)
            {
                synchronized(listeners)
                {
                    listeners.add(l);
                }
            }

            @Override
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
                while(it.hasNext())
                {
                    it.next().stateChanged(ev);
                }
            }

            @Override
            public void stateChanged(final ChangeEvent e)
            {
                fireChangeEvent();
            }
        }
        final WizardPanel wp = new WizardPanel();
        panel.addChangeListener(wp);
        final WizardDescriptor wizard = 
                new WizardDescriptor(new WizardDescriptor.Panel[] {wp});
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled =
                wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        return cancelled ? null : panel.getSelectedURL();
    }
    
    private void updateTableStatus()
    {
        valueChanged(null);
        lblStatus.setText(org.openide.util.NbBundle.getMessage(
                URLEditorPanel.class, "URLEditorPanel.updateTableStatus().lblStatus.text") // NOI18N
                + tModel.getRowCount());
        cmdSave.setEnabled(saveEnable());
    }

    private boolean isEdited()
    {
        return !deletedRows.isEmpty()
                || !addedRows.isEmpty()
                || !editedRows.isEmpty();
    }

    private boolean saveEnable()
    {
        return isEdited() && !chkAutosave.isSelected();
    }

//    private boolean discardEdit()
//    {
//        if(!isEdited())
//            return true;
//        final int answer = JOptionPane.showConfirmDialog(
//                findParent(this),
//                "Es wurden Änderungen vorgenommen. Möchten Sie diese jetzt "
//                + "verwerfen?",
//                "Änderungen verwerfen",
//                JOptionPane.YES_NO_CANCEL_OPTION,
//                JOptionPane.QUESTION_MESSAGE);
//        return JOptionPane.YES_OPTION == answer;
//    }

    private boolean saveEdit()
    {
        if(!isEdited())
        {
            return false;
        }
        final int answer = JOptionPane.showConfirmDialog(
                findParent(this),
                org.openide.util.NbBundle.getMessage(
                    URLEditorPanel.class, "URLEditorPanel.saveEdit().JOptionPane.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    URLEditorPanel.class, "URLEditorPanel.saveEdit().JOptionPane.title"), // NOI18N
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return JOptionPane.YES_OPTION == answer;
    }

    private JFrame findParent(final Container c)
    {
        if(c instanceof JFrame)
        {
            return (JFrame)c;
        }else if(c.getParent() == null)
        {
            return null;
        }else
        {
            return findParent(c.getParent());
        }
    }

    private void clear(final boolean table)
    {
        processed = false;
        if(table)
        {
            tModel.clear();
        }
        deletedRows.clear();
        addedRows.clear();
        editedRows.clear();
        originalBases.clear();
        cmdSave.setEnabled(saveEnable());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlUrls.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.pnlUrls.border.title"))); // NOI18N

        tblURLs.setModel(new URLTableModel());
        jScrollPane1.setViewportView(tblURLs);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        cmdNewURL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/add_row.png"))); // NOI18N
        cmdNewURL.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdNewURL.text")); // NOI18N
        cmdNewURL.setToolTipText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdNewURL.toolTipText")); // NOI18N
        cmdNewURL.setFocusable(false);
        cmdNewURL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNewURL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNewURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdNewURLActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdNewURL);

        cmdRemoveURL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/remove_row.png"))); // NOI18N
        cmdRemoveURL.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdRemoveURL.text")); // NOI18N
        cmdRemoveURL.setToolTipText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdRemoveURL.toolTipText")); // NOI18N
        cmdRemoveURL.setFocusable(false);
        cmdRemoveURL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveURL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdRemoveURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveURLActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdRemoveURL);

        cmdSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/filesave.png"))); // NOI18N
        cmdSave.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdSave.text")); // NOI18N
        cmdSave.setToolTipText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdSave.toolTipText")); // NOI18N
        cmdSave.setFocusable(false);
        cmdSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdSave);

        chkAutosave.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.chkAutosave.text")); // NOI18N
        chkAutosave.setToolTipText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.chkAutosave.toolTipText")); // NOI18N
        chkAutosave.setFocusable(false);
        chkAutosave.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chkAutosave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        chkAutosave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAutosaveActionPerformed(evt);
            }
        });
        jToolBar1.add(chkAutosave);

        lblStatus.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.lblStatus.text")); // NOI18N

        lblSelectedURL.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.lblSelectedURL.text")); // NOI18N

        javax.swing.GroupLayout pnlUrlsLayout = new javax.swing.GroupLayout(pnlUrls);
        pnlUrls.setLayout(pnlUrlsLayout);
        pnlUrlsLayout.setHorizontalGroup(
            pnlUrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlUrlsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlUrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1145, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlUrlsLayout.createSequentialGroup()
                        .addComponent(lblSelectedURL, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 786, Short.MAX_VALUE)
                        .addComponent(lblStatus)))
                .addContainerGap())
        );
        pnlUrlsLayout.setVerticalGroup(
            pnlUrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUrlsLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlUrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStatus)
                    .addComponent(lblSelectedURL))
                .addContainerGap())
        );

        txtProtocol.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.txtProtocol.text")); // NOI18N

        txtServer.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.txtServer.text")); // NOI18N

        txtPath.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.txtPath.text")); // NOI18N

        txtObject.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.txtObject.text")); // NOI18N

        lblProtocol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblProtocol.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.lblProtocol.text")); // NOI18N

        lblServer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblServer.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.lblServer.text")); // NOI18N

        lblPath.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPath.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.lblPath.text")); // NOI18N

        lblObject.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblObject.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.lblObject.text")); // NOI18N

        cmdSearch.setMnemonic('S');
        cmdSearch.setText(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.cmdSearch.text")); // NOI18N
        cmdSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlUrls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblProtocol, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtProtocol, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtServer, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblPath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtPath, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblObject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtObject, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)))
                    .addComponent(cmdSearch, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProtocol)
                    .addComponent(lblServer)
                    .addComponent(lblPath)
                    .addComponent(lblObject))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtObject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(cmdSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlUrls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        pnlUrls.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(URLEditorPanel.class, "URLEditorPanel.pnlUrls.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cmdSearchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmdSearchActionPerformed
    {//GEN-HEADEREND:event_cmdSearchActionPerformed
        if(!isEdited() || !saveEdit())
        {
            final URL searchURL = new URL();
            final URLBase searchURLBase = new URLBase();
            searchURLBase.setProtocolPrefix(txtProtocol.getText());
            searchURLBase.setServer(txtServer.getText());
            searchURLBase.setPath(txtPath.getText());
            searchURL.setUrlbase(searchURLBase);
            searchURL.setObjectName(txtObject.getText());
            RequestProcessor.getDefault().post(new Runnable()
            {
                @Override
                public void run()
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            cmdSearch.setText(org.openide.util.NbBundle
                                    .getMessage(URLEditorPanel.class,
                                    "URLEditorPanel.cmdSearchActionPerformed(ActionEvent).cmdSearch.loading")); // NOI18N
                            cmdSearch.setEnabled(false);
                        }
                    });
                    final List<URL> result = backend.getURLsLikeURL(searchURL);
                    tModel.setContent(result);
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            cmdSearch.setText(org.openide.util.NbBundle
                                    .getMessage(URLEditorPanel.class,
                                    "URLEditorPanel.cmdSearchActionPerformed(ActionEvent).cmdSearch.search")); // NOI18N
                            cmdSearch.setEnabled(true);
                        }
                    });
                }
            });
            clear(true);
        }
    }//GEN-LAST:event_cmdSearchActionPerformed

    private void cmdRemoveURLActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmdRemoveURLActionPerformed
    {//GEN-HEADEREND:event_cmdRemoveURLActionPerformed
        final int[] rows = tblURLs.getSelectedRows();
        if(chkAutosave.isSelected())
        {
            for(final int row : rows)
            {
                deletedRows.add(row);
            }
            cmdSaveActionPerformed(evt);
        }else
        {
            final int[] backwardsRows = new int[rows.length];
            Arrays.sort(rows);
            // reverse the order
            for(int i = rows.length - 1, j = 0; i >= 0; --i, ++j)
            {
                backwardsRows[j] = rows[i];
            }
            tModel.delete(backwardsRows);
        }
        cmdSave.setEnabled(saveEnable());
    }//GEN-LAST:event_cmdRemoveURLActionPerformed

    private void cmdSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmdSaveActionPerformed
    {//GEN-HEADEREND:event_cmdSaveActionPerformed
        // TODO: validate edited and new rows
        //       is a totally empty url invalid?
        for(final Integer row : addedRows)
        {
            tModel.setURL(row, backend.storeURL(tModel.getURL(row)));
        }
        for(final Integer row : editedRows)
        {
            backend.storeURL(tModel.getURL(row));
        }
        Collections.sort(deletedRows, new Comparator<Integer>() 
        {
            @Override
            public int compare(final Integer i1, final Integer i2)
            {
                // sort backwards
                return i2 - i1;
            }
        });
        for(final Integer row : deletedRows)
        {
            backend.deleteURL(tModel.deleteURL(row));
        }
        if(!originalBases.isEmpty())
        {
            backend.deleteURLBasesIfUnused(
                    new ArrayList(originalBases.values()));
        }
        tModel.notifyListeners(new TableModelEvent(tModel));
        clear(false);
    }//GEN-LAST:event_cmdSaveActionPerformed

    private void cmdNewURLActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmdNewURLActionPerformed
    {//GEN-HEADEREND:event_cmdNewURLActionPerformed
        tModel.addNew();
        cmdSave.setEnabled(saveEnable());
    }//GEN-LAST:event_cmdNewURLActionPerformed

    private void chkAutosaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_chkAutosaveActionPerformed
    {//GEN-HEADEREND:event_chkAutosaveActionPerformed
        if(chkAutosave.isSelected())
        {
            final int answer = JOptionPane.showConfirmDialog(
                    findParent(this),
                    org.openide.util.NbBundle.getMessage(
                    URLEditorPanel.class, "URLEditorPanel.chkAutosaveActionPerformed(ActionEvent).JOptionPane.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                    URLEditorPanel.class, "URLEditorPanel.chkAutosaveActionPerformed(ActionEvent).JOptionPane.title"), // NOI18N
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if(JOptionPane.NO_OPTION == answer)
            {
                chkAutosave.setSelected(false);
            }else
            {
                cmdSaveActionPerformed(evt);
            }
        }
    }//GEN-LAST:event_chkAutosaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JCheckBox chkAutosave = new javax.swing.JCheckBox();
    private final transient javax.swing.JButton cmdNewURL = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdRemoveURL = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdSave = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdSearch = new javax.swing.JButton();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JToolBar jToolBar1 = new javax.swing.JToolBar();
    private final transient javax.swing.JLabel lblObject = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblPath = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblProtocol = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblSelectedURL = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblServer = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStatus = new javax.swing.JLabel();
    private final transient javax.swing.JPanel pnlUrls = new javax.swing.JPanel();
    private final transient javax.swing.JTable tblURLs = new javax.swing.JTable();
    private final transient javax.swing.JTextField txtObject = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtPath = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtProtocol = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtServer = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    
    private final class URLTableModel implements TableModel
    {
        private final transient List<URL> urls;
        private final transient Set<TableModelListener> listeners;
        
        URLTableModel()
        {
            urls = new ArrayList<URL>();
            listeners = new HashSet<TableModelListener>();
        }

        void setContent(final Collection<URL> urls)
        {
            this.urls.clear();
            this.urls.addAll(urls);
            notifyListeners(new TableModelEvent(this));
        }

        void clear()
        {
            this.urls.clear();
            notifyListeners(new TableModelEvent(this));
        }

        void delete(final int[] rows)
        {
            if(chkAutosave.isSelected())
            {
                throw new UnsupportedOperationException(
                        "do not call if autosave is active"); // NOI18N
            }else
            {
                for(final int row : rows)
                {
                    if(addedRows.contains(row))
                    {
                        addedRows.remove((Integer)row);
                        urls.remove(row);
                    }else if(editedRows.contains(row))
                    {
                        editedRows.remove((Integer)row);
                        if(originalBases.containsKey(row))
                        {
                            urls.get(row).setUrlbase(originalBases.remove(row));
                        }
                        deletedRows.add(row);
                    }else if(!deletedRows.contains(row))
                    {
                        deletedRows.add(row);
                    }
                }
                notifyListeners(new TableModelEvent(this));
            }
        }

        void addNew()
        {
            final URL newUrl = new URL();
            final URLBase newUrlbase = new URLBase();
            newUrlbase.setProtocolPrefix("http://"); // NOI18N
            newUrlbase.setServer(""); // NOI18N
            newUrlbase.setPath(""); // NOI18N
            newUrl.setUrlbase(newUrlbase);
            newUrl.setObjectName(""); // NOI18N
            addedRows.add(urls.size());
            urls.add(newUrl);
            notifyListeners(new TableModelEvent(
                    this,
                    urls.size() - 1,
                    urls.size() - 1,
                    TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.INSERT));
        }

        URL getURL(final int row)
        {
            return urls.get(row);
        }

        URL deleteURL(final int row)
        {
            final URL url = urls.remove(row);
            notifyListeners(new TableModelEvent(
                    this,
                    row,
                    row,
                    TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.DELETE));
            return url;
        }

        void setURL(final int row, final URL url)
        {
            urls.set(row, url);
            notifyListeners(new TableModelEvent(
                    this,
                    row,
                    row,
                    0,
                    TableModelEvent.UPDATE));
        }

        @Override
        public int getRowCount()
        {
            return urls.size();
        }

        @Override
        public int getColumnCount()
        {
            return 5;
        }

        @Override
        public String getColumnName(final int columnIndex)
        {
            switch(columnIndex)
            {
                case 0:
                    return org.openide.util.NbBundle.getMessage(
                            URLEditorPanel.class, "URLEditorPanel.getColumnName(int).case0"); // NOI18N
                case 1:
                    return org.openide.util.NbBundle.getMessage(
                            URLEditorPanel.class, "URLEditorPanel.getColumnName(int).case1"); // NOI18N
                case 2:
                    return org.openide.util.NbBundle.getMessage(
                            URLEditorPanel.class, "URLEditorPanel.getColumnName(int).case2"); // NOI18N
                case 3:
                    return org.openide.util.NbBundle.getMessage(
                            URLEditorPanel.class, "URLEditorPanel.getColumnName(int).case3"); // NOI18N
                case 4:
                    return org.openide.util.NbBundle.getMessage(
                            URLEditorPanel.class, "URLEditorPanel.getColumnName(int).case4"); // NOI18N
                default:
                    throw new IllegalArgumentException(
                            "unknown column: " + columnIndex); // NOI18N
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex)
        {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            return columnIndex != 0;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex)
        {
            final URL url = urls.get(rowIndex);
            switch(columnIndex)
            {
                case 0:
                    return url.getId();
                case 1:
                    return url.getUrlbase().getProtocolPrefix();
                case 2:
                    return url.getUrlbase().getServer();
                case 3:
                    return url.getUrlbase().getPath();
                case 4:
                    return url.getObjectName();
                default:
                    throw new IllegalArgumentException(
                            "unknown column: " + columnIndex); // NOI18N
            }
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, 
                final int columnIndex)
        {
            if(!(aValue instanceof String))
            {
                throw new IllegalArgumentException(
                        "only String is supported: " + aValue); // NOI18N
            }
            final String value = (String)aValue;
            final URL url = urls.get(rowIndex);
            final URLBase urlbase = url.getUrlbase();
            switch(columnIndex)
            {
                case 0:
                    throw new IllegalArgumentException(
                            "cannot edit uneditable column: " // NOI18N
                            + columnIndex);
                case 1:
                    if(!originalBases.containsKey(rowIndex))
                    {
                        originalBases.put(rowIndex, urlbase);
                    }
                    url.setUrlbase(copyOf(urlbase));
                    url.getUrlbase().setProtocolPrefix(value);
                    break;
                case 2:
                    if(!originalBases.containsKey(rowIndex))
                    {
                        originalBases.put(rowIndex, urlbase);
                    }
                    url.setUrlbase(copyOf(urlbase));
                    url.getUrlbase().setServer(value);
                    break;
                case 3:
                    if(!originalBases.containsKey(rowIndex))
                    {
                        originalBases.put(rowIndex, urlbase);
                    }
                    url.setUrlbase(copyOf(urlbase));
                    url.getUrlbase().setPath(value);
                    break;
                case 4:
                    url.setObjectName(value);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "unknown column: " + columnIndex); // NOI18N
            }
            if(deletedRows.contains(rowIndex))
            {
                deletedRows.remove((Integer)rowIndex);
            }
            if(!addedRows.contains(rowIndex))
            {
                editedRows.add(rowIndex);
            }
            notifyListeners(new TableModelEvent(
                    this,
                    rowIndex,
                    rowIndex,
                    columnIndex,
                    TableModelEvent.UPDATE));
        }

        void notifyListeners(final TableModelEvent tme)
        {
            final Iterator<TableModelListener> it;
            synchronized(listeners)
            {
                it = new HashSet<TableModelListener>(listeners).iterator();
            }
            while(it.hasNext())
            {
                it.next().tableChanged(tme);
            }
        }

        @Override
        public void addTableModelListener(final TableModelListener l)
        {
            synchronized(listeners)
            {
                listeners.add(l);
            }
        }

        @Override
        public void removeTableModelListener(final TableModelListener l)
        {
            synchronized(listeners)
            {
                listeners.remove(l);
            }
        }

        private URLBase copyOf(final URLBase urlbase)
        {
            final URLBase newbase = new URLBase();
            newbase.setProtocolPrefix(urlbase.getProtocolPrefix());
            newbase.setServer(urlbase.getServer());
            newbase.setPath(urlbase.getPath());
            return newbase;
        }
    }

    private final class URLCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(
                final JTable table,
                final Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column)
        {
            final Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if(c instanceof JLabel)
            {
                final JLabel label = (JLabel)c;
                if(isSelected)
                {
                    if(deletedRows.contains(row))
                    {
                        label.setBorder(
                                BorderFactory.createLineBorder(Color.RED));
                    }else if(addedRows.contains(row))
                    {
                        label.setBorder(
                                BorderFactory.createLineBorder(Color.GREEN));
                    }else if(editedRows.contains(row))
                    {
                        label.setBorder(
                                BorderFactory.createLineBorder(Color.ORANGE));
                    }else
                    {
                        label.setBorder(BorderFactory.createEmptyBorder());
                    }
                }else
                {
                    if(deletedRows.contains(row))
                    {
                        label.setBackground(Color.RED);
                    }else if(addedRows.contains(row))
                    {
                        label.setBackground(Color.GREEN);
                    }else if(editedRows.contains(row))
                    {
                        label.setBackground(Color.ORANGE);
                    }else
                    {
                        label.setBackground(tblURLs.getBackground());
                    }
                }
            }
            return c;
        }
    }
}
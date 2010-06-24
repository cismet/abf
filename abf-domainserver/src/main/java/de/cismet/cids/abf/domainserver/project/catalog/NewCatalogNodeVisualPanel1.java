/*
 * NavigatorNodeManagementContextCookie.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.URLEditorPanel;
import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.common.URL;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.EditorKit;
import org.apache.log4j.Logger;
import org.openide.util.ImageUtilities;

/**
 *
 * @author martin.scholl@cismet.de
 * @version 1.8
 */
public final class NewCatalogNodeVisualPanel1 extends JPanel implements Observer
{
    private static final transient Logger LOG = Logger.getLogger(
            NewCatalogNodeVisualPanel1.class);

    public static final String NO_OBJECT = org.openide.util.NbBundle.getMessage(
            NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.NO_OBJECT"); // NOI18N
    
    private final transient NewCatalogNodeWizardPanel1 model;
    private final transient CachingURLComboBoxModel cachingURLModel;
    private final transient Icon loadingIcon;
    private final transient Icon loadingErrIcon;
    private transient CatNode catNode;
    
    public NewCatalogNodeVisualPanel1(final NewCatalogNodeWizardPanel1 model)
    {
        this.model = model;
        this.cachingURLModel = new CachingURLComboBoxModel();
        loadingIcon = new ImageIcon(ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "wait_16.png")); // NOI18N
        loadingErrIcon = new ImageIcon(ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "error_16.png")); // NOI18N
        initComponents();
    }
    
    String getNodeName()
    {
        return txtNodeName.getText();
    }
    
    CatNode getCatNode()
    {
        if(catNode == null)
        {
            catNode = new CatNode();
        }
        catNode.setName(txtNodeName.getText());
        try
        {
            catNode.setObjectId(Integer.parseInt(txtObject.getText()));
        } catch (final NumberFormatException ex)
        {
            catNode.setObjectId(null);
        }
        URL url = (URL)cboDesc.getSelectedItem();
        if(url.equals(URL.NO_DESCRIPTION))
        {
            url = null;
        }
        catNode.setUrl(url);
        catNode.setNodeType(cboNodeType.getSelectedItem().toString());
        final CidsClass cc = (CidsClass)cboClass.getSelectedItem();
        catNode.setCidsClass((cc.equals(CidsClass.NO_CLASS)) ? null : cc);
        String dynChildren = edpDynChild.getText();
        if("".equals(dynChildren)) // NOI18N
        {
            dynChildren = null;
        }
        catNode.setDynamicChildren(dynChildren);
        catNode.setSqlSort(cboSqlSort.isSelected());
        return catNode;
    }
    
    Domain getLinkDomain()
    {
        return (Domain)cboDomain.getSelectedItem();
    }

    private void init()
    {
        catNode = model.getCatNode();
        final JLabel lblLoading = new JLabel(
                org.openide.util.NbBundle.getMessage(
                    NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.init().lblLoading.text")); // NOI18N
        lblLoading.setIcon(loadingIcon);
        final JLabel lblLoadingErr = new JLabel(
                org.openide.util.NbBundle.getMessage(
                    NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.init().lblLoadingErr")); // NOI18N
        lblLoadingErr.setIcon(loadingErrIcon);
        final ListCellRenderer renderer = new Renderers.UnifiedCellRenderer();
        // set up classes combo box
        final DefaultComboBoxModel dcbmClass = (DefaultComboBoxModel)
                cboClass.getModel();
        final ItemListener itemLClass = new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent e)
            {
                if(ItemEvent.SELECTED == e.getStateChange()
                        && !(e.getItem() instanceof CidsClass))
                {
                    dcbmClass.setSelectedItem(CidsClass.NO_CLASS);
                }
            }
        };
        dcbmClass.removeAllElements();
        dcbmClass.addElement(CidsClass.NO_CLASS);
        dcbmClass.addElement(lblLoading);
        cboClass.setRenderer(renderer);
        cboClass.addItemListener(itemLClass);
        cboClass.setSelectedIndex(0);
        final Thread loadClassesThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final List<CidsClass> classes = model.getBackend()
                            .getAllEntities(CidsClass.class);
                    Collections.sort(classes, new Comparators.CidsClasses());
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dcbmClass.removeAllElements();
                            dcbmClass.addElement(CidsClass.NO_CLASS);
                            for(final CidsClass c : classes)
                            {
                                dcbmClass.addElement(c);
                            }
                            if(model.getCatNode().getCidsClass() != null)
                            {
                                cboClass.setSelectedItem(
                                        model.getCatNode().getCidsClass());
                            }
                        }
                    });
                    cboClass.removeItemListener(itemLClass);
                }catch(final Exception e)
                {
                    LOG.error("could not load cidsClasses", e); // NOI18N
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dcbmClass.removeElementAt(1);
                            dcbmClass.addElement(lblLoadingErr);
                        }
                    });
                }finally
                {
                    pack();
                }
            }
        });
        loadClassesThread.start();
        // set up description combo box
        cboDesc.setModel(cachingURLModel);
        cboDesc.setRenderer(renderer);
        cboDesc.setSelectedIndex(0);
        // set up domains combo box
        final DefaultComboBoxModel dcbmDomains = (DefaultComboBoxModel)
                cboDomain.getModel();
        dcbmDomains.removeAllElements();
        cboDomain.setRenderer(renderer);
        // domains cannot be loaded in background because a domain MUST be
        // selected
        final List<Domain> domains = model.getBackend().getAllEntities(Domain.
                class);
        Collections.sort(domains, new Comparators.Domains());
        for(final Domain d : domains)
        {
            dcbmDomains.addElement(d);
        }
        // set up node type combo box
        final DefaultComboBoxModel dcbmNodeTypes = (DefaultComboBoxModel)
                cboNodeType.getModel();
        dcbmNodeTypes.removeAllElements();
        final String[] types = new String[] {"C", "N", "O"}; // NOI18N
        for(final String s : types)
        {
            dcbmNodeTypes.addElement(s);
        }
        cboNodeType.setRenderer(new Renderers.NodeTypeRenderer());
        cboNodeType.setSelectedItem("N"); // NOI18N

        txtObject.setText(NO_OBJECT);
        txtNodeName.setText(org.openide.util.NbBundle.getMessage(
                NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.txtNodeName.text")); // NOI18N
        edpDynChild.setContentType("text/x-sql"); // NOI18N
        final EditorKit kit = JEditorPane.createEditorKitForContentType(
                "text/x-sql"); // NOI18N
        kit.install(edpDynChild);
        edpDynChild.setEditorKit(kit);
        edpDynChild.setText(""); // NOI18N
        cboSqlSort.setSelected(false);
        if(catNode.getName() != null)
        {
            initValues();
        }
        if(catNode.getIsRoot())
        {
            cboDomain.setSelectedItem(org.openide.util.NbBundle.getMessage(
                    NewCatalogNodeVisualPanel1.class,
                    "NewCatalogNodeVisualPanel1.init().cboDomain.selectedItem")); // NOI18N
            cboDomain.setEnabled(false);
            lblDomain.setEnabled(false);
        }else
        {
            lblDynChild.setEnabled(false);
            edpDynChild.setEnabled(false);
            cboSqlSort.setEnabled(false);
        }
        txtNodeName.getDocument().addDocumentListener(new
                DocumentListenerImpl());
    }

    private void pack()
    {
        Container c = this;
        while(c != null)
        {
            if(c instanceof Window)
            {
                final Window w = (Window)c;
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        w.pack();
                    }
                });
                return;
            }
            c = c.getParent();
        }
    }

    private void initValues()
    {
        txtNodeName.setText(catNode.getName());
        txtObject.setText(String.valueOf(catNode.getObjectId()));
        final CidsClass cc = catNode.getCidsClass();
        if(cc != null)
        {
            final DefaultComboBoxModel dcbm =
                    (DefaultComboBoxModel)cboClass.getModel();
            if(dcbm.getIndexOf(cc) < 0)
            {
                dcbm.addElement(cc);
            }
            cboClass.setSelectedItem(cc);
        }
        final URL url = catNode.getUrl();
        if(url != null)
        {
            final DefaultComboBoxModel dcbm  =
                    (DefaultComboBoxModel)cboDesc.getModel();
            if(dcbm.getIndexOf(url) < 0)
            {
                dcbm.addElement(url);
            }
            cboDesc.setSelectedItem(url);
        }
        cboNodeType.setSelectedItem(catNode.getNodeType());
        Boolean selected = catNode.getSqlSort();
        if(selected == null)
        {
            selected = Boolean.FALSE;
        }
        cboSqlSort.setSelected(selected);
        edpDynChild.setText(catNode.getDynamicChildren());
        if(model.getLinkDomain() != null)
        {
            cboDomain.setSelectedItem(model.getLinkDomain());
        }
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.getName().returnvalue"); // NOI18N
    }
    
    @Override
    public void update(final Observable o, final Object arg)
    {
        if(o instanceof NewCatalogNodeWizardPanel1)
        {
            init();
        }
    }
    
    private final class DocumentListenerImpl implements DocumentListener
    {
        @Override
        public void changedUpdate(final DocumentEvent e)
        {
            model.nameChanged();
        }

        @Override
        public void removeUpdate(final DocumentEvent e)
        {
            changedUpdate(e);
        }

        @Override
        public void insertUpdate(final DocumentEvent e)
        {
            changedUpdate(e);
        }
    }

    private final class CachingURLComboBoxModel extends DefaultComboBoxModel
    {
        private final transient JLabel lblSelect;

        CachingURLComboBoxModel()
        {
            lblSelect = new JLabel(org.openide.util.NbBundle.getMessage(
                    NewCatalogNodeVisualPanel1.class,
                    "NewCatalogNodeVisualPanel1.CachingURLComboBoxModel.lblSelect.text")); // NOI18N
            addElement(URL.NO_DESCRIPTION);
            addElement(lblSelect);
        }
        
        @Override
        public void setSelectedItem(final Object anObject)
        {
            // it is the selection item
            if(anObject.equals(lblSelect))
            {
                final URL newURL = URLEditorPanel
                        .showURLEditorDialog(model.getBackend());
                if(newURL == null)
                {
                    super.setSelectedItem(URL.NO_DESCRIPTION);
                }else
                {
                    if(getIndexOf(newURL) < 0)
                    {
                        addElement(newURL);
                    }
                    setSelectedItem(newURL);
                }
            }else
            {
                super.setSelectedItem(anObject);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.openide.awt.Mnemonics.setLocalizedText(lblNodeName, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblNodeName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblDesc, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblDesc.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblClass, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblClass.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblObject, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblObject.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblNodeType, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblNodeType.text")); // NOI18N

        jScrollPane1.setViewportView(edpDynChild);

        org.openide.awt.Mnemonics.setLocalizedText(lblDynChild, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblDynChild")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cboSqlSort, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.cboSqlSort.text")); // NOI18N
        cboSqlSort.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cboSqlSort.setIconTextGap(15);

        org.openide.awt.Mnemonics.setLocalizedText(lblDomain, org.openide.util.NbBundle.getMessage(NewCatalogNodeVisualPanel1.class, "NewCatalogNodeVisualPanel1.lblDomain.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblNodeName)
                        .add(26, 26, 26)
                        .add(txtNodeName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(lblDesc)
                                .add(cboDesc, 0, 300, Short.MAX_VALUE)
                                .add(cboNodeType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(lblNodeType))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 106, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(txtObject)
                            .add(lblObject)
                            .add(layout.createSequentialGroup()
                                .add(lblClass)
                                .add(259, 259, 259))
                            .add(cboClass, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                    .add(lblDynChild)
                    .add(cboSqlSort)
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblDomain)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cboDomain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 264, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNodeName)
                    .add(txtNodeName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblDesc)
                    .add(lblClass))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cboClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cboDesc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNodeType)
                    .add(lblObject))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtObject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cboNodeType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblDynChild)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(16, 16, 16)
                .add(cboSqlSort)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblDomain)
                    .add(cboDomain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboClass = new javax.swing.JComboBox();
    private final transient javax.swing.JComboBox cboDesc = new javax.swing.JComboBox();
    private final transient javax.swing.JComboBox cboDomain = new javax.swing.JComboBox();
    private final transient javax.swing.JComboBox cboNodeType = new javax.swing.JComboBox();
    private final transient javax.swing.JCheckBox cboSqlSort = new javax.swing.JCheckBox();
    private final transient javax.swing.JEditorPane edpDynChild = new javax.swing.JEditorPane();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
    private final transient javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
    private final transient javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
    private final transient javax.swing.JLabel lblClass = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblDesc = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblDomain = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblDynChild = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblNodeName = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblNodeType = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblObject = new javax.swing.JLabel();
    private final transient javax.swing.JTextField txtNodeName = new javax.swing.JTextField();
    private final transient javax.swing.JTextField txtObject = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
}

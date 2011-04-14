/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXTable;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import de.cismet.cids.jpa.entity.cidsclass.Type;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class NewCidsClassVisualPanel3 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(NewCidsClassVisualPanel3.class);

    private static final int KEY = 0;
    private static final int VALUE = 1;

    private static final URI WIKI_URI;

    static {
        URI uri = null;
        try {
            uri = new URI("http://wiki.cismet.de"); // NOI18N
        } catch (final URISyntaxException e) {
            LOG.error("illegal uri: " + uri, e);    // NOI18N
        }

        WIKI_URI = uri;
    }

    //~ Instance fields --------------------------------------------------------

    private final transient NewCidsClassWizardPanel3 model;
    private transient CidsClass cidsClass;
    private transient ClassAttrTableModel tableModel;
    private transient FutureTask<Type> typeCache;

    private final transient ActionListener hypL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton cmdNewClassAttr = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdRemoveClassAttr = new javax.swing.JButton();
    private final transient org.jdesktop.swingx.JXHyperlink hypAvailableAttrs = new org.jdesktop.swingx.JXHyperlink();
    private final transient javax.swing.JLabel lblAvailableAttributes = new javax.swing.JLabel();
    private final transient javax.swing.JScrollPane scpClassAttr = new javax.swing.JScrollPane();
    private final transient javax.swing.JTable tblClassAttr = new JXTable();
    private final transient javax.swing.JToolBar tboClassAttr = new javax.swing.JToolBar();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewCidsClassVisualPanel3 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public NewCidsClassVisualPanel3(final NewCidsClassWizardPanel3 model) {
        this.model = model;
        this.hypL = new HyperlinkListener();

        initComponents();

        final JXTable classAttrTable = (JXTable)tblClassAttr;
        classAttrTable.setAutoStartEditOnKeyStroke(true);
        classAttrTable.setTerminateEditOnFocusLost(true);
        classAttrTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
        classAttrTable.getDefaultEditor(String.class).addCellEditorListener(new CellEditorListener() {

                @Override
                public void editingCanceled(final ChangeEvent e) {
                    model.fireChangeEvent();
                }

                @Override
                public void editingStopped(final ChangeEvent e) {
                    model.fireChangeEvent();
                }
            });

        hypAvailableAttrs.addActionListener(WeakListeners.create(ActionListener.class, hypL, hypAvailableAttrs));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        cidsClass = model.getCidsClass();
        tableModel = new ClassAttrTableModel(cidsClass.getClassAttributes());
        tblClassAttr.setModel(tableModel);
        typeCache = new FutureTask(new Callable<Type>() {

                    @Override
                    public Type call() throws Exception {
                        final Backend b = model.getProject().getCidsDataObjectBackend();
                        final List<Type> types = b.getAllEntities(Type.class);

                        return types.get(0);
                    }
                });
        RequestProcessor.getDefault().post(typeCache);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NewCidsClassVisualPanel3.class, "NewCidsClassVisualPanel3.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsClass getCidsClass() {
        final Set<ClassAttribute> set = cidsClass.getClassAttributes();
        set.clear();
        for (final ClassAttribute ca : tableModel.cas) {
            set.add(ca);
        }

        return cidsClass;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        scpClassAttr.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassVisualPanel3.class,
                    "NewCidsClassVisualPanel3.scpClassAttr.border.title"))); // NOI18N
        scpClassAttr.setPreferredSize(new java.awt.Dimension(130, 296));
        scpClassAttr.setViewportView(tblClassAttr);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 584;
        gridBagConstraints.ipady = 230;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(scpClassAttr, gridBagConstraints);

        tboClassAttr.setFloatable(false);

        cmdNewClassAttr.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cids/abf/domainserver/images/add_row.png"))); // NOI18N
        cmdNewClassAttr.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdNewClassAttrActionPerformed(evt);
                }
            });
        tboClassAttr.add(cmdNewClassAttr);

        cmdRemoveClassAttr.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cids/abf/domainserver/images/remove_row.png"))); // NOI18N
        cmdRemoveClassAttr.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveClassAttrActionPerformed(evt);
                }
            });
        tboClassAttr.add(cmdRemoveClassAttr);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 391;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(tboClassAttr, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            hypAvailableAttrs,
            NbBundle.getMessage(NewCidsClassVisualPanel3.class, "NewCidsClassVisualPanel3.hypAvailableAttrs.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(hypAvailableAttrs, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblAvailableAttributes,
            NbBundle.getMessage(
                NewCidsClassVisualPanel3.class,
                "NewCidsClassVisualPanel3.lblAvailableAttributes.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblAvailableAttributes, gridBagConstraints);
    }                                                                     // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdNewClassAttrActionPerformed(final java.awt.event.ActionEvent evt) {                                              //GEN-FIRST:event_cmdNewClassAttrActionPerformed
        final ClassAttribute ca = new ClassAttribute();
        ca.setCidsClass(cidsClass);
        ca.setAttrKey("");                                                                                                           // NOI18N
        try {
            ca.setType(typeCache.get());
        } catch (final InterruptedException ex) {
            LOG.error("type retrieval was interrupted", ex);                                                                         // NOI18N
            ErrorUtils.showErrorMessage(
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassVisualPanel3.class,
                    "NewCidsClassVisualPanel3.cmdNewClassAttrActionPerformed(ActionEvent).InterruptedException.ErrorUtils.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassVisualPanel3.class,
                    "NewCidsClassVisualPanel3.cmdNewClassAttrActionPerformed(ActionEvent).InterruptedException.ErrorUtils.title"),   // NOI18N
                ex);

            return;
        } catch (final ExecutionException ex) {
            LOG.error("error during type loading", ex);                                                                            // NOI18N
            ErrorUtils.showErrorMessage(
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassVisualPanel3.class,
                    "NewCidsClassVisualPanel3.cmdNewClassAttrActionPerformed(ActionEvent).ExecutionException.ErrorUtils.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    NewCidsClassVisualPanel3.class,
                    "NewCidsClassVisualPanel3.cmdNewClassAttrActionPerformed(ActionEvent).ExecutionException.ErrorUtils.title"),   // NOI18N
                ex);

            return;
        }
        ca.setAttrValue("");                            // NOI18N
        tableModel.addClassAttribute(ca);
        model.fireChangeEvent();
        final JXTable jxt = (JXTable)tblClassAttr;
        jxt.setSortOrder(0, SortOrder.ASCENDING);
        for (int i = 0; i < tableModel.getRowCount(); ++i) {
            if ("".equals(tableModel.getValueAt(i, 0))) // NOI18N
            {
                tblClassAttr.requestFocus();
                tblClassAttr.editCellAt(jxt.convertRowIndexToView(i), 0);
                break;
            }
        }
    }                                                   //GEN-LAST:event_cmdNewClassAttrActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveClassAttrActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveClassAttrActionPerformed
        final int selectedRow = tblClassAttr.getSelectedRow();
        final JXTable table = (JXTable)tblClassAttr;
        if (selectedRow >= 0) {
            final int modelRow = table.convertRowIndexToModel(selectedRow);
            tableModel.removeClassAttribute(modelRow);
            final int rc = tblClassAttr.getRowCount();
            if (rc > 0) {
                if ((rc - 1) >= selectedRow) {
                    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                } else {
                    table.getSelectionModel().setSelectionInterval(rc - 1, rc - 1);
                }
            }
        }
        model.fireChangeEvent();
    }                                                                                      //GEN-LAST:event_cmdRemoveClassAttrActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class HyperlinkListener implements ActionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(WIKI_URI);
                } catch (final IOException ex) {
                    LOG.error("cannot open uri", ex);                                                       // NOI18N
                    JOptionPane.showMessageDialog(
                        NewCidsClassVisualPanel3.this,
                        NbBundle.getMessage(
                                    NewCidsClassVisualPanel3.class,
                                    "HyperlinkListener.actionPerformed(ActionEvent).cannotGotoURL.message") // NOI18N
                                + WIKI_URI,
                        NbBundle.getMessage(
                            NewCidsClassVisualPanel3.class,
                            "HyperlinkListener.actionPerformed(ActionEvent).cannotGotoURL.title"),          // NOI18N
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(
                    NewCidsClassVisualPanel3.this,
                    NbBundle.getMessage(
                                NewCidsClassVisualPanel3.class,
                                "HyperlinkListener.actionPerformed(ActionEvent).cannotOpenBrowser.message") // NOI18N
                            + WIKI_URI,
                    NbBundle.getMessage(
                        NewCidsClassVisualPanel3.class,
                        "HyperlinkListener.actionPerformed(ActionEvent).cannotOpenBrowser.title"),          // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ClassAttrTableModel extends AbstractTableModel {

        //~ Instance fields ----------------------------------------------------

        private final List<ClassAttribute> cas;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ClassAttrTableModel object.
         *
         * @param  cas  DOCUMENT ME!
         */
        ClassAttrTableModel(final Collection<ClassAttribute> cas) {
            this.cas = new ArrayList<ClassAttribute>();
            this.cas.addAll(cas);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Object getValueAt(final int row, final int column) {
            switch (column) {
                case KEY: {
                    return cas.get(row).getAttrKey();
                }
                case VALUE: {
                    return cas.get(row).getAttrValue();
                }
                default: {
                    throw new IllegalArgumentException("unknown column: " + column); // NOI18N
                }
            }
        }

        @Override
        public int getRowCount() {
            return cas.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(final int column) {
            switch (column) {
                case KEY: {
                    return org.openide.util.NbBundle.getMessage(
                            NewCidsClassVisualPanel3.class,
                            "NewCidsClassVisualPanel3.ClassAttrTableModel.getColumnName(int).column1"); // NOI18N
                }
                case VALUE: {
                    return org.openide.util.NbBundle.getMessage(
                            NewCidsClassVisualPanel3.class,
                            "NewCidsClassVisualPanel3.ClassAttrTableModel.getColumnName(int).column2"); // NOI18N
                }
                default: {
                    throw new IllegalArgumentException("unknown column: " + column);                    // NOI18N
                }
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(final Object aValue, final int row, final int column) {
            if (aValue instanceof String) {
                switch (column) {
                    case KEY: {
                        cas.get(row).setAttrKey(((String)aValue).trim());
                        break;
                    }
                    case VALUE: {
                        cas.get(row).setAttrValue(((String)aValue).trim());
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("unknown column: " + column); // NOI18N
                    }
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ca  DOCUMENT ME!
         */
        public void addClassAttribute(final ClassAttribute ca) {
            cas.add(ca);
            fireTableDataChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rowIndex  DOCUMENT ME!
         */
        public void removeClassAttribute(final int rowIndex) {
            cas.remove(rowIndex);
            fireTableDataChanged();
        }
    }
}

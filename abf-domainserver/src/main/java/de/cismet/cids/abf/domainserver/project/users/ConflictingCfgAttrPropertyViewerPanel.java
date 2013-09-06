/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.cismet.cids.abf.domainserver.project.configattr.ConfigAttrEntryNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Comparators.ConfigAttrEntries;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class ConflictingCfgAttrPropertyViewerPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient ConfigAttrEntry mainEntry;
    private final transient String mainEntryOriginUgName;
    private final transient List<Object[]> conflictingEntries;
    private final transient ListSelectionListener selL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblOrigin;
    private javax.swing.JLabel lblOriginValue;
    private javax.swing.JLabel lblOther;
    private javax.swing.JLabel lblValue;
    private javax.swing.JList lstOthers;
    private javax.swing.JPanel pnlOthers;
    private javax.swing.JTextArea txaValue;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ConflictingCfgAttrPropertyViewerPanel.
     *
     * @param   mainEntry              DOCUMENT ME!
     * @param   mainEntryOriginUgName  DOCUMENT ME!
     * @param   conflictingEntries     DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public ConflictingCfgAttrPropertyViewerPanel(final ConfigAttrEntry mainEntry,
            final String mainEntryOriginUgName,
            final List<Object[]> conflictingEntries) {
        if (mainEntry == null) {
            throw new IllegalArgumentException("mainEntry must not be null"); // NOI18N
        }
        this.mainEntry = mainEntry;
        this.mainEntryOriginUgName = mainEntryOriginUgName;
        this.conflictingEntries = conflictingEntries;
        this.selL = new EntrySelectionListener();

        initComponents();

        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        String via = ""; // NOI18N
        if (mainEntry.getUsergroup() == null) {
            // implies domain entry
            via = "<font color=\"!controlShadow\"> (via " + mainEntryOriginUgName + ")</font>";  // NOI18N
        }
        lblOriginValue.setText("<html>" + createEntryOriginString(mainEntry) + via + "</html>"); // NOI18N
        lblOriginValue.setIcon(new ImageIcon(ConfigAttrEntryNode.getIcon(mainEntry)));

        if ((conflictingEntries == null) || conflictingEntries.isEmpty()) {
            pnlOthers.setEnabled(false);
        } else {
            Collections.sort(conflictingEntries, new Comparator<Object[]>() {

                    private final Comparators.ConfigAttrEntries comp = new ConfigAttrEntries();

                    @Override
                    public int compare(final Object[] o1, final Object[] o2) {
                        return comp.compare((ConfigAttrEntry)o1[0], (ConfigAttrEntry)o2[0]);
                    }
                });
            lstOthers.setListData(conflictingEntries.toArray());
            lstOthers.setCellRenderer(new ConflictingEntriesCellRenderer());
            lstOthers.addListSelectionListener(WeakListeners.create(ListSelectionListener.class, selL, lstOthers));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   entry  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String createEntryOriginString(final ConfigAttrEntry entry) {
        final StringBuilder sb = new StringBuilder();

        final User user = entry.getUser();
        if (user != null) {
            sb.append(user.getLoginname()).append('(').append(user.getId()).append(")@"); // NOI18N
        }
        final UserGroup ug = entry.getUsergroup();
        if (ug != null) {
            sb.append(ug.getName()).append('(').append(ug.getId()).append(")@");          // NOI18N
        }

        sb.append(entry.getDomain());

        return sb.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblOrigin = new javax.swing.JLabel();
        lblOriginValue = new javax.swing.JLabel();
        pnlOthers = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txaValue = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstOthers = new javax.swing.JList();
        lblValue = new javax.swing.JLabel();
        lblOther = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        lblOrigin.setFont(new java.awt.Font("Lucida Grande", 1, 13));            // NOI18N
        lblOrigin.setText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblOrigin.text"));        // NOI18N
        lblOrigin.setToolTipText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblOrigin.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(lblOrigin, gridBagConstraints);

        lblOriginValue.setText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblOriginValue.text"));        // NOI18N
        lblOriginValue.setToolTipText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblOriginValue.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(lblOriginValue, gridBagConstraints);

        pnlOthers.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlOthers.setOpaque(false);
        pnlOthers.setLayout(new java.awt.GridBagLayout());

        txaValue.setColumns(20);
        txaValue.setEditable(false);
        txaValue.setRows(5);
        txaValue.setToolTipText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.txaValue.toolTipText")); // NOI18N
        jScrollPane2.setViewportView(txaValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        pnlOthers.add(jScrollPane2, gridBagConstraints);

        lstOthers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstOthers.setToolTipText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lstOthers.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(lstOthers);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        pnlOthers.add(jScrollPane1, gridBagConstraints);

        lblValue.setText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblValue.text"));        // NOI18N
        lblValue.setToolTipText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblValue.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 0);
        pnlOthers.add(lblValue, gridBagConstraints);

        lblOther.setText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblOther.text"));        // NOI18N
        lblOther.setToolTipText(NbBundle.getMessage(
                ConflictingCfgAttrPropertyViewerPanel.class,
                "ConflictingCfgAttrPropertyViewerPanel.lblOther.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 0);
        pnlOthers.add(lblOther, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(pnlOthers, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class EntrySelectionListener implements ListSelectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                final Object selectedItem = lstOthers.getSelectedValue();
                if (selectedItem == null) {
                    txaValue.setText(null);
                } else {
                    txaValue.setText(((ConfigAttrEntry)((Object[])selectedItem)[0]).getValue().getValue());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ConflictingEntriesCellRenderer extends DefaultListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel label = (JLabel)super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);
            final Object[] obj = (Object[])value;
            final ConfigAttrEntry entry = (ConfigAttrEntry)obj[0];

            label.setIcon(new ImageIcon(ConfigAttrEntryNode.getIcon(entry)));

            String via = "";
            if (entry.getUsergroup() == null) {
                // implies domain entry
                via = "<font color=\"!controlShadow\"> (via " + obj[1] + ")</font>";    // NOI18N
            }
            label.setText("<html>" + createEntryOriginString(entry) + via + "</html>"); // NOI18N

            return label;
        }
    }
}

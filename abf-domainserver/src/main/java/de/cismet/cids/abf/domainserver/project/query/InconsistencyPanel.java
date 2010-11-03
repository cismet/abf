/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import java.awt.Color;
import java.awt.Component;

import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.cismet.cids.maintenance.InspectionResult;
import de.cismet.cids.maintenance.container.Row;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class InconsistencyPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient InspectionResult[] results;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JTabbedPane tbpTable = new javax.swing.JTabbedPane();
    private final transient javax.swing.JTextArea txaMessage = new javax.swing.JTextArea();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new InconsistencyPanel object.
     *
     * @param  results  DOCUMENT ME!
     */
    public InconsistencyPanel(final InspectionResult[] results) {
        this.results = Arrays.copyOf(results, results.length);
        initComponents();
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        for (final InspectionResult result : results) {
            tbpTable.addTab(result.getTable().getTableName(),
                createPanel(result));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   result  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JPanel createPanel(final InspectionResult result) {
        final JPanel panel = new JPanel();
        final JTable errorTable = new JTable(new DefaultTableModel());
        final JLabel errorCodeLabel = new JLabel();
        final JLabel errorCodeDisplayLabel = new JLabel();
        final JLabel messageLabel = new JLabel();
        final JLabel messageDisplayLabel = new JLabel();
        final JScrollPane errorTableScrollPane = new JScrollPane();
        final GroupLayout jPanel1Layout = new GroupLayout(panel);
        errorTableScrollPane.setViewportView(errorTable);
        panel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.LEADING).add(
                jPanel1Layout.createSequentialGroup().add(
                    jPanel1Layout.createParallelGroup(GroupLayout.LEADING).add(errorCodeLabel).add(messageLabel)).add(
                    17,
                    17,
                    17).add(
                    jPanel1Layout.createParallelGroup(GroupLayout.LEADING).add(messageDisplayLabel).add(
                        errorCodeDisplayLabel)).addContainerGap(434, Short.MAX_VALUE)).add(
                errorTableScrollPane,
                GroupLayout.DEFAULT_SIZE,
                568,
                Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.LEADING).add(
                jPanel1Layout.createSequentialGroup().add(
                    jPanel1Layout.createParallelGroup(GroupLayout.BASELINE).add(errorCodeLabel).add(
                        errorCodeDisplayLabel)).addPreferredGap(LayoutStyle.RELATED).add(
                    jPanel1Layout.createParallelGroup(GroupLayout.BASELINE).add(messageLabel).add(
                        messageDisplayLabel)).add(26, 26, 26).add(
                    errorTableScrollPane,
                    GroupLayout.DEFAULT_SIZE,
                    268,
                    Short.MAX_VALUE)));
        errorCodeLabel.setText(org.openide.util.NbBundle.getMessage(
                InconsistencyPanel.class,
                "InconsistencyPanel.createPanel(InspectionResult).errorCodeLabel.text")); // NOI18N
        errorCodeDisplayLabel.setText(String.valueOf(result.getMessageCode()));
        messageLabel.setText(org.openide.util.NbBundle.getMessage(
                InconsistencyPanel.class,
                "InconsistencyPanel.createPanel(InspectionResult).messageLabel.text"));   // NOI18N
        messageLabel.setText(result.getResultMessage());
        final DefaultTableModel dtm = (DefaultTableModel)errorTable.getModel();
        dtm.setColumnIdentifiers(result.getTable().getColumnNames());
        errorTable.setTableHeader(new JTableHeader(errorTable.getColumnModel()));
        if (result.getErroneousRowCount() > 0) {
            for (final Row row : result.getErroneousRows()) {
                dtm.addRow(row.getRowdata().toArray());
            }
            final TableCellRenderer renderer = new TableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            final JTable table,
                            final Object value,
                            final boolean isSelected,
                            final boolean hasFocus,
                            final int row,
                            final int column) {
                        final JLabel label = new JLabel();
                        final Row.ErrorAwareEntry entry = (Row.ErrorAwareEntry)value;
                        label.setText(entry.getData().toString());
                        label.setAlignmentX(JTextField.CENTER_ALIGNMENT);
                        if (entry.hasError()) {
                            label.setForeground(Color.RED);
                        }
                        return label;
                    }
                };

            final Enumeration<TableColumn> e = errorTable.getColumnModel().getColumns();
            while (e.hasMoreElements()) {
                e.nextElement().setCellRenderer(renderer);
            }
        }
        return panel;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        txaMessage.setColumns(20);
        txaMessage.setRows(5);
        txaMessage.setText(org.openide.util.NbBundle.getMessage(
                InconsistencyPanel.class,
                "InconsistencyPanel.txaMessage.text")); // NOI18N
        jScrollPane1.setViewportView(txaMessage);

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        tbpTable,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        589,
                        Short.MAX_VALUE).add(
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        589,
                        Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().addContainerGap().add(
                    jScrollPane1,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    84,
                    Short.MAX_VALUE).add(13, 13, 13).add(
                    tbpTable,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    380,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
    } // </editor-fold>//GEN-END:initComponents
}

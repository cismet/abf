/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.EditorKit;

import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class QueryManipulationVisualPanel2 extends JPanel implements Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            QueryManipulationVisualPanel2.class);

    //~ Instance fields --------------------------------------------------------

    private final QueryManipulationWizardPanel2 model;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JCheckBox chkBatch = new javax.swing.JCheckBox();
    private final transient javax.swing.JCheckBox chkConjunction = new javax.swing.JCheckBox();
    private final transient javax.swing.JCheckBox chkResult = new javax.swing.JCheckBox();
    private final transient javax.swing.JCheckBox chkRoot = new javax.swing.JCheckBox();
    private final transient javax.swing.JCheckBox chkSearch = new javax.swing.JCheckBox();
    private final transient javax.swing.JCheckBox chkUnion = new javax.swing.JCheckBox();
    private final transient javax.swing.JCheckBox chkUpdate = new javax.swing.JCheckBox();
    private final transient javax.swing.JTextArea descArea = new javax.swing.JTextArea();
    private final transient javax.swing.JEditorPane edpStmt = new javax.swing.JEditorPane();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblDesc = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStatement = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblTheStmt = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManipulationVisualPanel2 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public QueryManipulationVisualPanel2(
            final QueryManipulationWizardPanel2 model) {
        this.model = model;
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init");                                                         // NOI18N
        }
        final Query query = model.getQuery();
        edpStmt.setContentType("text/x-sql");                                          // NOI18N
        final EditorKit kit = JEditorPane.createEditorKitForContentType("text/x-sql"); // NOI18N
        kit.install(edpStmt);
        edpStmt.setEditorKit(kit);
        edpStmt.setText(query.getStatement());
        edpStmt.setEditable(false);
        descArea.setText(query.getDescription());
        chkBatch.setSelected(query.getIsBatch());
        chkConjunction.setSelected(query.getIsConjunction());
        chkRoot.setSelected(query.getIsRoot());
        chkSearch.setSelected(query.getIsSearch());
        chkUnion.setSelected(query.getIsUnion());
        chkUpdate.setSelected(query.getIsUpdate());
        final Integer result = query.getResult();
        chkResult.setSelected((result != null) && (result == 2));
        edpStmt.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(final FocusEvent e) {
                    descArea.requestFocusInWindow();
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    // not needed
                }
            });
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Query getQuery() {
        final Query query = model.getQuery();
        query.setDescription(descArea.getText());
        query.setIsBatch(chkBatch.isSelected());
        query.setIsConjunction(chkConjunction.isSelected());
        query.setIsRoot(chkRoot.isSelected());
        query.setIsSearch(chkSearch.isSelected());
        query.setIsUnion(chkUnion.isSelected());
        query.setIsUpdate(chkUpdate.isSelected());
        query.setResult(chkResult.isSelected() ? 2 : 1);
        return query;
    }

    @Override
    public void update(final Observable o, final Object arg) {
        if ("readSettings".equals(arg)) // NOI18N
        {
            init();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(
            lblStatement,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.lblStatement.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            lblDesc,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.lblDesc.text")); // NOI18N

        descArea.setColumns(20);
        descArea.setRows(5);
        jScrollPane2.setViewportView(descArea);

        org.openide.awt.Mnemonics.setLocalizedText(
            chkResult,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkResult.text")); // NOI18N
        chkResult.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            chkUpdate,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkUpdate.text")); // NOI18N
        chkUpdate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            chkUnion,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkUnion.text")); // NOI18N
        chkUnion.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            chkRoot,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkRoot.text")); // NOI18N
        chkRoot.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            chkBatch,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkBatch.text")); // NOI18N
        chkBatch.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            chkConjunction,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkConjunction.text")); // NOI18N
        chkConjunction.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            chkSearch,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.chkSearch.text")); // NOI18N
        chkSearch.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            lblTheStmt,
            org.openide.util.NbBundle.getMessage(
                QueryManipulationVisualPanel2.class,
                "QueryManipulationVisualPanel2.lblTheStmt.text")); // NOI18N

        jScrollPane1.setViewportView(edpStmt);

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        621,
                        Short.MAX_VALUE).add(lblStatement).add(
                        layout.createSequentialGroup().add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                jScrollPane2,
                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                300,
                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(lblDesc)).add(64, 64, 64).add(
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lblTheStmt).add(
                                chkUpdate).add(chkSearch).add(chkConjunction).add(chkBatch).add(chkRoot).add(
                                chkUnion).add(chkResult)))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(lblStatement).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jScrollPane1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    90,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(17, 17, 17).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblDesc).add(
                        lblTheStmt)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        jScrollPane2,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        271,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(
                        layout.createSequentialGroup().add(chkResult).add(18, 18, 18).add(chkUpdate).add(
                            18,
                            18,
                            18).add(chkUnion).add(18, 18, 18).add(chkRoot).add(18, 18, 18).add(chkBatch).add(
                            18,
                            18,
                            18).add(chkConjunction).add(18, 18, 18).add(chkSearch))).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents
}

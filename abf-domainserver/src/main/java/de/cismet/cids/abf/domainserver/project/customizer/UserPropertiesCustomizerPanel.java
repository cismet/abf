/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.customizer;

import org.openide.util.NbBundle;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class UserPropertiesCustomizerPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkShowCfgAttrProperties;
    private javax.swing.JCheckBox chkShowLegacyCfgAttrProperties;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form UserPropertiesCustomizerPanel.
     */
    public UserPropertiesCustomizerPanel() {
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isShowCfgAttrProperties() {
        return chkShowCfgAttrProperties.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isShowLegacyCfgAttrProperties() {
        return chkShowLegacyCfgAttrProperties.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isSelected  DOCUMENT ME!
     */
    public void setShowCfgAttrProperties(final boolean isSelected) {
        this.chkShowCfgAttrProperties.setSelected(isSelected);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isSelected  DOCUMENT ME!
     */
    public void setShowLegacyCfgAttrProperties(final boolean isSelected) {
        this.chkShowLegacyCfgAttrProperties.setSelected(isSelected);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        chkShowLegacyCfgAttrProperties = new javax.swing.JCheckBox();
        chkShowCfgAttrProperties = new javax.swing.JCheckBox();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        chkShowLegacyCfgAttrProperties.setText(NbBundle.getMessage(
                UserPropertiesCustomizerPanel.class,
                "UserPropertiesCustomizerPanel.chkShowLegacyCfgAttrProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(chkShowLegacyCfgAttrProperties, gridBagConstraints);

        chkShowCfgAttrProperties.setText(NbBundle.getMessage(
                UserPropertiesCustomizerPanel.class,
                "UserPropertiesCustomizerPanel.chkShowCfgAttrProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(chkShowCfgAttrProperties, gridBagConstraints);
    }                                                                            // </editor-fold>//GEN-END:initComponents
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 mscholl
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * MavenArtifactEntryPanel.java
 *
 * Created on Aug 9, 2010, 2:57:04 PM
 */
package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import org.openide.util.NbBundle;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class MavenArtifactEntryPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 932070615167654076L;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblGroupIdSuffix;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtGroupIdSuffix;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MavenArtifactEntryPanel.
     */
    public MavenArtifactEntryPanel() {
        initComponents();
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        txtGroupIdSuffix.setDocument(new IdDocument());
        txtArtifactId.setDocument(new IdDocument());
        txtGroupIdSuffix.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    updateGroupId();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    updateGroupId();
                }

                @Override
                public void changedUpdate(final DocumentEvent e) {
                    updateGroupId();
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void updateGroupId() {
        final String suffix = txtGroupIdSuffix.getText();
        if (suffix.isEmpty()) {
            txtGroupId.setText(InstallAsMavenArtifactAction.DEFAULT_GROUPID);
        } else {
            txtGroupId.setText(InstallAsMavenArtifactAction.DEFAULT_GROUPID + "." + suffix); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getValidId(final String name) {
        final StringBuilder builder = new StringBuilder(name);
        for (int i = 0; i < builder.length(); ++i) {
            final char nameChar = builder.charAt(i);

            if (('A' <= nameChar) && ('Z' >= nameChar)) {
                // it is uppercase
                builder.setCharAt(i, Character.toLowerCase(nameChar));
                if (i > 0) {
                    builder.insert(i, '-');
                    ++i;
                }
            } else if (('_' == nameChar) || Character.isWhitespace(nameChar)) {
                builder.setCharAt(i, '-');
            } else if ((('0' > nameChar) && ('-' != nameChar))
                        || (('9' < nameChar) && ('A' > nameChar))
                        || (('Z' < nameChar) && ('a' > nameChar))
                        || ('z' < nameChar)) {
                // it is an illegal character
                builder.deleteCharAt(i);
                --i;
            }
        }

        return builder.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  groupIdSuffix  DOCUMENT ME!
     */
    void setGroupIdSuffix(final String groupIdSuffix) {
        txtGroupIdSuffix.setText(getValidId(groupIdSuffix));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getGroupId() {
        return txtGroupId.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  artifactId  DOCUMENT ME!
     */
    void setArtifactId(final String artifactId) {
        txtArtifactId.setText(getValidId(artifactId));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getArtifactId() {
        return txtArtifactId.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  version  DOCUMENT ME!
     */
    void setVersion(final String version) {
        txtVersion.setText(version);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getVersion() {
        return txtVersion.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblGroupIdSuffix = new javax.swing.JLabel();
        txtGroupIdSuffix = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();

        lblArtifactId.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.lblArtifactId.text")); // NOI18N

        txtArtifactId.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.txtArtifactId.text")); // NOI18N

        lblGroupIdSuffix.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.lblGroupIdSuffix.text")); // NOI18N

        txtGroupIdSuffix.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.txtGroupIdSuffix.text")); // NOI18N

        lblVersion.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.lblVersion.text")); // NOI18N

        txtVersion.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.txtVersion.text")); // NOI18N

        lblGroupId.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.lblGroupId.text")); // NOI18N

        txtGroupId.setBackground(javax.swing.UIManager.getDefaults().getColor("TextComponent.selectionBackgroundInactive"));
        txtGroupId.setText(NbBundle.getMessage(MavenArtifactEntryPanel.class, "MavenArtifactEntryPanel.txtGroupId.text")); // NOI18N
        txtGroupId.setFocusTraversalKeysEnabled(false);
        txtGroupId.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblGroupIdSuffix)
                    .add(lblGroupId)
                    .add(lblArtifactId)
                    .add(lblVersion))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtArtifactId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .add(txtGroupIdSuffix, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtGroupId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblGroupIdSuffix)
                    .add(txtGroupIdSuffix, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblGroupId)
                    .add(txtGroupId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblArtifactId)
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblVersion))
                .addContainerGap(32, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class IdDocument extends PlainDocument {

        //~ Static fields/initializers -----------------------------------------

        /** Use serialVersionUID for interoperability. */
        private static final long serialVersionUID = 2625644009656835684L;

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
            final String valid = getValidId(str);
            super.insertString(offs, valid, a);
        }
    }
}

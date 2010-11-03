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
package de.cismet.cids.abf.domainserver.project.configattr;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
import org.openide.util.NbBundle;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.WeakListeners;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class NewKeyVisualPanel1 extends JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient NewKeyWizardPanel1 model;
    private final transient DocumentListenerImpl docL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblKey;
    private javax.swing.JTextField txtKey;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewKeyVisualPanel1 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public NewKeyVisualPanel1(final NewKeyWizardPanel1 model) {
        initComponents();
        this.model = model;
        docL = new DocumentListenerImpl();
        txtKey.getDocument().addDocumentListener(WeakListeners.document(docL, txtKey.getDocument()));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.getName().returnValue"); // NOI18N
    }

    ConfigAttrKey getKey(){
        final ConfigAttrKey key = new ConfigAttrKey();
        key.setKey(txtKey.getText());

        return key;
    }

    /**
     * DOCUMENT ME!
     */
    void init() {
        model.fireChangeEvent();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblKey = new javax.swing.JLabel();
        txtKey = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(lblKey, NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.lblKey.text")); // NOI18N

        txtKey.setText(NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.txtKey.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtKey, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(lblKey))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKey)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            model.fireChangeEvent();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            model.fireChangeEvent();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            model.fireChangeEvent();
        }
    }
}
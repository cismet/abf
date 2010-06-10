/*
 * GeneralVisualPanel.java, encoding: UTF-8
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
 * Created on 25. August 2007, 14:48
 */

package de.cismet.cids.abf.librarysupport.project.customizer;

/**
 *
 * @author mscholl
 * @version 1.2
 */
public final class GeneralVisualPanel extends javax.swing.JPanel
{
    /** Creates new form GeneralVisualPanel */
    public GeneralVisualPanel()
    {
        initComponents();
    }
    
    public boolean isAutoReload()
    {
        return autoreloadCheckbox.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        autoreloadCheckbox.setSelected(true);
        autoreloadCheckbox.setText(org.openide.util.NbBundle.getMessage(GeneralVisualPanel.class, "Chk_autoReload")); // NOI18N
        autoreloadCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(GeneralVisualPanel.class, "Chk_autoReloadTooltip")); // NOI18N
        autoreloadCheckbox.setEnabled(false);
        autoreloadCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(autoreloadCheckbox)
                .addContainerGap(133, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(autoreloadCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(274, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JCheckBox autoreloadCheckbox = new javax.swing.JCheckBox();
    // End of variables declaration//GEN-END:variables
    
}

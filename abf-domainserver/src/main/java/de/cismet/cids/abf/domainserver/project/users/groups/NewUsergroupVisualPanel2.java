/*
 * NewUsergroupVisualPanel2.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.users.groups;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.UserNode;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.util.Set;
import javax.swing.JPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class NewUsergroupVisualPanel2 extends JPanel
{
    private final transient ExplorerManager basicExplorerManager;
    private final transient ExplorerManager newUserExplorerManager;
    private final transient NewUsergroupWizardPanel2 model;
   
    public NewUsergroupVisualPanel2(final NewUsergroupWizardPanel2 model)
    {
        this.model = model;
        initComponents();
        basicExplorerManager = new ExplorerManager();
        newUserExplorerManager = new ExplorerManager();
        ((BeanTreeView)scpUsers).setRootVisible(false);
        ((BeanTreeView)scpNewGroupMembers).setRootVisible(false);
    }
    
    void init()
    {
        final UserManagement um = model.getProject().getLookup()
                .lookup(UserManagement.class);
        basicExplorerManager.setRootContext(um);
        newUserExplorerManager.setRootContext(new AbstractNode(new Children.
                Array()));
        final Set<User> users = model.getUserGroup().getUsers();
        final DomainserverProject project = model.getProject();
        if(users != null)
        {
            final UserNode[] userNodes = new UserNode[users.size()];
            int i = -1;
            for(final User u : users)
            {
                userNodes[++i] = new UserNode(u, project);
            }
            addUserNodes(userNodes);
        }
    }
    
    UserGroup getUserGroup()
    {
        final Node[] nodes = newUserExplorerManager.getRootContext().
                getChildren().getNodes();
        final UserGroup ug = model.getUserGroup();
        ug.getUsers().clear();
        for(final Node n : nodes)
        {
            ug.getUsers().add(((UserNode)n).getUser());
        }
        return ug;
    }

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel2.class, "NewUsergroupVisualPanel2.getName().returnvalue"); // NOI18N
    }
    
    private void addUserNodes(final Node[] nodeArray)
    {
        for(final Node n : nodeArray)
        {
            if(n instanceof UserNode)
            {
                final UserNode uNode = new UserNode(((UserNode)n).getUser(),
                        ((UserNode)n).getDomainserverProject());
                final Node[] na = new Node[1];
                na[0] = uNode;
                newUserExplorerManager.getRootContext().getChildren().remove(
                        na);
                newUserExplorerManager.getRootContext().getChildren().add(na);
            }else if(n instanceof UserGroupNode)
            {
                addUserNodes(n.getChildren().getNodes());
            }
        }
    }
    
    final class LeftPanel extends JPanel implements ExplorerManager.Provider
    {
        @Override
        public ExplorerManager getExplorerManager()
        {
            return basicExplorerManager;
        }
    }
    
    final class RightPanel extends JPanel implements ExplorerManager.Provider
    {
        @Override
        public ExplorerManager getExplorerManager()
        {
            return newUserExplorerManager;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout panLeftLayout = new org.jdesktop.layout.GroupLayout(panLeft);
        panLeft.setLayout(panLeftLayout);
        panLeftLayout.setHorizontalGroup(
            panLeftLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scpUsers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        );
        panLeftLayout.setVerticalGroup(
            panLeftLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, scpUsers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout panRightLayout = new org.jdesktop.layout.GroupLayout(panRight);
        panRight.setLayout(panRightLayout);
        panRightLayout.setHorizontalGroup(
            panRightLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scpNewGroupMembers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
        );
        panRightLayout.setVerticalGroup(
            panRightLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scpNewGroupMembers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(cmdAddToGroup, "-->");
        cmdAddToGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddToGroupActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cmdRemoveFromNewGroup, "<--");
        cmdRemoveFromNewGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveFromNewGroupActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(panLeft, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(cmdRemoveFromNewGroup, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cmdAddToGroup, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panRight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panLeft, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(panRight, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(129, 129, 129)
                .add(cmdAddToGroup)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmdRemoveFromNewGroup)
                .addContainerGap(125, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void cmdRemoveFromNewGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveFromNewGroupActionPerformed
        newUserExplorerManager.getRootContext().getChildren().remove( 
                newUserExplorerManager.getSelectedNodes());
    }//GEN-LAST:event_cmdRemoveFromNewGroupActionPerformed
    
    private void cmdAddToGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddToGroupActionPerformed
        addUserNodes(basicExplorerManager.getSelectedNodes());
    }//GEN-LAST:event_cmdAddToGroupActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton cmdAddToGroup = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdRemoveFromNewGroup = new javax.swing.JButton();
    private final transient javax.swing.JPanel panLeft = new LeftPanel();
    private final transient javax.swing.JPanel panRight = new RightPanel();
    private final transient javax.swing.JScrollPane scpNewGroupMembers = new BeanTreeView();
    private final transient javax.swing.JScrollPane scpUsers = new BeanTreeView();
    // End of variables declaration//GEN-END:variables
}
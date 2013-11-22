/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users.groups;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children.SortedArray;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Comparator;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.UserNode;

import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class AddUsersVisualPanel extends JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient ExplorerManager basicExplorerManager;
    private final transient ExplorerManager newUserExplorerManager;
    private final transient AddUsersWizardPanel model;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton cmdAddToGroup = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdRemoveFromNewGroup = new javax.swing.JButton();
    private final transient javax.swing.JPanel panLeft = new LeftPanel();
    private final transient javax.swing.JPanel panRight = new RightPanel();
    private final transient javax.swing.JScrollPane scpNewGroupMembers = new BeanTreeView();
    private final transient javax.swing.JScrollPane scpUsers = new BeanTreeView();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewUsergroupVisualPanel2 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public AddUsersVisualPanel(final AddUsersWizardPanel model) {
        this.model = model;
        initComponents();
        basicExplorerManager = new ExplorerManager();
        newUserExplorerManager = new ExplorerManager();
        ((BeanTreeView)scpUsers).setRootVisible(false);
        ((BeanTreeView)scpNewGroupMembers).setRootVisible(false);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        final UserManagement um = model.getProject().getLookup().lookup(UserManagement.class);
        basicExplorerManager.setRootContext(um);
        final SortedArray children = new Children.SortedArray();
        children.setComparator(new Comparator<Node>() {

                @Override
                public int compare(final Node o1, final Node o2) {
                    return ((UserNode)o1).getUser()
                                .getLoginname()
                                .toLowerCase()
                                .compareTo(((UserNode)o2).getUser().getLoginname().toLowerCase());
                }
            });
        newUserExplorerManager.setRootContext(new AbstractNode(children));

        final Set<User> users = model.getUserGroup().getUsers();
        final DomainserverProject project = model.getProject();
        if (users != null) {
            final UserNode[] userNodes = new UserNode[users.size()];
            int i = -1;
            for (final User u : users) {
                userNodes[++i] = new UserNode(u, project);
            }
            addUserNodes(userNodes);
        }

        ((TitledBorder)panRight.getBorder()).setTitle(
            NbBundle.getMessage(
                AddUsersVisualPanel.class,
                "AddUsersVisualPanel.panRight.border.title", // NOI18N
                model.getUserGroup().getName()));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    UserGroup getUserGroup() {
        final Node[] nodes = newUserExplorerManager.getRootContext().getChildren().getNodes();
        final UserGroup ug = model.getUserGroup();
        ug.getUsers().clear();
        for (final Node n : nodes) {
            ug.getUsers().add(((UserNode)n).getUser());
        }
        return ug;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewUsergroupVisualPanel2.class,
                "NewUsergroupVisualPanel2.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nodeArray  DOCUMENT ME!
     */
    private void addUserNodes(final Node[] nodeArray) {
        for (final Node n : nodeArray) {
            if (n instanceof UserNode) {
                final UserNode uNode = new UserNode(((UserNode)n).getUser(),
                        ((UserNode)n).getDomainserverProject());
                final Node[] na = new Node[1];
                na[0] = uNode;
                newUserExplorerManager.getRootContext().getChildren().remove(
                    na);
                newUserExplorerManager.getRootContext().getChildren().add(na);
            } else if (n instanceof UserGroupNode) {
                addUserNodes(n.getChildren().getNodes());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        panLeft.setBorder(javax.swing.BorderFactory.createTitledBorder(
                NbBundle.getMessage(AddUsersVisualPanel.class, "AddUsersVisualPanel.panLeft.border.title"))); // NOI18N

        final org.jdesktop.layout.GroupLayout panLeftLayout = new org.jdesktop.layout.GroupLayout(panLeft);
        panLeft.setLayout(panLeftLayout);
        panLeftLayout.setHorizontalGroup(
            panLeftLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                scpUsers,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                184,
                Short.MAX_VALUE));
        panLeftLayout.setVerticalGroup(
            panLeftLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                scpUsers,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                288,
                Short.MAX_VALUE));

        panRight.setBorder(javax.swing.BorderFactory.createTitledBorder(
                NbBundle.getMessage(AddUsersVisualPanel.class, "AddUsersVisualPanel.panRight.border.title"))); // NOI18N

        final org.jdesktop.layout.GroupLayout panRightLayout = new org.jdesktop.layout.GroupLayout(panRight);
        panRight.setLayout(panRightLayout);
        panRightLayout.setHorizontalGroup(
            panRightLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                scpNewGroupMembers,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                186,
                Short.MAX_VALUE));
        panRightLayout.setVerticalGroup(
            panRightLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                scpNewGroupMembers,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                288,
                Short.MAX_VALUE));

        org.openide.awt.Mnemonics.setLocalizedText(
            cmdAddToGroup,
            NbBundle.getMessage(AddUsersVisualPanel.class, "AddUsersVisualPanel.cmdAddToGroup.text")); // NOI18N
        cmdAddToGroup.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdAddToGroupActionPerformed(evt);
                }
            });

        org.openide.awt.Mnemonics.setLocalizedText(
            cmdRemoveFromNewGroup,
            NbBundle.getMessage(AddUsersVisualPanel.class, "AddUsersVisualPanel.cmdRemoveFromNewGroup.text")); // NOI18N
        cmdRemoveFromNewGroup.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveFromNewGroupActionPerformed(evt);
                }
            });

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().addContainerGap().add(
                    panLeft,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(
                        cmdRemoveFromNewGroup,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        cmdAddToGroup,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panRight,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        panLeft,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        panRight,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE))).add(
                layout.createSequentialGroup().add(129, 129, 129).add(cmdAddToGroup).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(cmdRemoveFromNewGroup).addContainerGap(
                    125,
                    Short.MAX_VALUE)));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveFromNewGroupActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveFromNewGroupActionPerformed
        newUserExplorerManager.getRootContext().getChildren().remove(
            newUserExplorerManager.getSelectedNodes());
    }                                                                                         //GEN-LAST:event_cmdRemoveFromNewGroupActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdAddToGroupActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdAddToGroupActionPerformed
        addUserNodes(basicExplorerManager.getSelectedNodes());
    }                                                                                 //GEN-LAST:event_cmdAddToGroupActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class LeftPanel extends JPanel implements ExplorerManager.Provider {

        //~ Methods ------------------------------------------------------------

        @Override
        public ExplorerManager getExplorerManager() {
            return basicExplorerManager;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class RightPanel extends JPanel implements ExplorerManager.Provider {

        //~ Methods ------------------------------------------------------------

        @Override
        public ExplorerManager getExplorerManager() {
            return newUserExplorerManager;
        }
    }
}

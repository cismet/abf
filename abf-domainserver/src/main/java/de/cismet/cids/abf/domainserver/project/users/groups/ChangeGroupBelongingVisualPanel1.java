/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users.groups;

import org.apache.log4j.Logger;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;
import de.cismet.cids.abf.utilities.Comparators;

import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class ChangeGroupBelongingVisualPanel1 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ChangeGroupBelongingVisualPanel1.class);

    //~ Instance fields --------------------------------------------------------

    private final transient ChangeGroupBelongingWizardPanel1 model;
    private final transient List<UserGroup> membership;
    private final transient List<UserGroup> touchedGroups;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JList lstAllGroups = new DragJList();
    private final transient javax.swing.JList lstGroupMembership = new DropJList();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ChangeGroupBelongingVisualPanel1 object.
     *
     * @param  model  DOCUMENT ME!
     */
    public ChangeGroupBelongingVisualPanel1(
            final ChangeGroupBelongingWizardPanel1 model) {
        this.model = model;
        membership = new ArrayList<UserGroup>();
        touchedGroups = new ArrayList<UserGroup>();
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        final List<UserGroup> l = model.getBackend().getAllEntities(UserGroup.class);
        final List<UserGroup> groups = new ArrayList<UserGroup>();

        for (final UserGroup ug : l) {
            if (!ProjectUtils.isRemoteGroup(ug, model.getProject())) {
                groups.add(ug);
            }
        }

        Collections.sort(groups, new Comparators.UserGroups());

        membership.clear();
        membership.addAll(model.getUser().getUserGroups());
        final ListCellRenderer lcr = new Renderers.UserGroupListRenderer(model.getProject());
        lstAllGroups.setCellRenderer(lcr);
        lstGroupMembership.setCellRenderer(lcr);
        lstAllGroups.setListData(groups.toArray());
        lstGroupMembership.setListData(membership.toArray());
        touchedGroups.clear();
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                ChangeGroupBelongingVisualPanel1.class,
                "ChangeGroupBelongingVisualPanel1.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<UserGroup> getTouchedGroups() {
        return touchedGroups;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    ChangeGroupBelongingVisualPanel1.class,
                    "ChangeGroupBelongingVisualPanel1.jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setViewportView(lstAllGroups);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(
                    ChangeGroupBelongingVisualPanel1.class,
                    "ChangeGroupBelongingVisualPanel1.jScrollPane2.border.title"))); // NOI18N
        jScrollPane2.setViewportView(lstGroupMembership);

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    jScrollPane2,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    269,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    21,
                    Short.MAX_VALUE).add(
                    jScrollPane1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    266,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        313,
                        Short.MAX_VALUE).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        jScrollPane2,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        313,
                        Short.MAX_VALUE)).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class DragJList extends JList implements DragGestureListener, DragSourceListener {

        //~ Methods ------------------------------------------------------------

        // <editor-fold defaultstate="collapsed" desc=" Not needed listener impls ">
        @Override
        public void dragDropEnd(final DragSourceDropEvent e) {
            // not needed
        }

        @Override
        public void dragEnter(final DragSourceDragEvent e) {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent e) {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent e) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent e) {
            // not needed
        }
        // </editor-fold>

        //~ Instance fields ----------------------------------------------------

        private final transient DragSource dragSource;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DragJList object.
         */
        public DragJList() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this                              // drag gesture recognizer
                );
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void dragGestureRecognized(final DragGestureEvent e) {
            final Object o = this.getSelectedValue();
            final Transferable trans = new CidsUserGroupTransferable(o);
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class DropJList extends JList implements DropTargetListener, DragGestureListener, DragSourceListener {

        //~ Methods ------------------------------------------------------------

        // <editor-fold defaultstate="collapsed" desc=" Not needed listener impls ">
        @Override
        public void dragExit(final DropTargetEvent dte) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragEnter(final DragSourceDragEvent dsde) {
            // not needed
        }

        @Override
        public void dragOver(final DragSourceDragEvent dsde) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent dsde) {
            // not needed
        }

        @Override
        public void dragExit(final DragSourceEvent dse) {
            // not needed
        }
        // </editor-fold>

        //~ Instance fields ----------------------------------------------------

        private final transient int acceptableActions;
        private final transient DropTarget dt;
        private final transient DragSource dragSource;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DropJList object.
         */
        public DropJList() {
            acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
            // needed
            dt = new DropTarget(this, acceptableActions, this);
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this                              // drag gesture recognizer
                );
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void drop(final DropTargetDropEvent dtde) {
            try {
                final Object o = dtde.getTransferable().getTransferData(CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                if (o instanceof UserGroup) {
                    final UserGroup ug = (UserGroup)o;
                    if (!membership.contains(ug)) {
                        final User user = model.getUser();
                        ug.getUsers().add(user);
                        user.getUserGroups().add(ug);
                        membership.add(ug);
                        lstGroupMembership.setListData(membership.toArray());
                        if (!touchedGroups.contains(ug)) {
                            touchedGroups.add(ug);
                        }
                    }
                }
            } catch (final Exception e) {
                LOG.error("could not drop", e); // NOI18N
            } finally {
                dtde.dropComplete(true);
            }
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge) {
            final Object o = this.getSelectedValue();
            final Transferable trans = new CidsUserGroupTransferable(o);
            dragSource.startDrag(dge, DragSource.DefaultCopyDrop, trans, this);
        }

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde) {
            if (!dsde.getDropSuccess()) {
                try {
                    final Object o = dsde.getDragSourceContext()
                                .getTransferable()
                                .getTransferData(
                                    CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                    if (o instanceof UserGroup) {
                        final UserGroup ug = (UserGroup)o;
                        final User user = model.getUser();
                        ug.getUsers().remove(user);
                        user.getUserGroups().remove(ug);
                        membership.remove(ug);
                        lstGroupMembership.setListData(membership.toArray());
                        if (!touchedGroups.contains(ug)) {
                            touchedGroups.add(ug);
                        }
                    }
                } catch (final Exception ex) {
                    LOG.warn("could not obtain transferable data", ex); // NOI18N
                }
            }
        }
    }
}

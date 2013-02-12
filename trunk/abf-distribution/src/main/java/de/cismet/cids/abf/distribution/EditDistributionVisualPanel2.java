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
package de.cismet.cids.abf.distribution;

import org.apache.maven.artifact.Artifact;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultListRenderer;

import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SortOrder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cismet.cids.abf.utilities.MavenTypeTransferable;
import de.cismet.cids.abf.utilities.MavenUtils;
import de.cismet.cids.abf.utilities.files.FileUtils;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class EditDistributionVisualPanel2 extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String DEFAULT_GID_PREFIX = "de.cismet.cids.local"; // NOI18N

    private static final String GROUP_AID = "__onlyGroup__"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient EditDistributionWizardPanel2 model;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jtpApps;
    private javax.swing.JTree jtrLocal;
    private javax.swing.JPanel pnlTab;
    private javax.swing.JPanel pnlTree;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form EditDistributionVisualPanel2.
     *
     * @param  model  DOCUMENT ME!
     */
    public EditDistributionVisualPanel2(final EditDistributionWizardPanel2 model) {
        this.model = model;
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        List<Artifact> artifacts = null;
        final List<RepositoryInfo> infos = RepositoryPreferences.getInstance().getRepositoryInfos();
        for (final RepositoryInfo info : infos) {
            if (info.isLocal()) {
                final File localrepo = new File(info.getRepositoryPath() + File.separator
                                + DEFAULT_GID_PREFIX.replace('.', '/'));
                if (localrepo.exists()) {
                    artifacts = resolveArtifacts(new File(info.getRepositoryPath()), localrepo);
                }
            }
        }

        if (artifacts == null) {
            artifacts = new ArrayList<Artifact>(1);
        }

        populateLocalTree(artifacts);
        populateAppPane(model.getApps());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Map<Artifact, Set<Artifact>> getCustomSelection() {
        final Map<Artifact, Set<Artifact>> selection = new HashMap<Artifact, Set<Artifact>>(jtpApps.getTabCount(), 1);

        for (int i = 0; i < jtpApps.getTabCount(); ++i) {
            final JList list = (JList)jtpApps.getComponentAt(i);
            final Set<Artifact> artifacts = new HashSet<Artifact>();

            for (int j = 0; j < list.getModel().getSize(); ++j) {
                artifacts.add((Artifact)list.getModel().getElementAt(j));
            }

            Artifact app = null;
            for (final Artifact artifact : model.getApps()) {
                if (artifact.getArtifactId().equals(jtpApps.getTitleAt(i))) {
                    app = artifact;
                    break;
                }
            }

            assert app != null : "app cannot be null at this point"; // NOI18N

            selection.put(app, artifacts);
        }

        return selection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  artifacts  DOCUMENT ME!
     */
    private void populateLocalTree(final List<Artifact> artifacts) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true); // NOI18N
        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        jtrLocal.setModel(treeModel);

        final HashMap<Artifact, List<Artifact>> groups = new HashMap<Artifact, List<Artifact>>((artifacts.size() / 5)
                        + 1);
        for (final Artifact artifact : artifacts) {
            final Artifact groupArtifact = EmbedderFactory.getProjectEmbedder()
                        .createArtifact(artifact.getGroupId(), GROUP_AID, "", "", ""); // NOI18N
            if (!groups.containsKey(groupArtifact)) {
                groups.put(groupArtifact, new ArrayList<Artifact>());
            }

            groups.get(groupArtifact).add(artifact);
        }

        final List<Artifact> groupArtifacts = new ArrayList<Artifact>(groups.keySet());
        Collections.sort(groupArtifacts, new Comparator<Artifact>() {

                @Override
                public int compare(final Artifact o1, final Artifact o2) {
                    return o1.getGroupId().compareTo(o2.getGroupId());
                }
            });

        for (final Artifact groupArtifact : groupArtifacts) {
            final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupArtifact, true);
            final List<Artifact> artifactList = groups.get(groupArtifact);
            Collections.sort(artifactList, new Comparator<Artifact>() {

                    @Override
                    public int compare(final Artifact o1, final Artifact o2) {
                        final int idCompare = o1.getArtifactId().compareTo(o2.getArtifactId());

                        if (idCompare == 0) {
                            return o1.getVersion().compareTo(o2.getVersion());
                        } else {
                            return idCompare;
                        }
                    }
                });

            for (final Artifact artifact : artifactList) {
                final DefaultMutableTreeNode artifactNode = new DefaultMutableTreeNode(artifact, false);
                groupNode.add(artifactNode);
            }

            root.add(groupNode);
        }

        jtrLocal.setRootVisible(false);
        jtrLocal.setShowsRootHandles(true);
        jtrLocal.expandPath(new TreePath(root));
        jtrLocal.setDragEnabled(false);
        jtrLocal.setTransferHandler(null);
        jtrLocal.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        jtrLocal.setCellRenderer(new ArtifactTreeRenderer());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  apps  DOCUMENT ME!
     */
    private void populateAppPane(final List<Artifact> apps) {
        jtpApps.removeAll();

        Collections.sort(apps);
        for (final Artifact app : apps) {
            final DnDList list = new DnDList();
            list.setModel(new DefaultListModel());
            list.setCellRenderer(new ArtifactListRenderer());
            list.setDragEnabled(false);
            list.setTransferHandler(null);
            jtpApps.addTab(app.getArtifactId(), list);
        }

        jtpApps.setSelectedIndex(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   localRepo  DOCUMENT ME!
     * @param   group      dir DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<Artifact> resolveArtifacts(final File localRepo, final File group) {
        final List<Artifact> result = new ArrayList<Artifact>();
        final File[] subfiles = group.listFiles(new FileUtils.DirAndJarFilter());
        for (final File subfile : subfiles) {
            if (subfile.isFile()) { // NOI18N

                final String groupId = MavenUtils.extractGroupId(localRepo, subfile);
                final String artifactId = MavenUtils.extractArtifactId(subfile);
                final String version = MavenUtils.extractVersion(subfile);
                final Artifact artifact = EmbedderFactory.getProjectEmbedder()
                            .createArtifact(groupId, artifactId, version, "compile", "jar"); // NOI18N
                artifact.setResolved(true);
                artifact.setFile(subfile);
                result.add(artifact);
            } else {
                result.addAll(resolveArtifacts(localRepo, subfile));
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(
                EditDistributionVisualPanel2.class,
                "EditDistributionVisualPanel2.getName().returnValue"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        pnlTree = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtrLocal = new DnDTree();
        pnlTab = new javax.swing.JPanel();
        jtpApps = new javax.swing.JTabbedPane();

        jSplitPane1.setDividerLocation(260);

        pnlTree.setBorder(javax.swing.BorderFactory.createTitledBorder(
                NbBundle.getMessage(
                    EditDistributionVisualPanel2.class,
                    "EditDistributionVisualPanel2.pnlTree.border.title"))); // NOI18N

        jtrLocal.setDragEnabled(true);
        jScrollPane1.setViewportView(jtrLocal);

        final javax.swing.GroupLayout pnlTreeLayout = new javax.swing.GroupLayout(pnlTree);
        pnlTree.setLayout(pnlTreeLayout);
        pnlTreeLayout.setHorizontalGroup(
            pnlTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                246,
                Short.MAX_VALUE));
        pnlTreeLayout.setVerticalGroup(
            pnlTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1,
                javax.swing.GroupLayout.Alignment.TRAILING,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                238,
                Short.MAX_VALUE));

        jSplitPane1.setLeftComponent(pnlTree);

        final javax.swing.GroupLayout pnlTabLayout = new javax.swing.GroupLayout(pnlTab);
        pnlTab.setLayout(pnlTabLayout);
        pnlTabLayout.setHorizontalGroup(
            pnlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jtpApps,
                javax.swing.GroupLayout.Alignment.TRAILING,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                200,
                Short.MAX_VALUE));
        pnlTabLayout.setVerticalGroup(
            pnlTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jtpApps,
                javax.swing.GroupLayout.Alignment.TRAILING,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                266,
                Short.MAX_VALUE));

        jSplitPane1.setRightComponent(pnlTab);

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jSplitPane1,
                javax.swing.GroupLayout.Alignment.TRAILING,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                471,
                Short.MAX_VALUE));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jSplitPane1,
                javax.swing.GroupLayout.Alignment.TRAILING,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                270,
                Short.MAX_VALUE));
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class ArtifactTreeRenderer extends DefaultTreeCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon localIcon = ImageUtilities.loadImageIcon(
                DistributionProject.IMAGE_FOLDER
                        + "home_16.gif", // NOI18N
                false);
        private final transient ImageIcon jarIcon = ImageUtilities.loadImageIcon(
                DistributionProject.IMAGE_FOLDER
                        + "jar_16.gif", // NOI18N
                false);

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getTreeCellRendererComponent(final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean hasFocus) {
            final JLabel component = (JLabel)super.getTreeCellRendererComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);

            final Object object = ((DefaultMutableTreeNode)value).getUserObject();
            if (object instanceof Artifact) {
                final Artifact artifact = (Artifact)object;
                final String artefactId = artifact.getArtifactId();

                if ((artefactId == null) || artefactId.isEmpty() || GROUP_AID.equals(artefactId)) {
                    component.setText(artifact.getGroupId());
                    component.setIcon(localIcon);
                } else {
                    if ((artifact.getVersion() == null) || artifact.getVersion().isEmpty()) {
                        component.setText(artefactId);
                    } else {
                        component.setText(artefactId + "-" + artifact.getVersion()); // NOI18N
                    }
                    component.setIcon(jarIcon);
                }
            } else {
                component.setText(value.toString());
            }

            return component;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class ArtifactListRenderer extends DefaultListRenderer {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon jarIcon = ImageUtilities.loadImageIcon(
                DistributionProject.IMAGE_FOLDER
                        + "jar_16.gif", // NOI18N
                false);

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel component = (JLabel)super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus);

            if (value instanceof Artifact) {
                final Artifact artifact = (Artifact)value;
                final String artifactId = artifact.getArtifactId();

                String groupIdShort = artifact.getGroupId().replace(DEFAULT_GID_PREFIX, ""); // NOI18N
                if (groupIdShort.isEmpty()) {
                    groupIdShort = "default";                                                // NOI18N
                } else {
                    groupIdShort = groupIdShort.substring(1);
                }

                if ((artifact.getVersion() == null) || artifact.getVersion().isEmpty()) {
                    component.setText(artifactId + " (" + groupIdShort + ")");                               // NOI18N
                } else {
                    component.setText(artifactId + "-" + artifact.getVersion() + " (" + groupIdShort + ")"); // NOI18N
                }
                component.setIcon(jarIcon);
            }

            return component;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class DnDTree extends JXTree implements DragGestureListener, DragSourceListener {

        //~ Instance fields ----------------------------------------------------

        private final transient DragSource dragSource;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DnDTree object.
         */
        DnDTree() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this);                            // drag gesture recognizer
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        List<Artifact> getSelectedArtifacts() {
            final TreePath[] selectedObjects = getSelectionPaths();
            final List<Artifact> selected = new ArrayList<Artifact>(selectedObjects.length);

            for (final TreePath path : selectedObjects) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                final Artifact artifact = (Artifact)node.getUserObject();
                if (GROUP_AID.equals(artifact.getArtifactId())) {
                    final Enumeration children = node.children();
                    while (children.hasMoreElements()) {
                        final DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
                        final Artifact childArtifact = (Artifact)child.getUserObject();
                        if (!selected.contains(childArtifact)) {
                            selected.add(childArtifact);
                        }
                    }
                } else {
                    if (!selected.contains(artifact)) {
                        selected.add(artifact);
                    }
                }
            }

            return selected;
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge) {
            final Transferable trans = new MavenTypeTransferable(getSelectedArtifacts());
            dragSource.startDrag(dge, DragSource.DefaultCopyDrop, trans, this);
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

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde) {
            // not needed
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class DnDList extends JXList implements DropTargetListener,
        DragSourceListener,
        DragGestureListener {

        //~ Instance fields ----------------------------------------------------

        // TODO: solve the log4j class loading issue
        // private static final transient Logger LOG = Logger.getLogger(DnDList.class);

        private final transient DragSource dragSource;
        private final transient int acceptableActions;
        private final transient DropTarget dt;

        private boolean delete;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DnDTree object.
         */
        DnDList() {
            super(true);
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this);                            // drag gesture recognizer
            acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
            dt = new DropTarget(this, acceptableActions, this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        List<Artifact> getSelectedArtifacts() {
            final int[] indices = getSelectedIndices();
            final List<Artifact> artifacts = new ArrayList<Artifact>(indices.length);

            for (final int index : indices) {
                artifacts.add((Artifact)getModel().getElementAt(index));
            }

            return artifacts;
        }

        @Override
        public void drop(final DropTargetDropEvent dtde) {
            delete = false;
            try {
                final Object o = dtde.getTransferable().getTransferData(MavenTypeTransferable.ARTIFACT_LIST_FLAVOR);
                final List<Artifact> artifacts = (List<Artifact>)o;
                final DefaultListModel model = (DefaultListModel)getModel();
                for (final Artifact artifact : artifacts) {
                    if (!model.contains(artifact)) {
                        model.addElement(artifact);
                    }
                }
                setSortOrder(SortOrder.ASCENDING);
                dtde.dropComplete(true);
            } catch (final UnsupportedFlavorException ex) {
//                LOG.warn("unsupported flavor occured during drop", ex);
                dtde.dropComplete(false);
            } catch (final IOException ex) {
//                LOG.warn("ioexception occured during drop", ex);
                dtde.dropComplete(false);
            }
        }

        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
            // not needed
        }

        @Override
        public void dragExit(final DropTargetEvent dte) {
            // not needed
        }

        @Override
        public void dragEnter(final DragSourceDragEvent dsde) {
            delete = false;
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
            delete = true;
        }

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde) {
            if (delete) {
                try {
                    final Object o = dsde.getDragSourceContext()
                                .getTransferable()
                                .getTransferData(MavenTypeTransferable.ARTIFACT_LIST_FLAVOR);
                    final List<Artifact> artifacts = (List<Artifact>)o;
                    final DefaultListModel model = (DefaultListModel)getModel();
                    for (final Artifact artifact : artifacts) {
                        model.removeElement(artifact);
                    }
                } catch (final UnsupportedFlavorException ex) {
                } catch (final IOException ex) {
                } finally {
                    delete = false;
                }
            }
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge) {
            final Transferable trans = new MavenTypeTransferable(getSelectedArtifacts());
            dragSource.startDrag(dge, DragSource.DefaultCopyDrop, trans, this);
        }
    }
}

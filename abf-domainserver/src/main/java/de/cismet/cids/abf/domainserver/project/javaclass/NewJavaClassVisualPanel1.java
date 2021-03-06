/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.javaclass;

import org.apache.log4j.Logger;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeSelectionModel;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.utils.Renderers;
import de.cismet.cids.abf.utilities.CidsDistClassLoader;
import de.cismet.cids.abf.utilities.SimpleManifest;

import de.cismet.cids.jpa.entity.cidsclass.JavaClass;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public final class NewJavaClassVisualPanel1 extends JPanel implements ExplorerManager.Provider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewJavaClassVisualPanel1.class);

    //~ Instance fields --------------------------------------------------------

    private final transient RequestProcessor reqProc;
    private final transient RequestProcessor.Task updateTask;
    private final transient RequestProcessor.Task expandTask;

    private final transient DocumentListener docL;

    private final transient DomainserverProject project;
    private final transient FutureTask<CidsDistClassLoader> loaderFuture;
    private final transient ExplorerManager manager;
    private final transient Image classIcon;
    private final transient Image jarIcon;
    private final transient NewJavaClassWizardPanel1 model;
    private transient List<SimpleManifest> manifestList;
    private transient CidsDistClassLoader loader;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JComboBox cboType = new javax.swing.JComboBox();
    private final transient javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
    private final transient javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
    private final transient javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblChosenClass = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblNotice = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblType = new javax.swing.JLabel();
    private final transient javax.swing.JList lstUsability = new javax.swing.JList();
    private final transient javax.swing.JScrollPane scpBeanTreeView = new BeanTreeView();
    private final transient javax.swing.JTabbedPane tpaClass = new javax.swing.JTabbedPane();
    private final transient javax.swing.JTextArea txaNotice = new javax.swing.JTextArea();
    private final transient javax.swing.JTextField txtClass = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form NewJavaClassVisualPanel1.
     *
     * @param   project  DOCUMENT ME!
     * @param   model    DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public NewJavaClassVisualPanel1(final DomainserverProject project, final NewJavaClassWizardPanel1 model) {
        this.model = model;
        this.project = project;
        manager = new ExplorerManager();
        docL = new DocumentListenerImpl();
        classIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "javaclass.png");                 // NOI18N
        jarIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "jar_16.gif");                    // NOI18N
        reqProc = new RequestProcessor("updater", 1, true); // NOI18N
        loaderFuture = new FutureTask(new Callable<CidsDistClassLoader>() {

                    @Override
                    public CidsDistClassLoader call() throws Exception {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("init cdcl");        // NOI18N
                        }
                        final CidsDistClassLoader cdcl = CidsDistClassLoader.getInstance(project.getDistRoot());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("cdcl initialized"); // NOI18N
                        }
                        NewJavaClassVisualPanel1.this.loader = cdcl;
                        return cdcl;
                    }
                });
        expandTask = reqProc.create(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (manager) {
                            if (Thread.interrupted()) {
                                return;
                            }
                            ((TreeView)scpBeanTreeView).expandAll();
                        }
                    }
                });
        updateTask = reqProc.create(new Runnable() {

                    @Override
                    public void run() {
                        if (Thread.interrupted()) {
                            return;
                        }
                        if (loader == null) {
                            try {
                                loader = loaderFuture.get(300, TimeUnit.MILLISECONDS);
                            } catch (final ExecutionException ex) {
                                LOG.error("execution aborted, " // NOI18N
                                            + "update cancelled", ex); // NOI18N
                                return;
                            } catch (final TimeoutException ex) {
                                LOG.error("timeout occurred, " // NOI18N
                                            + "update cancelled", ex); // NOI18N
                                return;
                            } catch (final InterruptedException ex) {
                                LOG.warn("execution interrupted, " // NOI18N
                                            + "update cancelled", ex); // NOI18N
                                return;
                            }
                        }
                        if (loader == null) {
                            throw new IllegalStateException(
                                "at this point "          // NOI18N
                                        + "loader cannot be null! debug required!"); // NOI18N
                        }
                        if (Thread.interrupted()) {
                            return;
                        }
                        final Map<String, String[]> hits = loader.getHits(
                                txtClass.getText());
                        if (Thread.interrupted()) {
                            return;
                        }
                        if (hits != null) {
                            updateTree(hits);
                        }
                    }
                });
        initComponents();
        init();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.getName().returnvalue"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    JavaClass getJavaClass() {
        final JavaClass javaClass = new JavaClass();
        javaClass.setQualifier(txtClass.getText());
        javaClass.setNotice(txaNotice.getText());
        javaClass.setType(cboType.getSelectedItem().toString());
        return javaClass;
    }

    /**
     * DOCUMENT ME!
     */
    private void init() {
        manager.addPropertyChangeListener(new PropertyChangeListenerImpl());
        final BeanTreeView tree = (BeanTreeView)scpBeanTreeView;
        tree.setRootVisible(false);
        tree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        reqProc.post(loaderFuture);
        txtClass.setText("");                                   // NOI18N
        txtClass.getDocument().addDocumentListener(docL);
        tpaClass.setTitleAt(
            0,
            org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.tpaClass.titleAt0")); // NOI18N
        tpaClass.setTitleAt(
            1,
            org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.tpaClass.titleAt1")); // NOI18N
        tpaClass.setTitleAt(
            2,
            org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.tpaClass.titleAt2")); // NOI18N
        tpaClass.setSelectedIndex(0);
        cboType.removeAllItems();
        final Field[] fields = JavaClass.Type.class.getFields();
        try {
            for (int i = 0; i < fields.length; ++i) {
                final Object o = fields[i].get(new Object());
                cboType.addItem(((JavaClass.Type)o).getType());
            }
        } catch (IllegalArgumentException ex) {
            LOG.error("could not add type to combobox", ex);    // NOI18N
        } catch (IllegalAccessException ex) {
            LOG.error("could not add type to combobox", ex);    // NOI18N
        }
        cboType.setRenderer(new Renderers.JavaClassTypeRenderer());
        final DefaultListModel lModel = new DefaultListModel();
        loadManifests();
        for (final SimpleManifest manifest : manifestList) {
            lModel.addElement(manifest);
        }
        lstUsability.setModel(lModel);
        lstUsability.setCellRenderer(new UsabilityRenderer());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void loadManifests() {
        final FileObject lib = project.getDistRoot().getFileObject("lib"); // NOI18N
        if (lib == null) {
            throw new IllegalStateException("could not locate lib dir");   // NOI18N
        }
        final List<FileObject> manDirs = new LinkedList<FileObject>();
        final Enumeration<? extends FileObject> dirs = lib.getFolders(true);
        while (dirs.hasMoreElements()) {
            final FileObject fo = dirs.nextElement();
            if (fo.getName().startsWith("starter"))                        // NOI18N
            {
                final FileObject manDir = fo.getFileObject("src/plain");   // NOI18N
                if (manDir != null) {
                    manDirs.add(manDir);
                }
            }
        }
        // TODO: do not throw uncaught exception, rather show message in starter
        // tab to indicate that dir is not present and nothing can be chkd
        if (manDirs.isEmpty()) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                NewJavaClassVisualPanel1.class,
                                "NewJavaClassVisualPanel1.loadManifests().JOptionPane.noStarterFolder.message"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                NewJavaClassVisualPanel1.class,
                                "NewJavaClassVisualPanel1.loadManifests().JOptionPane.noStarterFolder.title"), // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            throw new IllegalStateException(
                "cidsDistribution has no starter dirs");                                                       // NOI18N
        }
        final ArrayList<Manifest> manifests = new ArrayList<Manifest>(5);
        manifestList = new ArrayList<SimpleManifest>(5);
        final String libPath = FileUtil.toFile(lib).getAbsolutePath() + File.separator;
        for (final FileObject manDir : manDirs) {
            final Enumeration<? extends FileObject> e = manDir.getData(false);
            while (e.hasMoreElements()) {
                final FileObject f = e.nextElement();
                if ("mf".equalsIgnoreCase(f.getExt()))                                                         // NOI18N
                {
                    try {
                        final String absPath = FileUtil.toFile(f).getAbsolutePath();
                        final String display =
                            absPath.replace(libPath, "")                                                       // NOI18N
                            .replace(File.separator
                                        + "src"                                                                // NOI18N
                                        + File.separator
                                        + "plain"                                                              // NOI18N
                                        + File.separator,
                                " :: ");                                                                       // NOI18N
                        manifests.add(new Manifest(f.getInputStream()));
                        manifestList.add(new SimpleManifest(
                                display.substring(0,
                                    display.length()
                                            - 3),
                                absPath,
                                new String[0]));
                    } catch (Exception ex) {
                        LOG.error("could not read manifest: "                                                  // NOI18N
                                    + f.getNameExt(), ex);
                    }
                }
            }
        }
        for (int i = 0; i < manifests.size(); ++i) {
            final Manifest manifest = manifests.get(i);
            final String cp = manifest.getMainAttributes().getValue(
                    Attributes.Name.CLASS_PATH);
            if (cp == null) {
                LOG.warn("no cp entry found in: " + manifest);                                                 // NOI18N
                continue;
            }
            final String[] jars = cp.split(" ");                                                               // NOI18N
            final List<String> onlyJars = new ArrayList<String>();
            for (final String jar : jars) {
                if ((jar != null)
                            && !jar.equals("")                                                                 // NOI18N
                            && (jar.charAt(0) != '-'))                                                         // NOI18N
                {
                    final FileObject jarFO = lib.getFileObject(jar.substring(3));
                    if (jarFO != null) {
                        onlyJars.add(FileUtil.toFile(jarFO).getAbsolutePath());
                    } else {
                        LOG.warn("could not find jarfile: " + jar);                                            // NOI18N
                    }
                }
            }
            manifestList.get(i).setClassPath(onlyJars);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  values  DOCUMENT ME!
     */
    private void updateTree(final Map<String, String[]> values) {
        if (Thread.interrupted()) {
            return;
        }
        final Node root = new AbstractNode(new RootNodeChildren(values));
        manager.setRootContext(root);
        // TODO: fix expander and update in general
        if (values.size() < 11) {
            expandTask.schedule(50);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblChosenClass.setText(org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.lblChosenClass.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                scpBeanTreeView,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                653,
                Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                scpBeanTreeView,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                369,
                Short.MAX_VALUE));

        tpaClass.addTab("tab1", jPanel1);

        lblType.setText(org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.lblType.text")); // NOI18N

        lblNotice.setText(org.openide.util.NbBundle.getMessage(
                NewJavaClassVisualPanel1.class,
                "NewJavaClassVisualPanel1.lblNotice.text")); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txaNotice.setColumns(20);
        txaNotice.setRows(5);
        txaNotice.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txaNotice);

        final org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                jPanel2Layout.createSequentialGroup().addContainerGap().add(
                    jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        jScrollPane1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        629,
                        Short.MAX_VALUE).add(
                        jPanel2Layout.createSequentialGroup().add(lblType).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(cboType, 0, 506, Short.MAX_VALUE)).add(
                        lblNotice)).addContainerGap()));
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                jPanel2Layout.createSequentialGroup().addContainerGap().add(
                    jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblType).add(
                        cboType,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(19, 19, 19).add(lblNotice).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jScrollPane1,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    284,
                    Short.MAX_VALUE).addContainerGap()));

        tpaClass.addTab("tab2", jPanel2);

        jScrollPane2.setViewportView(lstUsability);

        final org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                jScrollPane2,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                653,
                Short.MAX_VALUE));
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                jScrollPane2,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                369,
                Short.MAX_VALUE));

        tpaClass.addTab("tab3", jPanel3);

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        tpaClass,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        661,
                        Short.MAX_VALUE).add(
                        layout.createSequentialGroup().add(lblChosenClass).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            txtClass,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            561,
                            Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblChosenClass).add(
                        txtClass,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    tpaClass,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    406,
                    Short.MAX_VALUE).addContainerGap()));
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class RootNodeChildren extends Children.Keys {

        //~ Instance fields ----------------------------------------------------

        // using the map interface causes weird behavior. it must be specified
        // absolutely to work as expected, probably because of the super class
        private final transient java.util.Map<String, String[]> values;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RootNodeChildren object.
         *
         * @param  values  DOCUMENT ME!
         */
        RootNodeChildren(final java.util.Map<String, String[]> values) {
            this.values = values;
            final String[] keySet = new String[values.size()];
            values.keySet().toArray(keySet);
            Arrays.sort(keySet);
            setKeys(keySet);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object object) {
            final String key = object.toString();
            return new Node[] { new JarNode(key, values.get(key)) };
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class JarNode extends AbstractNode {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new JarNode object.
         *
         * @param  name     DOCUMENT ME!
         * @param  classes  DOCUMENT ME!
         */
        JarNode(final String name, final String[] classes) {
            super(new JarNodeChildren(classes));
            final int last = name.lastIndexOf(File.separator);
            if (last == -1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("jarnode" + name); // NOI18N
                }
                setName(name);
            } else {
                setName(name.substring(last + 1));
            }
            setShortDescription(name);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Image getIcon(final int i) {
            return jarIcon;
        }

        @Override
        public Image getOpenedIcon(final int i) {
            return jarIcon;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class JarNodeChildren extends Children.Keys {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new JarNodeChildren object.
         *
         * @param  classes  DOCUMENT ME!
         */
        JarNodeChildren(final String[] classes) {
            Arrays.sort(classes);
            setKeys(classes);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected Node[] createNodes(final Object object) {
            return new Node[] { new ClassNode(object.toString()) };
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class ClassNode extends AbstractNode {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ClassNode object.
         *
         * @param  name  DOCUMENT ME!
         */
        ClassNode(final String name) {
            super(Children.LEAF);
            setName(name);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Image getIcon(final int i) {
            return classIcon;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class DocumentListenerImpl implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        // every method schedules the task directly instead of delegating it.
        // done for performance reasons
        @Override
        public void insertUpdate(final DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            updateTask.cancel();
            updateTask.schedule(0);
            model.fireChangeEvent();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class PropertyChangeListenerImpl implements PropertyChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                final Node[] n = manager.getSelectedNodes();
                if ((n != null) && (n.length == 1) && (n[0] instanceof ClassNode)) {
                    reqProc.post(new Runnable() {

                            @Override
                            public void run() {
                                txtClass.setText(((ClassNode)n[0]).getName());
                            }
                        });
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class UsabilityRenderer implements ListCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JCheckBox box = new JCheckBox();
            final SimpleManifest manifest = (SimpleManifest)value;
            box.setText(manifest.getManifestName());
            final String clazz = txtClass.getText();
            final String[] classPath = manifest.getClassPath();
            if (loader == null) {
                box.setSelected(false);
            } else {
                box.setSelected(loader.isLoadable(clazz, classPath));
            }
            return box;
        }
    }
}

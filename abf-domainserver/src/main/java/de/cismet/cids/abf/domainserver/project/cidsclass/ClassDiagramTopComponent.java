/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.AttributeWidget;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassGraphScene;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassNodeWidget;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.RelationWidget;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.SatelliteLookupHint;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.domainserver.project.view.ViewNode;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;

import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

// TODO: completely refactor this class
/**
 * Top component which displays something.
 *
 * @version  $Revision$, $Date$
 */
public final class ClassDiagramTopComponent extends TopComponent implements ExplorerManager.Provider {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -4208581785937901599L;
    private static final transient Logger LOG = Logger.getLogger(
            ClassDiagramTopComponent.class);

    /** path to the icon used by the component and its open action. */
    static final String ICON_PATH = "de/cismet/cids/abf/abfcore/projecttypes/"
        + "domainserver/nodes/classmanagement/classes.png";

    private static final Image FOREIGN_KEY = Utilities.loadImage(
            "de/cismet/cids/abf/abfcore/res/images/foreignKey.png",
            true); // NOI18N
    private static final Image SEARCH_INDEX = Utilities.loadImage(
            "de/cismet/cids/abf/abfcore/res/images/search.png",
            true); // NOI18N
    private static final Image PRIMARY_KEY = Utilities.loadImage(
            "de/cismet/cids/abf/abfcore/res/images/key.png",
            true); // NOI18N
    private static final Image ARRAY = Utilities.loadImage(
            "de/cismet/cids/abf/abfcore/res/images/array.png",
            true); // NOI18N

    private static final String DEFAULT_VIEW_NAME = "default";

    //~ Instance fields --------------------------------------------------------

    private transient ExplorerManager explorerManager = new ExplorerManager();

    private String viewName;
    private String preferredId;
    private JComponent myView;
    private ViewNode viewNode;
    private boolean restored;
    private DomainserverProject project;

    private final ClassGraphScene scene;
    private final HashMap<String, ClassNodeWidget> classNodeWidgets;
    private final HashMap<String, AttributeWidget> attributes;
    private final HashMap<String, RelationWidget> relations;
    private final Vector<CidsClassNode> allClasses;
    private final WidgetAction popupMenuAction;
    private final Set<CidsClassNode> classNodes;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkSaveChanges;
    private javax.swing.JButton cmdDoLayout;
    private javax.swing.JButton cmdResetZoom;
    private javax.swing.JButton cmdSetViewName;
    private javax.swing.JButton cmdZoomIn;
    private javax.swing.JButton cmdZoomOut;
    private javax.swing.JButton cmdZoomToAll;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane scpDiagramPane;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassDiagramTopComponent object.
     */
    private ClassDiagramTopComponent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating new ClassDiagrammTopComponent");
        }
        initComponents();
        viewName = DEFAULT_VIEW_NAME;
        scene = new ClassGraphScene();
        classNodeWidgets = new HashMap<String, ClassNodeWidget>();
        attributes = new HashMap<String, AttributeWidget>();
        relations = new HashMap<String, RelationWidget>();
        allClasses = new Vector<CidsClassNode>();
        classNodes = new HashSet<CidsClassNode>();
        popupMenuAction = ActionFactory.createPopupMenuAction(new MyPopupMenuProvider());
        // 2 Lookups zusammenfassen
        associateLookup(
            new ProxyLookup(
                new Lookup[] {
                    Lookups.singleton(this),
                    Lookups.fixed(
                        new Object[] { new SatelliteLookupHint() })
                }));
        try {
            setName(NbBundle.getMessage(ClassDiagramTopComponent.class,
                    "ClassDiagramTopComponent.name"));
            setToolTipText(NbBundle.getMessage(ClassDiagramTopComponent.class,
                    "ClassDiagramTopComponent.tooltip"));
            setIcon(Utilities.loadImage(ICON_PATH, true));
            if (LOG.isDebugEnabled()) {
                LOG.debug("trying to create view from scene");
            }
            myView = scene.createView();
            if (LOG.isDebugEnabled()) {
                LOG.debug("setting viewport of diagramm pane to created view");
            }
            scpDiagramPane.setViewportView(myView);
            final ObjectSceneListener l = new ObjectSceneListener() {

                    public void focusChanged(final ObjectSceneEvent ose, final Object object, final Object object0) {
                    }

                    public void highlightingChanged(
                            final ObjectSceneEvent e,
                            final Set<Object> s,
                            final Set<Object> s2) {
                    }

                    public void hoverChanged(final ObjectSceneEvent ose, final Object object, final Object object0) {
                    }

                    public void objectAdded(final ObjectSceneEvent objectSceneEvent,
                            final Object object) {
                    }

                    public void objectRemoved(final ObjectSceneEvent ose, final Object object) {
                    }

                    public void objectStateChanged(
                            final ObjectSceneEvent ose,
                            final Object object,
                            final ObjectState objectState,
                            final ObjectState objectState0) {
                    }

                    public void selectionChanged(final ObjectSceneEvent e, final Set<Object> s1, final Set<Object> s2) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("selection changed: " + e + " :: " + s1
                                + " :: " + s2);
                        }
                        Node activation = null;
                        final Set selection = getScene().getSelectedObjects();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("selected object count: " + selection.size());
                        }
                        if (selection.size() == 1) {
                            final Object sel = selection.iterator().next();
                            final ClassNodeWidget cnw = classNodeWidgets.get(sel);
                            if (cnw != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("got activated node from ClassNodeW"
                                        + "idget: " + cnw);
                                }
                                activation = cnw.getCidsClassNode();
                            } else {
                                final AttributeWidget aw = attributes.get(sel);
                                if (aw != null) {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("got activated node from Attrib"
                                            + "uteWidget: " + aw);
                                    }
                                    activation = aw.getCidsAttributeNode();
                                }
                            }
                            if (activation != null) {
                                try {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("changing explorermanager root "
                                            + "context");
                                    }
                                    explorerManager.setRootContext(activation);
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("changing explorermanager selec"
                                            + "ted nodes");
                                    }
                                    explorerManager.setSelectedNodes(
                                        new Node[] { activation });
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("setting activated nodes to exp"
                                            + "lorermanager's selection");
                                    }
                                    setActivatedNodes(explorerManager.getSelectedNodes());
                                } catch (final Exception ex) {
                                    LOG.error("could not set activated nodes", ex);
                                    ErrorUtils.showErrorMessage(
                                        "Es ist ein uner"
                                        + "warteter Fehler aufgetreten. Bitte "
                                        + "melden Sie den Fehler, damit er "
                                        + "schnellstmöglich behoben werden "
                                        + "kann.\n\nVielen Dank",
                                        ex);
                                }
                            }
                        }
                    }
                };
            scene.addObjectSceneListener(
                l,
                new ObjectSceneEventType[] { ObjectSceneEventType.OBJECT_SELECTION_CHANGED });
        } catch (final Exception ex) {
            LOG.error("error during initialisation", ex);
            ErrorUtils.showErrorMessage(
                "Es ist ein unerwarteter Fehler aufge"
                + "treten. Bitte melden Sie den Fehler, damit er schnellstm"
                + "öglich behoben werden kann.\n\nVielen Dank",
                ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cidsClassNodes  DOCUMENT ME!
     */
    public void addClassesWithRelations(final Collection<CidsClassNode> cidsClassNodes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding classes with relation: " + cidsClassNodes);
        }
        final Set<CidsClass> all = new HashSet<CidsClass>();
        for (final CidsClassNode ccn : cidsClassNodes) {
            all.addAll(getAllRelationClasses(ccn.getCidsClass(), new HashSet<CidsClass>()));
        }
        final Vector<CidsClassNode> v = new Vector<CidsClassNode>();
        for (final CidsClass cc : all) {
            v.add(new CidsClassNode(cc, project));
        }
        addClasses(v);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsClassNode  DOCUMENT ME!
     */
    public void addClassWithRelations(final CidsClassNode cidsClassNode) {
        final CidsClass c = cidsClassNode.getCidsClass();
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding class with relation: " + c.getName());
        }
        final Set<CidsClass> all = getAllRelationClasses(c, new HashSet<CidsClass>());
        final Vector<CidsClassNode> v = new Vector<CidsClassNode>();
        for (CidsClass cc : all) {
            v.add(new CidsClassNode(cc, project));
        }
        addClasses(v);
    }
    /**
     * TODO: try if this could be done by simply calling addclass for every node or if there will be issues regarding
     * the relations
     *
     * @param  cidsClassNodes  DOCUMENT ME!
     */
    public void addClasses(final Collection<CidsClassNode> cidsClassNodes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding classes: " + cidsClassNodes);
        }
        for (final CidsClassNode ccn : cidsClassNodes) {
            try {
                pureAddClass(ccn);
            } catch (final AssertionError ae) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("class was already in diagramm: " + ccn.getDisplayName(), ae);
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to add relations");
        }
        addRelations();
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to layout scene");
        }
        scene.layoutScene();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsClassNode  DOCUMENT ME!
     */
    private void pureAddClass(final CidsClassNode cidsClassNode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to purely add class: " + cidsClassNode);
        }
        pureAddClass(cidsClassNode, 0, 0, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsClassNode  DOCUMENT ME!
     * @param  x              DOCUMENT ME!
     * @param  y              DOCUMENT ME!
     * @param  minimized      DOCUMENT ME!
     */
    private void pureAddClass(final CidsClassNode cidsClassNode, final int x,
            final int y, final boolean minimized) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "trying to purely add class: " + cidsClassNode + " :: x"
                + " = " + x + " :: y = " + y + " :: minimized = "
                + minimized);
        }
        classNodes.add(cidsClassNode);
        final CidsClass cidsClass = cidsClassNode.getCidsClass();
        if (cidsClass == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("cidsclass of classnode is null, returning");
            }
            return;
        }
        final String domainServerName = cidsClassNode.getDomainserverProject().getDomainserverProjectNode().getName();
        final String widgetName = cidsClass.getId() + "@" + domainServerName;
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieving classnodewidget: " + widgetName);
        }
        ClassNodeWidget w = classNodeWidgets.get(widgetName);
        if (w == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("widget was null, adding new one");
            }
            w = (ClassNodeWidget)scene.addNode(widgetName);
            w.getActions().addAction(popupMenuAction);
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding pin to scene");
            }
            scene.addPin(widgetName, widgetName + ClassGraphScene.PIN_ID_DEFAULT_SUFFIX);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting widget minimized: " + minimized);
        }
        w.setMinimized(minimized);
        scene.validate();
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting widget's class node");
        }
        w.setCidsClassNode(cidsClassNode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting widget's preffered location: (" + x + ", " + y
                + ")");
        }
        w.setPreferredLocation(new Point(x, y));
        final Vector images = new Vector();
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieving icons");
        }
        final Image classIcon = ProjectUtils.getImageForIconAndProject(
                cidsClass.getClassIcon(),
                project);
        final Image objectIcon = ProjectUtils.getImageForIconAndProject(
                cidsClass.getObjectIcon(),
                project);
        images.add(classIcon);
        if (!cidsClass.getClassIcon().equals(cidsClass.getObjectIcon())) {
            images.add(objectIcon);
        }
        // Set the class stuff
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting widget's node properties");
        }
        w.setNodeProperties(cidsClassNode.getIcon(0), cidsClass.getTableName()
            + "   ", null, images);
        scene.validate();
        classNodeWidgets.put(widgetName, w);
        allClasses.add(cidsClassNode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to add the class attributes (children)");
        }
        for (final Attribute attr : cidsClass.getAttributes()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("current attribute: " + attr);
            }
            final String attrWidgetName;
            final String pkFieldName = cidsClass.getPrimaryKeyField();
            if (attr.getFieldName().equalsIgnoreCase(pkFieldName)) {
                attrWidgetName = widgetName + ".PK";
            } else {
                attrWidgetName = widgetName + "." + attr.getId();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding pin to scene");
            }
            final AttributeWidget pin = (AttributeWidget)scene.addPin(
                    widgetName,
                    attrWidgetName);
            scene.validate();
            // Set the atttribute name
            if (LOG.isDebugEnabled()) {
                LOG.debug("modifying the newly created pin: " + pin);
            }
            pin.setPinName(attr.getFieldName() + " ");
            pin.setCidsAttributeNode(new CidsAttributeNode(attr, cidsClass,
                    project));
            attributes.put(attrWidgetName, pin);
            if (LOG.isDebugEnabled()) {
                LOG.debug("setting the attribute's icons");
            }
            final Vector<Image> v = new Vector<Image>();
            if (attr.getFieldName().equalsIgnoreCase(pkFieldName)) {
                v.add(PRIMARY_KEY);
            }
            if (attr.isIndexed()) {
                v.add(SEARCH_INDEX);
            }
            if (attr.isArray()) {
                v.add(ARRAY);
            }
            if (attr.isForeignKey()) {
                v.add(FOREIGN_KEY);
            }
            pin.setGlyphs(v);
        }
        scene.validate();
    }
    /**
     * TODO: this is implemented via remove and add, refactor
     *
     * @param  ccn  DOCUMENT ME!
     */
    public void refreshClassWidget(final CidsClassNode ccn) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshing class widget for class node: " + ccn);
        }
        final CidsClass cidsClass = ccn.getCidsClass();
        final String domainServerName = ccn.getDomainserverProject().getDomainserverProjectNode().getName();
        final String widgetName = cidsClass.getId() + "@" + domainServerName;
        final ClassNodeWidget refresher = classNodeWidgets.get(widgetName);
        if (refresher != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("refreshing widget: " + refresher);
            }
            final int x = refresher.getLocation().x;
            final int y = refresher.getLocation().y;
            final boolean minimized = refresher.isMinimized();
            if (LOG.isDebugEnabled()) {
                LOG.debug("trying to remove relations");
            }
            removeRelations();
            if (LOG.isDebugEnabled()) {
                LOG.debug("removing widget from scene");
            }
            scene.removeNode(widgetName);
            classNodeWidgets.remove(widgetName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("trying to purely add class");
            }
            pureAddClass(ccn, x, y, minimized);
            if (LOG.isDebugEnabled()) {
                LOG.debug("trying to add relations");
            }
            addRelations();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ccn  DOCUMENT ME!
     */
    public void addClass(final CidsClassNode ccn) {
        try {
            pureAddClass(ccn);
        } catch (final AssertionError ae) {
            if (LOG.isInfoEnabled()) {
                LOG.info("class was already in diagramm: " + ccn.getDisplayName(), ae);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to add relations");
        }
        addRelations();
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to layout scene");
        }
        scene.layoutScene();
    }

    /**
     * DOCUMENT ME!
     */
    private void removeRelations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing relations");
        }
        Collection<String> keys = new Vector<String>(scene.getEdges());
        for (final String key : keys) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removing edge from scene for key: " + key);
                }
                scene.removeEdge(key);
            } catch (final Exception e) {
                LOG.warn("could not remove edge from scene for key: " + key);
            }
        }
        relations.clear();
    }

    /**
     * DOCUMENT ME!
     */
    private void addRelations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding relations");
        }
        for (final CidsClassNode classNode : allClasses) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("processing classnode: " + classNode);
            }
            final CidsClass cidsClass = classNode.getCidsClass();
            final String domainServerName = classNode.getDomainserverProject().getDomainserverProjectNode().getName();
            final String widgetName = cidsClass.getId() + "@"
                + domainServerName;
            for (final Attribute attr : cidsClass.getAttributes()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("processing attribute: " + attr);
                }
                final String attrWidgetName;
                final String pkFieldName = cidsClass.getPrimaryKeyField();
                if (attr.getFieldName().equalsIgnoreCase(pkFieldName)) {
                    attrWidgetName = widgetName + ".PK";
                } else {
                    attrWidgetName = widgetName + "." + attr.getId();
                }
                if (attr.isForeignKey()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("attr is foreign key: " + attr);
                    }
                    final String fromClassNodeWidgetString = widgetName;
                    final String toClassNodeWidgetString = attr.getForeignKeyClass() + "@" + domainServerName;
                    final String from = attrWidgetName;
                    final String edgeWidgetName = attrWidgetName + "->" + attr.getForeignKeyClass();
                    String to = toClassNodeWidgetString + ".PK";
                    if (attr.isArray()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("attr is array: " + attr);
                        }
                        try {
                            final Set<Attribute> foreignAttrs = attr.getCidsClass().getAttributes();
                            for (final Attribute foreignAttr : foreignAttrs) {
                                if (foreignAttr.getFieldName().equalsIgnoreCase(
                                                attr.getArrayKey())) {
                                    to = toClassNodeWidgetString + "."
                                        + foreignAttr.getId();
                                    break;
                                }
                            }
                        } catch (final Exception e) {
                            // Fehler weil die FKKlasse nicht vorhanden ist
                            LOG.warn("could not find foreign key class", e);
                        }
                    }
                    if (relations.get(edgeWidgetName) == null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("relation not present");
                        }
                        if (
                            (classNodeWidgets.get(toClassNodeWidgetString)
                                        != null)
                                    && (classNodeWidgets.get(
                                            fromClassNodeWidgetString) != null)) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("both related classes present, crea"
                                    + "ting new relation");
                            }
                            final RelationWidget edge = (RelationWidget)scene.addEdge(edgeWidgetName);
                            scene.setEdgeSource(edgeWidgetName, from);
                            scene.setEdgeTarget(edgeWidgetName, to);
                            scene.validate();
                            relations.put(edgeWidgetName, edge);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        scpDiagramPane = new javax.swing.JScrollPane();
        jToolBar1 = new javax.swing.JToolBar();
        chkSaveChanges = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        cmdSetViewName = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        cmdZoomOut = new javax.swing.JButton();
        cmdZoomIn = new javax.swing.JButton();
        cmdResetZoom = new javax.swing.JButton();
        cmdZoomToAll = new javax.swing.JButton();
        cmdDoLayout = new javax.swing.JButton();

        jLabel1.setBackground(java.awt.SystemColor.controlHighlight);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "jLabel1");

        setLayout(new java.awt.BorderLayout());
        add(scpDiagramPane, java.awt.BorderLayout.CENTER);

        chkSaveChanges.setSelected(true);
        chkSaveChanges.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chkSaveChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSaveChangesActionPerformed(evt);
            }
        });
        jToolBar1.add(chkSaveChanges);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/filesave.png"))); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 2));
        jToolBar1.add(jLabel2);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSeparator1.setMaximumSize(new java.awt.Dimension(1, 32767));
        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(cmdSetViewName, "Namen ändern");
        cmdSetViewName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSetViewNameActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdSetViewName);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSeparator2.setMaximumSize(new java.awt.Dimension(1, 32767));
        jToolBar1.add(jSeparator2);

        cmdZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/zoom_out.png"))); // NOI18N
        cmdZoomOut.setToolTipText("Zoom Out");
        cmdZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomOutActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdZoomOut);

        cmdZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/zoom_in.png"))); // NOI18N
        cmdZoomIn.setToolTipText("Zoom In");
        cmdZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomInActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdZoomIn);

        cmdResetZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/zoom_home.png"))); // NOI18N
        cmdResetZoom.setToolTipText("Originalgröße");
        cmdResetZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdResetZoomActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdResetZoom);

        cmdZoomToAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/zoom_fit.png"))); // NOI18N
        cmdZoomToAll.setToolTipText("alle anzeigen");
        cmdZoomToAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomToAllActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdZoomToAll);

        cmdDoLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/abf/domainserver/images/layout_orthogonal.png"))); // NOI18N
        cmdDoLayout.setToolTipText("Layout erneuern");
        cmdDoLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDoLayoutActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdDoLayout);

        add(jToolBar1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSaveChangesActionPerformed
    }//GEN-LAST:event_chkSaveChangesActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdSetViewNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSetViewNameActionPerformed
        if (LOG.isDebugEnabled()) {
            LOG.debug("asking for new view name");
        }
        final String newName = JOptionPane.showInputDialog(this,
                "Namen ändern: ", getViewName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("new name is: " + newName);
        }
        if ((newName != null) && (newName.trim().length() > 0)) {
            setViewName(newName);
        }
        ((ViewManagement)project.getLookup().lookup(ViewManagement.class)).refreshChildren();
    }//GEN-LAST:event_cmdSetViewNameActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomOutActionPerformed
        scene.setZoomFactor(scene.getZoomFactor() * 0.9);
        scene.revalidate();
        scene.validate();
    }//GEN-LAST:event_cmdZoomOutActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomInActionPerformed
        scene.setZoomFactor(scene.getZoomFactor() * 1.1);
        scene.revalidate();
        scene.validate();
    }//GEN-LAST:event_cmdZoomInActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdResetZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdResetZoomActionPerformed
        scene.setZoomFactor(1);
        scene.revalidate();
        scene.validate();
    }//GEN-LAST:event_cmdResetZoomActionPerformed

    /**
     * DOCUMENT ME!
     */
    public void zoomToFit() {
        Rectangle rectangle = new Rectangle(0, 0, 1, 1);
        for (final Widget widget : scene.getChildren()) {
            rectangle = rectangle.union(widget.convertLocalToScene(widget.getBounds()));
        }
        final Dimension dim = rectangle.getSize();
        final Dimension viewDim = scpDiagramPane.getViewportBorderBounds().getSize();
        scene.setZoomFactor(Math.min((float)viewDim.width / dim.width, (float)viewDim.height / dim.height));
        scene.revalidate();
        scene.validate();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomToAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomToAllActionPerformed
        zoomToFit();
    }//GEN-LAST:event_cmdZoomToAllActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdDoLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDoLayoutActionPerformed
        scene.layoutScene();
    }//GEN-LAST:event_cmdDoLayoutActionPerformed

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only, i.e. deserialization routines;
     * otherwise you could get a non-deserialized instance. To obtain the singleton instance, use {@link findInstance}.
     *
     * @return  DOCUMENT ME!
     */
    // TODO: this topcomponent is not used in terms of the api
    public static synchronized ClassDiagramTopComponent getDefault() {
        final ClassDiagramTopComponent comp = new ClassDiagramTopComponent();
        comp.preferredId = "ClassDiagramTopComponent" + "." + System.currentTimeMillis();
        return comp;
    }

    /**
     * Obtain the ClassDiagramTopComponent instance. Never call {@link #getDefault} directly!
     *
     * @return  DOCUMENT ME!
     */
// public static synchronized ClassDiagramTopComponent findInstance() {
// WindowManager.getDefault().findTopComponent("fdksjh");
// TopComponent win = WindowManager.getDefault().findTopComponent(preferredId);
// if (win == null) {
// ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find ClassDiagram component. It will not be located properly in the window system.");
//            return getDefault();
//        }
//        if (win instanceof ClassDiagramTopComponent) {
//            return (ClassDiagramTopComponent)win;
//        }
//        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
//        return getDefault();
//    }
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void componentOpened() {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void componentClosed() {
        try {
            // Wenn Änderungen gespeichert werden sollen dann jetzt
            if (chkSaveChanges.isSelected()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("saving diagramm: " + viewName);
                }
                final Document view = getViewDocument();
                if (viewNode != null) {
                    viewNode.setView(view);
                    viewNode.setTopComponent(null);
                }
                project.getProjectDirectory().refresh();
                final FileWriter out = new FileWriter(
                        new File(FileUtil.toFile(
                                project.getProjectDirectory()), getFileNameOfView(
                                viewName)));
                final OutputFormat format = OutputFormat.createPrettyPrint();
                // TODO: change encoding to UTF-8
                format.setEncoding("ISO-8859-1");
                try {
                    XMLWriter writer = new XMLWriter(out, format);
                    writer.write(view);
                    writer.flush();
                } catch (final Exception e) {
                    LOG.error("could not save diagramm to disk", e);
                    ErrorUtils.showErrorMessage(
                        "Das Diagramm '" + viewName
                        + "' konnte nicht gespeichert werden",
                        "Fehler beim"
                        + " Speichern",
                        e);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("diagramm shall not be saved: " + viewName);
                }
            }
        } catch (final Exception ex) {
            LOG.error("error during closing of component", ex);
        }
        ((ViewManagement)project.getLookup().lookup(ViewManagement.class)).refreshChildren();
    }

    /**
     * replaces this in object stream.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper(preferredId, viewName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected String preferredID() {
        return preferredId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DomainserverProject getDomainserverProject() {
        return project;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  project  DOCUMENT ME!
     */
    public void setDomainserverProject(final DomainserverProject project) {
        this.project = project;
        setViewName(getFreeViewname(project, "Neues Thema"));
        setToolTipText(NbBundle.getMessage(ClassDiagramTopComponent.class,
                "ClassDiagramTopComponent.tooltip"));
        explorerManager.setRootContext(project.getDomainserverProjectNode());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ClassGraphScene getScene() {
        return scene;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  viewName_  DOCUMENT ME!
     */
    public void setViewName(final String viewName_) {
        setViewName(viewName_, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  viewName_  DOCUMENT ME!
     * @param  forced     DOCUMENT ME!
     */
    public void setViewName(final String viewName_, final boolean forced) {
        try {
            if (forced) {
                this.viewName = viewName_;
            } else {
                this.viewName = getFreeViewname(project, viewName_);
            }
            setName(
                NbBundle.getMessage(ClassDiagramTopComponent.class,
                    "ClassDiagramTopComponent.name") + " [" + viewName + "@"
                + project.getDomainserverProjectNode().getName() + "]");
        } catch (final Exception e) {
            LOG.error("could not set viewname: " + viewName + " :: forced = "
                + forced, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   project  DOCUMENT ME!
     * @param   vn       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getFreeViewname(final DomainserverProject project,
            final String vn) {
        String ret = vn;
        int counter = 0;
        final Set opened = TopComponent.getRegistry().getOpened();
        final Vector<String> v = new Vector<String>();
        for (Object o : opened) {
            if (o instanceof ClassDiagramTopComponent) {
                v.add(((ClassDiagramTopComponent)o).getViewName());
            }
        }
        project.getProjectDirectory().refresh();
        while (
            ((project != null) && (project.getProjectDirectory().getFileObject(
                                getFileNameOfView(ret)) != null))
                    || (v.contains(ret))) {
            counter++;
            ret = vn + "-" + counter;
        }
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<CidsClass> getAllRelationClasses(final CidsClass cidsClass, final Set<CidsClass> relationClasses) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieving all relation classes for cidsclass: " + cidsClass);
        }
        relationClasses.add(cidsClass);
        for (final Attribute attr : cidsClass.getAttributes()) {
            if (attr.isForeignKey()) {
                final CidsClass foreignClass = project.getCidsDataObjectBackend()
                            .getEntity(CidsClass.class, attr.getForeignKeyClass());
                if ((foreignClass != null) && !(relationClasses.contains(foreignClass))) {
                    final Set sub = getAllRelationClasses(foreignClass, relationClasses);
                    relationClasses.addAll(sub);
                }
            }
        }
        return relationClasses;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   viewName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getFileNameOfView(final String viewName) {
        return "csClassView." + viewName + ".xml";
    }

    /**
     * DOCUMENT ME!
     *
     * @param  view  DOCUMENT ME!
     */
    public void restoreFromDocument(final Document view) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("restoring view from document: " + view);
        }
        final Backend backend = project.getCidsDataObjectBackend();
        setViewName(view.getRootElement().valueOf("//View/@name"), true);
        final double zoom = new Double(view.getRootElement().valueOf(
                    "//View/@zoom")).doubleValue();
        final double posX = new Double(view.getRootElement().valueOf(
                    "//View/@x")).doubleValue();
        final double posY = new Double(view.getRootElement().valueOf(
                    "//View/@y")).doubleValue();
        final List classes = view.selectNodes("//ClassNode");
        for (final Object o : classes) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("processing class: " + o);
            }
            if (o instanceof DefaultElement) {
                final String classKey = ((DefaultElement)o).valueOf("./@class");
                final int classId = new Integer(classKey.split("@")[0]).intValue();
                final int x = (int)new Double(((DefaultElement)o).valueOf(
                            "./@x")).doubleValue();
                final int y = (int)new Double(((DefaultElement)o).valueOf(
                            "./@y")).doubleValue();
                final String min = ((DefaultElement)o).valueOf("./@minimized");
                final boolean minimized = new Boolean(min).booleanValue();
                final CidsClass cidsClass = backend.getEntity(CidsClass.class,
                        classId);
                final CidsClassNode cNode = new CidsClassNode(cidsClass,
                        project);
                pureAddClass(cNode, x, y, minimized);
            }
        }
        addRelations();
        scene.setZoomFactor(zoom);
        final Rectangle r = myView.getVisibleRect();
        r.setBounds((int)posX, (int)posY, (int)r.getWidth(), (int)r.getHeight());
        EventQueue.invokeLater(new Runnable() {

                public void run() {
                    myView.scrollRectToVisible(r);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  widget  DOCUMENT ME!
     */
    public void removeClassWidget(final ClassNodeWidget widget) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing class widget: " + widget);
        }
        removeRelations();
        final CidsClassNode cidsClassNode = widget.getCidsClassNode();
        final String domainServerName = cidsClassNode.getDomainserverProject().getDomainserverProjectNode().getName();
        final CidsClass cidsClass = cidsClassNode.getCidsClass();
        final String widgetName = cidsClass.getId() + "@" + domainServerName;
        for (final Attribute attr : cidsClass.getAttributes()) {
            final String attrWidgetName;
            final String pkFieldName = cidsClass.getPrimaryKeyField();
            if (attr.getFieldName().equalsIgnoreCase(pkFieldName)) {
                attrWidgetName = widgetName + ".PK";
            } else {
                attrWidgetName = widgetName + "." + attr.getId();
            }
            attributes.remove(attrWidgetName);
        }
        classNodeWidgets.remove(widgetName);
        allClasses.remove(cidsClassNode);
        widget.removeFromParent();
        addRelations();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Document getViewDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating view document");
        }
        for (int i = 0; i < 1; ++i) {
            final Document viewDocument = DocumentHelper.createDocument();
            // TODO: change encoding to UTF-8
            viewDocument.setXMLEncoding("ISO-8859-1");
            final Element root = viewDocument.addElement(
                    "cidsABFClassViewInformation");
            try {
                final Element e = new DefaultElement("View");
                e.addAttribute("name", viewName);
                e.addAttribute("zoom", scene.getZoomFactor() + "");
                scene.getBounds();
                scene.getClientArea();
                e.addAttribute("x", myView.getVisibleRect().getX() + "");
                e.addAttribute("y", myView.getVisibleRect().getY() + "");
                for (final String key : classNodeWidgets.keySet()) {
                    final ClassNodeWidget cnw = classNodeWidgets.get(key);
                    final Point p = cnw.getPreferredLocation();
                    final double x = p.getX();
                    final double y = p.getY();
                    final Element pos = new DefaultElement("ClassNode");
                    pos.addAttribute("class", key);
                    pos.addAttribute("x", x + "");
                    pos.addAttribute("y", y + "");
                    pos.addAttribute("minimized", new Boolean(cnw.isMinimized()).toString());
                    e.add(pos);
                }
                root.add(e);
                return viewDocument;
            } catch (final Exception e) {
                LOG.warn("duplicate package, try again one time");
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isRestored() {
        return restored;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  restored  DOCUMENT ME!
     */
    public void setRestored(final boolean restored) {
        this.restored = restored;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ViewNode getViewNode() {
        return viewNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  viewNode  DOCUMENT ME!
     */
    public void setViewNode(final ViewNode viewNode) {
        this.viewNode = viewNode;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  saveChanges  DOCUMENT ME!
     */
    public void setSaveChanges(final boolean saveChanges) {
        chkSaveChanges.setSelected(saveChanges);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class ResolvableHelper implements Serializable {

        //~ Static fields/initializers -----------------------------------------

        private static final long serialVersionUID = 1L;

        //~ Instance fields ----------------------------------------------------

        private final String viewName;
        private final String id;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ResolvableHelper object.
         *
         * @param  id        DOCUMENT ME!
         * @param  viewName  DOCUMENT ME!
         */
        ResolvableHelper(final String id, final String viewName) {
            this.id = id;
            this.viewName = viewName;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Object readResolve() {
            final ClassDiagramTopComponent c = new ClassDiagramTopComponent();
            c.preferredId = id;
            c.setViewName(viewName);
            c.setRestored(true);
            return c;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class MyPopupMenuProvider implements PopupMenuProvider {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   widget         DOCUMENT ME!
         * @param   localLocation  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public JPopupMenu getPopupMenu(final Widget widget, final Point localLocation) {
            final JPopupMenu popupMenu = new JPopupMenu();
            final JMenuItem remove = new JMenuItem(((ClassNodeWidget)widget).getNodeName() + " entfernen");
            remove.addActionListener(
                new ActionListener() {

                    public void actionPerformed(final ActionEvent e) {
                        removeClassWidget((ClassNodeWidget)widget);
                    }
                });
            popupMenu.add(remove);
            return popupMenu;
        }
    }
}

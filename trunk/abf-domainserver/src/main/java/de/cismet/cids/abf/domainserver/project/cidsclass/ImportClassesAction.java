/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.awt.EventQueue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import de.cismet.cids.jpa.entity.common.CommonEntity;
import de.cismet.cids.jpa.entity.permission.Policy;

import static de.cismet.cids.abf.domainserver.project.cidsclass.ExportClassesAction.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ImportClassesAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ImportClassesAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final org.openide.nodes.Node[] nodes) {
        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final Properties p = project.getLookup().lookup(Properties.class);
        if (p == null) {
            throw new IllegalStateException("project.properties not found");                                             // NOI18N
        }
        final File inDir = new File(p.getProperty(ExportClassesAction.FILECHOOSER_DIR, System.getProperty("user.dir"))); // NOI18N
        final JFileChooser chooser = new JFileChooser(inDir);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory(); // NOI18N
                }

                @Override
                public String getDescription() {
                    return NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.getDescription().returnvalue"); // NOI18N
                }
            });

        int ret = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        while ((JFileChooser.APPROVE_OPTION == ret) && !chooser.getSelectedFile().canRead()) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.performAction(Node[]).JOptionPane.title"), // NOI18N
                JOptionPane.WARNING_MESSAGE);
            ret = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        }
        if (JFileChooser.APPROVE_OPTION == ret) {
            final File in = chooser.getSelectedFile();
            final String name = NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.performAction(Node[]).name") + "[" // NOI18N
                        + project.getProjectDirectory()                             // NOI18N
                        .getName() + "]";                                           // NOI18N
            final InputOutput io = IOProvider.getDefault().getIO(name, false);
            try {
                io.getOut().reset();
                io.getErr().reset();
            } catch (final IOException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("could not reset io tab", ex);                        // NOI18N
                }
            }
            io.select();
            final ProgressHandle handle = ProgressHandleFactory.createHandle(name);
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        handle.start();
                        handle.switchToIndeterminate();
                    }
                });
            try {
                if (importClasses(project.getCidsDataObjectBackend(), in, io, handle)) {
                    io.getOut()
                            .println(NbBundle.getMessage(
                                    ImportClassesAction.class,
                                    "ImportClassesAction.performAction(Node[]).importSucessfullCapitals"));
                    project.getLookup().lookup(ClassManagement.class).refresh();
                    project.getLookup().lookup(SyncManagement.class).refresh();
                }
            } finally {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            handle.finish();
                        }
                    });
            }
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportClassesAction.class, "ImportClassesAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { DomainserverContext.class };
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "database_import_16.gif"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(final org.openide.nodes.Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }

        final DomainserverProject firstProject = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        for (int i = 0; i < nodes.length; ++i) {
            final DomainserverProject project = nodes[i].getCookie(DomainserverContext.class).getDomainserverProject();
            // ensure that any selected node is in the same domainserver project
            if (!project.equals(firstProject)) {
                return false;
            }
        }

        return firstProject.isConnected();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   backend  DOCUMENT ME!
     * @param   in       DOCUMENT ME!
     * @param   io       DOCUMENT ME!
     * @param   handle   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean importClasses(final Backend backend,
            final File in,
            final InputOutput io,
            final ProgressHandle handle) {
        io.getOut()
                .println(NbBundle.getMessage(
                        ImportClassesAction.class,
                        "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).beginImportCapitals")); // NOI18N
        io.getOut().println();
        BufferedInputStream bis = null;
        Document doc = null;
        // parse the import file
        try {
            io.getOut()
                    .println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).readingBrackets") // NOI18N
                        + in.getName());
            bis = new BufferedInputStream(new FileInputStream(in));
            final InputSource is = new InputSource(bis);
            doc = XMLUtil.parse(is, false, true, null, new EntityResolver() {

                        @Override
                        public InputSource resolveEntity(final String publicId, final String systemId)
                                throws SAXException, IOException {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("trying to resolve entity: publicid: " + publicId + " systemid: " + systemId); // NOI18N
                            }

                            return new InputSource(new ByteArrayInputStream(new byte[0]));
                        }
                    });
        } catch (final Exception e) {
            LOG.error("could not parse document from file: " + in, e);                                                                      // NOI18N
            io.getErr().println();
            io.getErr()
                    .println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).readingInputFileError")             // NOI18N
                        + e.getMessage());
            e.printStackTrace(io.getErr());
            io.getErr().println();
            io.getErr()
                    .println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).importUnsuccessfulCapitalsError")); // NOI18N

            return false;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (final IOException e) {
                    LOG.warn("could not close input stream", e); // NOI18N
                }
            }
        }
        // validate the import file using the cids_export schema
        try {
            io.getOut()
                    .println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).validatingBrackets") // NOI18N
                        + in.getName());
            final SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = f.newSchema(this.getClass().getResource(EXPORT_SCHEMA));
            XMLUtil.validate(doc.getDocumentElement(), schema);
        } catch (final SAXException e) {
            LOG.error("input file not valid: " + in, e);                                                                     // NOI18N
            io.getErr().println();
            io.getErr()
                    .println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).validatingInputFileError") // NOI18N
                        + e.getMessage());
            e.printStackTrace(io.getErr());
            io.getErr().println();
            io.getErr()
                    .println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).importUnsuccessfulCapitalsError")); // NOI18N

            return false;
        }

        final NodeList classlist = doc.getElementsByTagNameNS(NAMESPACE, CS_CLASS);
        final List<CidsClass> classes = new ArrayList<CidsClass>(classlist.getLength());
        final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache =
            new HashMap<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>>();
        refcache.put(CidsClass.class, new HashMap<Integer, CidsClass>());
        refcache.put(Icon.class, new HashMap<Integer, Icon>());
        refcache.put(JavaClass.class, new HashMap<Integer, JavaClass>());
        refcache.put(Policy.class, new HashMap<Integer, Policy>());
        refcache.put(Attribute.class, new HashMap<Integer, Attribute>());
        refcache.put(ClassAttribute.class, new HashMap<Integer, ClassAttribute>());
        refcache.put(Type.class, new HashMap<Integer, Type>());
        final Map<String, Set<Attribute>> attrCache = new HashMap<String, Set<Attribute>>();
        final Map<CidsClass, Set<ClassAttribute>> classAttrCache = new HashMap<CidsClass, Set<ClassAttribute>>();
        io.getOut()
                .println(NbBundle.getMessage(
                        ImportClassesAction.class,
                        "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).createCacheBrackets")); // NOI18N
        final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache = getTypeCache(backend);
        final Map<Integer, CidsClass> foreignKeyCache = new HashMap<Integer, CidsClass>();
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    handle.switchToDeterminate(classlist.getLength());
                }
            });

        final Runnable progress = new Runnable() {

                @Override
                public void run() {
                    handle.progress(1);
                }
            };
        /*
         * create a cidsClass for each cs_class node in the import doucment and add them to the classes list
         */
        for (int i = 0; i < classlist.getLength(); ++i) {
            final Node node = classlist.item(i);
            classes.add(createClass(
                    node,
                    refcache,
                    typecache,
                    io.getOut(),
                    handle,
                    " ",
                    attrCache,
                    classAttrCache,
                    foreignKeyCache)); // NOI18N
            EventQueue.invokeLater(progress);
        }

        /*
         * save Icons
         */
        final HashMap<Integer, Icon> iconsMap = (HashMap<Integer, Icon>)refcache.get(Icon.class);
        for (final Icon i : iconsMap.values()) {
            backend.store(i);
        }

        /*
         * save JavaClasses
         */
        final HashMap<String, JavaClass> jcStringMap = new HashMap<String, JavaClass>();
        final HashMap<Integer, JavaClass> jcMap = (HashMap<Integer, JavaClass>)refcache.get(JavaClass.class);
        for (final JavaClass jc : jcMap.values()) {
            final String qualifier = jc.getQualifier();
            jcStringMap.put(qualifier, backend.store(jc));
        }

        /*
         * save Policies
         */
        final HashMap<Integer, Policy> policyMap = (HashMap<Integer, Policy>)refcache.get(Policy.class);
        for (final Policy p : policyMap.values()) {
            backend.store(p);
        }

        /*
         * save just the new cids classes, and types
         */
        final ArrayList<CidsClass> storedCidsClasses = new ArrayList<CidsClass>();
        for (final CidsClass cs : classes) {
            cs.setClassIcon(backend.getEntity(Icon.class, cs.getClassIcon().getName()));
            cs.setObjectIcon(backend.getEntity(Icon.class, cs.getObjectIcon().getName()));

            if ((cs.getEditor() != null) && (cs.getEditor().getId() == null)) {
                cs.setEditor(jcStringMap.get(cs.getEditor().getQualifier()));
            }
            if ((cs.getToString() != null) && (cs.getToString().getId() == null)) {
                cs.setToString(jcStringMap.get(cs.getToString().getQualifier()));
            }
            if ((cs.getRenderer() != null) && (cs.getRenderer().getId() == null)) {
                cs.setRenderer(jcStringMap.get(cs.getRenderer().getQualifier()));
            }
            if (cs.getAttributePolicy() != null) {
                cs.setAttributePolicy(backend.getEntity(Policy.class, cs.getAttributePolicy().getName()));
            }
            if (cs.getPolicy() != null) {
                cs.setPolicy(backend.getEntity(Policy.class, cs.getPolicy().getName()));
            }
            storedCidsClasses.add(backend.store(cs));
        }

        /*
         * after that add attributes and set types for them
         */
        for (final CidsClass cs : storedCidsClasses) {
            final String className = cs.getName();
            final Set<Attribute> attributes = attrCache.get(className);
            if (attributes != null) {
                for (final Attribute a : attributes) {
                    final Type type = a.getType();
                    final String typeName = type.getName();
                    a.setType(backend.getEntity(Type.class, typeName));
                    a.setCidsClass(cs);
                    if (a.isForeignKey()) {
                        final CidsClass fkClass = foreignKeyCache.get(a.getForeignKeyClass());
                        final Integer fkClassId = backend.getEntity(CidsClass.class, fkClass.getName()).getId();
                        a.setForeignKeyClass(fkClassId);
                    }
                    if ((a.getComplexEditor() != null) && (a.getComplexEditor().getId() == null)) {
                        a.setComplexEditor(jcStringMap.get(a.getComplexEditor().getQualifier()));
                    }
                    if ((a.getEditor() != null) && (a.getEditor().getId() == null)) {
                        a.setEditor(jcStringMap.get(a.getEditor().getQualifier()));
                    }
                    if ((a.getToString() != null) && (a.getToString().getId() == null)) {
                        a.setToString(jcStringMap.get(a.getToString().getQualifier()));
                    }
                }
                cs.setAttributes(attributes);
                backend.store(cs);
            }
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   backend  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> getTypeCache(final Backend backend) {
        final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> cache =
            new HashMap<Class<? extends CommonEntity>, List<? extends CommonEntity>>();

        cache.put(CidsClass.class, backend.getAllEntities(CidsClass.class));
        cache.put(Type.class, backend.getAllEntities(Type.class));
        cache.put(JavaClass.class, backend.getAllEntities(JavaClass.class));
        cache.put(Icon.class, backend.getAllEntities(Icon.class));
        cache.put(Attribute.class, backend.getAllEntities(Attribute.class));
        cache.put(ClassAttribute.class, backend.getAllEntities(ClassAttribute.class));
        cache.put(Policy.class, backend.getAllEntities(Policy.class));

        return cache;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   doc    DOCUMENT ME!
     * @param   which  DOCUMENT ME!
     * @param   id     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     *          <p>if an idref or cs_type node appears, this method will return the corresponding reference node in
     *          document, this means the node with the same id</p>
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private Node resolveNode(final Document doc, final Class<? extends CommonEntity> which, final Integer id) {
        // make a list with all nodes which could contain the referenced id
        final ArrayList<NodeList> allNodeLists = new ArrayList<NodeList>();
        if (CidsClass.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, CS_CLASS));
        } else if (Icon.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, CLASS_ICON_ID));
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, OBJECT_ICON_ID));
        } else if (JavaClass.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, TOSTRING));
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, RENDERER));
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, EDITOR));
        } else if (Policy.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, POLICY));
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, ATTR_POLICY));
        } else if (Attribute.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, CS_ATTR));
        } else if (ClassAttribute.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, CS_CLASS_ATTR));
        } else if (Type.class.isAssignableFrom(which)) {
            allNodeLists.add(doc.getElementsByTagNameNS(NAMESPACE, CS_TYPE));
        }

        /*
         * Search for the corresponding node in document
         */
        Boolean stop = false;
        Node rightParent = null;
        for (final NodeList nodelist : allNodeLists) {
            if (stop) {
                break;
            }
            for (int j = 0; j < nodelist.getLength(); j++) {
                if (stop) {
                    break;
                }
                // search in child nodes for the right id
                final NodeList idList = nodelist.item(j).getChildNodes();
                for (int k = 0; k < idList.getLength(); k++) {
                    final Node tmpChild = idList.item(k);
                    if (Node.ELEMENT_NODE == tmpChild.getNodeType()) {
                        final String name = tmpChild.getLocalName();
                        final String text = tmpChild.getTextContent();
                        // if the actual node is an id node, and the value of id is the same as idref
                        // we found the right Parent Node to move on
                        if (ID.equals(name) && (Integer.valueOf(text).equals(id))) {
                            rightParent = nodelist.item(j);
                            stop = true;
                            break;
                        }
                    }
                }
            }
        }
        if (rightParent == null) {
            throw new IllegalStateException("found idref but cache did not contain type " + id); // NOI18N
        }

        return rightParent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <T>        DOCUMENT ME!
     * @param   node       DOCUMENT ME!
     * @param   which      DOCUMENT ME!
     * @param   refcache   DOCUMENT ME!
     * @param   typecache  DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private <T extends CommonEntity> T resolveType(final Node node,
            final Class<T> which,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent) {
        Node child = node;

        if (FOREIGN_KEY_REF.equals(node.getLocalName()) || ID_REF.equals(node.getLocalName())) {
            child = node;
        } else {
            child = node.getFirstChild();
            while (Node.ELEMENT_NODE != child.getNodeType()) {
                child = child.getNextSibling();
            }
        }
        final String indent = preIndent + "\t"; // NOI18N
        // look in the refcache
        final Map<Integer, ? extends CommonEntity> types = refcache.get(which);
        final Integer idref = Integer.valueOf(child.getTextContent());
        final T cachedEntity = (T)types.get(idref);

        // if it's not in refcache, look in typecache
        if (cachedEntity == null) {
            final List<? extends CommonEntity> systemTypes = typecache.get(which);
            NodeList children = null;
            Node rightParent = null;
            // if the childNode is an idref node, first look for the corresponding node in doc

            if (ID_REF.equals(child.getLocalName()) || FOREIGN_KEY_REF.equals(child.getLocalName())) {
                final Document doc = child.getOwnerDocument();
                rightParent = resolveNode(doc, which, idref);
                if (rightParent == null) {
                    throw new IllegalStateException("Could not find id"); // NOI18N
                }
                children = rightParent.getChildNodes();
            } else {
                children = node.getChildNodes();
            }

            String comp = null;
            for (final CommonEntity ce : systemTypes) {
                if (CidsClass.class.isAssignableFrom(which)) {
                    if (comp == null) {
                        comp = getComparationValue(NNAME, children);
                    }
                    if (comp.equalsIgnoreCase(((CidsClass)ce).getName())) {
                        return (T)ce;
                    }
                } else if (Icon.class.isAssignableFrom(which)) {
                    if (comp == null) {
                        comp = getComparationValue(NNAME, children);
                    }
                    if (comp.equalsIgnoreCase(((Icon)ce).getName())) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).entityAllreadyExistsMessage",
                                PROCESS,
                                indent));
                        return (T)ce;
                    }
                } else if (JavaClass.class.isAssignableFrom(which)) {
                    if (comp == null) {
                        comp = getComparationValue(QUALIFIER, children);
                    }
                    // no ignorecase here because the qualifier is case sensitive
                    if (comp.equals(((JavaClass)ce).getQualifier())) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).entityAllreadyExistsMessage",
                                PROCESS,
                                indent));
                        return (T)ce;
                    }
                } else if (Type.class.isAssignableFrom(which)) {
                    if (comp == null) {
                        comp = getComparationValue(NNAME, children);
                    }
                    if (comp.equalsIgnoreCase(((Type)ce).getName())) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).entityAllreadyExistsMessage",
                                PROCESS,
                                indent));
                        return (T)ce;
                    }
                } else if (Policy.class.isAssignableFrom(which)) {
                    if (comp == null) {
                        comp = getComparationValue(NNAME, children);
                    }
                    if (comp.equalsIgnoreCase(((Policy)ce).getName())) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).entityAllreadyExistsMessage",
                                PROCESS,
                                indent));
                        return (T)ce;
                    }
                }
            }
            /*
             * now we know, that the element doesnt exists yet. we create it.
             */
            if (ID_REF.equals(child.getLocalName())) {
                final String parentName = rightParent.getLocalName();
                if (CS_CLASS.equals(parentName)) {
                    // return (T) createClass(rightParent,refcache,typecache,out, handle preIndent);
                } else if (CLASS_ICON_ID.equals(parentName)) {
                    return (T)createIcon(rightParent, refcache, typecache, out, preIndent);
                } else if (OBJECT_ICON_ID.equals(parentName)) {
                    return (T)createIcon(rightParent, refcache, typecache, out, preIndent);
                } else if (RENDERER.equals(parentName)) {
                    return (T)createJavaClass(rightParent, refcache, typecache, out, preIndent);
                } else if (TOSTRING.equals(parentName)) {
                    return (T)createJavaClass(rightParent, refcache, typecache, out, preIndent);
                } else if (EDITOR.equals(parentName)) {
                    return (T)createJavaClass(rightParent, refcache, typecache, out, preIndent);
                } else if (POLICY.equals(parentName)) {
                    return (T)createPolicy(rightParent, refcache, typecache, out, preIndent);
                } else if (ATTR_POLICY.equals(parentName)) {
                    return (T)createPolicy(rightParent, refcache, typecache, out, preIndent);
                } else if (CS_ATTR.equals(parentName)) {
                    // return (T) createAttribute()
                } else if (CS_CLASS_ATTR.equals(parentName)) {
                    // return (T) createClassAttribute();
                } else if (CS_TYPE.equals(parentName)) {
                    return (T)createType(rightParent, refcache, typecache, out, preIndent);
                }
            }
        } else {
            return cachedEntity;
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key    DOCUMENT ME!
     * @param   nodes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getComparationValue(final String key, final NodeList nodes) {
        for (int i = 0; i
                    < nodes.getLength(); ++i) {
            if (key.equals(nodes.item(i).getLocalName())) {
                return nodes.item(i).getTextContent();
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node             DOCUMENT ME!
     * @param   refcache         DOCUMENT ME!
     * @param   typecache        DOCUMENT ME!
     * @param   out              DOCUMENT ME!
     * @param   handle           DOCUMENT ME!
     * @param   preIndent        DOCUMENT ME!
     * @param   attrCache        DOCUMENT ME!
     * @param   classAttrCache   DOCUMENT ME!
     * @param   foreignKeyCache  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private CidsClass createClass(
            final Node node,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final ProgressHandle handle,
            final String preIndent,
            final Map<String, Set<Attribute>> attrCache,
            final Map<CidsClass, Set<ClassAttribute>> classAttrCache,
            final Map<Integer, CidsClass> foreignKeyCache) {
        final HashMap<Integer, CidsClass> cidsClasses = (HashMap<Integer, CidsClass>)refcache.get(CidsClass.class);
        final NodeList children = node.getChildNodes();
        // search for the name first, not efficient but user will get more appropriate info of what is going on
        String classname = null;
        for (int i = 0; i < children.getLength(); ++i) {
            final Node n = children.item(i);

            if ((Node.ELEMENT_NODE == n.getNodeType())
                        && (NNAME.equals(n.getLocalName()) || ID_REF.equals(n.getLocalName()))) {
                classname = n.getTextContent();
                break;
            }
        }

        if ((classname == null) || classname.trim().isEmpty()) {
            throw new IllegalStateException("classname is null or empty"); // NOI18N
        }
        out.println(NbBundle.getMessage(
                ImportClassesAction.class,
                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createClassMessage",
                PROCESS,
                preIndent,
                classname));
        CidsClass cidsClass = resolveType(node, CidsClass.class, refcache, typecache, out, preIndent);
        Integer id = null;
        if (cidsClass == null) {
            // increase indent
            final String indent = preIndent + "\t"; // NOI18N
            // we create a new cidsclass
            cidsClass = new CidsClass();
            final Set<Attribute> attributes = new HashSet<Attribute>();
            final Set<ClassAttribute> classAttributes = new HashSet<ClassAttribute>();

            /*
             * first create the type, because it could be used in attributes of other classes and so prevents infinite
             * loops
             */
            Node lastChild = node.getLastChild();
            while (lastChild.getPreviousSibling() != null) {
                if ((Node.ELEMENT_NODE == lastChild.getNodeType()) && lastChild.getNodeName().equals(CS_TYPE)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createTypeMessage",
                            CREATE,
                            indent));
                    Type type = resolveType(lastChild, Type.class, refcache, typecache, out, indent);
                    if (type == null) {
                        type = createType(lastChild, refcache, typecache, out, indent);
                    }
                    type.setCidsClass(cidsClass);
                    cidsClass.setType(type);
                    break;
                } else {
                    lastChild = lastChild.getPreviousSibling();
                }
            }
            for (int i = 0; i < children.getLength(); ++i) {
                final Node n = children.item(i);
                if (Node.ELEMENT_NODE == n.getNodeType()) {
                    final String localname = n.getLocalName();
                    final String value = n.getTextContent();
                    if (ID.equals(localname)) {
                        id = Integer.valueOf(value);
                    } else if (NNAME.equals(localname)) {
                        // we got it already but it is set here
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createNameMessage",
                                CREATE,
                                indent,
                                value));
                        cidsClass.setName(value);
                    } else if (CLASS_ICON_ID.equals(localname)) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createClassIconMessage",
                                CREATE,
                                indent));
                        // try to find icon in refcache or typecache, if not create a new one
                        Icon icon = resolveType(n, Icon.class, refcache, typecache, out, indent);
                        if (icon == null) {
                            icon = createIcon(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setClassIcon(icon);
                    } else if (DESCR.equals(localname)) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createDescriptionMessage",
                                CREATE,
                                indent,
                                value));
                        cidsClass.setDescription(value);
                    } else if (OBJECT_ICON_ID.equals(localname)) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createObjectIconMessage",
                                CREATE,
                                indent));
                        // try to find icon in refcache or typecache, if not create a new one
                        Icon icon = resolveType(n, Icon.class, refcache, typecache, out, indent);
                        if (icon == null) {
                            icon = createIcon(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setObjectIcon(icon);
                    } else if (TABLE_NAME.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createTableNameMessage",
                                CREATE,
                                indent,
                                value));
                        cidsClass.setTableName(value);
                    } else if (PRIM_KEY_FIELD.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createPrimKeyFieldMessage",
                                CREATE,
                                indent,
                                value));
                        cidsClass.setPrimaryKeyField(value);
                    } else if (INDEXED.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createIndexedMessage",
                                CREATE,
                                indent,
                                value));
                        cidsClass.setIndexed(Boolean.valueOf(value));
                    } else if (TOSTRING.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createToStringMessage",
                                CREATE,
                                indent));
                        // try to find JavaClass in caches, if not create a new one
                        JavaClass jc = resolveType(n, JavaClass.class, refcache, typecache, out, indent);
                        if (jc == null) {
                            jc = createJavaClass(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setToString(jc);
                    } else if (EDITOR.equals(localname)) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createEditorMessage",
                                CREATE,
                                indent));
                        // try to find JavaClass in caches, if not create a new one
                        JavaClass jc = resolveType(n, JavaClass.class, refcache, typecache, out, indent);
                        if (jc == null) {
                            jc = createJavaClass(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setEditor(jc);
                    } else if (RENDERER.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createRendererMessage",
                                CREATE,
                                indent));
                        // try to find JavaClass in caches, if not create a new one
                        JavaClass jc = resolveType(n, JavaClass.class, refcache, typecache, out, indent);
                        if (jc == null) {
                            jc = createJavaClass(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setRenderer(jc);
                    } else if (ARRAY_LINK.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createArrayLinkMessage",
                                CREATE,
                                indent,
                                value));
                        cidsClass.setArrayLink(Boolean.valueOf(value));
                    } else if (POLICY.equals(localname)) {
                        out.println(NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createPolicyMessage",
                                CREATE,
                                indent));
                        Policy policy = resolveType(n, Policy.class, refcache, typecache, out, indent);
                        if (policy == null) {
                            policy = createPolicy(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setPolicy(policy);
                    } else if (ATTR_POLICY.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createAttrPolicyMessage",
                                CREATE,
                                indent));
                        Policy policy = resolveType(n, Policy.class, refcache, typecache, out, indent);
                        if (policy == null) {
                            policy = createPolicy(n, refcache, typecache, out, indent);
                        }
                        cidsClass.setPolicy(policy);
                    } else if (CS_ATTR.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createAttrMessage",
                                CREATE,
                                indent));
                        Attribute attr = resolveType(n, Attribute.class, refcache, typecache, out, indent);
                        if (attr == null) {
                            attr = createAttribute(
                                    n,
                                    refcache,
                                    typecache,
                                    out,
                                    indent,
                                    attrCache,
                                    classAttrCache,
                                    foreignKeyCache);
                            attr.setCidsClass(cidsClass);
                        }
                        attributes.add(attr);
                    } else if (CS_CLASS_ATTR.equals(localname)) {
                        out.println(
                            NbBundle.getMessage(
                                ImportClassesAction.class,
                                "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createClassAttrMessage",
                                CREATE,
                                indent));
                        ClassAttribute csAttr = resolveType(n, ClassAttribute.class, refcache, typecache, out, indent);
                        if (csAttr == null) {
                            csAttr = createClassAttribute(
                                    n,
                                    refcache,
                                    typecache,
                                    out,
                                    indent,
                                    attrCache,
                                    classAttrCache,
                                    foreignKeyCache);
                            csAttr.setCidsClass(cidsClass);
                        }
                        classAttributes.add(csAttr);
                    }
                }
            }
            attrCache.put(cidsClass.getName(), attributes);
            classAttrCache.put(cidsClass, classAttributes);
        } else {
            out.println(NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).entityAllreadyExistsMessage",
                    PROCESS,
                    preIndent));
        }
        if (id != null) {
            cidsClasses.put(id, cidsClass);
            refcache.put(CidsClass.class, cidsClasses);
        }
        return cidsClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n          DOCUMENT ME!
     * @param   refcache   DOCUMENT ME!
     * @param   typecache  DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public Icon createIcon(final Node n,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent) {
        final String indent = preIndent + "\t"; // NOI18N
        final HashMap<Integer, Icon> iconList = (HashMap<Integer, Icon>)refcache.get(Icon.class);
        final Icon icon = new Icon();

        final NodeList children = n.getChildNodes();
        Integer id = null;
        for (int j = 0; j
                    < children.getLength(); j++) {
            if (Node.ELEMENT_NODE == children.item(j).getNodeType()) {
                final Node child = children.item(j);
                final String name = child.getLocalName();
                final String text = child.getTextContent();
                if (ID.equals(name)) {
                    id = Integer.valueOf(text);
                }
                if (NNAME.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createNameMessage",
                            CREATE,
                            indent,
                            text));
                    icon.setName(text);
                } else if (FILE_NAME.equals(name)) {
                    out.println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createIcon(Node,Map,Map,PrintWriter,String).createFileNameMessage",
                            CREATE,
                            preIndent,
                            text));
                    icon.setFileName(text);
                }
            }
        }
        if (id == null) {
            throw new IllegalStateException("Could not find id"); // NOI18N
        }
        iconList.put(id, icon);
        refcache.put(Icon.class, iconList);
        return icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n          DOCUMENT ME!
     * @param   refcache   DOCUMENT ME!
     * @param   typecache  DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public JavaClass createJavaClass(final Node n,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent) {
        final String indent = preIndent + "\t"; // NOI18N
        final HashMap<Integer, JavaClass> javaClassesList = (HashMap<Integer, JavaClass>)refcache.get(
                JavaClass.class);

        final JavaClass jc = new JavaClass();
        final NodeList children = n.getChildNodes();
        Integer id = null;
        for (int i = 0; i
                    < children.getLength(); i++) {
            final Node child = children.item(i);
            if (Node.ELEMENT_NODE == child.getNodeType()) {
                final String name = child.getLocalName();
                final String text = child.getTextContent();

                if (ID.equals(name)) {
                    id = Integer.valueOf(text);
                }
                if (QUALIFIER.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createJavaClass(Node,Map,Map,PrintWriter,String).createQualifierMessage",
                            CREATE,
                            indent,
                            text));
                    jc.setQualifier(text);
                } else if (TYPE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createJavaClass(Node,Map,Map,PrintWriter,String).createTypeMessage",
                            CREATE,
                            indent,
                            text));
                    jc.setType(text);
                } else if (NOTICE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createJavaClass(Node,Map,Map,PrintWriter,String).createNoticeMessage",
                            CREATE,
                            indent,
                            text));
                    jc.setNotice(text);
                }
            }
        }
        if (id == null) {
            throw new IllegalStateException("Could not find id"); // NOI18N
        }

        javaClassesList.put(id, jc);
        refcache.put(JavaClass.class, javaClassesList);
        return jc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n          DOCUMENT ME!
     * @param   refcache   DOCUMENT ME!
     * @param   typecache  DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public Type createType(final Node n,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent) {
        final String indent = preIndent + "\t"; // NOI18N
        final HashMap<Integer, Type> typeList = (HashMap<Integer, Type>)refcache.get(Type.class);

        final Type type = new Type();
        final NodeList children = n.getChildNodes();
        Integer id = null;
        for (int i = 0; i
                    < children.getLength(); i++) {
            if (Node.ELEMENT_NODE == children.item(i).getNodeType()) {
                final String name = children.item(i).getLocalName();
                final String text = children.item(i).getTextContent();
                if (ID.equals(name)) {
                    id = Integer.valueOf(text);
                } else if (NNAME.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createNameMessage",
                            CREATE,
                            indent,
                            text));
                    type.setName(text);
                } else if (COMPLEX_TYPE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createType(Node,Map,Map,PrintWriter,String).createComplexTypeMessage",
                            CREATE,
                            preIndent,
                            text));
                    type.setComplexType(Boolean.valueOf(text));
                } else if (DESCR.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createDescriptionMessage",
                            CREATE,
                            preIndent,
                            text));
                    type.setDescription(text);
                } else if (EDITOR.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createEditorMessage",
                            CREATE,
                            preIndent));
                    final JavaClass jc = createJavaClass(children.item(i), refcache, typecache, out, preIndent);
                    type.setEditor(jc);
                } else if (RENDERER.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createRendererMessage",
                            CREATE,
                            preIndent));
                    final JavaClass jc = createJavaClass(children.item(i), refcache, typecache, out, preIndent);
                    type.setEditor(jc);
                }
            }
        }
        if (id == null) {
            throw new IllegalStateException("Could not find id"); // NOI18N
        }
        typeList.put(id, type);
        refcache.put(Type.class, typeList);
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n          DOCUMENT ME!
     * @param   refcache   DOCUMENT ME!
     * @param   typecache  DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public Policy createPolicy(final Node n,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent) {
        final String indent = preIndent + "\t"; // NOI18N
        final HashMap<Integer, Policy> policyList = (HashMap<Integer, Policy>)refcache.get(Policy.class);
        final Policy policy = new Policy();
        Integer id = null;
        final NodeList children = n.getChildNodes();
        for (int i = 0; i
                    < children.getLength(); i++) {
            final Node child = children.item(i);
            if (Node.ELEMENT_NODE == child.getNodeType()) {
                final String name = child.getLocalName();
                final String text = child.getTextContent();

                if (ID.equals(name)) {
                    id = Integer.valueOf(text);
                } else if (NNAME.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createNameMessage",
                            CREATE,
                            indent,
                            text));
                    policy.setName(text);
                }
            }
        }
        if (id == null) {
            throw new IllegalStateException("Could not find id"); // NOI18N
        }
        policyList.put(id, policy);
        refcache.put(Policy.class, policyList);
        return policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n                DOCUMENT ME!
     * @param   refcache         DOCUMENT ME!
     * @param   typecache        DOCUMENT ME!
     * @param   out              DOCUMENT ME!
     * @param   preIndent        DOCUMENT ME!
     * @param   attrCache        DOCUMENT ME!
     * @param   classAttrCache   DOCUMENT ME!
     * @param   foreignKeyCache  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public Attribute createAttribute(final Node n,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent,
            final Map<String, Set<Attribute>> attrCache,
            final Map<CidsClass, Set<ClassAttribute>> classAttrCache,
            final Map<Integer, CidsClass> foreignKeyCache) {
        final String indent = preIndent + "\t"; // NOI18N
        final HashMap<Integer, Attribute> attributeList = (HashMap<Integer, Attribute>)refcache.get(
                Attribute.class);
        final Attribute attr = new Attribute();
        final NodeList children = n.getChildNodes();
        Integer id = null;
        for (int i = 0; i
                    < children.getLength(); i++) {
            final String name = children.item(i).getLocalName();
            final String text = children.item(i).getTextContent();
            if (Node.ELEMENT_NODE == children.item(i).getNodeType()) {
                if (ID.equals(name)) {
                    id = Integer.valueOf(text);
                } else if (CS_TYPE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createTypeMessage",
                            CREATE,
                            indent));
                    Type type = resolveType(children.item(i), Type.class, refcache, typecache, out, preIndent);
                    if (type == null) {
                        final NodeList grandChilds = children.item(i).getChildNodes();
                        for (int j = 0; j < grandChilds.getLength(); j++) {
                            if (Node.ELEMENT_NODE == grandChilds.item(i).getNodeType()) {
                                final String grandChildName = grandChilds.item(j).getNodeName();
                                final String grandChildText = grandChilds.item(j).getTextContent();
                                if (COMPLEX_TYPE.equals(grandChildName)) {
                                    if (Boolean.valueOf(grandChildText)) {
                                        /*
                                         * if the Type of the attribute is a complex one it must be a cidsClass that
                                         * wasn't created yet. So we look for the right cs_class node and create the
                                         * cidsClass.
                                         */
                                        Node idNode = children.item(i).getFirstChild();
                                        String classIdValue = null;
                                        while (idNode.getNextSibling() != null) {
                                            if ((Node.ELEMENT_NODE == idNode.getNodeType())
                                                        && CLASS_ID.equals(idNode.getNodeName())) {
                                                classIdValue = idNode.getTextContent();
                                                break;
                                            } else {
                                                idNode = idNode.getNextSibling();
                                            }
                                        }
                                        final Node rightParent = resolveNode(children.item(i).getOwnerDocument(),
                                                CidsClass.class,
                                                Integer.valueOf(classIdValue));
                                        final CidsClass cidsClass = createClass(
                                                rightParent,
                                                refcache,
                                                typecache,
                                                out,
                                                null,
                                                preIndent,
                                                attrCache,
                                                classAttrCache,
                                                foreignKeyCache);
                                        type = cidsClass.getType();
                                    } else {
                                        /*
                                         * if it is not a complex type, and we could not find in ref or typecache, just
                                         * create a new type
                                         */
                                        type = createType(children.item(i), refcache, typecache, out, preIndent);
                                    }
                                }
                            }
                        }
                    }
                    attr.setType(type);
                } else if (NNAME.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createNameMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setName(text);
                } else if (FIELD_NAME.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createFieldNameMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setFieldName(text);
                } else if (FOREIGN_KEY.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createForeignKeyMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setForeignKey(Boolean.valueOf(text));
                } else if (SUBSTITUTE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createSubstituteNameMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setSubstitute(Boolean.valueOf(text));
                } else if (FOREIGN_KEY_REF.equals(name)) {
                    CidsClass cidsClass = resolveType(children.item(i),
                            CidsClass.class,
                            refcache,
                            typecache,
                            out,
                            preIndent);
                    if (cidsClass == null) {
                        final Node rightParent = resolveNode(children.item(i).getOwnerDocument(),
                                CidsClass.class,
                                Integer.valueOf(text));
                        cidsClass = createClass(
                                rightParent,
                                refcache,
                                typecache,
                                out,
                                null,
                                preIndent,
                                attrCache,
                                classAttrCache,
                                foreignKeyCache);
                    }
                    foreignKeyCache.put(Integer.valueOf(text), cidsClass);
                    attr.setForeignKeyClass(Integer.valueOf(text));
                } else if (DESCR.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createDescriptionMessage",
                            CREATE,
                            preIndent,
                            text));
                    attr.setDescription(text);
                } else if (VISIBLE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createVisibleMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setVisible(Boolean.valueOf(text));
                } else if (INDEXED.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createIndexedMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setIndexed(Boolean.valueOf(text));
                } else if (ISARRAY.equals(name)) {
                    out.println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createIsArrayMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setArray(Boolean.valueOf(text));
                } else if (EDITOR.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createEditorMessage",
                            CREATE,
                            indent));
                    JavaClass jc = resolveType(children.item(i), JavaClass.class, refcache, typecache, out, preIndent);
                    if (jc == null) {
                        jc = createJavaClass(children.item(i), refcache, typecache, out, preIndent);
                    }
                    attr.setEditor(jc);
                } else if (TOSTRING.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createToStringMessage",
                            CREATE,
                            indent));
                    JavaClass jc = resolveType(children.item(i), JavaClass.class, refcache, typecache, out, preIndent);
                    if (jc == null) {
                        jc = createJavaClass(children.item(i), refcache, typecache, out, preIndent);
                    }
                    attr.setEditor(jc);
                } else if (COMPLEX_EDITOR.equals(name)) {
                    out.println(
                        NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createComplexEditorMessage",
                            CREATE,
                            indent,
                            text));
                    JavaClass jc = resolveType(children.item(i), JavaClass.class, refcache, typecache, out, preIndent);
                    if (jc == null) {
                        jc = createJavaClass(children.item(i), refcache, typecache, out, preIndent);
                    }
                    attr.setEditor(jc);
                } else if (OPTIONAL.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createOptionalMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setOptional(Boolean.valueOf(text));
                } else if (DEFAULT_VAL.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createDefaultValMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setDefaultValue(text);
                } else if (POS.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createPosMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setPosition(Integer.valueOf(text));
                } else if (PRECISION.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createPrecisionMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setPrecision(Integer.valueOf(text));
                } else if (SCALE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createScaleMessage",
                            CREATE,
                            indent,
                            text));
                    attr.setScale(Integer.valueOf(text));
                }
            }
        }
        if (id == null) {
            throw new IllegalStateException("Could not find id"); // NOI18N
        }
        attributeList.put(id, attr);
        refcache.put(Attribute.class, attributeList);
        return attr;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n                DOCUMENT ME!
     * @param   refcache         DOCUMENT ME!
     * @param   typecache        DOCUMENT ME!
     * @param   out              DOCUMENT ME!
     * @param   preIndent        DOCUMENT ME!
     * @param   attrCache        DOCUMENT ME!
     * @param   classAttrCache   DOCUMENT ME!
     * @param   foreignKeyCache  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public ClassAttribute createClassAttribute(final Node n,
            final Map<Class<? extends CommonEntity>, Map<Integer, ? extends CommonEntity>> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final String preIndent,
            final Map<String, Set<Attribute>> attrCache,
            final Map<CidsClass, Set<ClassAttribute>> classAttrCache,
            final Map<Integer, CidsClass> foreignKeyCache) {
        final String indent = preIndent + "\t"; // NOI18N
        final HashMap<Integer, ClassAttribute> csAttrList = (HashMap<Integer, ClassAttribute>)refcache.get(
                ClassAttribute.class);
        final ClassAttribute csAttr = new ClassAttribute();
        final NodeList children = n.getChildNodes();
        Integer id = null;

        for (int i = 0; i
                    < children.getLength(); i++) {
            final String name = children.item(i).getLocalName();
            final String text = children.item(i).getTextContent();
            if (Node.ELEMENT_NODE == children.item(i).getNodeType()) {
                if (ID.equals(name)) {
                    id = Integer.valueOf(text);
                } else if (CS_TYPE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClass(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createTypeMessage",
                            CREATE,
                            indent));
                    Type type = resolveType(children.item(i), Type.class, refcache, typecache, out, indent);
                    if (type == null) {
                        final NodeList grandChilds = children.item(i).getChildNodes();
                        for (int j = 0; j < grandChilds.getLength(); j++) {
                            if (Node.ELEMENT_NODE == grandChilds.item(i).getNodeType()) {
                                final String grandChildName = grandChilds.item(j).getNodeName();
                                final String grandChildText = grandChilds.item(j).getTextContent();
                                if (COMPLEX_TYPE.equals(grandChildName)) {
                                    if (Boolean.valueOf(grandChildText)) {
                                        /*
                                         * if the Type of the attribute is a complex one it must be a cidsClass that
                                         * wasn't created yet. So we look for the right cs_class node and create the
                                         * cidsClass.
                                         */
                                        Node idNode = children.item(i).getFirstChild();
                                        String classIdValue = null;
                                        while (idNode.getNextSibling() != null) {
                                            if ((Node.ELEMENT_NODE == idNode.getNodeType())
                                                        && CLASS_ID.equals(idNode.getNodeName())) {
                                                classIdValue = idNode.getTextContent();
                                                break;
                                            } else {
                                                idNode = idNode.getNextSibling();
                                            }
                                        }
                                        final Node rightParent = resolveNode(children.item(i).getOwnerDocument(),
                                                CidsClass.class,
                                                Integer.valueOf(classIdValue));
                                        final CidsClass cidsClass = createClass(
                                                rightParent,
                                                refcache,
                                                typecache,
                                                out,
                                                null,
                                                preIndent,
                                                attrCache,
                                                classAttrCache,
                                                foreignKeyCache);
                                        type = cidsClass.getType();
                                    } else {
                                        /*
                                         * if it is not a complex type, and it wasn't find in ref or typecache, just
                                         * create a new type
                                         */
                                        type = createType(children.item(i), refcache, typecache, out, preIndent);
                                    }
                                }
                            }
                        }
                    }
                    csAttr.setType(type);
                } else if (ATTR_KEY.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClassAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createAttrKeyMessage",
                            CREATE,
                            indent,
                            text));
                    csAttr.setAttrKey(text);
                } else if (ATTR_VALUE.equals(name)) {
                    out.println(NbBundle.getMessage(
                            ImportClassesAction.class,
                            "ImportClassesAction.createClassAttribute(Node,Map,Map,PrintWriter,ProgressHandle,String,Map,Map,Map).createAttrValueMessage",
                            CREATE,
                            indent,
                            text));
                    csAttr.setAttrValue(text);
                }
            }
        }
        if (id == null) {
            throw new IllegalStateException("Could not find id"); // NOI18N
        }
        csAttrList.put(id, csAttr);
        refcache.put(ClassAttribute.class, csAttrList);
        return csAttr;
    }
}

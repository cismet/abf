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

import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.awt.EventQueue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import de.cismet.cids.jpa.entity.permission.Policy;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ExportClassesAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            ExportClassesAction.class);

    public static final String ENCODING = "UTF-8";                                       // NOI18N
    public static final String NAMESPACE = "http://cids.cismet.de/abf/xmlexport";        // NOI18N
    public static final String EXPORT_SCHEMA = "cids_export.xsd";
    public static final String FILECHOOSER_DIR = "Filechooser.importAndExportDirectory"; // NOI18N
    public static final String ID_REF = "idref";                                         // NOI18N
    public static final String SEP = ": ";                                               // NOI18N
    public static final String PROCESS;
    public static final String CREATE;

    // cs_class fields
    public static final String CS_CLASS = "cs_class";                // NOI18N
    public static final String ID = "id";                            // NOI18N
    public static final String NNAME = "name";                       // NOI18N
    public static final String DESCR = "descr";                      // NOI18N
    public static final String CLASS_ICON_ID = "class_icon_id";      // NOI18N
    public static final String OBJECT_ICON_ID = "object_icon_id";    // NOI18N
    public static final String TABLE_NAME = "table_name";            // NOI18N
    public static final String PRIM_KEY_FIELD = "primary_key_field"; // NOI18N
    public static final String INDEXED = "indexed";                  // NOI18N
    public static final String TOSTRING = "tostring";                // NOI18N
    public static final String EDITOR = "editor";                    // NOI18N
    public static final String RENDERER = "renderer";                // NOI18N
    public static final String ARRAY_LINK = "array_link";            // NOI18N
    public static final String POLICY = "policy";                    // NOI18N
    public static final String ATTR_POLICY = "attribute_policy";     // NOI18N

    // cs_attr fields
    public static final String CS_ATTR = "cs_attr"; // NOI18N
    // ID
    public static final String CLASS_ID = "class_id"; // NOI18N
    // NAME
    public static final String FIELD_NAME = "field_name";                     // NOI18N
    public static final String FOREIGN_KEY = "foreign_key";                   // NOI18N
    public static final String SUBSTITUTE = "substitute";                     // NOI18N
    public static final String FOREIGN_KEY_REF = "foreign_key_references_to"; // NOI18N
    // DESCR
    public static final String VISIBLE = "visible"; // NOI18N
    // INDEXED
    public static final String ISARRAY = "isarray";     // NOI18N
    public static final String ARRAY_KEY = "array_key"; // NOI18N
    // EDITOR
    // TOSTRING
    public static final String COMPLEX_EDITOR = "complex_editor"; // NOI18N
    public static final String OPTIONAL = "optional";             // NOI18N
    public static final String DEFAULT_VAL = "default_value";     // NOI18N
    // FROMSTRING omitted
    public static final String POS = "pos";             // NOI18N
    public static final String PRECISION = "precision"; // NOI18N
    public static final String SCALE = "scale";         // NOI18N

    // cs_class_attr fields
    public static final String CS_CLASS_ATTR = "cs_class_attr"; // NOI18N
    // ID
    // CLASS_ID
    public static final String ATTR_KEY = "attr_key";     // NOI18N
    public static final String ATTR_VALUE = "attr_value"; // NOI18N

    // cs_icon fields
    public static final String CS_ICON = "cs_icon"; // NOI18N
    // ID
    // NAME
    public static final String FILE_NAME = "file_name"; // NOI18N

    // cs_java_class fields
    public static final String CS_JAVA_CLASS = "cs_java_class"; // NOI18N
    // ID
    public static final String QUALIFIER = "qualifier"; // NOI18N
    public static final String NOTICE = "notice";       // NOI18N
    public static final String TYPE = "type";           // NOI18N

    // cs_policy fields
    public static final String CS_POLICY = "cs_policy"; // NOI18N
    // ID
    // NAME

    // cs_type fields
    public static final String CS_TYPE = "cs_type"; // NOI18N
    // ID
    // NAME
    // CLASS_ID
    public static final String COMPLEX_TYPE = "complex_type"; // NOI18N

    // DESCR
    // EDITOR
    // RENDERER

    static {
        PROCESS = org.openide.util.NbBundle.getMessage(ExportClassesAction.class, "ExportClassesAction.PROCESS"); // NOI18N
        CREATE = org.openide.util.NbBundle.getMessage(ExportClassesAction.class, "ExportClassesAction.CREATE");   // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final org.openide.nodes.Node[] nodes) {
        final DomainserverProject project = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        final Properties p = project.getLookup().lookup(Properties.class);
        if (p == null) {
            throw new IllegalStateException("project.properties not found"); // NOI18N
        }
        final String chooserDir = p.getProperty(FILECHOOSER_DIR, System.getProperty("user.home"));
        File outDir = new File(chooserDir);                                  // NOI18N
        if (!(outDir.exists() && outDir.isDirectory() && outDir.canRead())) {
            outDir = new File(System.getProperty("user.home"));
        }
        final JFileChooser chooser = new JFileChooser(outDir);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory(); // NOI18N
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.getDescription().returnvalue"); // NOI18N
                }
            });
        chooser.setSelectedFile(new File("export.xml"));                         // NOI18N
        int ret = chooser.showSaveDialog(WindowManager.getDefault().getMainWindow());
        while ((JFileChooser.APPROVE_OPTION == ret)
                    && !chooser.getSelectedFile().canWrite()) {
            JOptionPane.showMessageDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(
                    ExportClassesAction.class,
                    "ExportClassesAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    ExportClassesAction.class,
                    "ExportClassesAction.performAction(Node[]).JOptionPane.title"), // NOI18N
                JOptionPane.WARNING_MESSAGE);
            ret = chooser.showSaveDialog(WindowManager.getDefault().getMainWindow());
        }
        if (JFileChooser.APPROVE_OPTION == ret) {
            final File outFile = chooser.getSelectedFile();
            p.setProperty(FILECHOOSER_DIR, outFile.getParentFile().getAbsolutePath());
            final List<CidsClass> classes = new LinkedList<CidsClass>();
            for (final org.openide.nodes.Node n : nodes) {
                final CidsClassContextCookie cookie = n.getCookie(CidsClassContextCookie.class);
                classes.add(cookie.getCidsClass());
            }
            final String name =
                org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.performAction(Node[]).name")    // NOI18N
                        + "["                                                    // NOI18N
                        + project.getProjectDirectory()
                        .getName()
                        + "]";                                                   // NOI18N
            final InputOutput io = IOProvider.getDefault().getIO(name, false);
            try {
                io.getOut().reset();
                io.getErr().reset();
            } catch (final IOException ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("cannot reset io tab", ex);
                }
            }
            io.select();
            final ProgressHandle handle = ProgressHandleFactory.createHandle(name);
            RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        exportClasses(classes, outFile, io, handle);
                    }
                });
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExportClassesAction.class, "ExportClassesAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                CidsClassContextCookie.class,
                DomainserverContext.class
            };
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER
                    + "database_export_16.gif"; // NOI18N
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
        // we iterate from the first because it shall be checked for cccc's, too
        for (int i = 0; i < nodes.length; ++i) {
            final DomainserverProject project = nodes[i].getCookie(DomainserverContext.class).getDomainserverProject();
            // really investigate the enable strategy of coockeaction -.-
            final CidsClassContextCookie cccc = nodes[i].getCookie(CidsClassContextCookie.class);
            // ensure that any class is in the same domainserver project
            if (!project.equals(firstProject) || (cccc == null)) {
                return false;
            }
        }

        return firstProject.isConnected();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classes  DOCUMENT ME!
     * @param  out      DOCUMENT ME!
     * @param  io       DOCUMENT ME!
     * @param  handle   DOCUMENT ME!
     */
    private void exportClasses(final List<CidsClass> classes,
            final File out,
            final InputOutput io,
            final ProgressHandle handle) {
        final Document doc = XMLUtil.createDocument("class_export", NAMESPACE, null, null); // NOI18N
        final Attr xsiAttr = doc.createAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation");
        xsiAttr.setValue(NAMESPACE + " http://www.cismet.de/schema/cids_export.xsd");
        doc.getDocumentElement().setAttributeNodeNS(xsiAttr);
        final List<String> refList = new ArrayList<String>();
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    handle.start(classes.size());
                }
            });

        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            io.getOut()
                    .println(org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.exportClasses(List<CidsClass>,File,InputOutput,ProgressHandle).beginExportCapitals")); // NOI18N
            io.getOut().println();
            final Runnable run = new Runnable() {

                    @Override
                    public void run() {
                        handle.progress(1);
                    }
                };
            for (final CidsClass clazz : classes) {
                doc.getDocumentElement().appendChild(createClassNode(
                        clazz,
                        doc,
                        refList,
                        io.getOut(),
                        " "));            // NOI18N
                EventQueue.invokeLater(run);
            }
            io.getOut()
                    .println(org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.exportClasses(List<CidsClass>,File,InputOutput,ProgressHandle).writingBrackets")
                        + out.getName()); // NOI18N
            bos = new BufferedOutputStream(new FileOutputStream(out));
            XMLUtil.write(doc, bos, ENCODING);
            io.getOut()
                    .println(org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.exportClasses(List<CidsClass>,File,InputOutput,ProgressHandle).validatingBrackets")
                        + out.getName()); // NOI18N
            bis = new BufferedInputStream(new FileInputStream(out));
            final InputSource is = new InputSource(bis);
            final Document in = XMLUtil.parse(
                    is,
                    false,
                    true,
                    null,
                    new EntityResolver() {

                        @Override
                        public InputSource resolveEntity(final String publicId,
                                final String systemId) throws SAXException, IOException {
                            return new InputSource(
                                    new ByteArrayInputStream(new byte[0]));
                        }
                    });

            final SchemaFactory f = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = f.newSchema(
                    this.getClass().getResource(EXPORT_SCHEMA));
            XMLUtil.validate(in.getDocumentElement(), schema);
            io.getOut().println();
            io.getOut()
                    .println(org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.exportClasses(List<CidsClass>,File,InputOutput,ProgressHandle).exportSuccessful_Capitals"));  // NOI18N
        } catch (final Exception ex) {
            LOG.error("error while creating export file", ex);                                                                                 // NOI18N
            io.getErr().println();
            io.getErr()
                    .println(
                        org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.exportClasses(List<CidsClass>,File,InputOutput,ProgressHandle).duringExportCapitalsError")    // NOI18N
                        + ex.getMessage());
            ex.printStackTrace(io.getErr());
            io.getErr().println();
            io.getErr()
                    .println(org.openide.util.NbBundle.getMessage(
                            ExportClassesAction.class,
                            "ExportClassesAction.exportClasses(List<CidsClass>,File,InputOutput,ProgressHandle).exportUnsuccessfulCapitals")); // NOI18N
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (final IOException ex) {
                    LOG.warn("could not close outputstream", ex);                                                                              // NOI18N
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (final IOException ex) {
                    LOG.warn("could not close inputstream", ex);                                                                               // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        handle.finish();
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsClass  DOCUMENT ME!
     * @param   doc        DOCUMENT ME!
     * @param   refList    DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node createClassNode(
            final CidsClass cidsClass,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        out.println(PROCESS + preIndent + cidsClass.getName());
        final Node classNode = doc.createElementNS(NAMESPACE, CS_CLASS);
        final String id = cidsClass.getId().toString();
        if (refList.contains(CS_CLASS + cidsClass.getId())) {
            classNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            // increase indent
            final String indent = preIndent
                        + "\t"; // NOI18N
            refList.add(CS_CLASS + cidsClass.getId());
            // create id node
            classNode.appendChild(createNode(ID, id, doc, out, indent));
            // create name node
            classNode.appendChild(createNode(
                    NNAME,
                    cidsClass.getName(),
                    doc,
                    out,
                    indent));
            if (cidsClass.getDescription() != null) {
                // create desc node
                classNode.appendChild(createNode(
                        DESCR,
                        cidsClass.getDescription(),
                        doc,
                        out,
                        indent));
            }
            // create complexType cs_classIcon node
            if (cidsClass.getClassIcon() != null) {
                classNode.appendChild(createIconNode(
                        CLASS_ICON_ID,
                        cidsClass.getClassIcon(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create complexType cs_object_icon node
            if (cidsClass.getObjectIcon() != null) {
                classNode.appendChild(createIconNode(
                        OBJECT_ICON_ID,
                        cidsClass.getObjectIcon(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create table name node
            classNode.appendChild(createNode(
                    TABLE_NAME,
                    cidsClass.getTableName(),
                    doc,
                    out,
                    indent));
            // create prim key field node
            classNode.appendChild(createNode(
                    PRIM_KEY_FIELD,
                    cidsClass.getPrimaryKeyField(),
                    doc,
                    out,
                    indent));
            // create indexed node
            classNode.appendChild(createNode(
                    INDEXED,
                    cidsClass.isIndexed().toString(),
                    doc,
                    out,
                    indent));
            // create complextype toString
            if (cidsClass.getToString() != null) {
                classNode.appendChild(createJavaClass(
                        TOSTRING,
                        cidsClass.getToString(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create complextype editor
            if (cidsClass.getEditor() != null) {
                classNode.appendChild(createJavaClass(
                        EDITOR,
                        cidsClass.getEditor(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create complextype renderer
            if (cidsClass.getRenderer() != null) {
                classNode.appendChild(createJavaClass(
                        RENDERER,
                        cidsClass.getRenderer(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create array link node
            classNode.appendChild(createNode(
                    ARRAY_LINK,
                    cidsClass.isArrayLink().toString(),
                    doc,
                    out,
                    indent));
            // create policy node
            if (cidsClass.getPolicy() != null) {
                classNode.appendChild(createPolicyNode(
                        POLICY,
                        cidsClass.getPolicy(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create attribute policy node
            if (cidsClass.getAttributePolicy() != null) {
                classNode.appendChild(createPolicyNode(
                        ATTR_POLICY,
                        cidsClass.getAttributePolicy(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create attribute nodes
            for (final Attribute attr : cidsClass.getAttributes()) {
                classNode.appendChild(createAttributeNode(
                        attr,
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create class attribute nodes
            if (cidsClass.getClassAttributes() != null) {
                for (final ClassAttribute attr : cidsClass.getClassAttributes()) {
                    classNode.appendChild(createClassAttributeNode(
                            attr,
                            doc,
                            refList,
                            out,
                            indent));
                }
            }
            // create class type node
            classNode.appendChild(createTypeNode(
                    cidsClass.getType(),
                    doc,
                    refList,
                    out,
                    indent));
        }
        return classNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   which   DOCUMENT ME!
     * @param   value   DOCUMENT ME!
     * @param   doc     DOCUMENT ME!
     * @param   out     DOCUMENT ME!
     * @param   indent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node createNode(
            final String which,
            final String value,
            final Document doc,
            final PrintWriter out,
            final String indent) {
        out.println(CREATE + indent + which + SEP + value);
        final Node node = doc.createElementNS(NAMESPACE, which);
        node.setTextContent(value);
        return node;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   whichIcon  DOCUMENT ME!
     * @param   icon       DOCUMENT ME!
     * @param   doc        DOCUMENT ME!
     * @param   refList    DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node createIconNode(
            final String whichIcon,
            final Icon icon,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        out.println(PROCESS + preIndent + icon.getName());
        final Node iconNode = doc.createElementNS(NAMESPACE, whichIcon);
        final String id = icon.getId().toString();
        if (refList.contains(CS_ICON + id)) {
            iconNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            final String indent = preIndent
                        + "\t"; // NOI18N
            // icon id
            iconNode.appendChild(createNode(ID, id, doc, out, indent));
            // icon name
            iconNode.appendChild(createNode(
                    NNAME,
                    icon.getName(),
                    doc,
                    out,
                    indent));
            // icon file name
            iconNode.appendChild(createNode(
                    FILE_NAME,
                    icon.getFileName(),
                    doc,
                    out,
                    indent));
            // add to reference list
            refList.add(CS_ICON + id);
        }
        return iconNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   whichClass  DOCUMENT ME!
     * @param   javaClass   DOCUMENT ME!
     * @param   doc         DOCUMENT ME!
     * @param   refList     DOCUMENT ME!
     * @param   out         DOCUMENT ME!
     * @param   preIndent   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Node createJavaClass(
            final String whichClass,
            final JavaClass javaClass,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        out.println(PROCESS + preIndent + javaClass.getQualifier());
        final Node jcNode = doc.createElementNS(NAMESPACE, whichClass);
        final String id = javaClass.getId().toString();
        if (refList.contains(CS_JAVA_CLASS + id)) {
            jcNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            final String indent = preIndent
                        + "\t"; // NOI18N
            // create javaclass id node
            jcNode.appendChild(createNode(ID, id, doc, out, indent));
            // create javaclass qualifier node
            if (javaClass.getQualifier() != null) {
                jcNode.appendChild(createNode(
                        QUALIFIER,
                        javaClass.getQualifier(),
                        doc,
                        out,
                        indent));
            }
            // create type node
            jcNode.appendChild(createNode(
                    TYPE,
                    javaClass.getType(),
                    doc,
                    out,
                    indent));
            // create notice node
            if (javaClass.getNotice() != null) {
                jcNode.appendChild(createNode(
                        NOTICE,
                        javaClass.getNotice(),
                        doc,
                        out,
                        indent));
            }
            // add to reference list
            refList.add(CS_JAVA_CLASS + id);
        }
        return jcNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   whichPolicy  DOCUMENT ME!
     * @param   policy       DOCUMENT ME!
     * @param   doc          DOCUMENT ME!
     * @param   refList      DOCUMENT ME!
     * @param   out          DOCUMENT ME!
     * @param   preIndent    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node createPolicyNode(
            final String whichPolicy,
            final Policy policy,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        out.println(PROCESS + preIndent + policy.getName());
        final Node pNode = doc.createElementNS(NAMESPACE, whichPolicy);
        final String id = policy.getId().toString();
        if (refList.contains(CS_POLICY + id)) {
            pNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            final String indent = preIndent
                        + "\t"; // NOI18N
            // create policy id node
            pNode.appendChild(createNode(ID, id, doc, out, indent));
            // create policy name node
            pNode.appendChild(createNode(
                    NNAME,
                    policy.getName(),
                    doc,
                    out,
                    indent));
            // add to reference list
            refList.add(CS_POLICY + id);
        }
        return pNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr       DOCUMENT ME!
     * @param   doc        DOCUMENT ME!
     * @param   refList    DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node createAttributeNode(
            final Attribute attr,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        out.println(PROCESS + preIndent + attr.getName());
        final Node attrNode = doc.createElementNS(NAMESPACE, CS_ATTR);
        final String id = attr.getId().toString();
        if (refList.contains(CS_ATTR + id)) {
            attrNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            final String indent = preIndent
                        + "\t"; // NOI18N
            // create attr id node
            attrNode.appendChild(createNode(ID, id, doc, out, indent));
            // create attr class id node
            attrNode.appendChild(createNode(
                    CLASS_ID,
                    attr.getCidsClass().getId().toString(),
                    doc,
                    out,
                    indent));
            // create attr type node
            attrNode.appendChild(createTypeNode(
                    attr.getType(),
                    doc,
                    refList,
                    out,
                    indent));
            // create attr name node
            attrNode.appendChild(createNode(
                    NNAME,
                    attr.getName(),
                    doc,
                    out,
                    indent));
            // create attr field_name node
            attrNode.appendChild(createNode(
                    FIELD_NAME,
                    attr.getFieldName(),
                    doc,
                    out,
                    indent));
            // create attr foreign_key node
            attrNode.appendChild(createNode(
                    FOREIGN_KEY,
                    attr.isForeignKey().toString(),
                    doc,
                    out,
                    indent));
            // create attr substitute node
            attrNode.appendChild(createNode(
                    SUBSTITUTE,
                    attr.isSubstitute().toString(),
                    doc,
                    out,
                    indent));
            // create foreign key class node
            if (attr.isForeignKey() && (attr.getForeignKeyClass() != null)) {
                attrNode.appendChild(createNode(
                        FOREIGN_KEY_REF,
                        attr.getForeignKeyClass().toString(),
                        doc,
                        out,
                        indent));
            }
            // create attr desc node
            if (attr.getDescription() != null) {
                attrNode.appendChild(createNode(
                        DESCR,
                        attr.getDescription(),
                        doc,
                        out,
                        indent));
            }
            // create attr visible node
            attrNode.appendChild(createNode(
                    VISIBLE,
                    attr.isVisible().toString(),
                    doc,
                    out,
                    indent));
            // create attr indexed node
            attrNode.appendChild(createNode(
                    INDEXED,
                    attr.isIndexed().toString(),
                    doc,
                    out,
                    indent));
            // create attr isarray node
            attrNode.appendChild(createNode(
                    ISARRAY,
                    attr.isArray().toString(),
                    doc,
                    out,
                    indent));
            // create attr array_key node
            if (attr.isArray() && (attr.getArrayKey() != null)) {
                attrNode.appendChild(createNode(
                        ARRAY_KEY,
                        attr.getArrayKey(),
                        doc,
                        out,
                        indent));
            }
            // create attr editor node
            if (attr.getEditor() != null) {
                attrNode.appendChild(createJavaClass(
                        EDITOR,
                        attr.getEditor(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create attr tostring node
            if (attr.getToString() != null) {
                attrNode.appendChild(createJavaClass(
                        TOSTRING,
                        attr.getToString(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create attr complex editor node
            if (attr.getComplexEditor() != null) {
                attrNode.appendChild(createJavaClass(
                        COMPLEX_EDITOR,
                        attr.getComplexEditor(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create attr optional node
            attrNode.appendChild(createNode(
                    OPTIONAL,
                    attr.isOptional().toString(),
                    doc,
                    out,
                    indent));
            // create attr default value node
            if (attr.getDefaultValue() != null) {
                attrNode.appendChild(createNode(
                        DEFAULT_VAL,
                        attr.getDefaultValue(),
                        doc,
                        out,
                        indent));
            }
            // fromString is omitted
            // create attr pos node
            if (attr.getPosition() != null) {
                attrNode.appendChild(createNode(
                        POS,
                        attr.getPosition().toString(),
                        doc,
                        out,
                        indent));
            }
            // create attr precision node
            if (attr.getPrecision() != null) {
                attrNode.appendChild(createNode(
                        PRECISION,
                        attr.getPrecision().toString(),
                        doc,
                        out,
                        indent));
            }
            // create attr scale node
            if (attr.getScale() != null) {
                attrNode.appendChild(createNode(
                        SCALE,
                        attr.getScale().toString(),
                        doc,
                        out,
                        indent));
            }
            // add to reference list
            refList.add(CS_ATTR + id);
        }
        return attrNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr       DOCUMENT ME!
     * @param   doc        DOCUMENT ME!
     * @param   refList    DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node createClassAttributeNode(
            final ClassAttribute attr,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        out.println(PROCESS + preIndent + attr.toString());
        final Node caNode = doc.createElementNS(NAMESPACE, CS_CLASS_ATTR);
        final String id = attr.getId().toString();
        if (refList.contains(CS_CLASS_ATTR + id)) {
            caNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            final String indent = preIndent
                        + "\t"; // NOI18N
            // create class attr id node
            caNode.appendChild(createNode(ID, id, doc, out, indent));
            // create class attr class_id node
            caNode.appendChild(createNode(
                    CLASS_ID,
                    attr.getCidsClass().getId().toString(),
                    doc,
                    out,
                    indent));
            // create class attr type node
            caNode.appendChild(createTypeNode(
                    attr.getType(),
                    doc,
                    refList,
                    out,
                    indent));
            // create class attr_key node
            caNode.appendChild(createNode(
                    ATTR_KEY,
                    attr.getAttrKey(),
                    doc,
                    out,
                    indent));
            // create class attr_value node
            caNode.appendChild(createNode(
                    ATTR_VALUE,
                    attr.getAttrValue(),
                    doc,
                    out,
                    indent));
            // add to reference list
            refList.add(CS_CLASS_ATTR + id);
        }
        return caNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type       DOCUMENT ME!
     * @param   doc        DOCUMENT ME!
     * @param   refList    DOCUMENT ME!
     * @param   out        DOCUMENT ME!
     * @param   preIndent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private Node createTypeNode(
            final Type type,
            final Document doc,
            final List<String> refList,
            final PrintWriter out,
            final String preIndent) {
        if (type.getName() == null) {
            throw (new IllegalStateException("Could not find type for Cidsclass " + type.getCidsClass().toString())); // NOI18N
        } else {
            out.println(PROCESS + preIndent + type.getName());
        }
        final Node typeNode = doc.createElementNS(NAMESPACE, CS_TYPE);
        final String id = type.getId().toString();
        // has the cidsclass already been exported for this type ?
        if (type.isComplexType()
                    && !refList.contains(CS_CLASS + type.getCidsClass().getId())) {
            // no, thus it has to be added
            doc.getDocumentElement().appendChild(createClassNode(
                    type.getCidsClass(),
                    doc,
                    refList,
                    out,
                    preIndent));
        }
        if (refList.contains(CS_TYPE + id)) {
            typeNode.appendChild(createNode(ID_REF, id, doc, out, preIndent));
        } else {
            final String indent = preIndent
                        + "\t"; // NOI18N
            // create type id node
            typeNode.appendChild(createNode(ID, id, doc, out, indent));
            // create type name node
            typeNode.appendChild(createNode(
                    NNAME,
                    type.getName(),
                    doc,
                    out,
                    indent));
            if (type.getCidsClass() != null) {
                // create type class_id node
                typeNode.appendChild(createNode(
                        CLASS_ID,
                        type.getCidsClass().getId().toString(),
                        doc,
                        out,
                        indent));
            }
            // create type complex_type node
            typeNode.appendChild(createNode(
                    COMPLEX_TYPE,
                    type.isComplexType().toString(),
                    doc,
                    out,
                    indent));
            // create type descr node
            if (type.getDescription() != null) {
                typeNode.appendChild(createNode(
                        DESCR,
                        type.getDescription(),
                        doc,
                        out,
                        indent));
            }
            // create type editor node
            if (type.getEditor() != null) {
                typeNode.appendChild(createJavaClass(
                        EDITOR,
                        type.getEditor(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // create type renderer node
            if (type.getRenderer() != null) {
                typeNode.appendChild(createJavaClass(
                        RENDERER,
                        type.getRenderer(),
                        doc,
                        refList,
                        out,
                        indent));
            }
            // add to reference list
            refList.add(CS_TYPE + id);
        }
        return typeNode;
    }
}

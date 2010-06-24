/*
 * ImportClassesAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import de.cismet.cids.jpa.entity.common.CommonEntity;
import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import static de.cismet.cids.abf.domainserver.project.cidsclass.ExportClassesAction.*;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class ImportClassesAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            ImportClassesAction.class);

    @Override
    protected void performAction(final org.openide.nodes.Node[] nodes)
    {
        final DomainserverProject project = nodes[0]
                .getCookie(DomainserverContext.class).getDomainserverProject();
        final Properties p = project.getLookup().lookup(Properties.class);
        if(p == null)
        {
            throw new IllegalStateException(
                    "project.properties not found"); // NOI18N
        }
        final File inDir = new File(p.getProperty(
                ExportClassesAction.FILECHOOSER_DIR,
                System.getProperty("user.dir"))); // NOI18N
        final JFileChooser chooser = new JFileChooser(inDir);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(final File f)
            {
                return f.getName().toLowerCase().endsWith(".xml") // NOI18N
                        || f.isDirectory();
            }

            @Override
            public String getDescription()
            {
                return org.openide.util.NbBundle.getMessage(
                        ImportClassesAction.class,
                        "ImportClassesAction.getDescription().returnvalue"); // NOI18N
            }
        });
        int ret = chooser.showOpenDialog(
                WindowManager.getDefault().getMainWindow());
        while(JFileChooser.APPROVE_OPTION == ret
                && !chooser.getSelectedFile().canRead())
        {
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    org.openide.util.NbBundle.getMessage(
                        ImportClassesAction.class,
                        "ImportClassesAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        ImportClassesAction.class, "ImportClassesAction.performAction(Node[]).JOptionPane.title"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            ret = chooser.showOpenDialog(
                WindowManager.getDefault().getMainWindow());
        }
        if(JFileChooser.APPROVE_OPTION == ret)
        {
            final File in = chooser.getSelectedFile();
            final String name = org.openide.util.NbBundle.getMessage(
                    ImportClassesAction.class, "ImportClassesAction.performAction(Node[]).name") // NOI18N
                    + "[" // NOI18N
                    + project.getProjectDirectory().getName()
                    + "]"; // NOI18N
            final InputOutput io = IOProvider.getDefault().getIO(name, false);
            final ProgressHandle handle = ProgressHandleFactory.createHandle(name);
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    handle.start();
                    handle.switchToIndeterminate();
                }
            });
            try
            {
                if(importClasses(project.getCidsDataObjectBackend(), in, io, handle))
                {
                    project.getLookup().lookup(ClassManagement.class).refresh();
                    project.getLookup().lookup(SyncManagement.class).refresh();
                }
            }finally
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handle.finish();
                    }
                });
            }
        }
    }

    @Override
    protected int mode()
    {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(ImportClassesAction.class, "ImportClassesAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            DomainserverContext.class
        };
    }

    @Override
    protected String iconResource()
    {
        return DomainserverProject.IMAGE_FOLDER + "database_import_16.gif"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }

    @Override
    protected boolean enable(final org.openide.nodes.Node[] nodes)
    {
        if(!super.enable(nodes))
        {
            return false;
        }

        final DomainserverProject firstProject = nodes[0].getCookie(DomainserverContext.class).getDomainserverProject();
        for(int i = 0; i < nodes.length; ++i)
        {
            final DomainserverProject project = nodes[i].getCookie(DomainserverContext.class).getDomainserverProject();
            // ensure that any selected node is in the same domainserver project
            if(!project.equals(firstProject))
            {
                return false;
            }
        }

        return firstProject.isConnected();
    }

    private boolean importClasses(final Backend backend, final File in,
            final InputOutput io, final ProgressHandle handle)
    {
        io.getOut().println(org.openide.util.NbBundle.getMessage(
                ImportClassesAction.class, "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).beginImportCapitals"));// NOI18N
        io.getOut().println();
        BufferedInputStream bis = null;
        Document doc = null;
        try
        {
            io.getOut().println(org.openide.util.NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).readingBrackets") + in.getName()); // NOI18N
            bis = new BufferedInputStream(new FileInputStream(in));
            final InputSource is = new InputSource(bis);
            doc  = XMLUtil.parse(is, false, true, null, new EntityResolver()
            {
                @Override
                public InputSource resolveEntity(final String publicId,
                        final String systemId) throws
                        SAXException,
                        IOException
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("trying to resolve entity: publicid: " + publicId + " systemid: " + systemId);//NOI18N
                    }
                    return new InputSource(
                            new ByteArrayInputStream(new byte[0]));
                }
            });
        }catch(final Exception e)
        {
            LOG.error("could not parse document from file: " + in, e); // NOI18N
            io.getErr().println();
            io.getErr().println(org.openide.util.NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).readingInputFileError") // NOI18N
                    + e.getMessage());
            e.printStackTrace(io.getErr());
            io.getErr().println();
            io.getErr().println(org.openide.util.NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).importUnsuccessfulCapitalsError")); // NOI18N
            return false;
        }finally
        {
            if(bis != null)
            {
                try
                {
                    bis.close();
                }catch(final IOException e)
                {
                    LOG.warn("could not close input stream", e); // NOI18N
                }
            }
        }
        try
        {
            io.getOut().println(org.openide.util.NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).validatingBrackets") + in.getName()); // NOI18N
            final SchemaFactory f = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            final Schema schema = f.newSchema(this.getClass().getResource(EXPORT_SCHEMA));
            XMLUtil.validate(doc.getDocumentElement(), schema);
        }catch(final SAXException e)
        {
            LOG.error("input file not valid: " + in, e); // NOI18N
            io.getErr().println();
            io.getErr().println(org.openide.util.NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).validatingInputFileError") // NOI18N
                    + e.getMessage());
            e.printStackTrace(io.getErr());
            io.getErr().println();
            io.getErr().println(NbBundle.getMessage(
                    ImportClassesAction.class,
                    "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).importUnsuccessfulCapitalsError")); // NOI18N
            return false;
        }
        final NodeList classlist = doc.getElementsByTagNameNS(NAMESPACE, CS_CLASS);
        final List<CidsClass> classes = new ArrayList<CidsClass>(classlist.getLength());
        final Map<Integer, ? extends CommonEntity> refcache = new Hashtable<Integer, CommonEntity>();
        io.getOut().println(NbBundle.getMessage(ImportClassesAction.class, "ImportClassesAction.importClasses(Backend,File,InputOutput,ProgressHandle).createCacheBrackets")); // NOI18N
        final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache = getTypeCache(backend);
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                handle.switchToDeterminate(classlist.getLength());
            }
        });
        final Runnable progress = new Runnable()
        {
            @Override
            public void run()
            {
                handle.progress(1);
            }
        };
        for(int i = 0; i < classlist.getLength(); ++i)
        {
            final Node node = classlist.item(i);
            classes.add(createClass(node, refcache, typecache, io.getOut(), handle, " "));
            EventQueue.invokeLater(progress);
        }
        return true;
    }

    private Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> getTypeCache(final Backend backend)
    {
        final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> cache =
                new Hashtable<Class<? extends CommonEntity>, List<? extends CommonEntity>>();
        cache.put(CidsClass.class, backend.getAllEntities(CidsClass.class));
        cache.put(Type.class, backend.getAllEntities(Type.class));
        cache.put(JavaClass.class, backend.getAllEntities(JavaClass.class));
        cache.put(Icon.class, backend.getAllEntities(Icon.class));
        return cache;
    }

    private <T extends CommonEntity> T resolveType(
            final Node node,
            final Class<T> which,
            final Map<Integer, ? extends CommonEntity> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache)
    {
        final Node child = node.getFirstChild();
        if(ID_REF.equals(child.getLocalName()))
        {
            final Integer idref = Integer.valueOf(child.getTextContent());
            final T ce = (T)refcache.get(idref);
            if(ce == null)
            {
                throw new IllegalStateException("found idref but cache did not contain type: " + idref); // NOI18N
            }else
            {
                return ce;
            }
        }else
        {
            final List<? extends CommonEntity> types = typecache.get(which);
            final NodeList children = node.getChildNodes();
            String comp = null;
            for(final CommonEntity ce : types)
            {
                if(CidsClass.class.isAssignableFrom(which))
                {
                    if(comp == null)
                    {
                        comp = getComparationValue(NNAME, children);
                    }
                    if(comp.equalsIgnoreCase(((CidsClass)ce).getName()))
                    {
                        return (T)ce;
                    }
                }else if(Icon.class.isAssignableFrom(which))
                {
                    if(comp == null)
                    {
                        comp = getComparationValue(NNAME, children);
                    }
                    if(comp.equalsIgnoreCase(((Icon)ce).getName()))
                    {
                        return (T)ce;
                    }
                }else if(JavaClass.class.isAssignableFrom(which))
                {
                    if(comp == null)
                    {
                        comp = getComparationValue(QUALIFIER, children);
                    }
                    // no ignorecase here because the qualifier is case sensitive
                    if(comp.equals(((JavaClass)ce).getQualifier()))
                    {
                        return (T)ce;
                    }
                }else if(Type.class.isAssignableFrom(which))
                {
                    if(comp == null)
                    {
                        comp = getComparationValue(NNAME, children);
                    }
                    if(comp.equalsIgnoreCase(((Type)ce).getName()))
                    {
                        return (T)ce;
                    }
                }
            }
        }
        return null;
    }

    private String getComparationValue(final String key, final NodeList nodes)
    {
        for(int i = 0; i < nodes.getLength(); ++i)
        {
            if(key.equals(nodes.item(i).getLocalName()))
            {
                return nodes.item(i).getTextContent();
            }
        }
        return null;
    }

    private CidsClass createClass(
            final Node node,
            final Map<Integer, ? extends CommonEntity> refcache,
            final Map<Class<? extends CommonEntity>, List<? extends CommonEntity>> typecache,
            final PrintWriter out,
            final ProgressHandle handle,
            final String preIndent)
    {
        final NodeList children = node.getChildNodes();
        // search for the name first, not efficient but user will get more appropriate info of what is going on
        String classname = null;
        for(int i = 0; i < children.getLength(); ++i)
        {
            final Node n = children.item(i);
            if(Node.ELEMENT_NODE == n.getNodeType() && NNAME.equals(n.getLocalName()))
            {
                classname = n.getTextContent();
                break;
            }
        }
        if(classname == null || classname.trim().isEmpty())
        {
            throw new IllegalStateException("classname is null or empty");
        }
        out.println(PROCESS + preIndent + classname);
        CidsClass cidsClass = resolveType(node, CidsClass.class, refcache, typecache);
        if(cidsClass == null)
        {
            // increase indent
            final String indent = preIndent + "\t"; // NOI18N
            // we create a new cidsclass
            cidsClass = new CidsClass();
            for(int i = 0; i < children.getLength(); ++i)
            {
                final Node n = children.item(i);
                if(Node.ELEMENT_NODE == n.getNodeType())
                {
                    final String localname = n.getLocalName();
                    final String value = n.getTextContent();
                    if(ID.equals(localname))
                    {
                        out.println(CREATE + indent + ID + SEP + value);
                        cidsClass.setId(Integer.valueOf(value));
                    }else if(NNAME.equals(localname))
                    {
                        // we got it already but it is set here
                        out.println(CREATE + indent + NNAME + SEP + value);
                        cidsClass.setName(value);
                    }else if(CLASS_ICON_ID.equals(localname))
                    {
                        //cidsClass.setClassIcon(createIcon());
                    }
                }
            }
        }else
        {
            out.print(PROCESS + preIndent + classname + " bereits im System vorhanden oder schon erzeugt");
        }
        return cidsClass;
    }
}
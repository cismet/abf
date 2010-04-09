/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.cidsjavatemplate;

import java.awt.Component;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class CidsJavaTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsJavaTemplateWizardIterator.class);

    static {
        DOMConfigurator.configure(CidsJavaTemplateWizardIterator.class.getResource("log4j.xml"));
    }

    //~ Instance fields --------------------------------------------------------

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CidsJavaTemplateWizardIterator createIterator() {
        return new CidsJavaTemplateWizardIterator();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel[] createPanels() {
        LOG.debug("createpanels");
        return new WizardDescriptor.Panel[] { new CidsJavaTemplateWizardPanel(), };
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String[] createSteps() {
        return new String[] { NbBundle.getMessage(CidsJavaTemplateWizardIterator.class, "LBL_CreateProjectStep") }; // NOI18N
    }

    @Override
    public Set instantiate() throws IOException {
        final Set resultSet = new LinkedHashSet();
        final File dirF = FileUtil.normalizeFile((File)wiz.getProperty("projdir")); // NOI18N
        dirF.mkdirs();
        final FileObject template = Templates.getTemplate(wiz);
        final FileObject dir = FileUtil.toFileObject(dirF);
        unZipFile(template.getInputStream(), dir);

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        final Enumeration e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            final FileObject subfolder = (FileObject)e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        final File parent = dirF.getParentFile();
        if ((parent != null) && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        return resultSet;
    }

    @Override
    public void initialize(final WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        final String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            final Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                final JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(
                    WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                    Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(final WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null); // NOI18N
        this.wiz.putProperty("name", null);    // NOI18N
        this.wiz = null;
        panels = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String name() {
        return org.openide.util.NbBundle.getMessage(
                CidsJavaTemplateWizardIterator.class,
                "Dsc_nOfm", // NOI18N
                new Object[] { Integer.valueOf(index + 1), Integer.valueOf(panels.length) });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean hasNext() {
        return index < (panels.length - 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  NoSuchElementException  DOCUMENT ME!
     */
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  NoSuchElementException  DOCUMENT ME!
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    /**
     * If nothing unusual changes in the middle of the wizard, simply:
     *
     * @param  l  DOCUMENT ME!
     */
    @Override
    public void addChangeListener(final ChangeListener l) {
        // not needed
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    @Override
    public void removeChangeListener(final ChangeListener l) {
        // not needed
    }

    /**
     * DOCUMENT ME!
     *
     * @param   source       DOCUMENT ME!
     * @param   projectRoot  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static void unZipFile(final InputStream source, final FileObject projectRoot) throws IOException {
        try {
            final ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry = str.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    final FileObject fo = FileUtil.createData(projectRoot,
                            entry.getName());
                    final FileLock lock = fo.lock();
                    try {
                        final OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
                entry = str.getNextEntry();
            }
        } finally {
            source.close();
        }
        hackUnzippedProject(FileUtil.toFile(projectRoot));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  projDir  DOCUMENT ME!
     */
    private static void hackUnzippedProject(final File projDir) {
        manipulateProjectProperties(projDir);
        manipulateProjectXML(projDir);
        manipulateBuildXML(projDir);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   projDir  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private static void manipulateProjectProperties(final File projDir) {
        final String projName = projDir.getName();
        final File f = new File(projDir, "nbproject" + File.separator + "project.properties"); // NOI18N
        if (!f.exists()) {
            final String message = "could not locate project.properties";                      // NOI18N
            LOG.error(message);
            throw new IllegalStateException(message);
        }
        final Properties p = new Properties();
        try {
            final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            p.load(bis);
        } catch (final IOException ex) {
            final String message = "could not load project.properties";
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        }
        p.setProperty("dist.jar.file", projName + ".jar");                                     // NOI18N
        p.setProperty("dist.jar", "${dist.dir}/${dist.jar.file}");                             // NOI18N
        addLibraries(projDir, p);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f));
            p.store(bos, null);
        } catch (final FileNotFoundException ex) {
            // should not occur
            final String message = "could not write project.properties"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final IOException ex) {
            final String message = "could not write project.properties"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (final IOException e) {
                LOG.warn("could not close outputstream", e);             // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   projDir  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private static void manipulateProjectXML(final File projDir) {
        final String projName = projDir.getName();
        final File f = new File(projDir, "nbproject" + File.separator + "project.xml"); // NOI18N
        if (!f.exists()) {
            final String message = "could not locate project.xml";                      // NOI18N
            LOG.error(message);
            throw new IllegalStateException(message);
        }
        final org.w3c.dom.Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(new BufferedReader(new FileReader(f))), false, false, null, null);
        } catch (final FileNotFoundException ex) {
            // should not occur
            final String message = "could not load project.xml";  // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final IOException ex) {
            final String message = "could not load project.xml";  // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final SAXException ex) {
            final String message = "could not parse project.xml"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        }
        final NodeList nl = doc.getElementsByTagName("name");     // NOI18N
        if (nl.getLength() != 1) {
            final String message = "could not locate project name tag"; // NOI18N
            LOG.error(message);
            throw new IllegalStateException(message);
        }
        nl.item(0).setTextContent(projName);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f));
            XMLUtil.write(doc, bos, "UTF-8");                     // NOI18N
        } catch (final FileNotFoundException ex) {
            // should not occur
            final String message = "could not write project.xml"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final IOException ex) {
            final String message = "could not write project.xml"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (final IOException e) {
                LOG.warn("could not close outputstream", e);      // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   projDir  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private static void manipulateBuildXML(final File projDir) {
        final String projName = projDir.getName();
        final File f = new File(projDir, "build.xml");           // NOI18N
        if (!f.exists()) {
            final String message = "could not locate build.xml"; // NOI18N
            LOG.error(message);
            throw new IllegalStateException(message);
        }
        final org.w3c.dom.Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(new BufferedReader(new FileReader(f))), false, false, null, null);
        } catch (final FileNotFoundException ex) {
            // should not occur
            final String message = "could not load build.xml";  // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final IOException ex) {
            final String message = "could not load build.xml";  // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final SAXException ex) {
            final String message = "could not parse build.xml"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        }
        doc.getDocumentElement().setAttribute("name", projName); // NOI18N
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f));
            XMLUtil.write(doc, bos, "UTF-8");                   // NOI18N
        } catch (final FileNotFoundException ex) {
            // should not occur
            final String message = "could not write build.xml"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } catch (final IOException ex) {
            final String message = "could not write build.xml"; // NOI18N
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (final IOException e) {
                LOG.warn("could not close outputstream", e);    // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  projDir  DOCUMENT ME!
     * @param  p        DOCUMENT ME!
     */
    private static void addLibraries(final File projDir, final Properties p) {
        // TODO: implement library hack
        // add file.reference.<name> e.g. file.reference.log4j-1.2.8.jar=<path>
        // for each library, then
        // modify javac.classpath=${<file.ref>}:${<file.ref>}....
        final String up = ".." + File.separator;                     // NOI18N
        final String localPath = up + up + up;
        final String extPath = localPath + up + "ext";               // NOI18N
        final String intPath = localPath + up + "int";               // NOI18N
        final String[] paths = {
                localPath.substring(
                    0,
                    localPath.length() - File.separator.length()),
                extPath,
                intPath
            };
        final String javacCPProp = p.getProperty("javac.classpath"); // NOI18N
        final StringBuffer classPath;
        if (javacCPProp == null) {
            classPath = new StringBuffer();
        } else {
            classPath = new StringBuffer(javacCPProp);
        }
        final String fileRef = "file.reference.";                    // NOI18N
        final FileFilter jarFilter = new FileFilter() {

                @Override
                public boolean accept(final File pathname) {
                    return pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".zip"); // NOI18N
                }
            };
        for (final String path : paths) {
            final File libDir = new File(projDir, path);
            final File[] jarFiles = libDir.listFiles(jarFilter);
            for (final File jar : jarFiles) {
                p.setProperty(fileRef + jar.getName(),
                    path + File.separator + jar.getName());
                classPath.append(":${")                                                                // NOI18N
                .append(fileRef).append(jar.getName()).append('}');                                    // NOI18N
            }
        }
        p.setProperty("javac.classPath", classPath.toString());                                        // NOI18N
    }
}

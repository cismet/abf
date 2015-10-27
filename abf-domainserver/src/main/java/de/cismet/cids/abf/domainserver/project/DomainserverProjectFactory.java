/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DomainserverProjectFactory implements ProjectFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DomainserverProjectFactory.class);

    public static final String PROJECT_DIR = "cidsDomainServer";        // NOI18N
    public static final String PROJECT_PROPFILE = "project.properties"; // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainserverProjectFactory object.
     */
    public DomainserverProjectFactory() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("new DomainserverProjectFactory created"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
        return isProject(dir) ? new DomainserverProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        final Properties properties = project.getLookup().lookup(Properties.class);
        OutputStream fos = null;
        FileLock lock = null;
        try {
            final File file = new File(
                    PROJECT_DIR,
                    PROJECT_PROPFILE);
            final FileObject fo = FileUtil.createData(file);
            lock = fo.lock();
            fos = fo.getOutputStream(lock);
            properties.store(fos, "Cids Domainserver Project Properties");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    LOG.warn("cannot close FileOutputStream when writing project properties", ex); // NOI18N
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    @Override
    public boolean isProject(final FileObject projectDirectory) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "isProject: "
                        + projectDirectory.getPath()
                        + " :: " // NOI18N
                        + (projectDirectory.getFileObject(PROJECT_DIR) != null));
        }
        return projectDirectory.getFileObject(PROJECT_DIR) != null;
    }
}

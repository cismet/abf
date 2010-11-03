/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project;

import org.apache.log4j.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;

import org.openide.filesystems.FileObject;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class LibrarySupportProjectFactory implements ProjectFactory {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROJECT_PROP = "cidsLibBase/project.properties"; // NOI18N

    private static final transient Logger LOG = Logger.getLogger(LibrarySupportProjectFactory.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LibrarySupportProjectFactory object.
     */
    public LibrarySupportProjectFactory() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LibrarySupportProjectFactory created"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LibrarySupportProjectFactory: loading project from dir: " + dir.getPath()); // NOI18N
        }
        return isProject(dir) ? new LibrarySupportProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        // not needed atm
    }

    @Override
    public boolean isProject(final FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_PROP) != null;
    }
}

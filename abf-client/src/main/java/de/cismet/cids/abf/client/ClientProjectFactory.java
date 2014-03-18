/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.3
 */
public class ClientProjectFactory implements ProjectFactory {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROJECT_DIR = "cidsClient";              // NOI18N
    public static final String PROJECT_PROPFILE = "project.properties"; // NOI18N

    //~ Methods ----------------------------------------------------------------

    @Override
    public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
        return isProject(dir) ? new ClientProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        // noop
    }

    @Override
    public boolean isProject(final FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_DIR) != null;
    }
}

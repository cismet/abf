/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.broker;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class BrokerProjectFactory implements ProjectFactory {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROJECT_DIR = "cidsBroker";              // NOI18N
    public static final String PROJECT_PROPFILE = "project.properties"; // NOI18N

    //~ Methods ----------------------------------------------------------------

    @Override
    public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
        return isProject(dir) ? new BrokerProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        final FileObject projectRoot = project.getProjectDirectory();
        if (projectRoot.getFileObject(PROJECT_DIR) == null) {
            throw new IOException("Project dir " + projectRoot.getPath() + " deleted, cannot save project"); // NOI18N
        }
        // Force creation of the scenes/ dir if it was deleted
        ((BrokerProject)project).getWebinterfaceFolder(true);
        // Find the properties file pvproject/project.properties,
        // creating it if necessary
        final String propsPath = PROJECT_DIR + "/" + PROJECT_PROPFILE; // NOI18N
        FileObject propertiesFile = projectRoot.getFileObject(propsPath);
        if (propertiesFile == null) {
            // Recreate the properties file if needed
            propertiesFile = projectRoot.createData(propsPath);
        }
        final Properties properties = project.getLookup().lookup(Properties.class);
        final File f = FileUtil.toFile(propertiesFile);
        properties.store(new FileOutputStream(f), "Cids Broker Project Properties"); // NOI18N
    }

    @Override
    public boolean isProject(final FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_DIR) != null;
    }
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;

import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.Properties;

import javax.swing.Icon;

import de.cismet.cids.abf.utilities.project.NotifyProperties;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.3
 */
public class ClientProject implements Project {

    //~ Static fields/initializers ---------------------------------------------

    public static final String WEBINTERFACE_DIR = "webinterface"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject projectDir;
    private final transient ProjectState state;
    private final transient LogicalViewProvider logicalView;
    private transient Lookup lkp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClientProject object.
     *
     * @param  projectDir  DOCUMENT ME!
     * @param  state       DOCUMENT ME!
     */
    public ClientProject(final FileObject projectDir, final ProjectState state) {
        this.projectDir = projectDir;
        this.state = state;
        logicalView = new ClientLogicalView(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   create  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FileObject getWebinterfaceFolder(final boolean create) {
        FileObject result = projectDir.getFileObject(WEBINTERFACE_DIR);
        if ((result == null) && create) {
            try {
                result = projectDir.createFolder(WEBINTERFACE_DIR);
            } catch (final IOException ioe) {
                ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        ClientProject.class,
                        "ClientProject.getWebinterfaceFolder(boolean).ErrorUtils.message"), // NOI18N
                    ioe);
            }
        }
        return result;
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(
                    new Object[] {
                        this,                     // project spec requires a project be in its own lookup
                        state,                    // allow outside code to mark the project eg. need saving
                        new ActionProviderImpl(), // Provides standard actions
                        new Info(),               // Project information implementation
                        logicalView,              // Logical view of project implementation
                        new ClientProjectCustomizer(this)
                    });
            return lkp;
        }
        return lkp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getProjectProperties() {
        return projectDir.getFileObject(ClientProjectFactory.PROJECT_DIR + "/" + ClientProjectFactory.PROJECT_PROPFILE); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Properties loadProperties() {
        final FileObject fob = projectDir.getFileObject(
                ClientProjectFactory.PROJECT_DIR
                        + "/" // NOI18N
                        + ClientProjectFactory.PROJECT_PROPFILE);
        final Properties properties = new NotifyProperties(state);
        if (fob != null) {
            try {
                properties.load(fob.getInputStream());
            } catch (final IOException e) {
                ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        ClientProject.class,
                        "ClientProject.loadProperties().ErrorUtils.message"), // NOI18N
                    e);
            }
        }
        return properties;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ActionProviderImpl implements ActionProvider {

        //~ Methods ------------------------------------------------------------

        @Override
        public String[] getSupportedActions() {
            return new String[0];
        }

        @Override
        public void invokeAction(final String string, final Lookup lookup) throws IllegalArgumentException {
            // do nothing
        }

        @Override
        public boolean isActionEnabled(final String string, final Lookup lookup) throws IllegalArgumentException {
            return false;
        }
    }

    /**
     * Implementation of project system's ProjectInformation class.
     *
     * @version  $Revision$, $Date$
     */
    private final class Info implements ProjectInformation {

        //~ Instance fields ----------------------------------------------------

        private final transient Icon icon;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Info object.
         */
        Info() {
            icon = ImageUtilities.loadImageIcon(ClientProject.class.getPackage().getName().replaceAll("\\.", "/") // NOI18N
                            + "/client.png",                                          // NOI18N
                    false);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p) {
            // do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p) {
            // do nothing, won't change
        }

        @Override
        public Project getProject() {
            return ClientProject.this;
        }
    }
}

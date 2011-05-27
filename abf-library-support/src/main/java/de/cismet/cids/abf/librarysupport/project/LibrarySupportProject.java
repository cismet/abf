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
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectState;

import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cids.abf.librarysupport.project.customizer.LibrarySupportProjectCustomizer;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.6
 */
public final class LibrarySupportProject implements Project {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(LibrarySupportProject.class);

    public static final String EXT_DIR = "ext";         // NOI18N
    public static final String INT_DIR = "int";         // NOI18N
    public static final String STARTER_DIR = "starter"; // NOI18N
    public static final String LOCAL_DIR = "local";     // NOI18N

    public static final String IMAGE_FOLDER = "de/cismet/cids/abf/librarysupport/images/"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient RequestProcessor processor = new RequestProcessor("LibrarySupportRP(" + this + ")", 7);
    private final transient FileObject distDir;
    private final transient ProjectState state;
    private final transient LibrarySupportLogicalView view;
    private transient Lookup lookup;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of LibrarySupportProject.
     *
     * @param  dir    DOCUMENT ME!
     * @param  state  DOCUMENT ME!
     */
    public LibrarySupportProject(final FileObject dir, final ProjectState state) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("constructing...");       // NOI18N
        }
        this.distDir = dir;
        this.state = state;
        this.view = new LibrarySupportLogicalView(this);
        if (LOG.isDebugEnabled()) {
            LOG.debug("construction finished"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FileObject getProjectDirectory() {
        return distDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RequestProcessor getProcessor() {
        return processor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getProjectProperties() {
        return distDir.getFileObject(
                "cidsLibBase/project.properties"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getBuildXML() {
        return distDir.getFileObject(
                "cidsLibBase/resource/build.xml"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getDefaultManifest() {
        return distDir.getFileObject(
                "cidsLibBase/resource/default.mf"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @depricated  removed for safety reasons, you have to provide your own keystore
     */
    public FileObject getKeystore() {
        return distDir.getFileObject(
                "cidsLibBase/resource/.keystore"); // NOI18N
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(
                    new Object[] {
                        this,
                        state,
                        distDir,
                        new Info(),
                        new LibrarySupportProjectCustomizer(this),
                        view
                    });
        }
        return lookup;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lkp  DOCUMENT ME!
     */
    void addLookup(final Lookup lkp) {
        if (lkp != null) {
            lookup = new ProxyLookup(getLookup(), lkp);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class Info implements ProjectInformation {

        //~ Instance fields ----------------------------------------------------

        private final transient ImageIcon icon = new ImageIcon(
                ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                            + "libbase.png")); // NOI18N

        //~ Methods ------------------------------------------------------------

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getName() {
            return
                java.util.ResourceBundle.getBundle(
                        "de/cismet/cids/abf/librarysupport/project/Bundle") // NOI18N
                .getString("LibrarySupportProject.getName().returnvalue"); // NOI18N
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
            return LibrarySupportProject.this;
        }
    }
}

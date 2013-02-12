/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.DeployAllJarsAction;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.DeployChangedJarsAction;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.RefreshCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.starter.ManifestNode;
import de.cismet.cids.abf.librarysupport.project.nodes.wizard.NewStarterWizardAction;
import de.cismet.cids.abf.utilities.ModificationStore;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.15
 */
public final class StarterManagement extends ProjectNode implements SourceContextCookie,
    StarterManagementContextCookie,
    RefreshCookie,
    Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            StarterManagement.class);

    public static final String SRC_DIR = "src/plain"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient FileChangeListener fileL;
    private final transient FileObject starterDir;
    private final transient FileObject sourceFO;
    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StarterManagement object.
     *
     * @param   project     DOCUMENT ME!
     * @param   starterDir  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public StarterManagement(final LibrarySupportProject project, final FileObject starterDir) {
        super(new StarterManagementChildren(project, starterDir.getFileObject(
                    SRC_DIR)), project);
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializing"); // NOI18N
        }
        this.starterDir = starterDir;
        this.sourceFO = starterDir.getFileObject(SRC_DIR);
        // TODO: maybe try to add this dir and do not throw uncaught exception
        // IN ANY CASE
        if (sourceFO == null) {
            throw new IllegalArgumentException("provided starterdir " // NOI18N
                        + "is not valid: " + starterDir); // NOI18N
        }
        getCookieSet().add(this);
        ModificationStore.getInstance().addObserver(this);
        fileL = new FileChangeListenerImpl();
        this.sourceFO.addFileChangeListener(fileL);
        nodeImage = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                        + "starter_16.gif"); // NOI18N
        final String name = starterDir.getName();
        setName(name);
        setDisplayName(name);

        // init children to ensure the filechangelisteners will be initialized
        project.getProcessor().post(new Runnable() {

                @Override
                public void run() {
                    getChildren().getNodes();
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getStarterObject() {
        return starterDir;
    }

    @Override
    public FileObject getSourceObject() {
        return sourceFO;
    }

    @Override
    public boolean isSourceObjectObservered() {
        return true;
    }

    @Override
    public void setSourceObjectObserved(final boolean observed) {
        if (observed) {
            sourceFO.addFileChangeListener(fileL);
        } else {
            sourceFO.removeFileChangeListener(fileL);
        }
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i) {
        if (ModificationStore.getInstance().anyModifiedInContext(
                        FileUtil.toFile(
                            sourceFO).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED)) {
            final Image badge = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "blueDot_7.gif"); // NOI18N
            return ImageUtilities.mergeImages(nodeImage, badge, 10, 10);
        }
        return nodeImage;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(DeployChangedJarsAction.class),
                CallableSystemAction.get(DeployAllJarsAction.class),
                null,
                CallableSystemAction.get(NewStarterWizardAction.class)
            };
    }

    @Override
    public StarterManagement getStarterManagementContext() {
        return this;
    }

    @Override
    public void update(final Observable observable, final Object object) {
        if (observable instanceof ModificationStore) {
            fireIconChange();
        }
    }

    @Override
    public void refresh() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refresh requested"); // NOI18N
        }
        ((StarterManagementChildren)getChildren()).refreshAll();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class FileChangeListenerImpl implements FileChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void fileFolderCreated(final FileEvent fileEvent) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(getName() + " fireFolderCreated: " // NOI18N
                            + fileEvent.getFile().getNameExt());
            }
        }

        @Override
        public void fileDataCreated(final FileEvent fileEvent) {
            final FileObject fo = fileEvent.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(getName() + " fireDataCreated: " // NOI18N
                            + fo.getNameExt());
            }
            if (fo.getExt().equals("mf"))          // NOI18N
            {
                final String path = FileUtil.toFile(fo).getAbsolutePath();
                ModificationStore.getInstance()
                        .putModification(path.substring(
                                0,
                                path.length()
                                - 3), ModificationStore.MOD_CHANGED);
                refresh();
            }
        }

        @Override
        public void fileChanged(final FileEvent fileEvent) {
            final FileObject fo = fileEvent.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(getName() + " fireChanged: " // NOI18N
                            + fo.getNameExt());
            }
            if (fo.getExt().endsWith("mf"))    // NOI18N
            {
                final String path = FileUtil.toFile(fo).getAbsolutePath();
                ModificationStore.getInstance()
                        .putModification(path.substring(
                                0,
                                path.length()
                                - 3), ModificationStore.MOD_CHANGED);
            }
        }

        @Override
        public void fileDeleted(final FileEvent fileEvent) {
            final FileObject fo = fileEvent.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(getName() + " fireDeleted: " // NOI18N
                            + fo.getNameExt());
            }
            if (fo.getExt().endsWith("mf"))    // NOI18N
            {
                final String path = FileUtil.toFile(fo).getAbsolutePath();
                ModificationStore.getInstance()
                        .removeAllModificationsInContext(
                            path.substring(0, path.length() - 3),
                            ModificationStore.MOD_CHANGED);
                refresh();
            }
        }

        @Override
        public void fileRenamed(final FileRenameEvent fre) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(getName() + " fireRename: " // NOI18N
                            + fre.getFile().getNameExt());
            }
            refresh();
        }

        @Override
        public void fileAttributeChanged(final FileAttributeEvent fae) {
            // do nothing
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class StarterManagementChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            StarterManagementChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject source;
    private final transient LibrarySupportProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StarterManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     * @param  source   DOCUMENT ME!
     */
    StarterManagementChildren(final LibrarySupportProject project, final FileObject source) {
        this.source = source;
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createNodes(final Object object) {
        final FileObject key = (FileObject)object;
        try {
            final ManifestNode node = new ManifestNode(project, key);
            return new Node[] { node };
        } catch (final DataObjectNotFoundException ex) {
            LOG.error("could not create manifestnode", ex); // NOI18N
            return null;
        } catch (final FileNotFoundException ex) {
            LOG.error("could not create manifestnode", ex); // NOI18N
            return null;
        }
    }

    @Override
    protected void addNotify() {
        source.refresh();
        final ArrayList<FileObject> fos = new ArrayList<FileObject>();
        for (final Enumeration<? extends FileObject> e = source.getData(false); e.hasMoreElements();) {
            final FileObject fo = e.nextElement();
            if (fo.getExt().equals("mf")) // NOI18N
            {
                fos.add(fo);
            }
        }
        Collections.sort(fos, new Comparator<FileObject>() {

                @Override
                public int compare(final FileObject f1, final FileObject f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });
        setKeys(fos);
    }

    /**
     * DOCUMENT ME!
     */
    void refreshAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("running refresh");  // NOI18N
        }
        addNotify();
        if (LOG.isDebugEnabled()) {
            LOG.debug("refresh finished"); // NOI18N
        }
    }
}

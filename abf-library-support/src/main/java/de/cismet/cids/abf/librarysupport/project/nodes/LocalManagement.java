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
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

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
import de.cismet.cids.abf.librarysupport.project.nodes.actions.RebuildFromJarAction;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.RefreshCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.liblocal.SourceFolderNode;
import de.cismet.cids.abf.librarysupport.project.nodes.wizard.NewJarWizardAction;
import de.cismet.cids.abf.utilities.ModificationStore;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.11
 */
public final class LocalManagement extends ProjectNode implements LocalManagementContextCookie,
    SourceContextCookie,
    RefreshCookie,
    Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(LocalManagement.class);

    public static final String SRC_DIR = "src/plain"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject localDir;
    private final transient FileObject sourceDir;
    private final transient FileChangeListener fileL;

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of LocalManagement.
     *
     * @param   project   DOCUMENT ME!
     * @param   localDir  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public LocalManagement(final LibrarySupportProject project, final FileObject localDir) {
        super(new ResourceJarManangementChildren(project, localDir.getFileObject(SRC_DIR)), project);
        this.localDir = localDir;
        this.sourceDir = localDir.getFileObject(SRC_DIR);
        if (sourceDir == null) {
            throw new IllegalArgumentException("provided localdir is " // NOI18N
                        + "not valid: " + localDir); // NOI18N
        }
        getCookieSet().add(this);
        ModificationStore.getInstance().addObserver(this);
        fileL = new FileChangeListenerImpl();
        if (LOG.isDebugEnabled()) {
            LOG.debug("local management created fileL: " + fileL); // NOI18N
        }
        sourceDir.addFileChangeListener(fileL);
        nodeImage = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                        + "home_16.gif"); // NOI18N
        setName(localDir.getName());

        // init children to ensure the filechangelisteners will be initialized
        project.getProcessor().post(new Runnable() {

                @Override
                public void run() {
                    getChildren().getNodes();
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FileObject getSourceObject() {
        return sourceDir;
    }

    @Override
    public boolean isSourceObjectObservered() {
        return true;
    }

    @Override
    public void setSourceObjectObserved(final boolean observed) {
        if (observed) {
            sourceDir.addFileChangeListener(fileL);
        } else {
            sourceDir.removeFileChangeListener(fileL);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FileObject getLocalObject() {
        return localDir;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i) {
        Image image = nodeImage;
        if (ModificationStore.getInstance().anyModifiedInContext(
                        FileUtil.toFile(
                            sourceDir).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED)) {
            final Image badge = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "blueDot_7.gif"); // NOI18N
            image = ImageUtilities.mergeImages(nodeImage, badge, 10, 10);
        }

        return image;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(DeployChangedJarsAction.class),
                CallableSystemAction.get(DeployAllJarsAction.class),
                null,
                CallableSystemAction.get(NewJarWizardAction.class),
                null,
                CallableSystemAction.get(RebuildFromJarAction.class)
            };
    }

    @Override
    public void refresh() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refresh requested"); // NOI18N
        }
        ((ResourceJarManangementChildren)getChildren()).refreshAll();
    }

    @Override
    public void update(final Observable observable, final Object object) {
        if (observable instanceof ModificationStore) {
            fireIconChange();
        }
    }

    // TODO: LocalManagementContextCookie is just used as marker cookie atm
    @Override
    public LocalManagement getLocalManagementContext() {
        return this;
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
                LOG.debug(getName() + " :: " + fileEvent.getFile() // NOI18N
                            + " :: fireFolderCreated"); // NOI18N
            }
            ModificationStore.getInstance()
                    .putModification(
                        FileUtil.toFile(fileEvent.getFile()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            refresh();
        }

        @Override
        public void fileDataCreated(final FileEvent fileEvent) {
            // not needed
        }

        @Override
        public void fileChanged(final FileEvent fileEvent) {
            // not needed
        }

        @Override
        public void fileDeleted(final FileEvent fileEvent) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(getName() + ":: " + fileEvent.getFile() // NOI18N
                            + " :: fireDestroy"); // NOI18N
            }
            ModificationStore.getInstance()
                    .removeAllModificationsInContext(
                        FileUtil.toFile(fileEvent.getFile()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            refresh();
        }

        @Override
        public void fileRenamed(final FileRenameEvent fre) {
            if (LOG.isInfoEnabled()) {
                LOG.info(getName() + " :: " + fre.getFile() // NOI18N
                            + " :: fileRenamed, BUT NOTHING DONE"); // NOI18N
            }
        }

        @Override
        public void fileAttributeChanged(final FileAttributeEvent fae) {
            // not needed
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class ResourceJarManangementChildren extends Children.Keys<FileObject> {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ResourceJarManangementChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient LibrarySupportProject project;
    private final transient FileObject sources;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ResourceJarManangementChildren.
     *
     * @param  project  DOCUMENT ME!
     * @param  sources  DOCUMENT ME!
     */
    public ResourceJarManangementChildren(final LibrarySupportProject project, final FileObject sources) {
        this.project = project;
        this.sources = sources;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createNodes(final FileObject fo) {
        return new Node[] { new SourceFolderNode(project, fo) };
    }

    @Override
    protected void addNotify() {
        if (sources == null) {
            LOG.error("could not obtain source directory"); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    ResourceJarManangementChildren.class,
                    "LocalManagement.addNotify().ErrorUtils.message"),
                null);                                      // NOI18N
            setKeys(new FileObject[0]);
            
            return;
        }

        sources.refresh();
        final ArrayList<FileObject> fos = new ArrayList<FileObject>();
        for (final Enumeration<? extends FileObject> e = sources.getFolders(false); e.hasMoreElements();) {
            final FileObject f = e.nextElement();
            if (!RebuildFromJarAction.BACKUP_DIR_NAME.equalsIgnoreCase(f.getName())) {
                fos.add(f);
            }
        }
        // TODO: outsource comparator
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

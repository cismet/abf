/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.liblocal;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.utilities.ModificationStore;
import de.cismet.cids.abf.utilities.files.FileUtils;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.12
 */
public final class SourceFolderNodeChildren extends Children.Keys<Object> implements SourceContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger( SourceFolderNodeChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient LibrarySupportProject project;
    private final transient FileObject sourceDir;

    private final transient FileChangeListener fileL;
    private final transient boolean locked;
    private transient boolean loading;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SourceFolderNodeChildren object.
     *
     * @param  project    DOCUMENT ME!
     * @param  sourceDir  DOCUMENT ME!
     */
    public SourceFolderNodeChildren(final LibrarySupportProject project, final FileObject sourceDir) {
        this.project = project;
        this.sourceDir = sourceDir;
        locked = FileUtils.containsClassFiles(sourceDir);
        if (locked) {
            fileL = null;
        } else {
            fileL = new FileChangeListenerImpl();
            sourceDir.addFileChangeListener(fileL);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("new fileL for dir '" + sourceDir.getName() // NOI18N
                        + "': " + fileL);     // NOI18N
        }
        loading = false;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createNodes(final Object object) {
        if (object instanceof FileObject) {
            return createNode((FileObject)object);
        } else if (object instanceof Node) {
            return new Node[] { (Node)object };
        } else {
            return new Node[] {};
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Node[] createNode(final FileObject fo) {
        try {
            return new Node[] {
                    new PackageFolderNode(project,
                        DataFolder.findFolder(fo),
                        DataFolder.findFolder(sourceDir))
                };
        } catch (final IllegalArgumentException e) {
            LOG.error("could not find DataFolder for FileObject", e); // NOI18N
            return new Node[] {};
        }
    }

    @Override
    protected void addNotify() {
        // if we are loading we can get outta here fast
        if (loading) {
            setKeys(new Object[] { new LoadingNode() });
            return;
        }
        sourceDir.refresh(true);
        final LinkedList<FileObject> keys = new LinkedList<FileObject>();
        for (final Enumeration<? extends FileObject> folderEnum = sourceDir.getFolders(true);
                    folderEnum.hasMoreElements();) {
            final FileObject fo = folderEnum.nextElement();
            // remove the listener before the refresh to not have it attached
            // twice that no change is thrown after reload
            fo.removeFileChangeListener(fileL);
            fo.refresh(true);
            fo.addFileChangeListener(fileL);
            if (isKey(fo)) {
                keys.add(fo);
            }
        }
        if (isKey(sourceDir)) {
            keys.addFirst(sourceDir);
        }
        // TODO: file object comparator could be outsourced
        Collections.sort(keys, new Comparator<FileObject>() {

                @Override
                public int compare(final FileObject f1, final FileObject f2) {
                    return f1.getPath().compareToIgnoreCase(f2.getPath());
                }
            });
        setKeys(keys);
        refresh();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isKey(final FileObject key) {
        if (key.isValid()
                    && key.isFolder()
                    && !FileUtils.containsOnlyMetaFiles(key)) {
            for (final FileObject fobj : key.getChildren()) {
                if (!fobj.isFolder()) {
                    return true;
                }
            }
        } else if (key.isValid()
                    && key.isFolder()
                    && !key.getFolders(false).hasMoreElements()) {
            return true;
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     */
    void refreshAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("running refresh"); // NOI18N
        }
        addNotify();
        if (LOG.isDebugEnabled()) {
            LOG.debug("refresh finished");
        }
    }

    @Override
    public FileObject getSourceObject() throws FileNotFoundException {
        return sourceDir;
    }

    @Override
    public boolean isSourceObjectObservered() {
        return !locked;
    }

    @Override
    public void setSourceObjectObserved(final boolean observed) {
        if (!isSourceObjectObservered()) {
            return;
        }
        // if the observation is cancelled something is done, so set to
        // "loading" mode and detach the listener
        if (observed) {
            loading = false;
            // refresh must be done BEFORE the listener is added again
            refreshAll();
            sourceDir.addFileChangeListener(fileL);
        } else {
            loading = true;
            sourceDir.removeFileChangeListener(fileL);
            // refresh must be done AFTER the listener is removed
            refreshAll();
        }
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
        public void fileFolderCreated(final FileEvent fe) {
            final Node parent = getNode();
            if (parent == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("I should be gc'ed soon!");          // NOI18N
                    LOG.debug("unregister self: " + this);         // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(parent.getName() + " :: " + fe.getFile() // NOI18N
                            + " :: fileFolderCreated");            // NOI18N
                LOG.debug("fileL:: fileFolderCreated: " + this);   // NOI18N
            }
            ModificationStore.getInstance()
                    .putModification(
                        FileUtil.toFile(fe.getFile()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            refreshAll();
        }

        @Override
        public void fileDataCreated(final FileEvent fe) {
            final Node parent = getNode();
            if (parent == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("I should be gc'ed soon!");          // NOI18N
                    LOG.debug("unregister self: " + this);         // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            final FileObject file = fe.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(parent.getName() + " :: " + file         // NOI18N
                            + " :: fileDataCreated");              // NOI18N
                LOG.debug("fileL :: fileDataCreated: " + this);    // NOI18N
            }
            if (FileUtils.isMetaFile(file)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            ModificationStore.getInstance()
                    .putModification(
                        FileUtil.toFile(file.getParent()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            refreshAll();
        }

        @Override
        public void fileChanged(final FileEvent fe) {
            final Node parent = getNode();
            if (parent == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("I should be gc'ed soon!");          // NOI18N
                    LOG.debug("unregister self: " + this);         // NOI18N
                }
                return;
            }
            final FileObject file = fe.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(parent.getName() + " :: " + file         // NOI18N
                            + " :: fileChanged");                  // NOI18N
                LOG.debug("fileL :: fileChanged: " + this);        // NOI18N
            }
            if (FileUtils.isMetaFile(file)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            if (file.isData()) {
                ModificationStore.getInstance()
                        .putModification(
                            FileUtil.toFile(file.getParent()).getAbsolutePath(),
                            ModificationStore.MOD_CHANGED);
            } else {
                ModificationStore.getInstance()
                        .putModification(
                            FileUtil.toFile(file).getAbsolutePath(),
                            ModificationStore.MOD_CHANGED);
            }
            refreshAll();
        }

        @Override
        public void fileDeleted(final FileEvent fe) {
            final Node parent = getNode();
            if (parent == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("I should be gc'ed soon!");          // NOI18N
                    LOG.debug("unregister self: " + this);         // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            final FileObject file = fe.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(parent.getName() + " :: " + file         // NOI18N
                            + " :: fileDeleted");                  // NOI18N
                LOG.debug("fileL :: fileDeleted: " + this);        // NOI18N
            }
            if (FileUtils.isMetaFile(file)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            // maybe sourceDir is not valid anymore due to deletion, so check
            if (sourceDir.isValid() && !file.equals(sourceDir)) {
                ModificationStore.getInstance()
                        .putModification(
                            FileUtil.toFile(file.getParent()).getAbsolutePath(),
                            ModificationStore.MOD_CHANGED);
            }
            refreshAll();
        }

        @Override
        public void fileRenamed(final FileRenameEvent fre) {
            final Node parent = getNode();
            if (parent == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("I should be gc'ed soon!");          // NOI18N
                    LOG.debug("unregister self: " + this);         // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            FileObject file = fre.getFile();
            if (LOG.isDebugEnabled()) {
                LOG.debug(parent.getName() + " :: " + file         // NOI18N
                            + " :: fileRenamed");                  // NOI18N
                LOG.debug("fileL ::fileRenamed:" + this);          // NOI18N
            }
            if (FileUtils.isMetaFile(file)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            if (file.isData()) {
                file = file.getParent();
            }
            final String newPath = FileUtil.toFile(file).getAbsolutePath();
            final String commonPath = newPath.substring(0, newPath.lastIndexOf(
                        System.getProperty("file.separator")));    // NOI18N
            ModificationStore.getInstance().renameElement(
                commonPath
                        + File.separator
                        + fre.getName(),
                newPath);
            refreshAll();
        }

        @Override
        public void fileAttributeChanged(final FileAttributeEvent fae) {
            // not needed
        }
    }
}

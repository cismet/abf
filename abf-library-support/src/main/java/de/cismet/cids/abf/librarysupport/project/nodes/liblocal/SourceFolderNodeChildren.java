/*
 * SourceFolderNodeChildren.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 13. Juli 2007, 16:25
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes.liblocal;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.utilities.files.FileUtils;
import de.cismet.cids.abf.utilities.ModificationStore;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
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

/**
 *
 * @author mscholl
 * @version 1.12
 */
public final class SourceFolderNodeChildren extends Children.Keys implements
        SourceContextCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            SourceFolderNodeChildren.class);
    
    private final transient LibrarySupportProject project;
    private final transient FileObject sourceDir;
    
    private final transient FileChangeListener fileL;
    private final transient boolean locked;
    private transient boolean loading;

    public SourceFolderNodeChildren(final LibrarySupportProject project, final 
            FileObject sourceDir)
    {
        this.project = project;
        this.sourceDir = sourceDir;
        locked = FileUtils.containsClassFiles(sourceDir);
        if(locked)
        {
            fileL = null;
        }else
        {
            fileL = new FileChangeListenerImpl();
            sourceDir.addFileChangeListener(fileL);
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("new fileL for dir '" + sourceDir.getName() // NOI18N
                    + "': " + fileL); // NOI18N
        }
        loading = false;
    }

    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof FileObject)
        {
            return createNode((FileObject)object);
        }else if(object instanceof Node)
        {
            return new Node[] {(Node)object};
        }else
        {
            return new Node[] {};
        }
    }
    
    private Node[] createNode(final FileObject fo)
    {
        try
        {
            return new Node[]
            {
                new PackageFolderNode(project,
                        DataFolder.findFolder(fo),
                        DataFolder.findFolder(sourceDir))
            };
        } catch(final IllegalArgumentException e)
        {
            LOG.error("could not find DataFolder for FileObject", e); // NOI18N
            return new Node[]{};
        }
    }

    @Override
    protected void addNotify()
    {
        // if we are loading we can get outta here fast
        if(loading)
        {
            setKeys(new Object[] {new LoadingNode()});
            return;
        }
        sourceDir.refresh(true);
        final LinkedList<FileObject> keys = new LinkedList<FileObject>();
        for(final Enumeration<? extends FileObject> folderEnum = sourceDir.
                getFolders(true); folderEnum.hasMoreElements();)
        {
            final FileObject fo = folderEnum.nextElement();
            // remove the listener before the refresh to not have it attached
            // twice that no change is thrown after reload
            fo.removeFileChangeListener(fileL);
            fo.refresh(true);
            fo.addFileChangeListener(fileL);
            if(isKey(fo))
            {
                keys.add(fo);
            }
        }
        if(isKey(sourceDir))
        {
            keys.addFirst(sourceDir);
        }
        // TODO: file object comparator could be outsourced
        Collections.sort(keys, new Comparator<FileObject>()
        {
            @Override
            public int compare(final FileObject f1, final FileObject f2)
            {
                return f1.getPath().compareToIgnoreCase(f2.getPath());
            }
        });
        setKeys(keys);
        refresh();
    }

    private boolean isKey(final FileObject key)
    {
        if(key.isValid()
                && key.isFolder()
                && !FileUtils.containsOnlyMetaFiles(key))
        {
            for(final FileObject fobj : key.getChildren())
            {
                if(!fobj.isFolder())
                {
                    return true;
                }
            }
        }else if(key.isValid()
                && key.isFolder()
                && !key.getFolders(false).hasMoreElements())
        {
            return true;
        }
        return false;
    }
    
    void refreshAll()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("running refresh"); // NOI18N
        }
        addNotify();
        if(LOG.isDebugEnabled())
        {
            LOG.debug("refresh finished");
        }
    }

    @Override
    public FileObject getSourceObject() throws FileNotFoundException
    {
        return sourceDir;
    }

    @Override
    public boolean isSourceObjectObservered()
    {
        return !locked;
    }

    @Override
    public void setSourceObjectObserved(final boolean observed)
    {
        if(!isSourceObjectObservered())
        {
            return;
        }
        // if the observation is cancelled something is done, so set to
        // "loading" mode and detach the listener
        if(observed)
        {
            loading = false;
            // refresh must be done BEFORE the listener is added again
            refreshAll();
            sourceDir.addFileChangeListener(fileL);
        }else
        {
            loading = true;
            sourceDir.removeFileChangeListener(fileL);
            // refresh must be done AFTER the listener is removed
            refreshAll();
        }
    }
    
    private final class FileChangeListenerImpl implements FileChangeListener
    {
        @Override
        public void fileFolderCreated(final FileEvent fe)
        {
            final Node parent = getNode();
            if(parent == null)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("I should be gc'ed soon!"); // NOI18N
                    LOG.debug("unregister self: " + this); // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            if(LOG.isDebugEnabled())
            {
                LOG.debug(parent.getName() + " :: " + fe.getFile() // NOI18N
                        + " :: fileFolderCreated"); // NOI18N
                LOG.debug("fileL:: fileFolderCreated: " + this); // NOI18N
            }
            ModificationStore.getInstance().putModification(
                    FileUtil.toFile(fe.getFile()).getAbsolutePath(),
                    ModificationStore.MOD_CHANGED);
            refreshAll();
        }

        @Override
        public void fileDataCreated(final FileEvent fe)
        {
            final Node parent = getNode();
            if(parent == null)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("I should be gc'ed soon!"); // NOI18N
                    LOG.debug("unregister self: " + this); // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            final FileObject file = fe.getFile();
            if(LOG.isDebugEnabled())
            {
                LOG.debug(parent.getName() + " :: " + file // NOI18N
                        + " :: fileDataCreated"); // NOI18N
                LOG.debug("fileL :: fileDataCreated: " + this); // NOI18N
            }
            if(FileUtils.isMetaFile(file))
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            ModificationStore.getInstance().putModification(
                    FileUtil.toFile(file.getParent()).getAbsolutePath(),
                    ModificationStore.MOD_CHANGED);
            refreshAll();
        }
        
        @Override
        public void fileChanged(final FileEvent fe)
        {
            final Node parent = getNode();
            if(parent == null)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("I should be gc'ed soon!"); // NOI18N
                    LOG.debug("unregister self: " + this); // NOI18N
                }
                return;
            }
            final FileObject file = fe.getFile();
            if(LOG.isDebugEnabled())
            {
                LOG.debug(parent.getName() + " :: " + file // NOI18N
                        + " :: fileChanged"); // NOI18N
                LOG.debug("fileL :: fileChanged: " + this); // NOI18N
            }
            if(FileUtils.isMetaFile(file))
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            if(file.isData())
            {
                ModificationStore.getInstance().putModification(
                        FileUtil.toFile(file.getParent()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            }
            else
            {
                ModificationStore.getInstance().putModification(
                        FileUtil.toFile(file).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            }
            refreshAll();
        }

        @Override
        public void fileDeleted(final FileEvent fe)
        {
            final Node parent = getNode();
            if(parent == null)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("I should be gc'ed soon!"); // NOI18N
                    LOG.debug("unregister self: " + this); // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            final FileObject file = fe.getFile();
            if(LOG.isDebugEnabled())
            {
                LOG.debug(parent.getName() + " :: " + file // NOI18N
                        + " :: fileDeleted"); // NOI18N
                LOG.debug("fileL :: fileDeleted: " + this); // NOI18N
            }
            if(FileUtils.isMetaFile(file))
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            // maybe sourceDir is not valid anymore due to deletion, so check
            if(sourceDir.isValid() && !file.equals(sourceDir))
            {
                ModificationStore.getInstance().putModification(
                        FileUtil.toFile(file.getParent()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            }
            refreshAll();
        }
        
        @Override
        public void fileRenamed(final FileRenameEvent fre)
        {
            final Node parent = getNode();
            if(parent == null)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("I should be gc'ed soon!"); // NOI18N
                    LOG.debug("unregister self: " + this); // NOI18N
                }
                sourceDir.removeFileChangeListener(this);
                return;
            }
            FileObject file = fre.getFile();
            if(LOG.isDebugEnabled())
            {
                LOG.debug(parent.getName() + " :: " + file // NOI18N
                        + " :: fileRenamed"); // NOI18N
                LOG.debug("fileL ::fileRenamed:" + this); // NOI18N
            }
            if(FileUtils.isMetaFile(file))
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("file is meta file, doing nothing"); // NOI18N
                }
                return;
            }
            if(file.isData())
            {
                file = file.getParent();
            }
            final String newPath = FileUtil.toFile(file).getAbsolutePath();
            final String commonPath = newPath.substring(0, newPath.lastIndexOf(
                    System.getProperty("file.separator"))); // NOI18N
            ModificationStore.getInstance().renameElement(
                    commonPath + File.separator + fre.getName(), newPath);
            refreshAll();
        }
        
        @Override
        public void fileAttributeChanged(final FileAttributeEvent fae)
        {
            // not needed
        }
    }
}

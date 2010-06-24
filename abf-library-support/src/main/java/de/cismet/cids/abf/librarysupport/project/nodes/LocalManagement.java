/*
 * LocalManagement.java, encoding: UTF-8
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
 * Created on 10. Juli 2007, 15:56
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes;

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
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Action;
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


/**
 *
 * @author mscholl
 * @version 1.11
 */
public final class LocalManagement extends ProjectNode implements
        LocalManagementContextCookie,
        SourceContextCookie,
        RefreshCookie,
        Observer
{
    private static final transient Logger LOG = Logger.getLogger(
            LocalManagement.class);
    
    public static final String SRC_DIR = "src/plain"; // NOI18N

    private final transient FileObject localDir;
    private final transient FileObject sourceDir;
    private final transient FileChangeListener fileL;
    
    private final transient Image nodeImage;
    
    /**
     * Creates a new instance of LocalManagement
     */
    public LocalManagement(final LibrarySupportProject project, final FileObject 
            localDir)
    {
        super(new ResourceJarManangementChildren(project, localDir.
                getFileObject(SRC_DIR)), project);
        this.localDir = localDir;
        this.sourceDir = localDir.getFileObject(SRC_DIR);
        if(sourceDir == null)
        {
            throw new IllegalArgumentException("provided localdir is " // NOI18N
                    + "not valid: " + localDir); // NOI18N
        }
        getCookieSet().add(this);
        ModificationStore.getInstance().addObserver(this);
        fileL = new FileChangeListenerImpl();
        if(LOG.isDebugEnabled())
        {
            LOG.debug("local management created fileL: " + fileL); // NOI18N
        }
        sourceDir.addFileChangeListener(fileL);
        nodeImage = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                + "home_16.gif"); // NOI18N
        setName(localDir.getName());
        // init children to ensure the filechangelisteners will be initialized
        getChildren().getNodes();
    }

    @Override
    public FileObject getSourceObject()
    {
        return sourceDir;
    }

    @Override
    public boolean isSourceObjectObservered()
    {
        return true;
    }

    @Override
    public void setSourceObjectObserved(final boolean observed)
    {
        if(observed)
        {
            sourceDir.addFileChangeListener(fileL);
        }else
        {
            sourceDir.removeFileChangeListener(fileL);
        }
    }

    public FileObject getLocalObject()
    {
        return localDir;
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i)
    {
        if(ModificationStore.getInstance().anyModifiedInContext(FileUtil.toFile(
                sourceDir).getAbsolutePath(), ModificationStore.MOD_CHANGED))
        {
            final Image badge = ImageUtilities.loadImage(LibrarySupportProject
                    .IMAGE_FOLDER + "blueDot_7.gif"); // NOI18N
            return ImageUtilities.mergeImages(nodeImage, badge, 10, 10);
        }
        return nodeImage;
    }   
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[] 
        {
            CallableSystemAction.get(DeployChangedJarsAction.class),
            CallableSystemAction.get(DeployAllJarsAction.class), null,
            CallableSystemAction.get(NewJarWizardAction.class), null,
            CallableSystemAction.get(RebuildFromJarAction.class)
        };
    }

    @Override
    public void refresh()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("refresh requested"); // NOI18N
        }
        ((ResourceJarManangementChildren)getChildren()).refreshAll();
    }

    @Override
    public void update(final Observable observable, final Object object)
    {
        if(observable instanceof ModificationStore)
        {
            fireIconChange();
        }
    }

    // TODO: LocalManagementContextCookie is just used as marker cookie atm
    @Override
    public LocalManagement getLocalManagementContext()
    {
        return this;
    }

    private final class FileChangeListenerImpl implements FileChangeListener
    {
        @Override
        public void fileFolderCreated(final FileEvent fileEvent)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug(getName() + " :: " + fileEvent.getFile() // NOI18N
                        + " :: fireFolderCreated"); // NOI18N
            }
            ModificationStore.getInstance().putModification(
                    FileUtil.toFile(fileEvent.getFile()).getAbsolutePath(),
                    ModificationStore.MOD_CHANGED);
            refresh();
        }

        @Override
        public void fileDataCreated(final FileEvent fileEvent)
        {
            // not needed
        }

        @Override
        public void fileChanged(final FileEvent fileEvent)
        {
            // not needed
        }

        @Override
        public void fileDeleted(final FileEvent fileEvent)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug(getName() + ":: " + fileEvent.getFile() // NOI18N
                        + " :: fireDestroy"); // NOI18N
            }
            ModificationStore.getInstance().removeAllModificationsInContext(
                    FileUtil.toFile(fileEvent.getFile()).getAbsolutePath(), 
                    ModificationStore.MOD_CHANGED);
            refresh();
        }

        @Override
        public void fileRenamed(final FileRenameEvent fre)
        {
            if(LOG.isInfoEnabled())
            {
                LOG.info(getName() + " :: " + fre.getFile() // NOI18N
                        + " :: fileRenamed, BUT NOTHING DONE"); // NOI18N
            }
        }

        @Override
        public void fileAttributeChanged(final FileAttributeEvent fae)
        {
            // not needed
        }
    }
}

final class ResourceJarManangementChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            ResourceJarManangementChildren.class);
    
    private final transient LibrarySupportProject project;
    private final transient FileObject sources;
    
    /**
     * Creates a new instance of ResourceJarManangementChildren
     */
    public ResourceJarManangementChildren(final LibrarySupportProject project, 
            final FileObject sources)
    {
        this.project = project;
        this.sources = sources;
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        final FileObject fo = (FileObject)object;
        return new Node[] {new SourceFolderNode(project, fo)};
    }
    
    @Override
    protected void addNotify()
    {
        if(sources == null)
        {
            LOG.error("could not obtain source directory"); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    ResourceJarManangementChildren.class,
                    "LocalManagement.addNotify().ErrorUtils.message"), null); // NOI18N
            setKeys(new Object[] {});
            return;
        }
        sources.refresh();
        final ArrayList<FileObject> fos = new ArrayList<FileObject>();
        for(final Enumeration<? extends FileObject> e =
                sources.getFolders(false); e.hasMoreElements();)
        {
            final FileObject f = e.nextElement();
            if(!RebuildFromJarAction.BACKUP_DIR_NAME
                    .equalsIgnoreCase(f.getName()))
            {
                fos.add(f);
            }
        }
        // TODO: outsource comparator
        Collections.sort(fos, new Comparator<FileObject>()
        {
            @Override
            public int compare(final FileObject f1, final FileObject f2)
            {
                return f1.getName().compareTo(f2.getName());
            }
        });
        setKeys(fos);
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
            LOG.debug("refresh finished"); // NOI18N
        }
    }
}

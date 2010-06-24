/*
 * SourceFolderNode.java, encoding: UTF-8
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
 * Created on 13. Juli 2007, 14:13
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes.liblocal;

import de.cismet.cids.abf.utilities.DnDUtils;
import de.cismet.cids.abf.utilities.DnDUtils.UnifiedDnDFilePasteType;
import de.cismet.cids.abf.utilities.DnDUtils.UnifiedFilePasteType;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.*;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.DeployJarAction;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.RebuildFromJarAction;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.PackageContextCookieImpl;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.wizard.AddFilesWizardAction;
import de.cismet.cids.abf.librarysupport.project.nodes.wizard.NewWizardAction;
import de.cismet.cids.abf.librarysupport.project.nodes.wizard.RenameJarWizardAction;
import de.cismet.cids.abf.utilities.files.FileUtils;
import de.cismet.cids.abf.utilities.ModificationStore;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author mscholl
 * @version 1.12
 */
public final class SourceFolderNode extends ProjectNode implements
        SourceContextCookie,
        Observer
{
    private static final transient Logger LOG;

    private static final Image NODE_IMAGE;
    private static final String BINARY_ARCHIVE;
    
    private final transient FileObject sourceDir;
    private final transient boolean locked;

    static
    {
        LOG = Logger.getLogger(SourceFolderNode.class);
        NODE_IMAGE = ImageUtilities.loadImage(LibrarySupportProject
                .IMAGE_FOLDER + "jar_16.gif"); // NOI18N
        BINARY_ARCHIVE = org.openide.util.NbBundle.getMessage(
                SourceFolderNode.class, "SourceFolderNode.BINARY_ARCHIVE"); // NOI18N
    }

    /**
     * Creates a new instance of SourceFolderNode
     */
    public SourceFolderNode(final LibrarySupportProject project, final 
            FileObject sourceDir)
    {
        super(new SourceFolderNodeChildren(project, sourceDir), project);
        this.sourceDir = sourceDir;
        locked = FileUtils.containsClassFiles(sourceDir);
        final String name = sourceDir.getName();
        super.setName(name);
        if(locked)
        {
            setDisplayName(name + " [" + BINARY_ARCHIVE + "]"); // NOI18N
            setChildren(Children.LEAF);
        }else
        {
            getCookieSet().add(this);
            getCookieSet().add(
                    new PackageContextCookieImpl(sourceDir, sourceDir));
            ModificationStore.getInstance().addObserver(this);
            // init children to ensure the filechangelisteners will be
            // initialized
            getChildren().getNodes();
        }
    }

    @Override
    public String getHtmlDisplayName()
    {
        if(locked)
        {
            return "<font color='!textText'>" // NOI18N
                    + getName()
                    + "</font><font color='!controlShadow'> [" // NOI18N
                    + BINARY_ARCHIVE
                    + "]</font>"; // NOI18N
        }else
        {
            return super.getHtmlDisplayName();
        }
    }

    @Override
    public FileObject getSourceObject()
    {
        return sourceDir;
    }

    @Override
    public boolean isSourceObjectObservered()
    {
        final Children ch = getChildren();
        if(ch instanceof SourceContextCookie)
        {
            return ((SourceContextCookie)ch).isSourceObjectObservered();
        }else
        {
            return false;
        }
    }

    @Override
    public void setSourceObjectObserved(final boolean observed)
    {
        final Children ch = getChildren();
        if(ch instanceof SourceContextCookie)
        {
            ((SourceContextCookie)ch).setSourceObjectObserved(observed);
        }
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i)
    {
        Image image = NODE_IMAGE;
        // this instance or any child was changed?
        if(ModificationStore.getInstance().anyModifiedInContext(FileUtil.toFile(
                sourceDir).getAbsolutePath(), ModificationStore.MOD_CHANGED))
        {
            final Image badge = ImageUtilities.loadImage(LibrarySupportProject
                    .IMAGE_FOLDER + "blueDot_7.gif"); // NOI18N
            image = ImageUtilities.mergeImages(image, badge, 10, 10);
        }else if(locked)
        {
            final Image badge = ImageUtilities.loadImage(LibrarySupportProject
                    .IMAGE_FOLDER + "lockBadge_16.png"); // NOI18N
            return ImageUtilities.mergeImages(image, badge, 3, 3);
        }
        final Node parent = getParentNode();
        if(parent instanceof LocalManagement)
        {
            final LocalManagement lm = (LocalManagement)parent;
            if(lm.getLocalObject().getFileObject(
                    sourceDir.getName(), "jar") == null) // NOI18N
            {
                // asuming jar not present
                final Image badge = ImageUtilities.loadImage(
                        LibrarySupportProject.IMAGE_FOLDER
                        + "newBadge_16.png"); // NOI18N
                image = ImageUtilities.mergeImages(image, badge, 1, 0);
            }
        }
        return image;
    }

    @Override
    public Action[] getActions(final boolean b)
    {
        if(locked)
        {
            return new Action[]
            {
                CallableSystemAction.get(RebuildFromJarAction.class)
            };
        }else
        {
            return new Action[]
            {
                CallableSystemAction.get(DeployJarAction.class), null,
                CallableSystemAction.get(NewWizardAction.class),
                CallableSystemAction.get(AddFilesWizardAction.class), null,
                CallableSystemAction.get(CopyAction.class),
                CallableSystemAction.get(CutAction.class),
                CallableSystemAction.get(PasteAction.class), null,
                CallableSystemAction.get(DeleteAction.class),
                CallableSystemAction.get(RenameJarWizardAction.class), null,
                CallableSystemAction.get(RebuildFromJarAction.class)
            };
        }
    }

    @Override
    public PasteType getDropType(final Transferable t, final int action,
            final int index)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("see what flavor is supported"); // NOI18N
        }
        if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("javaFileListFlavor is supported"); // NOI18N
            }
            try
            {
                final Object obj = t
                        .getTransferData(DataFlavor.javaFileListFlavor);
                if(obj != null)
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("transferdata already present"); // NOI18N
                    }
                    if(!DnDUtils.acceptFiles(
                            (List)obj, FileUtil.toFile(sourceDir), action))
                    {
                        return super.getDropType(t, action, index);
                    }
                }
                return new UnifiedDnDFilePasteType(t, action, sourceDir);
            }catch(final UnsupportedFlavorException ex)
            {
                LOG.warn("javaFileListFlavor not supported despite " // NOI18N
                        + "transferable's \"tell\"", ex); // NOI18N
            }catch(final IOException ex)
            {
                LOG.warn("could not get transferdata before " // NOI18N
                        + "paste action", ex); // NOI18N
            }
        }else if(t.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR))
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("uriListFlavor is supported"); // NOI18N
            }
            final List<File> fileList = DnDUtils.getFileListFromURIList(t);
            if(fileList != null)
            {
                if(DnDUtils.acceptFiles(
                        fileList, FileUtil.toFile(sourceDir), action))
                {
                    return new UnifiedDnDFilePasteType(t, action, sourceDir);
                }
            }else
            {
                LOG.warn("cannot retrieve transfer data"); // NOI18N
            }
        }else
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("no known flavor supported"); // NOI18N
            }
        }
        return super.getDropType(t, action, index);
    }

    @Override
    protected void createPasteTypes(final Transferable t,
            final List<PasteType> s)
    {
        super.createPasteTypes(t, s);
        s.add(new UnifiedFilePasteType(t, sourceDir));
    }

    @Override
    public boolean canCut()
    {
        return false;
    }

    @Override
    public boolean canCopy()
    {
        return false;
    }

    @Override
    public boolean canRename()
    {
        // we should not permit rename because there is no validation
        // if direct renaming in the tree is requested then an additional
        // validation has to be implemented in setName(String)
        return false;
    }

    @Override
    public boolean canDestroy()
    {
        return true;
    }
    
    @Override
    public void destroy() throws IOException
    {
        FileLock lock = null;
        try
        {
            lock = sourceDir.lock();
            sourceDir.delete(lock);
            lock.releaseLock();
            super.destroy();
        } catch (final IOException ex)
        {
            LOG.error("delete failed: " + sourceDir.getName(), ex); // NOI18N
            ErrorManager.getDefault().annotate(ex, org.openide.util.NbBundle
                    .getMessage(SourceFolderNode.class, 
                    "SourceFolderNode.destroy().ErrorManager.message") + sourceDir.getName()); // NOI18N
        } finally
        {
            if(lock != null && lock.isValid())
            {
                lock.releaseLock();
            }
        }
    }
    
    // rename action uses this
    @Override
    public void setName(final String newName)
    {
        FileLock lock = null;
        try
        {
            lock = sourceDir.lock();
            sourceDir.rename(lock, newName, null);
            lock.releaseLock();
            super.setName(newName);
        } catch (final IOException ex)
        {
            LOG.error("rename failed: " + sourceDir.getName() + // NOI18N
                    " to " + newName, ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        } finally
        {
            if(lock != null && lock.isValid())
            {
                lock.releaseLock();
            }
        }
    }
    
    @Override
    public void update(final Observable observable, final Object object)
    {
        if(observable instanceof ModificationStore)
        {
            fireIconChange();
        }
    }
}

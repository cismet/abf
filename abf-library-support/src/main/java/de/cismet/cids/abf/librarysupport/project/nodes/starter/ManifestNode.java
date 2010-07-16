/*
 * ManifestNode.java, encoding: UTF-8
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
 * Created on 27. August 2007, 09:49
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes.starter;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.DeployJarAction;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.ManifestProviderCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.wizard.RenameManifestWizardAction1;
import de.cismet.cids.abf.utilities.ModificationStore;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author mscholl
 * @version 1.8
 */
public final class ManifestNode extends DataNode implements 
        Observer,
        SourceContextCookie,
        ManifestProviderCookie,
        LibrarySupportContextCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            ManifestNode.class);
    
    private final transient LibrarySupportProject project;
    private final transient Image nodeImage;
    
    public ManifestNode(final LibrarySupportProject project, final FileObject 
            manifest) throws
            DataObjectNotFoundException,
            FileNotFoundException
    {
        super(DataObject.find(manifest), Children.LEAF);
        this.project = project;
        getSourceObject();
        getCookieSet().add(this);
        ModificationStore.getInstance().addObserver(this);
        nodeImage = ImageUtilities.loadImage(LibrarySupportProject
                .IMAGE_FOLDER + "start_16.png"); // NOI18N
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i)
    {
        try
        {
            if(ModificationStore.getInstance().wasModified(FileUtil.toFile(
                    getSourceObject()).getAbsolutePath(), ModificationStore.
                    MOD_CHANGED))
            {
                final Image badge = ImageUtilities.loadImage(
                        LibrarySupportProject.IMAGE_FOLDER
                        + "blueDot_7.gif"); // NOI18N
                return ImageUtilities.mergeImages(nodeImage, badge, 10, 10);
            }
        } catch (final FileNotFoundException ex)
        {
            LOG.warn("could not put modification because source " // NOI18N
                    + "folder not available", ex); // NOI18N
        }
        return nodeImage;
    }

    @Override
    public Action[] getActions(final boolean b)
    {
        final Action[] supar = super.getActions(b);
        final Action[] ret = new Action[supar.length + 3];
        for(int i = 0; i < supar.length; i++)
        {
            if(i == 0)
            {
                ret[i] = supar[i];
                ret[i+1] = null;
                ret[i+2] = CallableSystemAction.get(DeployJarAction.class);
                ret[i+3] = null;
            }else if(supar[i] instanceof RenameAction)
            {
                ret[i+3] = CallableSystemAction.get(RenameManifestWizardAction1.
                        class);
            }else
            {
                ret[i+3] = supar[i];
            }
        }
        return ret;
    }

    @Override
    public Action getPreferredAction()
    {
        return CallableSystemAction.get(OpenAction.class);
    }

    @Override
    public LibrarySupportProject getLibrarySupportContext()
    {
        return project;
    }

    @Override
    public void setName(final String string)
    {
        setName(string, true);
    }

    @Override
    public void setName(final String string, final boolean b)
    {
        FileLock lock = null;
        // try to rename corresponding src folder too
        try
        {
            // get the old folder with dataobject before it is renamed
            final FileObject parent = getManifest().getParent();
            final FileObject srcDir = getSourceObject();
            final String oldName = FileUtil.toFile(srcDir).getAbsolutePath();
            if(srcDir == null)
            {
                throw new IOException("the source dir could not " // NOI18N
                        + "be obtained"); // NOI18N
            }
            // manifest must be renamed first
            super.setName(string, b);
            // now the source folder can be renamed
            lock = srcDir.lock();
            srcDir.rename(lock, string, null);
            ModificationStore.getInstance().renameElement(oldName, FileUtil.
                    toFile(srcDir).getAbsolutePath());
            parent.refresh();
        } catch (final Exception ex)
        {
            LOG.error("could not rename manifest", ex); // NOI18N
            ErrorManager.getDefault().annotate(ex, org.openide.util.NbBundle
                    .getMessage(ManifestNode.class, 
                    "ManifestNode.setName(String,boolean).ErrorManager.message")); // NOI18N
        } finally
        {
            if(lock != null && lock.isValid())
            {
                lock.releaseLock();
            }
        }
    }

    @Override
    public void destroy() throws IOException
    {
        try
        {
            final FileObject parent = getManifest().getParent();
            final FileObject srcDir = getSourceObject();
            FileLock lock = null;
            // now the dataobject may be destroyed
            super.destroy();
            if(srcDir == null)
            {
                return;
            }
            // try to destroy corresponding src folder too
            try
            {
                lock = srcDir.lock();
                srcDir.delete(lock);
            } catch (final IOException ex)
            {
                LOG.error("could not delete source folder", ex); // NOI18N
                ErrorManager.getDefault().annotate(ex, org.openide.util.NbBundle
                        .getMessage(ManifestNode.class, 
                        "ManifestNode.destroy().ErrorManager.message")); // NOI18N
            } finally
            {
                if(lock != null && lock.isValid())
                {
                    lock.releaseLock();
                }
                parent.refresh();
            }
        } catch(final Exception e)
        {
            LOG.error("exception in destroy", e); // NOI18N
        }
    }

    @Override
    public String getHtmlDisplayName() 
    {
        return getManifest().getName();
    }

    @Override
    public void update(final Observable observable, final Object object)
    {
        if(observable instanceof ModificationStore)
        {
            fireIconChange();
        }
    }

    @Override
    public FileObject getManifest()
    {
        return getDataObject().getPrimaryFile();
    }

    @Override
    public boolean canRename()
    {
        return false;
    }

    @Override
    public FileObject getSourceObject() throws FileNotFoundException
    {
        final FileObject manifest = getDataObject().getPrimaryFile();
        final FileObject parent = manifest.getParent();
        final FileObject srcDir = parent.getFileObject(manifest.getName());
        if(srcDir == null)
        {
            LOG.warn("source dir is not present, trying to create it");// NOI18N
            try
            {
                parent.createFolder(manifest.getName());
            }catch(final Exception e)
            {
                LOG.error("could not create source dir", e); // NOI18N
                ErrorManager.getDefault().annotate(e, org.openide.util.NbBundle
                        .getMessage(ManifestNode.class, 
                        "ManifestNode.getSourceObject().ErrorManager.message")); // NOI18N
                throw new FileNotFoundException("src dir not present " // NOI18N
                        + "and not creatable\n\n" + e); // NOI18N
            }
        }
        return srcDir;
    }

    @Override
    public boolean isSourceObjectObservered()
    {
        return false;
    }

    @Override
    public void setSourceObjectObserved(final boolean observed)
    {
        // is not observed so do nothing
    }
}
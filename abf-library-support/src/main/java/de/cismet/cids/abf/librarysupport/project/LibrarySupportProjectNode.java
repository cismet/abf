/*
 * LibrarySupportProjectNode.java, encoding: UTF-8
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
 * Created on 10. Juli 2007, 17:30
 *
 */

package de.cismet.cids.abf.librarysupport.project;

import de.cismet.cids.abf.librarysupport.JarHandler;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.nodes.ExtManagement;
import de.cismet.cids.abf.librarysupport.project.nodes.IntManagement;
import de.cismet.cids.abf.librarysupport.project.nodes.LocalManagement;
import de.cismet.cids.abf.librarysupport.project.nodes.StarterManagement;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.DeployChangedJarsAction;
import de.cismet.cids.abf.librarysupport.project.nodes.actions.RefreshAction;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.RefreshCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.utilities.ModificationStore;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author mscholl
 * @version 1.10
 */
public final class LibrarySupportProjectNode extends AbstractNode implements
        RefreshCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            LibrarySupportProjectNode.class);

    private final transient LibrarySupportProject project;
    private final transient Image nodeImage;
    
    public LibrarySupportProjectNode(final LibrarySupportProject project)
    {
        this(project, new InstanceContent());
    }
    
    private LibrarySupportProjectNode(final LibrarySupportProject project, final
            InstanceContent content)
    {
        super(new LibrarySupportProjectNodeChildren(project), new 
                AbstractLookup(content));
        nodeImage = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                + "category_16.gif"); // NOI18N
        this.project = project;
        content.add(project);
        content.add((RefreshCookie)this);
        setName(project.getProjectDirectory().getParent().getName() + 
                NbBundle.getMessage(
                    this.getClass(), "LibrarySupportProjectNode.name")); // NOI18N
        getChildren().getNodes();
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }
    
    @Override
    public Image getIcon(final int i)
    {
        final PropertyProvider provider = PropertyProvider.getInstance(project.
                getProjectProperties());
        final String path = provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE);
        if(path == null || path.trim().equals("")) // NOI18N
        {
            final Image badge = ImageUtilities.loadImage(
                    "de/cismet/cids/abf/librarysupport/images/" // NOI18N
                    + "warningBadge.gif"); // NOI18N
            setShortDescription(java.util.ResourceBundle.getBundle(
                    "de/cismet/cids/abf/librarysupport/project/Bundle")// NOI18N
                    .getString("LibrarySupportProjectNode.getIcon(int).shortDescription.keystoreNotSet")); // NOI18N
            return ImageUtilities.mergeImages(nodeImage, badge, 3, 3);
        }
        final File ks = new File(path);
        if(!ks.exists() || !ks.isFile() || !ks.canRead())
        {
            final Image badge = ImageUtilities.loadImage(
                    "de/cismet/cids/abf/librarysupport/images/" // NOI18N
                    + "warningBadge.gif"); // NOI18N
            setShortDescription(java.util.ResourceBundle.getBundle(
                    "de/cismet/cids/abf/librarysupport/project/Bundle")// NOI18N
                    .getString("LibrarySupportProjectNode.getIcon(int).shortDescription.givenKeystoreUnusable")); // NOI18N
            return ImageUtilities.mergeImages(nodeImage, badge, 3, 3);
            
        }
        setShortDescription(FileUtil.toFile(project.getProjectDirectory()).
                getAbsolutePath());
        return nodeImage;
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        final ProjectCloseHookAction closeHook = new ProjectCloseHookAction(
                CommonProjectActions.closeProjectAction());
        return new Action[] 
        {
            closeHook, null,
            CommonProjectActions.setAsMainProjectAction(), 
            CallableSystemAction.get(RefreshAction.class), null,
            CommonProjectActions.customizeProjectAction()
        };
    }

    @Override
    public void refresh()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("refresh requested"); // NOI18N
        }
        ((LibrarySupportProjectNodeChildren)getChildren()).refreshAll();
    }
    
    public void firePropertiesChange()
    {
        fireIconChange();
    }
    
    private final class ProjectCloseHookAction implements Action
    {
        private final transient Action delegate;
        
        public ProjectCloseHookAction(final Action delegate)
        {
            this.delegate = delegate;
        }
        
        @Override
        public Object getValue(final String arg0)
        {
            return delegate.getValue(arg0);
        }

        @Override
        public void putValue(final String arg0, final Object arg1)
        {
            delegate.putValue(arg0, arg1);
        }

        @Override
        public void setEnabled(final boolean arg0)
        {
            delegate.setEnabled(arg0);
        }

        @Override
        public boolean isEnabled()
        {
            return delegate.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p)
        {
            delegate.addPropertyChangeListener(p);
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p)
        {
            delegate.removePropertyChangeListener(p);
        }

        @Override
        public void actionPerformed(final ActionEvent arg0)
        {
            if(ModificationStore.getInstance().anyModifiedInContext(FileUtil.
                    toFile(project.getProjectDirectory()).getAbsolutePath(), 
                    ModificationStore.MOD_CHANGED))
            {
                final int answer = JOptionPane.showConfirmDialog(
                        WindowManager.getDefault().getMainWindow(), 
                        java.util.ResourceBundle.getBundle("de/cismet" // NOI18N
                        + "/cids/abf/librarysupport/project/Bundle") // NOI18N
                        .getString("LibrarySupportProjectNode.closing().JOptionPane.confirmDialog.message"), // NOI18N
                        java.util.ResourceBundle.getBundle("de/cismet" // NOI18N
                        + "/cids/abf/librarysupport/project/Bundle") // NOI18N
                        .getString("LibrarySupportProjectNode.closing().JOptionPane.confirmDialog.title"), // NOI18N
                        JOptionPane.YES_NO_CANCEL_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
                if(answer == JOptionPane.CANCEL_OPTION)
                {
                    return;
                }else if(answer == JOptionPane.YES_OPTION)
                {
                    try
                    {
                        performDeploy();
                    }catch(final IOException ex)
                    {
                        // if this exception is caught simply return since a
                        // log message has already been shown and delegation to
                        // underlying close action shall not occur
                        return;
                    }
                }
            }
            // always delegate if we reach this part of code
            delegate.actionPerformed(arg0);
        }
        
        private void performDeploy() throws IOException
        {
            final List<DeployInformation> infos = new LinkedList<
                    DeployInformation>();
            final ModificationStore modStore = ModificationStore.getInstance();
            for(final Node node : getChildren().getNodes(true))
            {
                for(final Action a : node.getActions(false))
                {
                    // if this action is registered it node should be of type
                    // LocalManagement or StarterManagement
                    if(a instanceof DeployChangedJarsAction)
                    {
                        for(final Node ch : node.getChildren().getNodes())
                        {
                            final DeployInformation info = DeployInformation.
                                    getDeployInformation(ch);
                            if(modStore.anyModifiedInContext(FileUtil.toFile(
                                    info.getSourceDir()).getAbsolutePath(),
                                    ModificationStore.MOD_CHANGED))
                            {
                                infos.add(info);
                            }
                        }
                        // continue with outer loop since only one action of 
                        // this type should be registered
                        break;
                    }
                }
            }
            try
            {
                JarHandler.deployAllJars(infos, JarHandler.
                        ANT_TARGET_DEPLOY_CHANGED_JARS);
                
                for(final DeployInformation info : infos)
                {
                    modStore.removeAllModificationsInContext(FileUtil.toFile(
                            info.getSourceDir()).getAbsolutePath(),
                            ModificationStore.MOD_CHANGED);
                }
            } catch (final IOException ex)
            {
                LOG.warn("could not deploy changed jars", ex); // NOI18N
                throw ex;
            }
        }
    }
}

final class LibrarySupportProjectNodeChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            LibrarySupportProjectNodeChildren.class);
    
    private final transient LibrarySupportProject project;
    private final transient FileChangeListener fileL;
    
    public LibrarySupportProjectNodeChildren(final LibrarySupportProject 
            project)
    {
        this.project = project;
        this.fileL = new FileChangeListenerImpl();
        final FileObject projDir = project.getProjectDirectory();
        projDir.addFileChangeListener((FileChangeListener)WeakListeners.create(
                FileChangeListener.class, fileL, projDir));
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        if(!(object instanceof FileObject))
        {
            throw new IllegalArgumentException(
                    "object must be FileObject: " + object); // NOI18N
        }
        final FileObject fo = (FileObject)object;
        if(fo.getName().equals(LibrarySupportProject.EXT_DIR) ||
                fo.getName().equals(LibrarySupportProject.INT_DIR))
        {
            final DataFolder df = DataFolder.findFolder(fo);
            final DataFilter filter = new DataFilter() 
            {
                @Override
                public boolean acceptDataObject(final DataObject dO) 
                {
                    //CVS Folder
                    final FileObject fo = dO.getPrimaryFile();
                    if(fo.isFolder() 
                            && fo.getName().equalsIgnoreCase("cvs")) // NOI18N
                    {
                        return false;
                    }
                    if(fo.isFolder() ||
                            fo.getExt().equalsIgnoreCase("jar") || // NOI18N
                            fo.getExt().equalsIgnoreCase("zip") || // NOI18N
                            fo.getExt().equalsIgnoreCase("mf")  || // NOI18N
                            fo.getExt().equalsIgnoreCase("properties") // NOI18N
                            )
                    {
                        return true;
                    }
                    return false;
                }
            };
            final Children ch = df.createNodeChildren(filter);
            final Node node = new AbstractNode(ch);
            if(fo.getName().equals(LibrarySupportProject.EXT_DIR))
            {
                return new Node[] {new ExtManagement(project, node)};
            }else
            {
                return new Node[] {new IntManagement(project, node)};
            }
        }else if(fo.getName().toLowerCase().startsWith(
                LibrarySupportProject.STARTER_DIR))
        {
            try
            {
                return new Node[] {new StarterManagement(project, fo)};
            }catch(final IllegalArgumentException ex)
            {
                LOG.warn("could not create StarterManagement from dir: "//NOI18N
                        + fo, ex);
                return new Node[] {};
            }
        }else if(fo.getName().toLowerCase().startsWith(
                LibrarySupportProject.LOCAL_DIR))
        {
            try
            {
                return new Node[] {new LocalManagement(project, fo)};
            }catch(final IllegalArgumentException ex)
            {
                LOG.warn("could not create LocalManagement from dir: " // NOI18N
                        + fo, ex);
                return new Node[] {};
            }
        }
        return new Node[] {};
    }
    
    @Override
    protected void addNotify()
    {
        final FileObject projDir = project.getProjectDirectory();
        projDir.refresh();
        final FileObject extDir = projDir.getFileObject(
                LibrarySupportProject.EXT_DIR);
        final FileObject intDir = projDir.getFileObject(
                LibrarySupportProject.INT_DIR);
        final ArrayList<FileObject> fos = new ArrayList<FileObject>(10);
        if(extDir != null)
        {
            fos.add(extDir);
        }
        if(intDir != null)
        {
            fos.add(intDir);
        }
        fos.addAll(getStarterDirs());
        fos.addAll(getLocalDirs());
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
    
    private List<FileObject> getStarterDirs()
    {
        final FileObject projDir = project.getProjectDirectory();
        projDir.refresh();
        final ArrayList<FileObject> fos = new ArrayList<FileObject>(2);
        final ArrayList<FileObject> failed = new ArrayList<FileObject>(2);
        for(final Enumeration<? extends FileObject> e = projDir.getFolders(
                true); e.hasMoreElements();)
        {
            final FileObject fo = e.nextElement();
            if(fo.getName().toLowerCase().startsWith(
                    LibrarySupportProject.STARTER_DIR))
            {
                if(fo.getFileObject(StarterManagement.SRC_DIR) != null)
                {
                    fos.add(fo);
                }else
                {
                    failed.add(fo);
                }
            }
        }
        if(!failed.isEmpty())
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    final StringBuffer list = new StringBuffer(
                            "<ul type = \"circle\">"); // NOI18N
                    for(final FileObject f : failed)
                    {
                        list.append("<li>") // NOI18N
                                .append(f.getName()).append("</li>"); // NOI18N
                    }
                    list.append("</ul>"); // NOI18N
                    final int create = JOptionPane.showConfirmDialog(
                            WindowManager.getDefault().getMainWindow(),
                            NbBundle.getMessage(this.getClass(),
                                "LibrarySupportProjectNode.getStarterDirs().JOptionPane.confirmDialog.message", // NOI18N
                                list.toString()),
                            NbBundle.getMessage(this.getClass(),
                                "LibrarySupportProjectNode.getStarterDirs().JOptionPane.confirmDialog.title"), // NOI18N
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if(create == JOptionPane.YES_OPTION)
                    {
                        createStructure(failed, false);
                    }
                }
            });
        }
        Collections.sort(fos, new Comparator<FileObject>()
        {
            @Override
            public int compare(final FileObject f1, final FileObject f2)
            {
                return f1.getName().compareTo(f2.getName());
            }
        });
        return fos;
    }
    
    private List<FileObject> getLocalDirs()
    {
        final FileObject projDir = project.getProjectDirectory();
        projDir.refresh();
        final ArrayList<FileObject> fos = new ArrayList<FileObject>(2);
        final ArrayList<FileObject> failed = new ArrayList<FileObject>(2);
        for(final Enumeration<? extends FileObject> e = projDir.getFolders(
                true); e.hasMoreElements();)
        {
            final FileObject fo = e.nextElement();
            if(fo.getName().toLowerCase().startsWith(
                    LibrarySupportProject.LOCAL_DIR))
            {
                if(fo.getFileObject(LocalManagement.SRC_DIR) != null)
                {
                    fos.add(fo);
                }else
                {
                    failed.add(fo);
                }
            }
        }
        if(!failed.isEmpty())
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    final StringBuffer list = new StringBuffer(
                            "<ul type = \"circle\">"); // NOI18N
                    for(final FileObject f : failed)
                    {
                        list.append("<li>") // NOI18N
                                .append(f.getName()).append("</li>"); // NOI18N
                    }
                    list.append("</ul>"); // NOI18N
                    final int create = JOptionPane.showConfirmDialog(
                            WindowManager.getDefault().getMainWindow(),
                            NbBundle.getMessage(this.getClass(),
                                "LibrarySupportProjectNode.getLocalDirs().JOptionPane.confirmDialog.message", // NOI18N
                                list.toString()),
                            NbBundle.getMessage(this.getClass(),
                                "LibrarySupportProjectNode.getLocalDirs().JOptionPane.confirmDialog.title"), // NOI18N
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if(create == JOptionPane.YES_OPTION)
                    {
                        createStructure(failed, true);
                    }
                }
            });
        }
        Collections.sort(fos, new Comparator<FileObject>()
        {
            @Override
            public int compare(final FileObject f1, final FileObject f2)
            {
                return f1.getName().compareTo(f2.getName());
            }
        });
        return fos;
    }
    
    private void createStructure(final List<FileObject> fos, final boolean lcl)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("trying to create structure from archives"); // NOI18N
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(this.getClass(),
                "LibrarySupportProjectNode.createStructure(List<FileObject>,boolean).handle.message")); // NOI18N
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                handle.start();
                handle.switchToIndeterminate();
            }
        });
        final Thread runner = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final ArrayList<Thread> threads = new ArrayList<Thread>(fos.
                        size());
                for(final FileObject dir : fos)
                {
                    threads.add(new Thread(new StructureCreator(dir, lcl)));
                }
                for(final Thread t : threads)
                {
                    t.start();
                }
                for(final Thread t : threads)
                {
                    try
                    {
                        t.join();
                    } catch (InterruptedException ex)
                    {
                        LOG.error("could not join thread", ex); // NOI18N
                    }
                }
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handle.finish();
                        refreshAll();
                    }
                });
            }
        });
        runner.start();
    }
    
    private final class StructureCreator implements Runnable
    {
        private static final String TARGET_NAME = "extractAll"; // NOI18N
        
        private final transient FileObject dir;
        private final transient boolean local;
        
        StructureCreator(final FileObject dir, final boolean local)
        {
            this.dir = dir;
            this.local = local;
        }
        
        @Override
        public void run()
        {
            final File srcDir = new File(FileUtil.toFile(dir), local ? 
                LocalManagement.SRC_DIR : StarterManagement.SRC_DIR);
            if(!srcDir.mkdirs())
            {
                LOG.error("could not create source dirs"); // NOI18N
                return;
            }
            final Document buildDoc = XMLUtil.createDocument(
                    "project", null, null, null); // NOI18N
            final Element targetE = buildDoc.createElement("target"); // NOI18N
            targetE.setAttribute("name", TARGET_NAME); // NOI18N
            buildDoc.getDocumentElement().appendChild(targetE);
            final FileObject srcFO = FileUtil.toFileObject(srcDir);
            for(final Enumeration<? extends FileObject> e = dir.getData(
                    false); e.hasMoreElements();)
            {
                final FileObject toExtract = e.nextElement();
                if(toExtract.getExt().equalsIgnoreCase("jar") || // NOI18N
                        toExtract.getExt().equalsIgnoreCase("zip")) // NOI18N
                {
                    final FileObject target;
                    try
                    {
                        target = srcFO.createFolder(toExtract.getName());
                    } catch (final IOException ex)
                    {
                        LOG.error("could not create folder", ex); // NOI18N
                        return;
                    }
                    final Element extract = buildDoc
                            .createElement("unjar"); // NOI18N
                    extract.setAttribute("src", // NOI18N
                            FileUtil.toFile(toExtract).getAbsolutePath());
                    extract.setAttribute("dest", // NOI18N
                            FileUtil.toFile(target).getAbsolutePath());
                    targetE.appendChild(extract);
                }
            }
            final File outFile = new File(FileUtil.toFile(dir), dir.
                    getName() + "_build.xml"); // NOI18N
            if(outFile.exists() && !outFile.delete())
            {
                LOG.error("outfile could not be deleted: " + outFile); // NOI18N
                return;
            }
            BufferedOutputStream bos = null;
            try
            {
                bos = new BufferedOutputStream(new FileOutputStream(outFile));
                XMLUtil.write(buildDoc, bos, "UTF-8"); // NOI18N
            } catch (final IOException ex)
            {
                LOG.error("could not write tmp build file", ex); // NOI18N
                return;
            } finally
            {
                try
                {
                    bos.close();
                } catch (final IOException ex)
                {
                    LOG.warn("could not close outputstream", ex); // NOI18N
                }
            }
            try
            {
                final ExecutorTask task = ActionUtils.runTarget(FileUtil.
                        toFileObject(outFile), new String[]{TARGET_NAME}, null);
                task.waitFinished();
                if(task.result() != 0)
                {
                    LOG.error("extract failed"); // NOI18N
                }
            } catch (final Exception ex)
            {
                LOG.error("could not execute ant target", ex); // NOI18N
            }
            if(outFile.exists() && !outFile.delete())
            {
                LOG.error("outfile could not be deleted: " // NOI18N
                        + outFile.getAbsolutePath() + File.separator
                        + outFile.getName());
            }
            if(local)
            {
                cleanDir(dir);
            }else
            {
                if(!moveAndCleanDir(dir))
                {
                    LOG.error("could not find manifest and clean dir");// NOI18N
                    return;
                }
            }
            ModificationStore.getInstance().removeAllModificationsInContext(
                    FileUtil.toFile(dir).getAbsolutePath(), 
                    ModificationStore.MOD_CHANGED);
        }
        
        public void cleanDir(final FileObject extractDir)
        {
            extractDir.refresh();
            for(final Enumeration<? extends FileObject> e = extractDir.
                    getFolders(true); e.hasMoreElements();)
            {
                final FileObject fo = e.nextElement();
                if(fo.getName().equalsIgnoreCase("meta-inf") || // NOI18N
                        fo.getName().equalsIgnoreCase("cvs")) // NOI18N
                {
                    try
                    {
                        fo.delete();
                    } catch (final IOException ex)
                    {
                        LOG.error("could not delete folder: " // NOI18N
                                + FileUtil.toFile(fo).getAbsolutePath(), ex);
                    }
                }
            }
        }
        
        public boolean moveAndCleanDir(final FileObject extractDir)
        {
            extractDir.refresh();
            final FileObject plain = extractDir.getFileObject(StarterManagement.
                    SRC_DIR);
            if(plain == null)
            {
                LOG.warn("could not retrieve src dir"); // NOI18N
                return false;
            }
            for(final Enumeration<? extends FileObject> e = plain.getFolders(
                    false); e.hasMoreElements();)
            {
                final FileObject fo = e.nextElement();
                final FileObject manifest = fo.getFileObject(
                        "META-INF/MANIFEST.MF"); // NOI18N
                if(manifest == null)
                {
                    LOG.warn("no manifest included in jar: " // NOI18N
                            + fo.getName());
                    try
                    {
                        fo.getParent().createData(fo.getName(), "mf"); // NOI18N
                    }catch(final IOException ex)
                    {
                        LOG.error("could not create empty manifest", // NOI18N
                                ex);
                        ErrorManager.getDefault().annotate(ex,
                                MessageFormat.format(java.util.ResourceBundle
                                .getBundle("de/cismet/cids/abf/" // NOI18N
                                + "librarysupport/project/Bundle") // NOI18N
                                .getString("LibrarySupportProjectNode.moveAndCleanDir(FileObject).emptyManifestError"), fo // NOI18N
                                .getName()));
                    }
                }else
                {
                    FileLock lock = null;
                    try
                    {
                        lock = manifest.lock();
                        manifest.move(lock, fo.getParent(), fo.getName(), 
                                "mf"); // NOI18N
                    }catch(final IOException ex)
                    {
                        LOG.error("could not move manifest", ex); // NOI18N
                        return false;
                    }finally
                    {
                        if(lock != null && lock.isValid())
                        {
                            lock.releaseLock();
                        }
                    }
                    for(final Enumeration<? extends FileObject> e2 = fo.
                            getFolders(true); e2.hasMoreElements();)
                    {
                        final FileObject fo2 = e2.nextElement();
                        try
                        {
                            fo2.delete();
                        } catch (final IOException ex)
                        {
                            LOG.warn("could not delete folder: " // NOI18N
                                    + FileUtil.toFile(fo2).getAbsolutePath(),
                                    ex);
                        }
                    }
                }
            }
            return true;
        }
    }
    
    private final class FileChangeListenerImpl implements FileChangeListener
    {
        @Override
        public void fileFolderCreated(final FileEvent fe)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("received fileevent: " + fe); // NOI18N
            }
            addNotify();
        }

        @Override
        public void fileDataCreated(final FileEvent fe)
        {
            // do nothing
        }

        @Override
        public void fileChanged(final FileEvent fe)
        {
            // do nothing
        }

        @Override
        public void fileDeleted(final FileEvent fe)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("received fileevent: " + fe); // NOI18N
            }
            addNotify();
        }

        @Override
        public void fileRenamed(final FileRenameEvent fe)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("received fileevent: " + fe); // NOI18N
            }
            // maybe someone renamed an important folder
            addNotify();
        }

        @Override
        public void fileAttributeChanged(final FileAttributeEvent fe)
        {
            // do nothing
        }
    }
}
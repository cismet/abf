/*
 * RebuildFromJarAction.java, encoding: UTF-8
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
 * Created on ???
 *
 */
package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.LocalManagement;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.utilities.files.FileUtils;
import de.cismet.cids.abf.utilities.ModificationStore;
import java.awt.EventQueue;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

public final class RebuildFromJarAction extends CookieAction implements
        Cancellable
{
    private static final transient Logger LOG = Logger.getLogger(
            RebuildFromJarAction.class);

    public static final String BACKUP_DIR_NAME = ".backup"; // NOI18N

    private final transient ThreadLocal<LinkedList<Node>> errorNodes =
            new ThreadLocal<LinkedList<Node>>();

    private transient ProgressHandle handle;

    private volatile transient int threadCount     = 0;
    private volatile transient int handleSteps     = 0;
    private volatile transient int handleStepsDone = 0;

    private transient boolean cancelAction;

    @Override
    protected int mode()
    {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(RebuildFromJarAction.class, 
                "RebuildFromJarAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            SourceContextCookie.class
        };
    }

    @Override
    protected String iconResource()
    {
        return LibrarySupportProject.IMAGE_FOLDER + "jar_reload_24.png";//NOI18N
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return true;
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(!super.enable(nodes))
        {
            return false;
        }
        boolean local = false;
        boolean jar = false;
        for(final Node node : nodes)
        {
            if(node.getCookie(LocalManagementContextCookie.class) == null)
            {
                jar = true;
            }else
            {
                local = true;
            }
        }
        if(!(local ^ jar))
        {
            return false;
        }
        for(final Node node : nodes)
        {
            if(jar)
            {
                if(!jarAvailable(node))
                {
                    return false;
                }
            }else
            {
                for(final Node n : node.getChildren().getNodes())
                {
                    if(!jarAvailable(n))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean jarAvailable(final Node n)
    {
        try
        {
            return getJar(getJarDir(n), getSourceDir(n).getName()) != null;
        }catch(final FileNotFoundException ex)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("jar is not available for node: " + n, ex); // NOI18N
            }
            return false;
        }catch(final IllegalStateException ex)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("jar is not available for node: " + n, ex); // NOI18N
            }
            return false;
        }
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        class AnswerRunner implements Runnable
        {
            private transient int answer = JOptionPane.CANCEL_OPTION;

            @Override
            public void run()
            {
                answer = JOptionPane.showConfirmDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                RebuildFromJarAction.class,
                                "RebuildFromJarAction.performAction(Node[]).run().JOptionPane.confirmDialog.message", // NOI18N
                                BACKUP_DIR_NAME),
                            org.openide.util.NbBundle.getMessage(
                            RebuildFromJarAction.class, 
                                "RebuildFromJarAction.performAction(Node[]).run().JOptionPane.confirmDialog.title"), // NOI18N
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
            }

            public int getAnswer()
            {
                return answer;
            }
        }
        final AnswerRunner ar = new AnswerRunner();
        try
        {
            EventQueue.invokeAndWait(ar);
        }catch(final InterruptedException e)
        {
            LOG.warn("Optionpane was interrupted exiting action", e); // NOI18N
            return;
        }catch(final InvocationTargetException e)
        {
            LOG.warn("Optionpane caused exception, exiting aciton", e);// NOI18N
            return;
        }
        // don't do anything if cancel was chosen
        if(JOptionPane.CANCEL_OPTION == ar.getAnswer())
        {
            return;
        }
        init();
        if(nodes[0].getCookie(LocalManagementContextCookie.class) == null)
        {
            rebuildFromJar(nodes);
        }else
        {
            rebuildFromLocal(nodes);
        }
        if(errorNodes.get().size() > 0)
        {
            displayErrors(errorNodes.get());
        }
        cleanup();
    }

    private void init()
    {
        errorNodes.set(new LinkedList<Node>());
        if(++threadCount == 1)
        {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(RebuildFromJarAction.
                    class, "RebuildFromJarAction.handle.message"), (Cancellable)this); // NOI18N
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    handle.start();
                }
            });
            handleSteps = 0;
            handleStepsDone = 0;
            cancelAction = false;
        }
    }

    private void cleanup()
    {
        errorNodes.set(null);
        if(--threadCount == 0)
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    handle.finish();
                }
            });
        }
    }

    private void adjustHandle(final int addSteps)
    {
        handleSteps += addSteps;
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                handle.switchToDeterminate(handleSteps);
            }
        });
    }

    private void progressHandle()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                handle.progress(++handleStepsDone);
            }
        });
    }

    private void rebuildFromLocal(final Node[] nodes)
    {
        for(final Node node : nodes)
        {
            final LocalManagement local = node
                    .getCookie(LocalManagementContextCookie.class)
                    .getLocalManagementContext();
            // cache the child nodes as they will be lost
            final Node[] children = local.getChildren().getNodes(true);
            detach(node);
            rebuildFromJar(children);
            attach(node);
            cleanModifications(node);
        }
    }

    private void rebuildFromJar(final Node[] nodes)
    {
        adjustHandle(nodes.length);
        final FileFilter filter = new FileUtils.MetaInfFilter();
        for(final Node node : nodes)
        {
            final InputOutput ioTab = IOProvider.getDefault().getIO(
                    org.openide.util.NbBundle.getMessage(RebuildFromJarAction
                    .class, "RebuildFromJarAction.rebuildFromJar(Node[]).iotab.message") + node.getDisplayName(), // NOI18N
                    false);
            final OutputWriter out = ioTab.getOut();
            final OutputWriter err = ioTab.getErr();
            try
            {
                out.reset();
                err.reset();
            }catch(final IOException iOException)
            {
                LOG.warn("could not reset ioTab", iOException); // NOI18N
            }
            ioTab.select();
            try
            {
                final long begin = System.currentTimeMillis();
                detach(node);
                if(cancelAction)
                {
                    return;
                }
                final FileObject sourceDir = getSourceDir(node);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.workFolder") // NOI18N
                        + sourceDir.getPath());
                if(cancelAction)
                {
                    return;
                }
                final FileObject jarDir = getJarDir(node);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.sourceFolder")// NOI18N
                        + jarDir.getPath());
                if(cancelAction)
                {
                    return;
                }
                final FileObject backupDir = getBackupDir(sourceDir);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.backupFolder")// NOI18N
                        + backupDir.getPath());
                if(cancelAction)
                {
                    return;
                }
                final String jarName = sourceDir.getName();
                final FileObject jar = getJar(jarDir, jarName);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.sourceArchive")//NOI18N
                        + jar.getPath());
                out.println();
                if(cancelAction)
                {
                    return;
                }
                out.print(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.doBackup")); // NOI18N
                doBackup(backupDir, sourceDir);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.finished")); // NOI18N
                if(cancelAction)
                {
                    return;
                }
                out.print(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, 
                        "RebuildFromJarAction.rebuildFromJar(Node[]).out.emptyWorkDir")); // NOI18N
                FileUtils.deleteContent(sourceDir, true);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.finished")); // NOI18N
                if(cancelAction)
                {
                    return;
                }
                out.print(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, 
                        "RebuildFromJarAction.rebuildFromJar(Node[]).out.extractSourceToWorkDir")); // NOI18N
                FileUtils.extractJar(jar, sourceDir, filter);
                out.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, "RebuildFromJarAction.rebuildFromJar(Node[]).out.finished")); // NOI18N
                out.println();
                printSuccess(out, (System.currentTimeMillis() - begin) / 1000);
                cleanModifications(node);
            }catch(final Exception e)
            {
                LOG.error("could rebuild jar for node: " + node, e); // NOI18N
                err.println();
                err.println();
                err.println(org.openide.util.NbBundle.getMessage(
                        RebuildFromJarAction.class, 
                        "RebuildFromJarAction.rebuildFromJar(Node[]).out.errorWhileRebuild")); // NOI18N
                e.printStackTrace(err);
                errorNodes.get().add(node);
            }finally
            {
                attach(node);
            }
            progressHandle();
        }
    }

    private void printSuccess(final PrintWriter out, final long durationSec)
    {
        final String rebuildSuccess = org.openide.util.NbBundle.getMessage(
                RebuildFromJarAction.class, "RebuildFromJarAction.printSuccess(PrintWriter,long).rebuildSuccess"); // NOI18N
        final String duration = org.openide.util.NbBundle.getMessage(
                RebuildFromJarAction.class, "RebuildFromJarAction.printSuccess(PrintWriter,long).duration"); // NOI18N
        final String seconds = org.openide.util.NbBundle.getMessage(
                RebuildFromJarAction.class, "RebuildFromJarAction.printSuccess(PrintWriter,long).seconds"); // NOI18N
        final String secValue = String.valueOf(durationSec);
        final StringBuffer secondLine = new StringBuffer(
                rebuildSuccess.length() + 4);
        final StringBuffer thirdLine = new StringBuffer(
                duration.length() + secValue.length() + seconds.length() + 7);
        secondLine.append("| ").append(rebuildSuccess).append(" |"); // NOI18N
        thirdLine.append("| ").append(duration).append(": ") // NOI18N
                .append(secValue).append(' ') // NOI18N
                .append(seconds).append(" |"); // NOI18N
        while(secondLine.length() < thirdLine.length())
        {
            secondLine.insert(1, ' ').insert(secondLine.length() - 2, ' ');
        }
        while(secondLine.length() > thirdLine.length())
        {
            thirdLine.insert(duration.length() + 4, ' ');
        }
        final StringBuffer firstLine = new StringBuffer(secondLine.length());
        final StringBuffer fourthLine = new StringBuffer(secondLine.length());
        firstLine.append("/=\\"); // NOI18N
        fourthLine.append("\\=/"); // NOI18N
        while(firstLine.length() < secondLine.length())
        {
            firstLine.insert(1, '='); // NOI18N
            fourthLine.insert(1, '='); // NOI18N
        }
        out.println(firstLine);
        out.println(secondLine);
        out.println(thirdLine);
        out.println(fourthLine);
    }

    private void detach(final Node node)
    {
        final SourceContextCookie src = node
                .getCookie(SourceContextCookie.class);
        if(src != null && src.isSourceObjectObservered())
        {
            src.setSourceObjectObserved(false);
        }
    }

    private void attach(final Node node)
    {
        final SourceContextCookie src = node
                .getCookie(SourceContextCookie.class);
        if(src != null && src.isSourceObjectObservered())
        {
            src.setSourceObjectObserved(true);
        }
    }

    private void cleanModifications(final Node node)
    {
        try
        {
            final FileObject src = node.getCookie(SourceContextCookie.class)
                    .getSourceObject();
            ModificationStore.getInstance().removeAllModificationsInContext(
                    FileUtil.toFile(src).getAbsolutePath(),
                    ModificationStore.MOD_CHANGED);
        }catch(final FileNotFoundException e)
        {
            LOG.warn("could not remove modifications in context", e); // NOI18N
        }
    }

    private void displayErrors(final Collection<Node> nodes)
    {
        final StringBuffer htmlList = new StringBuffer(25);
        htmlList.append("<ul>"); // NOI18N
        for(final Node node : nodes)
        {
            htmlList.append("<li>") // NOI18N
                    .append(node.getDisplayName())
                    .append("</li>"); // NOI18N
        }
        htmlList.append("</ul>"); // NOI18N
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JOptionPane.showMessageDialog(
                        WindowManager.getDefault().getMainWindow(),
                        org.openide.util.NbBundle.getMessage(
                            RebuildFromJarAction.class, 
                            "RebuildFromJarAction.displayErrors(Collection<Node>).JoptionPane.messageDialog.message", htmlList.toString()),//NOI18N
                        org.openide.util.NbBundle.getMessage(
                            RebuildFromJarAction.class, "RebuildFromJarAction.displayErrors(Collection<Node>).JoptionPane.messageDialog.title"),// NOI18N
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void doBackup(final FileObject backupDir, final FileObject
            sourceDir)
            throws
            IOException
    {
        FileUtils.copyContent(sourceDir, backupDir, null, true);
    }

    private FileObject getBackupDir(final FileObject sourceDir) throws
            IOException
    {
        FileObject mainBackupDir = sourceDir.getParent()
                .getFileObject(BACKUP_DIR_NAME);
        if(mainBackupDir == null)
        {
            mainBackupDir = sourceDir.getParent().createFolder(BACKUP_DIR_NAME);
            ModificationStore.getInstance().removeAllModificationsInContext(
                    FileUtil.toFile(mainBackupDir).getAbsolutePath(),
                    ModificationStore.MOD_CHANGED);
        }
        FileObject backupDir = mainBackupDir.getFileObject(sourceDir.getName());
        if(backupDir == null)
        {
            backupDir = mainBackupDir.createFolder(sourceDir.getName());
        }else
        {
            FileUtils.deleteContent(FileUtil.toFile(backupDir), true);
        }
        return backupDir;
    }

    private FileObject getSourceDir(final Node node) throws
            FileNotFoundException,
            IllegalStateException
    {
        final FileObject sourceDir = node.getCookie(SourceContextCookie.class)
                .getSourceObject();
        if(!sourceDir.canRead())
        {
            throw new IllegalStateException(
                    "cannot read source dir: " + sourceDir.getPath()); // NOI18N
        }else if(!sourceDir.canWrite())
        {
            throw new IllegalStateException(
                    "cannot write to source dir: " // NOI18N
                    + sourceDir.getPath());
        }
        return sourceDir;
    }

    private FileObject getJarDir(final Node node)
    {
        final LocalManagementContextCookie lmcc = node.getParentNode()
                        .getCookie(LocalManagementContextCookie.class);
        if(lmcc == null)
        {
            // startermanagement not implemented yet
            return null;
//            final StarterManagementContextCookie smcc = node.getParentNode()
//                    .getCookie(StarterManagementContextCookie.class);
//            if(smcc == null)
//                return null;
//            return smcc.getStarterManagementContext().getStarterObject();
        }else
        {
            return lmcc.getLocalManagementContext().getLocalObject();
        }
    }

    private FileObject getJar(final FileObject jarDir, final String jarName)
            throws
            FileNotFoundException,
            IllegalStateException
    {
        if(jarDir == null)
        {
            throw new FileNotFoundException(
                    "cannot find jar with name '" // NOI18N
                    + jarName
                    + "' if jarDir is 'null'!"); // NOI18N
        }else if(jarName == null)
        {
            throw new FileNotFoundException(
                    "cannot find jar with name '" // NOI18N
                    + jarName
                    + "' in folder '" // NOI18N
                    + jarDir.getPath()
                    + "'!"); // NOI18N
        }
        final FileObject jar = jarDir.getFileObject(jarName, "jar"); // NOI18N
        if(jar == null)
        {
            throw new FileNotFoundException(
                    "cannot find jar with name '" // NOI18N
                    + jarName
                    + "' in folder '" // NOI18N
                    + jarDir.getPath()
                    + "'!"); // NOI18N
        }else if(!jar.canRead())
        {
            throw new IllegalStateException(
                    "cannot read jar with name '" // NOI18N
                    + jarName
                    + "' in folder '" // NOI18N
                    + jarDir.getPath()
                    + "'!"); // NOI18N
        }
        return jar;
    }

    @Override
    public boolean cancel()
    {
        cancelAction = true;
        return cancelAction;
    }
}
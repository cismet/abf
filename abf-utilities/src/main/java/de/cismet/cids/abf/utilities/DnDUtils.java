/*
 * DnDUtils.java, encoding: UTF-8
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
 * Created on 22.10.2009, 14:39:32
 *
 */

package de.cismet.cids.abf.utilities;

import de.cismet.cids.abf.utilities.files.FileUtils;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin Scholl
 */
public class DnDUtils
{
    private static final transient Logger LOG = Logger.getLogger(
            DnDUtils.class);

    public static final DataFlavor URI_LIST_FLAVOR;

    static
    {
        URI_LIST_FLAVOR = new DataFlavor(
                "text/uri-list;class=java.lang.String", // NOI18N
                "URI List DataFlavor (String)"); // NOI18N
    }

    private DnDUtils() {}

    public static List<File> getFileListFromURIList(final Transferable t)
    {
        if(!t.isDataFlavorSupported(URI_LIST_FLAVOR))
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("uri list flavor not supported by " // NOI18N
                        + "this transferable"); // NOI18N
            }
            return null;
        }
        try
        {
            final String data = (String)t.getTransferData(URI_LIST_FLAVOR);
            if(data == null)
            {
                throw new IOException("received null as transfer data");//NOI18N
            }
            final String[] lines = data.split("\\r\\n"); // NOI18N
            final ArrayList<File> list = new ArrayList<File>(lines.length);
            for(final String uri : lines)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("line: " + uri); // NOI18N
                }
                if(uri.startsWith("#")) // NOI18N
                {
                    // it is just a comment
                    continue;
                }
                list.add(new File(new URI(uri)));
            }
            return list;
        }catch(final URISyntaxException ex)
        {
            LOG.error("invalid URI", ex); // NOI18N
        }catch(final IllegalArgumentException ex)
        {
            LOG.error("invalid file URI", ex); // NOI18N
        }catch(final UnsupportedFlavorException ex)
        {
            LOG.warn("uri list flavor not supported despite the " // NOI18N
                    + "transferable tells something else", ex); // NOI18N
        }catch(final IOException ex)
        {
            LOG.warn("could not retrieve data from transferable", ex); // NOI18N
        }
        return null;
    }

    public static List<File> getFileList(final Transferable t) throws
            IOException
    {
        try
        {
            final List<File> fileList;
            if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                final List list = (List)t.getTransferData(
                        DataFlavor.javaFileListFlavor);
                final ArrayList<File> files = new ArrayList<File>(list.size());
                for(final Object o : list)
                {
                    files.add((File)o);
                }
                fileList = files;
            }else if(t.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR))
            {
                fileList = DnDUtils.getFileListFromURIList(t);
            }else
            {
                throw new IOException("cannot receive transferable " // NOI18N
                        + "data due to unknown data flavor"); // NOI18N
            }
            return fileList;
        }catch(final UnsupportedFlavorException e)
        {
            LOG.error("could not get filelist", e); // NOI18N
            throw new IOException("unsupported flavor despite " // NOI18N
                    + "flavor support", e); // NOI18N
        }catch(final IOException e)
        {
            LOG.error("could not get filelist", e); // NOI18N
            throw e;
        }
    }

    public static File checkFile(final File check, final File folder,
            final int action) throws
            IOException
    {
        if(!check.exists())
        {
            throw new IOException("file does not exist: " + check); // NOI18N
        }else if(!check.isFile())
        {
            throw new IOException("only files are accepted: " + check);// NOI18N
        }else if(!check.canRead())
        {
            throw new IOException("file cannot be read: " + check); // NOI18N
        }else if((NodeTransfer.MOVE & action) != 0 && !check.canWrite())
        {
            throw new IOException("file cannot be written: " + check); // NOI18N
        }else if(!folder.canWrite())
        {
            throw new IOException("cannot write to package folder: " // NOI18N
                    + folder);
        }
        return check;
    }

    public static boolean acceptFiles(final List files, final File folder,
            final int action)
    {
        boolean accept = true;
        for(final Object file : files)
        {
            try
            {
                DnDUtils.checkFile((File)file, folder, action);
            }catch(final IOException e)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("checkFile complains, not accepting " // NOI18N
                            + "drop action", e); // NOI18N
                }
                accept = false;
                break;
            }
        }
        return accept;
    }

    public static final class UnifiedFilePasteType extends PasteType
    {
        private final transient Transferable t;
        private final transient FileObject targetFolder;

        public UnifiedFilePasteType(final Transferable t,
                final FileObject targetFolder)
        {
            this.t = t;
            this.targetFolder = targetFolder;
        }

        @Override
        public Transferable paste() throws IOException
        {
            try
            {
                if(LOG.isInfoEnabled())
                {
                    LOG.info("action unknown, default to copy action");// NOI18N
                }
                final List<File> fileList = DnDUtils.getFileList(t);
                final int action;
                // default to copy mode if action not determinable
                final Node[] nodes = NodeTransfer.nodes(t, NodeTransfer.MOVE);
                if(nodes == null)
                {
                    action = NodeTransfer.COPY;
                }else
                {
                    action = NodeTransfer.MOVE;
                }
                final File dir = FileUtil.toFile(targetFolder);
                for(final File f : fileList)
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("received file: " + f); // NOI18N
                    }
                    checkFile(f, dir, action);
                }
                for(final File f : fileList)
                {
                    File outFile = new File(dir, f.getName());
                    String name = FileUtils.getName(f);
                    int i = 0;
                    while(outFile.exists())
                    {
                        name = FileUtils.getName(f) + "_" + ++i; // NOI18N
                        outFile = new File(dir, name + "." // NOI18N
                                + FileUtils.getExt(outFile));
                    }
                    // we have to copy all files because move causes deadlock
                    // the funny thing is, that dnd action does not have that
                    // issue
                    FileUtil.copyFile(
                            FileUtil.toFileObject(f),
                            targetFolder,
                            name);
                }
                // means that it is move action but we cannot simply move
                // the fileobject because that causes a deadlock in the
                // masterfilesystem
                // as all the objects have already been copied we'll destroy the
                // nodes now
                if(nodes != null)
                {
                      for(final Node node : nodes)
                      {
                          node.destroy();
                      }
                }
            }catch(final IOException e)
            {
                LOG.error("could not paste files", e); // NOI18N
                throw e;
            }
            return null;
        }
    }

    public static final class UnifiedDnDFilePasteType extends PasteType
    {
        private final transient Transferable t;
        private final transient int action;
        private final transient FileObject targetFolder;

        public UnifiedDnDFilePasteType(final Transferable t, final int action,
                final FileObject targetFolder)
        {
            this.t = t;
            this.action = action;
            this.targetFolder = targetFolder;
        }

        @Override
        public Transferable paste() throws IOException
        {
            try
            {
                if(LOG.isDebugEnabled())
                {
                    whichAction();
                }
                final List<File> fileList = DnDUtils.getFileList(t);
                final File dir = FileUtil.toFile(targetFolder);
                for(final File f : fileList)
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("received file: " + f); // NOI18N
                    }
                    checkFile(f, dir, action);
                }
                for(final File f : fileList)
                {
                    File outFile = new File(dir, f.getName());
                    String name = FileUtils.getName(f);
                    int i = 0;
                    while(outFile.exists())
                    {
                        name = FileUtils.getName(f) + "_" + ++i; // NOI18N
                        outFile = new File(dir, name + "." // NOI18N
                                + FileUtils.getExt(outFile));
                    }
                    if((NodeTransfer.COPY & action) != 0)
                    {
                        FileUtil.copyFile(
                                FileUtil.toFileObject(f),
                                targetFolder,
                                name);
                    }else if((NodeTransfer.MOVE & action) != 0)
                    {
                        FileUtil.moveFile(
                                FileUtil.toFileObject(f),
                                targetFolder,
                                name);
                    }else
                    {
                        LOG.warn("unsupported action: " + action); // NOI18N
                    }
                }
            }catch(final IOException e)
            {
                LOG.error("could not paste files", e); // NOI18N
                throw e;
            }
            return null;
        }

        private void whichAction()
        {
            LOG.debug("move: " + (DnDConstants.ACTION_MOVE == action));// NOI18N
            LOG.debug("copy: " + (DnDConstants.ACTION_COPY == action));// NOI18N
            LOG.debug("link: " + (DnDConstants.ACTION_LINK == action));// NOI18N
            LOG.debug("copy or move: " // NOI18N
                    + (DnDConstants.ACTION_COPY_OR_MOVE == action));
        }
    }
}

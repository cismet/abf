/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;

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

import de.cismet.cids.abf.utilities.files.FileUtils;

/**
 * DOCUMENT ME!
 *
 * @author   Martin Scholl
 * @version  $Revision$, $Date$
 */
public class DnDUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DnDUtils.class);

    public static final DataFlavor URI_LIST_FLAVOR;

    static {
        URI_LIST_FLAVOR = new DataFlavor("text/uri-list;class=java.lang.String", "URI List DataFlavor (String)"); // NOI18N
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DnDUtils object.
     */
    private DnDUtils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   t  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<File> getFileListFromURIList(final Transferable t) {
        if (!t.isDataFlavorSupported(URI_LIST_FLAVOR)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("uri list flavor not supported by this transferable"); // NOI18N
            }

            return null;
        }

        try {
            final String data = (String)t.getTransferData(URI_LIST_FLAVOR);
            if (data == null) {
                throw new IOException("received null as transfer data"); // NOI18N
            }
            final String[] lines = data.split("\\r\\n");                 // NOI18N
            final ArrayList<File> list = new ArrayList<File>(lines.length);
            for (final String uri : lines) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("line: " + uri);                           // NOI18N
                }
                if (uri.startsWith("#"))                                 // NOI18N
                {
                    // it is just a comment
                    continue;
                }
                list.add(new File(new URI(uri)));
            }

            return list;
        } catch (final URISyntaxException ex) {
            LOG.error("invalid URI", ex);                                                                // NOI18N
        } catch (final IllegalArgumentException ex) {
            LOG.error("invalid file URI", ex);                                                           // NOI18N
        } catch (final UnsupportedFlavorException ex) {
            LOG.warn("uri list flavor not supported despite the transferable tells something else", ex); // NOI18N
        } catch (final IOException ex) {
            LOG.warn("could not retrieve data from transferable", ex);                                   // NOI18N
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   t  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static List<File> getFileList(final Transferable t) throws IOException {
        try {
            final List<File> fileList;
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                final List list = (List)t.getTransferData(
                        DataFlavor.javaFileListFlavor);
                final ArrayList<File> files = new ArrayList<File>(list.size());
                for (final Object o : list) {
                    files.add((File)o);
                }
                fileList = files;
            } else if (t.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                fileList = DnDUtils.getFileListFromURIList(t);
            } else {
                throw new IOException("cannot receive transferable data due to unknown data flavor"); // NOI18N
            }

            return fileList;
        } catch (final UnsupportedFlavorException e) {
            LOG.error("could not get filelist", e);                                // NOI18N
            throw new IOException("unsupported flavor despite flavor support", e); // NOI18N
        } catch (final IOException e) {
            LOG.error("could not get filelist", e);                                // NOI18N
            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check   DOCUMENT ME!
     * @param   folder  DOCUMENT ME!
     * @param   action  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static File checkFile(final File check, final File folder, final int action) throws IOException {
        if (!check.exists()) {
            throw new IOException("file does not exist: " + check);             // NOI18N
        } else if (!check.isFile()) {
            throw new IOException("only files are accepted: " + check);         // NOI18N
        } else if (!check.canRead()) {
            throw new IOException("file cannot be read: " + check);             // NOI18N
        } else if (((NodeTransfer.MOVE & action) != 0) && !check.canWrite()) {
            throw new IOException("file cannot be written: " + check);          // NOI18N
        } else if (!folder.canWrite()) {
            throw new IOException("cannot write to package folder: " + folder); // NOI18N
        }

        return check;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   files   DOCUMENT ME!
     * @param   folder  DOCUMENT ME!
     * @param   action  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean acceptFiles(final List files, final File folder, final int action) {
        boolean accept = true;
        for (final Object file : files) {
            try {
                DnDUtils.checkFile((File)file, folder, action);
            } catch (final IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("checkFile complains, not accepting drop action", e); // NOI18N
                }
                accept = false;
                break;
            }
        }

        return accept;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class UnifiedFilePasteType extends PasteType {

        //~ Instance fields ----------------------------------------------------

        private final transient Transferable t;
        private final transient FileObject targetFolder;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UnifiedFilePasteType object.
         *
         * @param  t             DOCUMENT ME!
         * @param  targetFolder  DOCUMENT ME!
         */
        public UnifiedFilePasteType(final Transferable t, final FileObject targetFolder) {
            this.t = t;
            this.targetFolder = targetFolder;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Transferable paste() throws IOException {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("action unknown, default to copy action"); // NOI18N
                }

                final List<File> fileList = DnDUtils.getFileList(t);
                final int action;
                // default to copy mode if action not determinable
                final Node[] nodes = NodeTransfer.nodes(t, NodeTransfer.MOVE);
                if (nodes == null) {
                    action = NodeTransfer.COPY;
                } else {
                    action = NodeTransfer.MOVE;
                }

                final File dir = FileUtil.toFile(targetFolder);
                for (final File f : fileList) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("received file: " + f); // NOI18N
                    }
                    checkFile(f, dir, action);
                }

                for (final File f : fileList) {
                    File outFile = new File(dir, f.getName());
                    String name = FileUtils.getName(f);
                    int i = 0;
                    while (outFile.exists()) {
                        name = FileUtils.getName(f) + "_" + ++i;                         // NOI18N
                        outFile = new File(dir, name + "." + FileUtils.getExt(outFile)); // NOI18N
                    }
                    // we have to copy all files because move causes deadlock
                    // the funny thing is, that dnd action does not have that
                    // issue
                    FileUtil.copyFile(FileUtil.toFileObject(f), targetFolder, name);
                }

                // means that it is move action but we cannot simply move
                // the fileobject because that causes a deadlock in the
                // masterfilesystem
                // as all the objects have already been copied we'll destroy the
                // nodes now
                if (nodes != null) {
                    for (final Node node : nodes) {
                        node.destroy();
                    }
                }
            } catch (final IOException e) {
                LOG.error("could not paste files", e); // NOI18N
                throw e;
            }

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class UnifiedDnDFilePasteType extends PasteType {

        //~ Instance fields ----------------------------------------------------

        private final transient Transferable t;
        private final transient int action;
        private final transient FileObject targetFolder;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UnifiedDnDFilePasteType object.
         *
         * @param  t             DOCUMENT ME!
         * @param  action        DOCUMENT ME!
         * @param  targetFolder  DOCUMENT ME!
         */
        public UnifiedDnDFilePasteType(final Transferable t, final int action, final FileObject targetFolder) {
            this.t = t;
            this.action = action;
            this.targetFolder = targetFolder;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Transferable paste() throws IOException {
            try {
                if (LOG.isDebugEnabled()) {
                    whichAction();
                }

                final List<File> fileList = DnDUtils.getFileList(t);
                final File dir = FileUtil.toFile(targetFolder);
                for (final File f : fileList) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("received file: " + f);                                // NOI18N
                    }
                    checkFile(f, dir, action);
                }
                for (final File f : fileList) {
                    File outFile = new File(dir, f.getName());
                    String name = FileUtils.getName(f);
                    int i = 0;
                    while (outFile.exists()) {
                        name = FileUtils.getName(f) + "_" + ++i;                         // NOI18N
                        outFile = new File(dir, name + "." + FileUtils.getExt(outFile)); // NOI18N
                    }
                    if ((NodeTransfer.COPY & action) != 0) {
                        FileUtil.copyFile(FileUtil.toFileObject(f), targetFolder, name);
                    } else if ((NodeTransfer.MOVE & action) != 0) {
                        FileUtil.moveFile(FileUtil.toFileObject(f), targetFolder, name);
                    } else {
                        LOG.warn("unsupported action: " + action);                       // NOI18N
                    }
                }
            } catch (final IOException e) {
                LOG.error("could not paste files", e);                                   // NOI18N
                throw e;
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         */
        private void whichAction() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("move: " + (DnDConstants.ACTION_MOVE == action)); // NOI18N
                LOG.debug("copy: " + (DnDConstants.ACTION_COPY == action)); // NOI18N
                LOG.debug("link: " + (DnDConstants.ACTION_LINK == action)); // NOI18N
                LOG.debug("copy or move: "                                  // NOI18N
                            + (DnDConstants.ACTION_COPY_OR_MOVE == action));
            }
        }
    }
}

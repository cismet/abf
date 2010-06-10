/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.3
 */
public final class FileUtils {

    //~ Static fields/initializers ---------------------------------------------

    public static final int MAC_META = 1;

    public static final int UNIX_META = 2;

    public static final int WINDOWS_META = 3;

    public static final int ALL_META = 4;

    private static final String[] MAC_META_ENTRIES = { ".DS_Store" }; // NOI18N
    private static final String[] UNIX_META_ENTRIES = {};
    private static final String[] WINDOWS_META_ENTRIES = {};

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FileUtils.
     */
    private FileUtils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isMetaFile(final FileObject check) {
        return isMetaFile(FileUtil.toFile(check));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isMetaFile(final File check) {
        return isMetaFile(check, getMode());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     * @param   mode   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isMetaFile(final FileObject check, final int mode) {
        return isMetaFile(FileUtil.toFile(check), mode);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     * @param   mode   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isMetaFile(final File check, final int mode) {
        return checkMeta(check.getName(), getMetaEntries(mode));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String[] getMetaEntries(final int mode) {
        switch (mode) {
            case MAC_META: {
                return MAC_META_ENTRIES;
            }
            case UNIX_META: {
                return UNIX_META_ENTRIES;
            }
            case WINDOWS_META: {
                return WINDOWS_META_ENTRIES;
            }
            case ALL_META:
            // ALL_META is default case
            default: {
                final String[] allMetaEntries =
                    new String[MAC_META_ENTRIES.length
                        + UNIX_META_ENTRIES.length
                        + WINDOWS_META_ENTRIES.length];
                int i = -1;
                for (final String s : MAC_META_ENTRIES) {
                    allMetaEntries[++i] = s;
                }
                for (final String s : UNIX_META_ENTRIES) {
                    allMetaEntries[++i] = s;
                }
                for (final String s : WINDOWS_META_ENTRIES) {
                    allMetaEntries[++i] = s;
                }
                return allMetaEntries;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   filename  DOCUMENT ME!
     * @param   meta      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean checkMeta(final String filename, final String[] meta) {
        for (final String s : meta) {
            if (filename.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int getMode() {
        final String os = System.getProperty("os.name"); // NOI18N
        if (os.startsWith("Mac"))                        // NOI18N
        {
            return MAC_META;
        } else if (os.startsWith("Win"))                 // NOI18N
        {
            return WINDOWS_META;
        } else {
            return UNIX_META;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   file  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getName(final File file) {
        final String nameExt = file.getName();
        final int index = nameExt.lastIndexOf('.'); // NOI18N
        if (index == -1) {
            return nameExt;
        } else {
            return nameExt.substring(0, index);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   file  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getExt(final File file) {
        final String nameExt = file.getName();
        final int index = nameExt.lastIndexOf('.'); // NOI18N
        if (index == -1) {
            return "";                              // NOI18N
        } else {
            return nameExt.substring(index + 1, nameExt.length());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean containsOnlyMetaFiles(final FileObject check) {
        return containsOnlyMetaFiles(FileUtil.toFile(check));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean containsOnlyMetaFiles(final File check) {
        return containsOnlyMetaFiles(check, getMode());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     * @param   mode   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean containsOnlyMetaFiles(final FileObject check, final int mode) {
        return containsOnlyMetaFiles(FileUtil.toFile(check), mode);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   check  DOCUMENT ME!
     * @param   mode   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public static boolean containsOnlyMetaFiles(final File check, final int mode) {
        if (!check.isDirectory()) {
            throw new IllegalArgumentException("only directories can contain files"); // NOI18N
        }
        final String[] metaEntries = getMetaEntries(mode);
        final FilesFilter filter = new FilesFilter();
        for (final File f : check.listFiles(filter)) {
            if (!checkMeta(f.getName(), metaEntries)) {
                return false;
            }
        }
        return true;
    }
    /**
     * exception thrown and not handled to avoid logger call if it is necessary to use logger in this class return
     * boolean and handle exceptions.
     *
     * @param   inFile   DOCUMENT ME!
     * @param   outFile  DOCUMENT ME!
     *
     * @throws  FileNotFoundException  DOCUMENT ME!
     * @throws  IOException            DOCUMENT ME!
     */
    public static void copyFile(final File inFile, final File outFile) throws FileNotFoundException, IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(inFile);
            fos = new FileOutputStream(outFile);
            final byte[] buffer = new byte[1024];
            int i = 0;
            while ((i = fis.read()) != -1) {
                fos.write(buffer, 0, i);
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcDir     DOCUMENT ME!
     * @param   destDir    DOCUMENT ME!
     * @param   filter     DOCUMENT ME!
     * @param   recursive  DOCUMENT ME!
     *
     * @throws  FileNotFoundException  DOCUMENT ME!
     * @throws  IOException            DOCUMENT ME!
     */
    public static void copyContent(
            final FileObject srcDir,
            final FileObject destDir,
            final FileFilter filter,
            final boolean recursive) throws FileNotFoundException, IOException {
        copyContent(FileUtil.toFile(srcDir), FileUtil.toFile(destDir), filter, recursive);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcDir     DOCUMENT ME!
     * @param   destDir    DOCUMENT ME!
     * @param   filter     DOCUMENT ME!
     * @param   recursive  DOCUMENT ME!
     *
     * @throws  FileNotFoundException  DOCUMENT ME!
     * @throws  IOException            DOCUMENT ME!
     */
    public static void copyContent(
            final File srcDir,
            final File destDir,
            final FileFilter filter,
            final boolean recursive) throws FileNotFoundException, IOException {
        if (!srcDir.isDirectory()) {
            throw new FileNotFoundException(
                "you can only copy the content of a directory, for copying files use copy(File, File) instead"); // NOI18N
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new FileNotFoundException("you can only copy to a directory");                             // NOI18N
            }
        } else {
            destDir.mkdirs();
        }
        for (final File f : srcDir.listFiles(filter)) {
            if (f.isFile()) {
                copyFile(f, new File(destDir, f.getName()));
            }
            if (f.isDirectory()) {
                final File dir = new File(destDir, f.getName());
                dir.mkdir();
                if (recursive) {
                    copyContent(f, dir, filter, recursive);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcDir     DOCUMENT ME!
     * @param   recursive  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void deleteContent(final FileObject srcDir, final boolean recursive) throws IOException {
        deleteContent(FileUtil.toFile(srcDir), recursive);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcDir     DOCUMENT ME!
     * @param   recursive  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void deleteContent(final File srcDir, final boolean recursive) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new IOException("source dir is not a directory");                                            // NOI18N
        }
        for (final File f : srcDir.listFiles()) {
            if (f.isFile()) {
                if (!f.delete()) {
                    throw new IOException(
                        "could not delete file: " + f.getAbsolutePath() + File.pathSeparator + f.getName());   // NOI18N
                }
            } else if (f.isDirectory() && recursive) {
                deleteContent(f, recursive);
                if (!f.delete()) {
                    throw new IOException(
                        "could not delete folder: " + f.getAbsolutePath() + File.pathSeparator + f.getName()); // NOI18N
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcDir  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void deleteDir(final File srcDir) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new IOException("source dir is not a directory");                                            // NOI18N
        }
        deleteContent(srcDir, true);
        if (!srcDir.delete()) {
            throw new IOException(
                "could not delete file: " + srcDir.getAbsolutePath() + File.pathSeparator + srcDir.getName()); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dir  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean containsClassFiles(final FileObject dir) {
        final Enumeration<? extends FileObject> e = dir.getData(true);
        while (e.hasMoreElements()) {
            final FileObject fo = e.nextElement();
            if ("class".equalsIgnoreCase(fo.getExt())) // NOI18N
            {
                return true;
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   jar     DOCUMENT ME!
     * @param   dest    DOCUMENT ME!
     * @param   filter  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void extractJar(final FileObject jar, final FileObject dest, final FileFilter filter)
        throws IOException {
        extractJar(FileUtil.toFile(jar), FileUtil.toFile(dest), filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   jar     DOCUMENT ME!
     * @param   dest    DOCUMENT ME!
     * @param   filter  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void extractJar(final File jar, final File dest, final FileFilter filter) throws IOException {
        if (!dest.exists()) {
            throw new IOException("dest dir does not exist: " + dest);        // NOI18N
        }
        if (!jar.exists()) {
            throw new IOException("jar file does not exist: " + jar);         // NOI18N
        }
        if (!dest.isDirectory()) {
            throw new IOException("dest dir is not a directory: " + dest);    // NOI18N
        }
        if (!dest.canWrite()) {
            throw new IOException("cannot write to dest directory: " + dest); // NOI18N
        }
        if (!jar.canRead()) {
            throw new IOException("cannot read jar file: " + jar);            // NOI18N
        }
        final JarFile jarFile = new JarFile(jar);
        final Enumeration<JarEntry> e = jarFile.entries();
        while (e.hasMoreElements()) {
            // every entry is a file, dirs are not "recognised"
            final JarEntry entry = e.nextElement();
            final File f = new File(dest, entry.getName());
            if (!filter.accept(f)) {
                continue;
            }
            if (entry.isDirectory()) {
                if (!f.mkdirs()) {
                    throw new IOException("could not create dir: " + f); // NOI18N
                }
                continue;
            }
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                bis = new BufferedInputStream(jarFile.getInputStream(entry));
                bos = new BufferedOutputStream(new FileOutputStream(f));
                while (bis.available() > 0) {
                    bos.write(bis.read());
                }
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } finally {
                    if (bos != null) {
                        bos.close();
                    }
                }
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class DirectoryFilter implements FileFilter {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean accept(final File file) {
            return file.isDirectory();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class FilesFilter implements FileFilter {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean accept(final File file) {
            return !file.isDirectory();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class MetaInfFilter implements FileFilter {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean accept(final File file) {
            return !file.getAbsolutePath().contains(File.separator + "META-INF"); // NOI18N
        }
    }
}

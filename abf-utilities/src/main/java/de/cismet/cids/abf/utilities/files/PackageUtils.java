/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.files;

import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.1
 */
public final class PackageUtils {

    //~ Static fields/initializers ---------------------------------------------

    // TODO: add checks like is pakkage children of srcroot etc, null checks and
    // think of possibilities you have if a folder contains spaces
    // (exception??)

    public static final String ROOT_PACKAGE = "<package root>"; // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PackageUtils.
     */
    private PackageUtils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   srcRoot  DOCUMENT ME!
     * @param   pakkage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toPackage(final FileObject srcRoot, final FileObject pakkage) {
        if (srcRoot.equals(pakkage)) {
            return ROOT_PACKAGE;
        }
        return pakkage.getPath().replace(srcRoot.getPath(), "").substring(1).replace("/", "."); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pakkage          DOCUMENT ME!
     * @param   systemDependant  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toRelativePath(final String pakkage, final boolean systemDependant) {
        if (pakkage.equals(ROOT_PACKAGE)) {
            return "";                                   // NOI18N
        }
        if (systemDependant) {
            return pakkage.replace(".", File.separator); // NOI18N
        }
        return pakkage.replace(".", "/");                // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcRoot          DOCUMENT ME!
     * @param   pakkage          DOCUMENT ME!
     * @param   systemDependant  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toAbsolutePath(final FileObject srcRoot, final String pakkage, final boolean systemDependant) {
        String srcPath;
        final String separator;
        if (systemDependant) {
            srcPath = FileUtil.toFile(srcRoot).getAbsolutePath();
            separator = File.separator;
        } else {
            separator = "/"; // NOI18N
            srcPath = srcRoot.getPath();
            if (!srcPath.startsWith(separator)) {
                srcPath = separator + srcPath;
            }
        }
        if (pakkage.equals(ROOT_PACKAGE)) {
            return srcPath;
        }
        return srcPath + separator + toRelativePath(pakkage, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srcRoot  DOCUMENT ME!
     * @param   pakkage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FileObject toFileObject(final FileObject srcRoot, final String pakkage) {
        return srcRoot.getFileObject(toRelativePath(pakkage, false));
    }
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import java.io.File;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class MavenUtils {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   repoPath  DOCUMENT ME!
     * @param   artifact  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String extractGroupId(final File repoPath, final File artifact) {
        final StringBuilder builder = new StringBuilder();

        // begin at the dir above artifact id directory
        File current = artifact.getParentFile().getParentFile().getParentFile();

        while (!repoPath.equals(current)) {
            builder.insert(0, current.getName()).insert(0, '.');
            current = current.getParentFile();
        }

        // remove leading '.'
        builder.deleteCharAt(0);

        return builder.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   artifact  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String extractArtifactId(final File artifact) {
        return artifact.getParentFile().getParentFile().getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   artifact  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String extractVersion(final File artifact) {
        return artifact.getParentFile().getName();
    }
}

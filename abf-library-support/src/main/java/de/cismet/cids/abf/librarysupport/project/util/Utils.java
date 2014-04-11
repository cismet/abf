/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class Utils {

    //~ Static fields/initializers ---------------------------------------------

    public static final transient Pattern VAR_PATTERN = Pattern.compile("(.*)\\$\\{(.+)\\}(.*)"); // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Utils object.
     */
    private Utils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Replaces ${var} in paths with the equivalent value of System.getProperty.
     *
     * @param   path  DOCUMENT ME!
     *
     * @return  the path with the replacement for the var, the original path otherwise
     */
    public static String getPath(final String path) {
        if (path == null) {
            return null;
        }

        final String result;
        final Matcher matcher = VAR_PATTERN.matcher(path);
        if (matcher.matches()) {
            final String var = matcher.group(2);

            result = matcher.group(1) + System.getProperty(var) + matcher.group(3);
        } else {
            result = path;
        }

        return result;
    }
}

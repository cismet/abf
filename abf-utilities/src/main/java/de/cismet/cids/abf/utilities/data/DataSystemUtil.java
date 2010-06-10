/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.data;

import org.openide.loaders.DataFilter;
import org.openide.loaders.DataObject;

/**
 * DOCUMENT ME!
 *
 * @author   Martin Scholl
 * @version  $Revision$, $Date$
 */
public final class DataSystemUtil {

    //~ Static fields/initializers ---------------------------------------------

    public static final DataFilter NO_FOLDERS_FILTER;

    static {
        NO_FOLDERS_FILTER = new NoFoldersFilter();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class NoFoldersFilter implements DataFilter {

        //~ Static fields/initializers -----------------------------------------

        /** Use serialVersionUID for interoperability. */
        private static final long serialVersionUID = 6445764160586840464L;

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean acceptDataObject(final DataObject obj) {
            return !obj.getPrimaryFile().isFolder();
        }
    }
}

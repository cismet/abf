/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class CidsTypeTransferable implements Transferable {

    //~ Static fields/initializers ---------------------------------------------

    public static final DataFlavor CIDS_TYPE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "CidsType"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient Object o;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsTypeTransferable object.
     *
     * @param  o  DOCUMENT ME!
     */
    public CidsTypeTransferable(final Object o) {
        this.o = o;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns whether or not the specified data flavor is supported for this object.
     *
     * @param   flavor  the requested flavor for the data
     *
     * @return  boolean indicating whether or not the data flavor is supported
     */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return flavor.match(CIDS_TYPE_FLAVOR);
    }

    /**
     * Returns an object which represents the data to be transferred. The class of the object returned is defined by the
     * representation class of the flavor.
     *
     * @param      flavor  the requested flavor for the data
     *
     * @return     DOCUMENT ME!
     *
     * @exception  UnsupportedFlavorException  if the requested data flavor is not supported.
     * @exception  IOException                 if the data is no longer available in the requested flavor.
     *
     * @see        DataFlavor#getRepresentationClass
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return flavor.match(CIDS_TYPE_FLAVOR) ? o : null;
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data can be provided in. The array should be
     * ordered according to preference for providing the data (from most richly descriptive to least descriptive).
     *
     * @return  an array of data flavors in which this data can be transferred
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        final DataFlavor[] flavors = new DataFlavor[1];
        flavors[0] = CIDS_TYPE_FLAVOR;

        return flavors;
    }
}

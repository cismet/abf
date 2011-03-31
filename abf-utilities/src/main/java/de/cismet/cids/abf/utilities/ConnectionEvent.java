/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import java.util.EventObject;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConnectionEvent extends EventObject {

    //~ Instance fields --------------------------------------------------------

    private final boolean connected;
    private final boolean indeterminate;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionEvent object.
     *
     * @param  source         DOCUMENT ME!
     * @param  connected      DOCUMENT ME!
     * @param  indeterminate  DOCUMENT ME!
     */
    public ConnectionEvent(final Object source, final boolean connected, final boolean indeterminate) {
        super(source);
        this.connected = connected;
        this.indeterminate = indeterminate;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isIndeterminate() {
        return indeterminate;
    }
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConnectionSupport {

    //~ Instance fields --------------------------------------------------------

    private final transient Set<ConnectionListener> listeners;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectionSupport object.
     */
    public ConnectionSupport() {
        listeners = new HashSet<ConnectionListener>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  conL  DOCUMENT ME!
     */
    public void addConnectionListener(final ConnectionListener conL) {
        if (conL != null) {
            synchronized (listeners) {
                listeners.add(conL);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conL  DOCUMENT ME!
     */
    public void removeConnectionListener(final ConnectionListener conL) {
        if (conL != null) {
            synchronized (listeners) {
                listeners.remove(conL);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void fireConnectionStatusChanged(final ConnectionEvent event) {
        final Iterator<ConnectionListener> it;

        synchronized (listeners) {
            it = new HashSet<ConnectionListener>(listeners).iterator();
        }

        while (it.hasNext()) {
            it.next().connectionStatusChanged(event);
        }
    }
}

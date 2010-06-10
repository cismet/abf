/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface Connectable {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isConnected();
    /**
     * DOCUMENT ME!
     *
     * @param  connected  DOCUMENT ME!
     */
    void setConnected(final boolean connected);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isConnectionInProgress();
    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void addConnectionListener(final ConnectionListener l);
    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void removeConnectionListener(final ConnectionListener l);
}

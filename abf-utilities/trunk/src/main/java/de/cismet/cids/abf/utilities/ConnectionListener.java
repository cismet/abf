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
public interface ConnectionListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  isConnected  DOCUMENT ME!
     */
    void connectionStatusChanged(final boolean isConnected);
    /**
     * DOCUMENT ME!
     */
    void connectionStatusIndeterminate();
}

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
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class CacheException extends RuntimeException {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CacheException object.
     *
     * @param  message  DOCUMENT ME!
     * @param  cause    DOCUMENT ME!
     */
    public CacheException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

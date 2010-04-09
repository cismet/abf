/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * DOCUMENT ME!
 *
 * @author   Martin Scholl
 * @version  $Revision$, $Date$
 */
public final class UtilityCommons {

    //~ Static fields/initializers ---------------------------------------------

    public static final String IMAGE_FOLDER = "de/cismet/cids/abf/utilities/images/"; // NOI18N

    static {
        DOMConfigurator.configure(UtilityCommons.class.getResource("log4j.xml")); // NOI18N
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UtilityCommons object.
     */
    private UtilityCommons() {
    }
}

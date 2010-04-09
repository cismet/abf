/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.project;

import java.util.Properties;

import org.netbeans.spi.project.ProjectState;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class NotifyProperties extends Properties {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 3706583352903228873L;

    //~ Instance fields --------------------------------------------------------

    private final transient ProjectState state;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NotifyProperties object.
     *
     * @param  state  DOCUMENT ME!
     */
    public NotifyProperties(final ProjectState state) {
        this.state = state;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     * @param   val  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object put(final Object key, final Object val) {
        final Object result = super.put(key, val);
        if (((result == null) != (val == null)) || ((result != null) && (val != null) && !val.equals(result))) {
            state.markModified();
        }
        return result;
    }
}

/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.registry.cookie;

import de.cismet.cids.abf.registry.RegistryProject;

/**
 * DOCUMENT ME!
 *
 * @author   Martin Scholl
 * @version  $Revision$, $Date$
 */
public class RegistryProjectCookieImpl implements RegistryProjectCookie {

    //~ Instance fields --------------------------------------------------------

    private final transient RegistryProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegistryProjectCookieImpl object.
     *
     * @param  project  DOCUMENT ME!
     */
    public RegistryProjectCookieImpl(final RegistryProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public RegistryProject getProject() {
        return project;
    }
}

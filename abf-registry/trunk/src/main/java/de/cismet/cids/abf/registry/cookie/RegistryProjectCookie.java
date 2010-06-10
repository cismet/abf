/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.registry.cookie;

import de.cismet.cids.abf.registry.RegistryProject;

import org.openide.nodes.Node;

/**
 * DOCUMENT ME!
 *
 * @author   Martin Scholl
 * @version  $Revision$, $Date$
 */
public interface RegistryProjectCookie extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    RegistryProject getProject();
}

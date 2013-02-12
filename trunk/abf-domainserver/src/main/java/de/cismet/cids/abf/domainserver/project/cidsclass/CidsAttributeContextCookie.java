/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.nodes.Node;

import de.cismet.cids.jpa.entity.cidsclass.Attribute;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface CidsAttributeContextCookie extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Attribute getCidsAttribute();
    /**
     * DOCUMENT ME!
     *
     * @param  a  DOCUMENT ME!
     */
    void setCidsAttribute(final Attribute a);
}

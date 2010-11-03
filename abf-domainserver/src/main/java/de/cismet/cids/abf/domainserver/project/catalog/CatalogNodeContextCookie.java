/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.openide.nodes.Node;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface CatalogNodeContextCookie extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CatNode getCatNode();
    /**
     * DOCUMENT ME!
     *
     * @param  node  DOCUMENT ME!
     */
    void setCatNode(CatNode node);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CatNode getParent();
}

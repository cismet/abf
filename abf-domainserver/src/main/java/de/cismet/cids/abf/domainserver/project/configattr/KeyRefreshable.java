/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.abf.domainserver.project.configattr;

import org.openide.nodes.Node;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public interface KeyRefreshable extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    void refreshKey(ConfigAttrKey key);
}

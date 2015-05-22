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

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public interface GroupRefreshable extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cascade  DOCUMENT ME!
     */
    void refresh(boolean cascade);
    /**
     * DOCUMENT ME!
     *
     * @param  groups  DOCUMENT ME!
     */
    void refreshGroups(String... groups);
}

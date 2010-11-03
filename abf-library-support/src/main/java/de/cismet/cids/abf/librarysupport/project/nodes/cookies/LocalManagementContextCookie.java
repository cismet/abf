/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.cookies;

import org.openide.nodes.Node;

import de.cismet.cids.abf.librarysupport.project.nodes.LocalManagement;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public interface LocalManagementContextCookie extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    LocalManagement getLocalManagementContext();
}

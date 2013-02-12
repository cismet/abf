/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.cookies;

import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public interface ManifestProviderCookie extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FileObject getManifest();
}

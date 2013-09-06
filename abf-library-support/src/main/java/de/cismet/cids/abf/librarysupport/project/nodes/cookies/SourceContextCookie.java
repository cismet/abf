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

import java.io.FileNotFoundException;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.5
 */
public interface SourceContextCookie extends Node.Cookie {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  FileNotFoundException  DOCUMENT ME!
     */
    FileObject getSourceObject() throws FileNotFoundException;
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isSourceObjectObservered();
    /**
     * DOCUMENT ME!
     *
     * @param  observed  DOCUMENT ME!
     */
    void setSourceObjectObserved(final boolean observed);
}

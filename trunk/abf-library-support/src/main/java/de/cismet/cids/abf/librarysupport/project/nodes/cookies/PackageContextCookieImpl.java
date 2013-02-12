/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.cookies;

import org.openide.filesystems.FileObject;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.2
 */
public final class PackageContextCookieImpl implements PackageContextCookie {

    //~ Instance fields --------------------------------------------------------

    private final transient FileObject root;
    private final transient FileObject current;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PackageContextCookieImpl object.
     *
     * @param  root  DOCUMENT ME!
     * @param  cur   DOCUMENT ME!
     */
    public PackageContextCookieImpl(final FileObject root, final FileObject cur) {
        this.root = root;
        this.current = cur;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FileObject getRootFolder() {
        return root;
    }

    @Override
    public FileObject getCurrentFolder() {
        return current;
    }
}

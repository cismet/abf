/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.3
 */
public abstract class ProjectNode extends AbstractNode implements LibrarySupportContextCookie {

    //~ Instance fields --------------------------------------------------------

    protected final transient LibrarySupportProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProjectNode object.
     *
     * @param  ch       DOCUMENT ME!
     * @param  project  DOCUMENT ME!
     */
    public ProjectNode(final Children ch, final LibrarySupportProject project) {
        super(ch);
        this.project = project;
        getCookieSet().add(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public LibrarySupportProject getLibrarySupportContext() {
        return project;
    }
}

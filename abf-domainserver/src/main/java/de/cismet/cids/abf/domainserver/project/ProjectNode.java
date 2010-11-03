/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class ProjectNode extends AbstractNode implements DomainserverContext {

    //~ Instance fields --------------------------------------------------------

    protected final transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProjectNode object.
     *
     * @param  c  DOCUMENT ME!
     * @param  p  DOCUMENT ME!
     */
    public ProjectNode(final Children c, final DomainserverProject p) {
        super(c);
        this.project = p;
        getCookieSet().add(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public DomainserverProject getDomainserverProject() {
        return project;
    }
}

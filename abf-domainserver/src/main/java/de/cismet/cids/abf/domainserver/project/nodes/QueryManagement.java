/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

import java.awt.Image;

import java.util.Arrays;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.query.QueriesNode;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class QueryManagement extends ProjectNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public QueryManagement(final DomainserverProject project) {
        super(new QueryManagementChildren(project), project);
        this.setName(org.openide.util.NbBundle.getMessage(
                QueryManagement.class,
                "QueryManagement.QueryManagement(DomainserverProject).name")); // NOI18N
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "search.png");                                       // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        return nodeImage;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return nodeImage;
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class QueryManagementChildren extends Children.Array {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public QueryManagementChildren(final DomainserverProject project) {
        super(Arrays.asList(
                new Node[] { new QueriesNode(project) }));
    }
}

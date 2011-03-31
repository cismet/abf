/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.nodes;

import org.apache.log4j.Logger;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

import java.awt.Image;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.types.TypeNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.cidsclass.Type;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.4
 */
public final class TypeManagement extends ProjectNode implements ConnectionListener, Refreshable {

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TypeManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public TypeManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "datatype.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                TypeManagement.class,
                "TypeManagement.TypeManagement(DomainserverProject).displayName"));              // NOI18N
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

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        if (event.isConnected() && !event.isIndeterminate()) {
            setChildrenEDT(new TypeManagementChildren(project));
        } else {
            setChildrenEDT(Children.LEAF);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void refresh() {
        final Children ch = getChildren();
        if (ch instanceof ProjectChildren) {
            ((ProjectChildren)ch).refreshByNotify();
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class TypeManagementChildren extends ProjectChildren {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(TypeManagementChildren.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TypeManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public TypeManagementChildren(final DomainserverProject project) {
        super(project);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createUserNodes(final Object o) {
        if (o instanceof Type) {
            return new Node[] { new TypeNode((Type)o, project) };
        } else {
            return new Node[] {};
        }
    }

    @Override
    protected void threadedNotify() throws IOException {
        try {
            final List<Type> allTypes = project.getCidsDataObjectBackend().getAllEntities(Type.class);
            final List<Type> onlyUserDefined = new ArrayList<Type>(10);
            for (final Type t : allTypes) {
                if (t.isComplexType()) {
                    onlyUserDefined.add(t);
                }
            }

            Collections.sort(onlyUserDefined, new Comparators.AttrTypes());
            setKeysEDT(onlyUserDefined);
        } catch (final Exception ex) {
            LOG.error("could not create children", ex);               // NOI18N
            ErrorUtils.showErrorMessage(
                org.openide.util.NbBundle.getMessage(
                    TypeManagementChildren.class,
                    "TypeManagement.addNotify().ErrorUtils.message"), // NOI18N
                ex);
        }
    }
}

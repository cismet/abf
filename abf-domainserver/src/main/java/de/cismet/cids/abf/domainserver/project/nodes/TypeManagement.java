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

import java.awt.EventQueue;
import java.awt.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.types.TypeNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.cidsclass.Type;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  1.4
 */
public final class TypeManagement extends ProjectNode implements ConnectionListener {

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
        project.addConnectionListener(this);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "datatype.png");                                          // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                TypeManagement.class,
                "TypeManagement.TypeManagement(DomainserverProject).displayName")); // NOI18N
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
    public void connectionStatusChanged(final boolean isConnected) {
        if (project.isConnected()) {
            setChildren(new TypeManagementChildren(project));
        } else {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusIndeterminate() {
        // do nothing
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshChildren() {
        final Children ch = getChildren();
        if (ch instanceof TypeManagementChildren) {
            ((TypeManagementChildren)ch).refreshAll();
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class TypeManagementChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            TypeManagementChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TypeManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public TypeManagementChildren(final DomainserverProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createNodes(final Object object) {
        if (object instanceof Type) {
            return new Node[] { new TypeNode((Type)object, project) };
        } else {
            return new Node[] {};
        }
    }

    /**
     * DOCUMENT ME!
     */
    void refreshAll() {
        addNotify();
    }

    @Override
    protected void addNotify() {
        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final List<Type> allTypes = project.getCidsDataObjectBackend().getAllEntities(Type.class);
                            final List<Type> onlyUserDefined = new ArrayList<Type>(10);
                            for (final Type t : allTypes) {
                                if (t.isComplexType()) {
                                    onlyUserDefined.add(t);
                                }
                            }
                            Collections.sort(onlyUserDefined, new Comparators.AttrTypes());
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        setKeys(onlyUserDefined);
                                    }
                                });
                        } catch (final Exception ex) {
                            LOG.error("could not create children", ex);               // NOI18N
                            ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    TypeManagementChildren.class,
                                    "TypeManagement.addNotify().ErrorUtils.message"), // NOI18N
                                ex);
                        }
                    }
                }, "TypeManagementChildren::addNotifyRunner");                        // NOI18N
        t.start();
    }
}

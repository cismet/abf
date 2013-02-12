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
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassNode;
import de.cismet.cids.abf.domainserver.project.javaclass.NewJavaClassWizardAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.cidsclass.JavaClass;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class JavaClassManagement extends ProjectNode implements ConnectionListener,
    JavaClassManagementContextCookie,
    Refreshable {

    //~ Instance fields --------------------------------------------------------

    private final transient Image nodeImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JavaClassManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public JavaClassManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "java.png");  // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                JavaClassManagement.class,
                "JavaClassManagement.JavaClassManagement(DomainserverProject).displayName")); // NOI18N
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
            setChildrenEDT(new JavaClassManagementChildren(project));
        } else {
            setChildrenEDT(Children.LEAF);
        }
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(NewJavaClassWizardAction.class) };
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
final class JavaClassManagementChildren extends ProjectChildren {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(JavaClassManagementChildren.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JavaClassManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public JavaClassManagementChildren(final DomainserverProject project) {
        super(project);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createUserNodes(final Object o) {
        if (o instanceof JavaClass) {
            return new Node[] { new JavaClassNode((JavaClass)o, project) };
        } else {
            return new Node[] {};
        }
    }

    @Override
    protected void threadedNotify() throws IOException {
        try {
            final List<JavaClass> allClasses = project.getCidsDataObjectBackend().getAllEntities(JavaClass.class);
            Collections.sort(allClasses, new Comparators.JavaClasses());
            setKeysEDT(allClasses);
        } catch (final Exception ex) {
            LOG.error("could not load javaclasses", ex);                   // NOI18N
            ErrorUtils.showErrorMessage(
                org.openide.util.NbBundle.getMessage(
                    JavaClassManagement.class,
                    "JavaClassManagement.addNotify().ErrorUtils.message"), // NOI18N
                ex);
        }
    }
}

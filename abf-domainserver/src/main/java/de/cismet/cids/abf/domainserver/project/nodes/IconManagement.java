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
import de.cismet.cids.abf.domainserver.project.KeyContainer;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.icons.IconManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.icons.IconNode;
import de.cismet.cids.abf.domainserver.project.icons.NewIconAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.cidsclass.Icon;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class IconManagement extends ProjectNode implements ConnectionListener, IconManagementContextCookie {

    //~ Instance fields --------------------------------------------------------

    private final transient Image image;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IconManagement object.
     *
     * @param  project  DOCUMENT ME!
     */
    public IconManagement(final DomainserverProject project) {
        super(Children.LEAF, project);
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "icons.png"); // NOI18N
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        getCookieSet().add(this);

        setDisplayName(org.openide.util.NbBundle.getMessage(
                IconManagement.class,
                "IconManagement.IconManagement(DomainserverProject).displayName")); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int i) {
        return image;
    }

    @Override
    public Image getOpenedIcon(final int i) {
        return image;
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        if (!event.isIndeterminate()) {
            if (event.isConnected()) {
                setChildrenEDT(new IconManagementChildren(project));
            } else {
                setChildrenEDT(Children.LEAF);
            }
        }
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(NewIconAction.class) };
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshChildren() {
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
final class IconManagementChildren extends ProjectChildren {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(IconManagementChildren.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IconManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public IconManagementChildren(final DomainserverProject project) {
        super(project);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createUserNodes(final Object o) {
        if (o instanceof KeyContainer) {
            return new Node[] { new IconNode((Icon)((KeyContainer)o).getObject(), project) };
        } else {
            return new Node[] {};
        }
    }

    @Override
    protected void threadedNotify() throws IOException {
        try {
            final List<Icon> icons = project.getCidsDataObjectBackend().getAllEntities(Icon.class);
            Collections.sort(icons, new Comparators.Icons());
            setKeysEDT(KeyContainer.convertCollection(Icon.class, icons));
        } catch (final Exception ex) {
            final String message = "could not load icons";            // NOI18N
            LOG.error(message, ex);
            ErrorUtils.showErrorMessage(
                org.openide.util.NbBundle.getMessage(
                    IconManagementChildren.class,
                    "IconManagement.addNotify().ErrorUtils.message"), // NOI18N
                ex);

            throw new IOException(message, ex);
        }
    }
}

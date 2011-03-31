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

import java.awt.EventQueue;
import java.awt.Image;

import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.icons.IconManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.icons.IconNode;
import de.cismet.cids.abf.domainserver.project.icons.NewIconAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
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
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "icons.png"); // NOI18N
        project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
        getCookieSet().add(this);
        setDisplayName(org.openide.util.NbBundle.getMessage(
                IconManagement.class,
                "IconManagement.IconManagement(DomainserverProject).displayName"));
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
        if (event.isConnected() && !event.isIndeterminate()) {
            setChildrenEDT(new IconManagementChildren(project));
        } else {
            setChildrenEDT(Children.LEAF);
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
        if (ch instanceof IconManagementChildren) {
            ((IconManagementChildren)ch).refreshAll();
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
final class IconManagementChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            IconManagementChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;
    private transient LoadingNode loadingNode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IconManagementChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public IconManagementChildren(final DomainserverProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Node[] createNodes(final Object object) {
        if (object instanceof LoadingNode) {
            return new Node[] { (LoadingNode)object };
        } else if (object instanceof Icon) {
            return new Node[] { new IconNode((Icon)object, project) };
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
        loadingNode = new LoadingNode();
        setKeys(new Object[] { loadingNode });
        refresh();
        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final List<Icon> icons = project.getCidsDataObjectBackend().getAllEntities(Icon.class);
                            Collections.sort(icons, new Comparators.Icons());
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        setKeys(icons);
                                    }
                                });
                        } catch (final Exception ex) {
                            LOG.error("could not load icons", ex); // NOI18N
                            ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    IconManagementChildren.class,
                                    "IconManagement.addNotify().ErrorUtils.message"),
                                ex);                               // NOI18N
                        } finally {
                            if (loadingNode != null) {
                                loadingNode.dispose();
                                loadingNode = null;
                            }
                        }
                    }
                }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
        t.start();
    }
}

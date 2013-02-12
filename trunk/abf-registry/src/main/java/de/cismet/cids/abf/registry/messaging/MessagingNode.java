/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.registry.messaging;

import Sirius.server.registry.rmplugin.util.RMUser;

import org.apache.log4j.Logger;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;

import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RMUserCookie;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class MessagingNode extends AbstractNode implements ConnectionListener, RMUserCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(MessagingNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient RegistryProject registryProject;
    private final transient Image icon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingNode object.
     *
     * @param  registryProject  DOCUMENT ME!
     */
    public MessagingNode(final RegistryProject registryProject) {
        super(Children.LEAF);
        this.registryProject = registryProject;
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER + "allUsers.png");                         // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(MessagingNode.class, "MessagingNode.displayName")); // NOI18N
        registryProject.addConnectionListener(this);
        getCookieSet().add(new RegistryProjectCookieImpl(registryProject));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getOpenedIcon(final int i) {
        return icon;
    }

    @Override
    public Image getIcon(final int i) {
        return icon;
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshChildren() {
        if (registryProject.isConnected() && !registryProject.isConnectionInProgress()) {
            setChildren(new MessagingChildren(registryProject));
            try {
                registryProject.getMessageForwarder().logCurrentRegistry();
            } catch (final Exception ex) {
                LOG.error("could not log current registry", ex); // NOI18N
            }
        } else {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusChanged(final ConnectionEvent event) {
        refreshChildren();
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(SendMessageAction.class), };
    }

    // we won't use the general cookie impl here because the children will
    // probably change over time so we would have to store the impl as a member
    // to remove old impl, add new one and so on
    // SEE refreshChildren
    // TODO: refactor the children refresh "tactic"
    @Override
    public Set<RMUser> getRMUsers() {
        final Set<RMUser> users = new HashSet<RMUser>();
        for (final Node n : getChildren().getNodes()) {
            final RMUserCookie cookie = n.getCookie(RMUserCookie.class);
            if (cookie != null) {
                users.addAll(cookie.getRMUsers());
            }
        }
        return users;
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class MessagingChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(MessagingChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient RegistryProject registryProject;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingChildren object.
     *
     * @param  registryProject  DOCUMENT ME!
     */
    public MessagingChildren(final RegistryProject registryProject) {
        this.registryProject = registryProject;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   object  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected Node[] createNodes(final Object object) {
        if (object instanceof String) {
            return new Node[] { new MessagingDomainNode((String)object, registryProject) };
        } else {
            return new Node[] {};
        }
    }

    @Override
    protected void addNotify() {
        try {
            setKeys(registryProject.getMessageForwarder().getAllActiveDomains());
        } catch (final Exception ex) {
            LOG.error("could not add notify", ex); // NOI18N
        }
    }
}

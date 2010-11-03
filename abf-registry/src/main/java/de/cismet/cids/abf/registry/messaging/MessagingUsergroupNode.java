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

import javax.swing.Action;

import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RMUserCookieImpl;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class MessagingUsergroupNode extends AbstractNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image icon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingUsergroupNode object.
     *
     * @param  domain      DOCUMENT ME!
     * @param  usergroup   DOCUMENT ME!
     * @param  regProject  DOCUMENT ME!
     */
    public MessagingUsergroupNode(final String domain, final String usergroup, final RegistryProject regProject) {
        super(new MessagingUsergroupChildren(domain, usergroup, regProject));
        setDisplayName(usergroup);
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER + "group.png"); // NOI18N
        getCookieSet().add(new RMUserCookieImpl(getChildren()));
        getCookieSet().add(new RegistryProjectCookieImpl(regProject));
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

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(SendMessageAction.class), };
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class MessagingUsergroupChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(MessagingUsergroupChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient String domain;
    private final transient String usergroup;
    private final transient RegistryProject registryProject;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingUsergroupChildren object.
     *
     * @param  domain           DOCUMENT ME!
     * @param  usergroup        DOCUMENT ME!
     * @param  registryProject  DOCUMENT ME!
     */
    public MessagingUsergroupChildren(
            final String domain,
            final String usergroup,
            final RegistryProject registryProject) {
        this.domain = domain;
        this.usergroup = usergroup;
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
        if (object instanceof RMUser) {
            return new Node[] { new MessagingUserNode((RMUser)object, registryProject) };
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no RMUser"); // NOI18N
            }
            return new Node[] {};
        }
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        try {
            setKeys(registryProject.getMessageForwarder().getAllActiveUsers(usergroup, domain));
        } catch (final Exception ex) {
            LOG.error("could not set keys", ex); // NOI18N
        }
    }
}

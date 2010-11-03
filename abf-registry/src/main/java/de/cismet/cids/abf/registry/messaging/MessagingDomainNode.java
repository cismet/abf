/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.registry.messaging;

import org.apache.log4j.Logger;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

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
public class MessagingDomainNode extends AbstractNode {

    //~ Instance fields --------------------------------------------------------

    private final transient Image icon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingDomainNode object.
     *
     * @param  domain           DOCUMENT ME!
     * @param  registryProject  DOCUMENT ME!
     */
    public MessagingDomainNode(final String domain,
            final RegistryProject registryProject) {
        super(new MessagingDomainChildren(domain, registryProject));
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER + "domainserver.png"); // NOI18N
        setDisplayName(domain);
        getCookieSet().add(new RMUserCookieImpl(getChildren()));
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
class MessagingDomainChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(MessagingDomainChildren.class);

    //~ Instance fields --------------------------------------------------------

    private final transient RegistryProject registryProject;
    private final transient String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingDomainChildren object.
     *
     * @param  domain           DOCUMENT ME!
     * @param  registryProject  DOCUMENT ME!
     */
    public MessagingDomainChildren(final String domain, final RegistryProject registryProject) {
        this.registryProject = registryProject;
        this.domain = domain;
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
            return new Node[] { new MessagingUsergroupNode(domain, (String)object, registryProject) };
        } else {
            return new Node[] {};
        }
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        try {
            setKeys(registryProject.getMessageForwarder().getAllActiveGroups(domain));
        } catch (final IOException ex) {
            LOG.error("could not set keys from registry", ex); // NOI18N
        }
    }
}

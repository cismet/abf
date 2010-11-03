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
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;

import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RMUserCookie;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class MessagingUserNode extends AbstractNode implements RMUserCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(MessagingUserNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient RMUser user;
    private final transient Image icon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MessagingUserNode object.
     *
     * @param  user     DOCUMENT ME!
     * @param  project  DOCUMENT ME!
     */
    public MessagingUserNode(final RMUser user, final RegistryProject project) {
        super(Children.LEAF);
        this.user = user;
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER + "user.png"); // NOI18N
        setDisplayName(user.getUserName());
        getCookieSet().add(this);
        getCookieSet().add(new RegistryProjectCookieImpl(project));
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
    public Set<RMUser> getRMUsers() {
        final Set<RMUser> users = new HashSet<RMUser>(1);
        users.add(user);
        return users;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] { CallableSystemAction.get(SendMessageAction.class), };
    }

    @Override
    protected Sheet createSheet() {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName(org.openide.util.NbBundle.getMessage(
                MessagingUserNode.class,
                "MessagingUserNode.createSheet().displayName"));                                                   // NOI18N
        try {
            final Property nameProp = new PropertySupport.Reflection(user, String.class, "getUserName", null);     // NOI18N
            nameProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class,
                    "MessagingUserNode.createSheet().nameProp.name"));                                             // NOI18N
            final Property domainProp = new PropertySupport.Reflection(user, String.class, "getUserDomain", null); // NOI18N
            domainProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class,
                    "MessagingUserNode.createSheet().domainProp.name"));                                           // NOI18N
            final Property groupProp = new PropertySupport.Reflection(user, String.class, "getUserGroup", null);   // NOI18N
            groupProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class,
                    "MessagingUserNode.createSheet().groupProp.name"));                                            // NOI18N
            final Property onlineTimeProp = new PropertySupport.Reflection(
                    user,
                    String.class,
                    "getOnlineTimeAsString",
                    null);                                                                                         // NOI18N
            onlineTimeProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class,
                    "MessagingUserNode.createSheet().onlineTimeProp.name"));                                       // NOI18N
            final Property ipProp = new PropertySupport.Reflection(user, String.class, "getIpAddress", null);      // NOI18N
            ipProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class,
                    "MessagingUserNode.createSheet().ipProp.name"));                                               // NOI18N
            set.put(nameProp);
            set.put(domainProp);
            set.put(groupProp);
            set.put(onlineTimeProp);
            set.put(ipProp);
            sheet.put(set);
        } catch (final Exception ex) {
            LOG.warn("could not create property sheet", ex);                                                       // NOI18N
        }
        return sheet;
    }
}

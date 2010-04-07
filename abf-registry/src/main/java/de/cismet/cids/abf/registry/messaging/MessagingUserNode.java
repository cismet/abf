/*
 * MessagingUserNode.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 3. Januar 2007, 16:00
 *
 */
package de.cismet.cids.abf.registry.messaging;

import de.cismet.cids.abf.registry.cookie.RMUserCookie;
import Sirius.server.registry.rmplugin.util.RMUser;
import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;
import java.awt.Image;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class MessagingUserNode extends AbstractNode implements RMUserCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            MessagingUserNode.class);

    private final transient RMUser user;
    private final transient Image icon;

    public MessagingUserNode(final RMUser user, final RegistryProject project)
    {
        super(Children.LEAF);
        this.user = user;
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER
                + "user.png"); // NOI18N
        setDisplayName(user.getUserName());
        getCookieSet().add(this);
        getCookieSet().add(new RegistryProjectCookieImpl(project));
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        return icon;
    }

    @Override
    public Image getIcon(final int i)
    {
        return icon;
    }

    @Override
    public Set<RMUser> getRMUsers()
    {
        final Set<RMUser> users = new HashSet<RMUser>(1);
        users.add(user);
        return users;
    }

    @Override
    public Action[] getActions(final boolean b)
    {
        return  new Action[]
                {
                    CallableSystemAction.get(SendMessageAction.class),
                };
    }

    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName(org.openide.util.NbBundle.getMessage(
                MessagingUserNode.class, "Dsc_properties")); // NOI18N
        try
        {
            final Property nameProp = new PropertySupport.Reflection(
                    user, String.class, "getUserName", null); // NOI18N
            nameProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class, "Dsc_login")); // NOI18N
            final Property domainProp = new PropertySupport.Reflection(
                    user, String.class, "getUserDomain", null); // NOI18N
            domainProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class, "Dsc_domain")); // NOI18N
            final Property groupProp = new PropertySupport.Reflection(
                    user, String.class, "getUserGroup", null); // NOI18N
            groupProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class, "Dsc_usergroup")); // NOI18N
            final Property onlineTimeProp = new PropertySupport.Reflection(
                    user, String.class, "getOnlineTimeAsString", null);// NOI18N
            onlineTimeProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class, "Dsc_onlineTime")); // NOI18N
            final Property ipProp = new PropertySupport.Reflection(
                    user, String.class, "getIpAddress", null); // NOI18N
            ipProp.setName(org.openide.util.NbBundle.getMessage(
                    MessagingUserNode.class, "Dsc_ipAddress")); // NOI18N
            set.put(nameProp);
            set.put(domainProp);
            set.put(groupProp);
            set.put(onlineTimeProp);
            set.put(ipProp);
            sheet.put(set);
        }catch(final Exception ex)
        {
            LOG.warn("could not create property sheet", ex); // NOI18N
        }
        return sheet;
    }
}
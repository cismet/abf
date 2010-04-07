/*
 * MessagingNode.java, encoding: UTF-8
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
 * Created on 3. Januar 2007, 15:51
 *
 */
package de.cismet.cids.abf.registry.messaging;

import de.cismet.cids.abf.registry.cookie.RMUserCookie;
import Sirius.server.registry.rmplugin.util.RMUser;
import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;
import de.cismet.cids.abf.utilities.ConnectionListener;
import java.awt.Image;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class MessagingNode extends AbstractNode implements 
        ConnectionListener,
        RMUserCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            MessagingNode.class);

    private final transient RegistryProject registryProject;
    private final transient Image icon;

    public MessagingNode(final RegistryProject registryProject)
    {
        super(Children.LEAF);
        this.registryProject = registryProject;
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER
                + "allUsers.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                MessagingNode.class, "Dsc_loggedOnUsers")); // NOI18N
        registryProject.addConnectionListener(this);
        getCookieSet().add(new RegistryProjectCookieImpl(registryProject));
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

    public void refreshChildren()
    {
        if(registryProject.isConnected())
        {
            setChildren(new MessagingChildren(registryProject));
            try
            {
                registryProject.getMessageForwarder().logCurrentRegistry();
            }catch(final Exception ex)
            {
                LOG.error("could not log current registry", ex); // NOI18N
            }
        }else
        {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        refreshChildren();
    }

    @Override
    public void connectionStatusIndeterminate()
    {
        // not needed
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[]
                {
                    CallableSystemAction.get(SendMessageAction.class),
                };
    }

    // we won't use the general cookie impl here because the children will
    // probably change over time so we would have to store the impl as a member
    // to remove old impl, add new one and so on
    // SEE refreshChildren
    // TODO: refactor the children refresh "tactic"
    @Override
    public Set<RMUser> getRMUsers()
    {
        final Set<RMUser> users = new HashSet<RMUser>();
        for(final Node n : getChildren().getNodes())
        {
            final RMUserCookie cookie = n.getCookie(RMUserCookie.class);
            if(cookie != null)
            {
                users.addAll(cookie.getRMUsers());
            }
        }
        return users;
    }
}

class MessagingChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            MessagingChildren.class);

    private final transient RegistryProject registryProject;

    public MessagingChildren(final RegistryProject registryProject)
    {
        this.registryProject = registryProject;
    }

    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof String)
        {
            return new Node[]
                    {
                        new MessagingDomainNode((String)object, registryProject)
                    };
        }else
        {
            return new Node[] {};
        }
    }

    @Override
    protected void addNotify()
    {
        super.addNotify();
        try
        {
            setKeys(registryProject.getMessageForwarder()
                    .getAllActiveDomains());
        }catch(final Exception ex)
        {
            LOG.error("could not add notify", ex); // NOI18N
        }
    }
}
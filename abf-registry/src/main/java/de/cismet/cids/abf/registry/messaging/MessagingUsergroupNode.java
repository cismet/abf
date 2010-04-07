/*
 * MessagingUsergroupNode.java, encoding: UTF-8
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
 * Created on 3. Januar 2007, 17:37
 *
 */
package de.cismet.cids.abf.registry.messaging;

import de.cismet.cids.abf.registry.cookie.RMUserCookieImpl;
import Sirius.server.registry.rmplugin.util.RMUser;
import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;
import java.awt.Image;
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
public class MessagingUsergroupNode extends AbstractNode
{
    private final transient Image icon;

    public MessagingUsergroupNode(final String domain, final String usergroup,
            final RegistryProject regProject)
    {
        super(new MessagingUsergroupChildren(domain, usergroup, regProject));
        setDisplayName(usergroup);
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER
                + "group.png"); // NOI18N
        getCookieSet().add(new RMUserCookieImpl(getChildren()));
        getCookieSet().add(new RegistryProjectCookieImpl(regProject));
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
    public Action[] getActions(final boolean b)
    {
        return new Action[]
                {
                    CallableSystemAction.get(SendMessageAction.class),
                };
    }
}

class MessagingUsergroupChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            MessagingUsergroupChildren.class);

    private final transient String domain;
    private final transient String usergroup;
    private final transient RegistryProject registryProject;

    public MessagingUsergroupChildren(final String domain,
            final String usergroup, final RegistryProject registryProject)
    {
        this.domain = domain;
        this.usergroup = usergroup;
        this.registryProject = registryProject;
    }

    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof RMUser)
        {
            return new Node[]
                    {
                        new MessagingUserNode((RMUser)object, registryProject)
                    };
        }else
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("no RMUser"); // NOI18N
            }
            return new Node[] {};
        }
    }

    @Override
    protected void addNotify()
    {
        super.addNotify();
        try
        {
            setKeys(registryProject.getMessageForwarder().getAllActiveUsers(
                    usergroup, domain));
        }catch(final Exception ex)
        {
            LOG.error("could not set keys", ex); // NOI18N
        }
    }
}
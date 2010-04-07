/*
 * MessagingDomainNode.java, encoding: UTF-8
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
import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookieImpl;
import java.awt.Image;
import java.io.IOException;
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
public class MessagingDomainNode extends AbstractNode
{
    private final transient Image icon;

    public MessagingDomainNode(final String domain,
            final RegistryProject registryProject)
    {
        super(new MessagingDomainChildren(domain, registryProject));
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER
                + "domainserver.png"); // NOI18N
        setDisplayName(domain);
        getCookieSet().add(new RMUserCookieImpl(getChildren()));
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

    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[]
                {
                    CallableSystemAction.get(SendMessageAction.class),
                };
    }
}

class MessagingDomainChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            MessagingDomainChildren.class);

    private final transient RegistryProject registryProject;
    private final transient String domain;

    public MessagingDomainChildren(final String domain,
            final RegistryProject registryProject)
    {
        this.registryProject = registryProject;
        this.domain = domain;
    }

    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof String)
        {
            return new Node[]
                    {
                        new MessagingUsergroupNode(
                                domain, (String)object, registryProject)
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
                    .getAllActiveGroups(domain));
        }catch(final IOException ex)
        {
            LOG.error("could not set keys from registry", ex); // NOI18N
        }
    }
}
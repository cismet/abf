/*
 * UserManagement.java, encoding: UTF-8
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
 * Created on 24. Oktober 2006, 11:15
 *
 */

package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.users.AllUsersNode;
import de.cismet.cids.abf.domainserver.project.users.NewUserWizardAction;
import de.cismet.cids.abf.domainserver.project.users.UserManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.users.UserNode;
import de.cismet.cids.abf.domainserver.project.users.groups.NewUsergroupWizardAction;
import de.cismet.cids.abf.domainserver.project.users.groups.UserGroupNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.EventQueue;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.19
 */
public final class UserManagement extends ProjectNode implements 
        ConnectionListener,
        UserManagementContextCookie
{
    private final transient Image nodeImage;
    
    public UserManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        project.addConnectionListener(this);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "usermanagement.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                UserManagement.class, "Dsc_usermanagement")); // NOI18N
    }
    
    @Override
    public Image getIcon(final int i)
    {
        return nodeImage;
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return nodeImage;
    }
    
    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(project.isConnected())
        {
            setChildren(new UserGroupChildren());
        }else
        {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusIndeterminate()
    {
        // not needed
    }
    
    public void refreshChildren()
    {
        final UserGroupChildren ch = (UserGroupChildren)getChildren();
        ch.refreshAll();
        for(final Node node : ch.getNodes())
        {
            if(node instanceof Refreshable)
            {
                ((Refreshable)node).refresh();
            }
        }
    }
    
    public void refreshUser(final User user)
    {
        final UserGroupChildren ugch = (UserGroupChildren)getChildren();
        for(final Node node : ugch.getNodes())
        {
            final Children ch = node.getChildren();
            for(final Node n : ch.getNodes())
            {
                if(n instanceof UserNode)
                {
                    final UserNode un = (UserNode)n;
                    if(un.getUser().getId().equals(user.getId()))
                    {
                        un.refresh();
                    }
                }
            }
        }
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[] 
        {
            CallableSystemAction.get(NewUserWizardAction.class),
            CallableSystemAction.get(NewUsergroupWizardAction.class),
        };
    }
    
    private final class UserGroupChildren extends Children.Keys
    {
        @Override
        protected Node[] createNodes(final Object obj)
        {
            if(obj instanceof LoadingNode)
            {
                return new Node[] {(LoadingNode)obj};
            }else if(obj instanceof String)
            {
                return new Node[] {new AllUsersNode(project)};
            }else if(obj instanceof UserGroup)
            {
                return new Node[] {new UserGroupNode((UserGroup)obj, project)};
            }else
            {
                return new Node[] {};
            }
        }
        
        void refreshAll()
        {
            addNotify();
        }
        
        @Override
        protected void addNotify()
        {
            final LoadingNode loadingNode = new LoadingNode();
            setKeys(new Object[] {loadingNode});
            refresh();
            final Thread t = new Thread(new Runnable() 
            {
                @Override
                public void run()
                {
                    try
                    {
                        final List<UserGroup> ugs = project.
                                getCidsDataObjectBackend().getAllEntities(
                                UserGroup.class);
                        Collections.sort(ugs, new Comparators.UserGroups());
                        final List groupsAndMore = new ArrayList();
                        groupsAndMore.add(org.openide.util.NbBundle.getMessage(
                                UserManagement.class, "Dsc_allUsers"));// NOI18N
                        groupsAndMore.addAll(ugs);
                        EventQueue.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                setKeys(groupsAndMore);
                            }
                        });
                    }finally
                    {
                        if(loadingNode != null)
                        {
                            loadingNode.dispose();
                        }
                    }
                }
            }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
            t.start();
        }
    }
}
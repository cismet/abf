/*
 * UserGroupNode.java, encoding: UTF-8
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
 * Created on 24. Oktober 2006, 14:34
 *
 */

package de.cismet.cids.abf.domainserver.project.users.groups;


import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.users.NewUserWizardAction;
import de.cismet.cids.abf.domainserver.project.users.UserNode;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.17
 */
public final class UserGroupNode extends ProjectNode implements 
        UserGroupContextCookie,
        Refreshable
{
    private static final transient Logger LOG = Logger.getLogger(
            UserGroupNode.class);

    private final transient Image group;
    private final transient Image remotegroup;
    private transient UserGroup userGroup;
    
    public UserGroupNode(final UserGroup userGroup, final DomainserverProject 
            project)
    {
        super(new UserGroupNodeChildren(userGroup, project), project);
        this.userGroup = userGroup;
        group = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "group.png"); // NOI18N
        remotegroup = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "remotegroup.png"); // NOI18N
        getCookieSet().add(this);
    }
    
    @Override
    public String getDisplayName()
    {
        if(isRemote())
        {
            final Domain d = userGroup.getDomain();
            return userGroup.getName() + "@" + d.getName(); // NOI18N
        }else
        {
            return userGroup.getName();
        }
    }
    
    private boolean isRemote()
    {
        return ProjectUtils.isRemoteGroup(userGroup, project);
    }
    
    @Override
    public Image getIcon(final int i)
    {
        if(isRemote())
        {
            return remotegroup;
        }else
        {
            return group;
        }
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }
    
    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        try
        {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ID ">
            final Property idProp = new PropertySupport.Reflection(userGroup, 
                    Integer.class, "getId", null); // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    UserGroupNode.class, "UserGroupNode.createSheet().idProp")); // NOI18N
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
            final Property nameProp = new PropertySupport(
                    "name", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().nameProp.name"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().nameProp.nameOfUsergroup"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return getUserGroup().getName();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final UserGroup old = userGroup;
                    final String newName = (String)object;
                    try
                    {
                        userGroup.setName(newName);
                        userGroup = project.getCidsDataObjectBackend().store(
                                userGroup);
                        fireDisplayNameChange(old.getName(), newName);
                    }catch(final Exception e)
                    {
                        LOG.error("could not store usergroup", e); // NOI18N
                        ErrorManager.getDefault().notify(e);
                        userGroup = old;
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Domain ">
            final Property domainProp = new PropertySupport(
                    "domain", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().domainProp.domain"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().domainProp.domainOfUsergroup"), // NOI18N
                    true,
                    false)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return userGroup.getDomain();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    // not needed
                }
            };//</editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Description ">
            final Property descProp = new PropertySupport(
                    "description", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().descProp.description"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, 
                        "UserGroupNode.createSheet().descProp.descriptionOfUsergroup"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    return userGroup.getDescription();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final UserGroup old = userGroup;
                    try
                    {
                        userGroup.setDescription(object.toString());
                        userGroup = project.getCidsDataObjectBackend().store(
                                userGroup);
                    }catch(final Exception e)
                    {
                        LOG.error("could not set new description", e); // NOI18N
                        ErrorManager.getDefault().notify(e);
                        userGroup = old;
                    }
                }
            };//</editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: UserCount ">
            final Property countUser = new PropertySupport(
                    "usercount", // NOI18N
                    Integer.class,
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().countUser.usercount"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().countUser.usercountOfGroup"), // NOI18N
                    true,
                    false)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return userGroup.getUsers().size();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    // not needed
                }
            };//</editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: AdminCount ">
            final Property countAdmins = new PropertySupport(
                    "admincount", // NOI18N
                    Integer.class, 
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().countAdmins.admincount"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        UserGroupNode.class, "UserGroupNode.createSheet().countAdmins.admincountOfGroup"), // NOI18N
                    true,
                    false)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    int countA = 0;
                    final List<User> users = new ArrayList<User>(userGroup.
                            getUsers());
                    for(final User u : users)
                    {
                        if(u.isAdmin())
                        {
                            countA++;
                        }
                    }
                    return countA;
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    // not needed
                }
            };//</editor-fold>
            final Sheet.Set set = Sheet.createPropertiesSet();
            final Sheet.Set setAdditionalInfo = Sheet.createPropertiesSet();
            set.setDisplayName(org.openide.util.NbBundle.getMessage(
                    UserGroupNode.class, "UserGroupNode.createSheet().displayName")); // NOI18N
            setAdditionalInfo.setName("additionalInfo"); // NOI18N
            setAdditionalInfo.setDisplayName(org.openide.util.NbBundle
                    .getMessage(UserGroupNode.class, 
                    "UserGroupNode.createSheet().additionalInfo")); // NOI18N
            set.put(nameProp);
            set.put(domainProp);
            set.put(descProp);
            set.put(idProp);
            setAdditionalInfo.put(countUser);
            setAdditionalInfo.put(countAdmins);
            sheet.put(set);
            sheet.put(setAdditionalInfo);
        }catch(final Exception ex)
        {
            LOG.error("could not create property sheet", ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        }
        return sheet;
    }
    
    @Override
    public UserGroup getUserGroup()
    {
        return userGroup;
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        SystemAction newUser = null;
        if(!isRemote())
        {
            newUser = CallableSystemAction.get(NewUserWizardAction.class);
        }
        return new Action[] 
        {
            newUser, null,
            // CallableSystemAction.get(DeleteUserAction.class),
            // CallableSystemAction.get(NewUsergroupWizardAction.class),
            CallableSystemAction.get(DeleteUsergroupAction.class)
        };
    }

    @Override
    public void refresh()
    {
        userGroup = project.getCidsDataObjectBackend().getEntity(UserGroup.
                class, userGroup.getId());
        ((UserGroupNodeChildren)getChildren()).refreshAll(userGroup);
    }
}

final class UserGroupNodeChildren extends Children.Keys
{
    private final transient DomainserverProject project;
    private transient UserGroup userGroup;
    
    public UserGroupNodeChildren(final UserGroup userGroup, final 
            DomainserverProject project)
    {
        this.userGroup = userGroup;
        this.project = project;
    }

    @Override
    protected Node[] createNodes(final Object object)
    {
        return new Node[] {new UserNode((User)object, project)};
    }
    
    @Override
    protected void addNotify()
    {
        super.addNotify();
        final Set<User> users = userGroup.getUsers();
        final List<User> l = new ArrayList<User>(users);
        Collections.sort(l, new Comparators.Users());
        setKeys(l);
    }
    
    void refreshAll(final UserGroup ug)
    {
        if(ug != null)
        {
            userGroup = ug;
        }
        addNotify();
    }
}
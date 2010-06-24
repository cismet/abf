/*
 * AllUsersNode.java, encoding: UTF-8
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
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on ???
 *
 */

package de.cismet.cids.abf.domainserver.project.users;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.jpa.entity.user.User;
import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;


/**
 *
 * @author martin.scholl@cismet.de
 * @version 1.11
 */
public final class AllUsersNode extends ProjectNode implements Refreshable
{
    private final transient Image nodeImage;
    
    public AllUsersNode(final DomainserverProject project)
    {
        super(new AllUsersChildren(project), project);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "all_users.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                AllUsersNode.class, "AllUsersNode.displayName")); // NOI18N
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
    public Action[] getActions(final boolean b)
    {
        return new Action[]
        {
            CallableSystemAction.get(NewUserWizardAction.class)
        };
    }

    @Override
    public void refresh()
    {
        ((AllUsersChildren)getChildren()).refreshAll();
    }
}

final class AllUsersChildren extends Children.Keys
{
    private final transient DomainserverProject project;
    
    public AllUsersChildren(final DomainserverProject project)
    {
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
        final List<User> users = project.getCidsDataObjectBackend().
                getAllEntities(User.class);
        Collections.sort(users, new Comparators.Users());
        setKeys(users);
    }
    
    void refreshAll()
    {
        addNotify();
    }
}
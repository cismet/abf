/*
 * RemoveGroupMembershipAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.users.groups;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.UserContextCookie;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.EventQueue;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author martin.scholl@cismet.de
 * @version 1.7
 */
public final class RemoveGroupMembershipAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            RemoveGroupMembershipAction.class);

    @Override
    public String getName()
    {
        return NbBundle.getMessage(RemoveGroupMembershipAction.class, 
                "CTL_RemoveGroupMembershipAction"); // NOI18N
    }
    
    @Override
    protected String iconResource()
    {
        return DomainserverProject.IMAGE_FOLDER +
                "remove_user_from_group.png"; // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous()
    {
        return false;
    }

    @Override
    protected int mode()
    {
        return MODE_ALL;
    }

    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            DomainserverContext.class,
            UserContextCookie.class
        };
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final int answer = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(), 
                "Wollen Sie diese Benutzer wirklich entfernen ?",
                "Benutzer aus der Benutzergruppe entfernen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if(answer == JOptionPane.YES_OPTION)
        {
            final DomainserverProject project = nodes[0].getCookie(
                    DomainserverContext.class).getDomainserverProject();
            final Backend backend = project.getCidsDataObjectBackend();
            final UserManagement userManagement = project.getLookup().lookup(
                    UserManagement.class);
            for(final Node n : nodes)
            {
                final UserGroupNode ugn = n.getParentNode().getLookup().lookup(
                        UserGroupNode.class);
                final User usr = n.getCookie(UserContextCookie.class).getUser();
                if(ugn != null)
                {
                    final UserGroup ug = ugn.getUserGroup();
                    ug.getUsers().remove(usr);
                    try
                    {
                        backend.store(ug);
                    }catch(final RuntimeException e)
                    {
                        LOG.error("could not store usergroup '" + ug // NOI18N
                                + "' and hence user '" + usr // NOI18N
                                + "' was not removed", e); // NOI18N
                        // TODO: notify user
                    }
                    EventQueue.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run()
                        {
                            n.getCookie(Refreshable.class).refresh();
                            userManagement.refreshUser(usr);
                        }
                    });
                }else
                {
                    LOG.warn("the usergroup the user '" // NOI18N
                            + usr + "' was supposed to be in could not"// NOI18N
                            + " be found in lookup, nothing is done"); // NOI18N
                }
            }
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    userManagement.refreshChildren();
                }
            });
        }
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(!super.enable(nodes))
        {
            return false;
        }
        return nodes[0].getCookie(DomainserverContext.class)
                .getDomainserverProject().isConnected();
    }
}
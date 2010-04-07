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
 * Created on ???
 *
 */
package de.cismet.cids.abf.registry.messaging;

import Sirius.server.registry.rmplugin.exception.UnableToSendMessageException;
import de.cismet.cids.abf.registry.cookie.RMUserCookie;
import Sirius.server.registry.rmplugin.util.RMUser;
import de.cismet.cids.abf.registry.RegistryProject;
import de.cismet.cids.abf.registry.cookie.RegistryProjectCookie;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

public final class SendMessageAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            SendMessageAction.class);

    @Override
    public String getName()
    {
        return NbBundle.getMessage(SendMessageAction.class, 
                "CTL_SendMessageAction"); // NOI18N
    }

    @Override
    protected String iconResource()
    {
        return RegistryProject.IMAGE_FOLDER + "aim.png"; // NOI18N
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
    protected void performAction(final Node[] nodes)
    {
        final Map<RMUser, RegistryProject> map = 
                new Hashtable<RMUser, RegistryProject>();
        for(final Node node : nodes)
        {
            final Set<RMUser> users = 
                    node.getCookie(RMUserCookie.class).getRMUsers();
            final RegistryProject project = 
                    node.getCookie(RegistryProjectCookie.class).getProject();
            for(final RMUser user : users)
            {
                map.put(user, project);
            }
        }
        final String message = JOptionPane.showInputDialog(
                WindowManager.getDefault().getMainWindow(), 
                org.openide.util.NbBundle.getMessage(
                        SendMessageAction.class, 
                        "Dsc_whichMessageQuestion", 
                        map.size()));
        if(message != null && message.trim().length() > 0)
        {
            for(final Entry<RMUser, RegistryProject> entry : map.entrySet())
            {
                final RegistryProject project = entry.getValue();
                final RMUser user = entry.getKey();
                try
                {
                    project.getMessageForwarder().sendMessage(
                            user.getQualifiedName(),
                            user.getIpAddress(),
                            message,
                            org.openide.util.NbBundle.getMessage(
                                SendMessageAction.class, 
                                "Dsc_messageFromAdmin")); // NOI18N
                }catch(final RemoteException ex)
                {
                    LOG.error("could not send message", ex); // NOI18N
                }catch(final UnableToSendMessageException ex)
                {
                    LOG.error("could not send message", ex); // NOI18N
                }
            }
        }
    }

    @Override
    protected int mode()
    {
        return CookieAction.MODE_ALL;
    }

    @Override
    protected Class<?>[] cookieClasses()
    {
        return new Class[]
                {
                    RMUserCookie.class,
                    RegistryProjectCookie.class
                };
    }
}
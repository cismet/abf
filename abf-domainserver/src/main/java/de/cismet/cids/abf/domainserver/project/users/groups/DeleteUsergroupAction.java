/*
 * DeleteUsergroupAction.java, encoding: UTF-8
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
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.user.UserGroup;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

// TODO: why is a separate action used, destroy would probably be sufficient
/**
 *
 * @author martin.scholl@cismet.de
 */
public final class DeleteUsergroupAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            DeleteUsergroupAction.class);
    
    @Override
    protected void performAction(final Node[] nodes)
    {
        final int answer = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(
                    DeleteUsergroupAction.class, 
                    "Dsc_reallyDeleteUsergroupQuestion"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    DeleteUsergroupAction.class, 
                    "Dsc_deleteUsergroup"), // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if(answer == JOptionPane.YES_OPTION)
        {
            for(final Node n : nodes)
            {
                UserGroup ug = null;
                try
                {
                    final DomainserverProject project = n.getCookie(
                            DomainserverContext.class).getDomainserverProject();
                    final Backend backend = project.getCidsDataObjectBackend();
                    ug = n.getCookie(UserGroupContextCookie.class)
                            .getUserGroup();
                    backend.delete(ug);
                    project.getLookup().lookup(UserManagement.class)
                            .refreshChildren();
                }catch(final Exception e)
                {
                    LOG.error("could not delete usergroup: " + ug, e); // NOI18N
                }
            }
        }
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(DeleteUsergroupAction.class, 
                "CTL_DeleteUsergroupAction"); // NOI18N
    }
    
    @Override
    protected String iconResource()
    {
        return DomainserverProject.IMAGE_FOLDER + "delete_group.png"; // NOI18N
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
            UserGroupContextCookie.class
        };
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(!super.enable(nodes))
        {
            return false;
        }
        for(final Node n : nodes)
        {
            if(!n.getCookie(DomainserverContext.class).
                    getDomainserverProject().isConnected())
            {
                return false;
            }
        }
        return true;
    }
}
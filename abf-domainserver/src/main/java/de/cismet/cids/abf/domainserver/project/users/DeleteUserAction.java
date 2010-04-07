/*
 * DeleteUserAction.java, encoding: UTF-8
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


import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class DeleteUserAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            DeleteUserAction.class);
    
    @Override
    public String getName()
    {
        return NbBundle.getMessage(DeleteUserAction.class, 
                "CTL_DeleteUserAction"); // NOI18N
    }
    
    @Override
    protected String iconResource()
    {
        return DomainserverProject.IMAGE_FOLDER + "delete_user.png"; // NOI18N
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
    protected void performAction(final Node[] node)
    {
        final int answer = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(DeleteUserAction.class,
                    "Dsc_reallyDeleteUserQuestion"), // NOI18N
                org.openide.util.NbBundle.getMessage(DeleteUserAction.class, 
                    "Dsc_deleteUser"), // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if(answer == JOptionPane.YES_OPTION)
        {
            for(final Node n : node)
            {
                try
                {
                    n.destroy();
                }catch(final IOException ex)
                {
                    final String name = n.getCookie(UserContextCookie.class)
                            .getUser().getLoginname();
                    LOG.error("could not delete user: " + name); // NOI18N
                    ErrorManager.getDefault().notify(ex);
                }
            }
            final DomainserverProject p = node[0].getCookie(
                    DomainserverContext.class).getDomainserverProject();
            p.getLookup().lookup(UserManagement.class).refreshChildren();
        }
    }
}
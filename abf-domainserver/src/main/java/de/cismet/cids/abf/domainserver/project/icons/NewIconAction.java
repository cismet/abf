/*
 * NewIconAction.java, encoding: UTF-8
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
 * Created on 14. Januar 2008, 12:25
 */

package de.cismet.cids.abf.domainserver.project.icons;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.IconManagement;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author martin.scholl@cismet.de
 * @version 1.4
 */
public final class NewIconAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            NewIconAction.class);
    
    @Override
    protected int mode()
    {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            DomainserverContext.class,
            IconManagementContextCookie.class
        };
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final DomainserverProject project = ((DomainserverContext)nodes[0].
                getCookie(DomainserverContext.class)).getDomainserverProject();
        try
        {
            String icn = ""; // NOI18N
            while(icn != null)
            {
                icn = JOptionPane.showInputDialog(
                        WindowManager.getDefault().getMainWindow(),
                        org.openide.util.NbBundle.getMessage(
                            NewIconAction.class,
                            "NewIconAction.performAction(Node[]).JOptionPane.giveIconFilename.message")); // NOI18N
                if(icn == null)
                {
                    return;
                }
                // currently only names of length <= 32 are supported by db
                if(icn.trim().length() > 0 && icn.length() <= 32)
                {
                    final Icon icon = new Icon();
                    icon.setFileName(icn);
                    icon.setName(icn);
                    project.getCidsDataObjectBackend().store(icon);
                    project.getLookup().lookup(IconManagement.class)
                            .refreshChildren();
                    icn = null;
                }else
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                NewIconAction.class, 
                                "NewIconAction.performAction(Node[]).JOptionPane.givenFilenameInvalid.message"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                NewIconAction.class, "NewIconAction.performAction(Node[]).JOptionPane.givenFilenameInvalid.title"),//NOI18N
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }catch(final Exception e)
        {
            LOG.error("could not create new icon", e); // NOI18N
            ErrorManager.getDefault().notify(e); // NOI18N
        }
    }

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewIconAction.class, "NewIconAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        final boolean enable = super.enable(nodes);
        if(!enable)
        {
            return false;
        }
        final DomainserverContext dc = nodes[0].getCookie(DomainserverContext.
                class);
        if(dc == null)
        {
            LOG.warn("domainservercontext is null, " // NOI18N
                    + "cookieaction failed again"); // NOI18N
            return false;
        }
        return dc.getDomainserverProject().isConnected();
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }
}
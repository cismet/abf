/*
 * RefreshAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver;

import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class RefreshAction extends CookieAction
{
    @Override
    public String getName()
    {
        return NbBundle.getMessage(RefreshAction.class, 
                "CTL_RefreshAction"); // NOI18N
    }
    
    @Override
    protected void initialize()
    {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
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
            Refreshable.class
        };
    }
    
    @Override
    protected void performAction(final Node[] nodes)
    {
        for(final Node n : nodes)
        {
            n.getCookie(Refreshable.class).refresh();
        }
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        final boolean enable = super.enable(nodes);
        if(!enable)
        {
            return false;
        }
        for(final Node n : nodes)
        {
            if(n.getCookie(DomainserverContext.class) != null)
            {
                final DomainserverContext dc = (DomainserverContext)n.getCookie(
                        DomainserverContext.class);
                if(!dc.getDomainserverProject().isConnected())
                {
                    return false;
                }
            }
        }
        return true;
    }
}

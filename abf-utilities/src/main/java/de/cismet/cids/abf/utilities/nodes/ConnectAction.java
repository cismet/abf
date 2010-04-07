/*
 * ConnectAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.utilities.nodes;

import de.cismet.cids.abf.utilities.UtilityCommons;
import de.cismet.cids.abf.utilities.Connectable;
import de.cismet.cids.abf.utilities.ConnectionListener;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

public final class ConnectAction extends CookieAction
{
    @Override
    public String getName() 
    {
        final Connectable c = getConnectable();
        if(c != null && c.isConnected())
        {
            return org.openide.util.NbBundle.getMessage(
                    ConnectAction.class, "Dsc_disconnect"); // NOI18N
        }else
        {
            return org.openide.util.NbBundle.getMessage(
                    ConnectAction.class, "Dsc_connect"); // NOI18N
        }
    }
    
    @Override
    protected String iconResource() 
    {
        final Connectable c = getConnectable();
        if(c != null && c.isConnected())
        {
            return UtilityCommons.IMAGE_FOLDER
                    + "connect_established.png"; // NOI18N
        }else
        {
            return UtilityCommons.IMAGE_FOLDER + "connect_no.png"; // NOI18N
        }
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
    
    private void refreshUI() 
    {
        firePropertyChange(PROP_ICON, null, super.getIcon());
        firePropertyChange(SMALL_ICON, null, super.getIcon());
        firePropertyChange(NAME, null, getName());
    }
    
    private Connectable getConnectable()
    {
        final Node[] n = TopComponent.getRegistry().getActivatedNodes();
        if(n.length == 1 && n[0].getLookup().lookup(Connectable.class) != null) 
        {
            return n[0].getLookup().lookup(Connectable.class);
        }else
        {
            return null;
        }
    }

    @Override
    protected int mode()
    {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses()
    {
        return new Class[]
        {
            Connectable.class
        };
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final Connectable c = nodes[0].getLookup().lookup(Connectable.class);
        c.addConnectionListener(new ConnL(c));
        c.setConnected(!c.isConnected());
    }
    
    private final class ConnL implements ConnectionListener
    {
        private final transient Connectable c;
        
        ConnL(final Connectable c)
        {
            this.c = c;
        }
        
        @Override
        public void connectionStatusChanged(final boolean isConnected)
        {
            RequestProcessor.getDefault().post(new Runnable() 
            {
                @Override
                public void run()
                {
                    c.removeConnectionListener(ConnL.this); //TODO ConcurrentModificationException REFACTOR THIS ACTION !!!
                }
            }, 100);
            refreshUI();
        }

        @Override
        public void connectionStatusIndeterminate() 
        {
            // not needed
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
        return !nodes[0].getLookup().lookup(Connectable.class).
                isConnectionInProgress();
    }
}
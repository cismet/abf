/*
 * DomainserverProject.java, encoding: UTF-8
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
package de.cismet.cids.abf.domainserver.project.sync;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class SyncAction extends CallableSystemAction
{
    // TODO: refactor action
    @Override
    public void performAction()
    {
        TopComponent.getRegistry().getActivatedNodes()[0].getLookup().lookup(SyncManagement.class).executeStatements();
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(SyncAction.class, "SyncAction.getName().returnvalue");// NOI18N
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
    public boolean isEnabled()
    {
        if(!super.isEnabled())
            return false;

        final Node[] na = TopComponent.getRegistry().getActivatedNodes();
        if(na == null || na.length != 1)
            return false;

        final DomainserverProject project = na[0].getLookup().lookup(DomainserverProject.class);
        if(project == null || !project.isConnected())
            return false;

        return na[0].getLookup().lookup(SyncManagement.class) != null;
    }
}
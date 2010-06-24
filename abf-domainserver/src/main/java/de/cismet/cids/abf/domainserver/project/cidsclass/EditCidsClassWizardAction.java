/*
 * EditCidsClassWizardAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class EditCidsClassWizardAction extends NewCidsClassWizardAction
{
    @Override
    public String getName()
    {
        return NbBundle.getMessage(EditCidsClassWizardAction.class, 
                "EditCidsClassWizardAction.getName().returnvalue"); // NOI18N
    }
    
    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            DomainserverContext.class,
            CidsClassContextCookie.class
        };
    }

    @Override
    protected void performAction(final Node[] node)
    {
        final CidsClassContextCookie cccc = node[0].getCookie(
                CidsClassContextCookie.class);
        performAction(node[0], cccc.getCidsClass());
    }
}
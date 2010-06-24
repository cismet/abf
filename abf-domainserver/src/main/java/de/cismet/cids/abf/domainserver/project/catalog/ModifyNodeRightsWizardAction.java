/*
 * ModifyNodeRightsWizardAction.java, encoding: UTF-8
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
 * Created on 27. November 2007, 01:05
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class ModifyNodeRightsWizardAction extends NewCatalogNodeWizardAction
{
    @Override
    protected void performAction(final Node[] nodes)
    {
        final CatNode node = nodes[0].getCookie(CatalogNodeContextCookie.class).
                getCatNode();
        assert node != null;
        final WizardDescriptor.ArrayIterator it =
                new WizardDescriptor.ArrayIterator(getPanels());
        it.nextPanel();
        final WizardDescriptor wizard = new WizardDescriptor(it);
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                ModifyNodeRightsWizardAction.class, "ModifyNodeRightsWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        performAction(nodes, node, wizard);
    }

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                ModifyNodeRightsWizardAction.class, "ModifyNodeRightsWizardAction.getName().returnvalue");//NOI18N
    }
    
    @Override
    protected boolean customEnable(final Node[] nodes)
    {
        final CatalogNodeContextCookie cncc = nodes[0].getCookie(
                CatalogNodeContextCookie.class);
        if(cncc == null)
        {
            return false;
        }
        final CatNode node = cncc.getCatNode();
        if(node.getDerivePermFromClass() != null &&
                node.getDerivePermFromClass())
        {
            return false;
        }
        return nodes[0].getCookie(DomainserverContext.class).
                getDomainserverProject().isConnected();
    }
}
/*
 * TestQueryQizardAction.java, encoding: UTF-8
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
 * Created on 10. September 2007, 14:20
 */

package de.cismet.cids.abf.domainserver.project.query;

import de.cismet.cids.jpa.entity.query.Query;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class TestQueryWizardAction extends ModifyQueryWizardAction
{
    @Override
    protected void performAction(final Node[] nodes)
    {
        final Query query = nodes[0].getCookie(QueryContextCookie.class)
                .getQuery();
        if(query == null)
        {
            throw new IllegalStateException("query cannot be null"); // NOI18N
        }
        final WizardDescriptor.ArrayIterator it =
                new WizardDescriptor.ArrayIterator(getPanels());
        it.nextPanel();
        it.nextPanel();
        it.nextPanel();
        final WizardDescriptor wizard = new WizardDescriptor(it);
        wizard.putProperty(QUERY_PROPERTY, query);
        performAction(nodes, wizard);
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                TestQueryWizardAction.class, "TestQueryWizardAction.getName().returnvalue"); // NOI18N
    }
}
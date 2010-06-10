/*
 * NewQueryWizardAction.java, encoding: UTF-8
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
 * Created on 8. September 2007, 14:55
 *
 */

package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;
import org.openide.nodes.Node;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class NewQueryWizardAction extends QueryManipulationWizardAction
{
    private static final transient Logger LOG = Logger.getLogger(
            NewQueryWizardAction.class);

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewQueryWizardAction.class, "Dsc_newQuery"); // NOI18N
    }

    @Override
    protected void performAction(final Node[] node)
    {
        try
        {
            performAction(node, null);
        }catch(final Exception e)
        {
            LOG.error("error during perform action: " // NOI18N
                    + e.getMessage(), e);
        }
    }
}
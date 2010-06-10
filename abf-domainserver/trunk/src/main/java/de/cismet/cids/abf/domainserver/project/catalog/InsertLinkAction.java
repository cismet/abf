/*
 * UnlinkAction.java, encoding: UTF-8
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
 * Created on 31. Oktober 2007, 14:27
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class InsertLinkAction extends CookieAction
{
    @Override
    public String getName()
    {
        return NbBundle.getMessage(InsertLinkAction.class,
                "CTL_InsertLinkAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected int mode()
    {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses()
    {
        return new Class[]
        {
            DomainserverContext.class,
            CatalogNodeContextCookie.class
        };
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final DomainserverProject project = nodes[0].getCookie(
                DomainserverContext.class).getDomainserverProject();
        final CatalogNodeContextCookie[] cookies =
                project.getLinkableCatNodeCookies();
        for(final CatalogNodeContextCookie cncc : cookies)
        {
            project.getCidsDataObjectBackend().linkNode(cncc.getParent(), nodes[
                    0].getCookie(CatalogNodeContextCookie.class).getCatNode(),
                    cncc.getCatNode());
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
        final DomainserverProject project = nodes[0].getCookie(
                DomainserverContext.class).getDomainserverProject();
        final CatalogNodeContextCookie[] linkableNodes = project.
                getLinkableCatNodeCookies();
        if(linkableNodes == null || linkableNodes.length == 0)
        {
            return false;
        }
        final CatalogNodeContextCookie cookie = nodes[0].getCookie(
                CatalogNodeContextCookie.class);
        if(cookie == null)
        {
            return false;
        }
        final CatNode newParent = cookie.getCatNode();
        // one cannot add children to object nodes
        if(newParent.getNodeType().equals(CatNode.Type.OBJECT.getType()))
        {
            return false;
        }
        for(final CatalogNodeContextCookie cncc : linkableNodes)
        {
            final CatNode oldParent = cncc.getParent();
            // one cannot paste a node in itself
            if(cncc.getCatNode().equals(newParent))
            {
                return false;
            }
            // one cannot paste a node where it already is
            else if(oldParent != null && oldParent.equals(newParent))
            {
                return false;
            }
            // one cannot insert if this is dynamic node
            else if(newParent.getDynamicChildren() != null)
            {
                return false;
            }
            // one cannot insert dynamically created nodes 
            else if(oldParent != null && oldParent.getDynamicChildren() != null)
            {
                return false;
            }
        }
        return true;
    }
}
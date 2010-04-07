/*
 * ClientProjectNode.java, encoding: UTF-8
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
 * Created on 04.02.2010, 17:09:30
 *
 */
package de.cismet.cids.abf.client;

import java.awt.Image;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class ClientProjectNode extends FilterNode
{
    private final transient Image icon;

    public ClientProjectNode(final Node node, final ClientProject project)
    {
        super(node, new FilterNode.Children(node),
                //The projects system wants the project in the Node's lookup.
                //NewAction and friends want the original Node's lookup.
                //Make a merge of both
                new ProxyLookup(new Lookup[]
                {
                    Lookups.singleton(project),
                    node.getLookup()
                }));
        icon = ImageUtilities.loadImage(ClientProject.IMAGE_FOLDER
                + "client.png");
        setDisplayName(project.getProjectDirectory().getName()
                + " [cidsClient]"); // NOI18N
    }

    @Override
    public Image getIcon(final int type)
    {
        return icon;
    }

    @Override
    public Image getOpenedIcon(final int type)
    {
        return icon;
    }
}
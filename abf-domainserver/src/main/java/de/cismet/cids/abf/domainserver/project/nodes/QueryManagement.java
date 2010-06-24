/*
 * QueryManagement.java, encoding: UTF-8
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
 * Created on 30. August 2007, 10:40
 *
 */

package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.query.QueriesNode;
import java.awt.Image;
import java.util.Arrays;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class QueryManagement extends ProjectNode
{
    private final transient Image nodeImage;
    
    public QueryManagement(final DomainserverProject project)
    {
        super(new QueryManagementChildren(project), project);
        this.setName(org.openide.util.NbBundle.getMessage(
                QueryManagement.class, "QueryManagement.QueryManagement(DomainserverProject).name")); // NOI18N
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "search.png"); // NOI18N
    }

    @Override
    public Image getIcon(final int i)
    {
        return nodeImage;
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        return nodeImage;
    }
}

final class QueryManagementChildren extends Children.Array
{
    public QueryManagementChildren(final DomainserverProject project)
    {
        super(Arrays.asList(new Node[]
        {
            new QueriesNode(project)
        }));
    }
}
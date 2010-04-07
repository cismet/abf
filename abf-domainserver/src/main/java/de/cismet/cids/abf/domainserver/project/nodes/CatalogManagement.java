/*
 * CatalogManagement.java, encoding: UTF-8
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
 * Created on 21. Juni 2007, 16:20
 */

package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.catalog.CatalogNode;
import de.cismet.cids.abf.domainserver.project.catalog.ClassNodeManagement;
import de.cismet.cids.abf.domainserver.project.catalog.NavigatorNodeManagement;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import java.awt.Image;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;


/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.11
 */
public final class CatalogManagement extends ProjectNode implements 
        ConnectionListener
{

    private static final transient Logger LOG = Logger.getLogger(
            CatalogManagement.class);

    private final transient Image nodeImage;
    private final transient Map<CatNode, HashSet<CatalogNode>> openNodesCache;
    
    public CatalogManagement(final DomainserverProject project) 
    {
        super(new CatalogManagementChildren(project), project);
        openNodesCache = new HashMap<CatNode, HashSet<CatalogNode>>();
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "tree.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                CatalogManagement.class, "Dsc_catalog")); // NOI18N
        project.addConnectionListener(this);
    }
    
    public void addOpenNode(final CatNode node, final CatalogNode open)
    {
        if(node == null || node.getId() == null)
        {
            return;
        }
        synchronized(openNodesCache)
        {
            HashSet<CatalogNode> set = openNodesCache.get(node);
            if(set == null)
            {
                set = new HashSet<CatalogNode>(5);
            }
            set.add(open);
            openNodesCache.put(node, set);
        }
    }
    
    public void addedNode(final CatNode node)
    {
        if(node == null)
        {
            return;
        }
        final Set<CatalogNode> nodes = openNodesCache.get(node);
        if(nodes != null)
        {
            for(final CatalogNode cn : nodes)
            {
                cn.refresh();
            }
        }
    }
    
    public void removedNode(final CatNode node, final CatalogNode removed)
    {
        if(node == null || removed == null)
        {
            return;
        }
        synchronized(openNodesCache)
        {
            final Set<CatalogNode> nodes = openNodesCache.get(node);
            if(nodes != null)
            {
                nodes.remove(removed);
                if(nodes.isEmpty())
                {
                    openNodesCache.remove(node);
                }
            }
        }
        for(final Node n : removed.getChildren().getNodes())
        {
            if(n instanceof CatalogNode)
            {
                final CatalogNode cn = (CatalogNode)n;
                removedNode(cn.getCatNode(), cn);
            }else
            {
                LOG.warn("error during node removal, node was " // NOI18N
                        + "not instanceof CatalogNode: " + n); // NOI18N
            }
        }
    }
    
    public void destroyedNode(final CatNode node, final CatalogNode destroyed)
    {
        if(node == null || destroyed == null)
        {
            return;
        }
        final Set<CatalogNode> nodes;
        synchronized(openNodesCache)
        {
            nodes = openNodesCache.remove(node);
        }
        if(nodes != null)
        {
            for(final CatalogNode cn : nodes)
            {
                if(!cn.equals(destroyed))
                {
                    final Node parent = cn.getParentNode();
                    if(parent == null)
                    {
                        LOG.warn("parent node not present"); // NOI18N
                    }else
                    {
                        ((Refreshable)parent).refresh();
                    }
                }
                removedNode(node, cn);
            }
        }
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

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(!isConnected)
        {
            synchronized(openNodesCache)
            {
                openNodesCache.clear();
            }
        }
    }

    @Override
    public void connectionStatusIndeterminate()
    {
        // not needed
    }
}

final class CatalogManagementChildren extends Children.Keys 
{
    public CatalogManagementChildren(final DomainserverProject project) 
    {
        setKeys(new Object[]
        {
            new NavigatorNodeManagement(project),
            new ClassNodeManagement(project)
        });
    }

    @Override
    protected Node[] createNodes(final Object key) 
    {
        return new Node[] {(Node)key};
    }
}
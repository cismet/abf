/*
 * DomainserverLogicalView.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:25
 *
 */

package de.cismet.cids.abf.domainserver.project;

import de.cismet.cids.abf.domainserver.project.catalog.CatalogNodeContextCookie;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import org.apache.log4j.Logger;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.9
 */
public final class DomainserverLogicalView implements LogicalViewProvider
{
    private static final transient Logger LOG = Logger.getLogger(
            DomainserverLogicalView.class);
    
    private final transient DomainserverProject project;
    private transient int skippedCounter;
    
    public DomainserverLogicalView(final DomainserverProject project)
    {
        this.project = project;
        skippedCounter = 0;
    }
    
    @Override
    public Node findPath(final Node node, final Object object)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("searching for '" // NOI18N
                    + object
                    + "' from '" // NOI18N
                    + node
                    + "'"); // NOI18N
        }
        if(node == null || object == null)
        {
            return null;
        }
        if(object instanceof Class)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("object is instanceof class"); // NOI18N
            }
            for(final Node n : node.getChildren().getNodes())
            {
                if(n.getCookie((Class)object) != null)
                {
                    return n;
                }
                final Node ret = findPath(n, object);
                if(ret != null)
                {
                    skippedCounter = 0;
                    return ret;
                }
            }
        }else if(object instanceof Object[])
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("object is instanceof Object[]"); // NOI18N
            }
            final Object[] ccc = (Object[])object;
            if(ccc.length != 3)
            {
                throw new IllegalStateException("object array must " // NOI18N
                        + "have length 3"); // NOI18N
            }
            if(ccc[0] instanceof Class)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("ccc[0] is instanceof class"); // NOI18N
                }
                final Class cookie = (Class)ccc[0];
                for(final Node n : node.getChildren().getNodes())
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("processing node: " + n); // NOI18N
                    }
                    final Object o = n.getCookie(cookie);
                    if(o instanceof CatalogNodeContextCookie)
                    {
                        final CatNode cn = ((CatalogNodeContextCookie)o)
                                .getCatNode();
                        if(cn.getId().equals(((CatNode)ccc[1]).getId()))
                        {
                            if(skippedCounter == (Integer)ccc[2])
                            {
                                skippedCounter = 0;
                                return n;
                            }else
                            {
                                ++skippedCounter;
                            }
                        }
                        final Node ret = findPath(n, object);
                        if(ret != null)
                        {
                            skippedCounter = 0;
                            return ret;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public Node createLogicalView()
    {
        final DataFolder projDirDataObject = DataFolder.findFolder(
                project.getProjectDirectory());
        final Children children = projDirDataObject.createNodeChildren(new 
                DataFilter()
        {
            @Override
            public boolean acceptDataObject(final DataObject dataObject)
            {
                return "properties".equalsIgnoreCase( // NOI18N
                        dataObject.getPrimaryFile().getExt());
            }
        });
        final Node n = new AbstractNode(children);
        try
        {
            return new DomainserverProjectNode(n, project);
        }catch(final DataObjectNotFoundException donfe)
        {
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverLogicalView.class, 
                    "DomainserverLogicalView.createLogicalView().ErrorUtils.unknownErrorMessage"), // NOI18N
                    donfe);
            //Fallback - the directory couldn't be created -
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);
        }
    }
}
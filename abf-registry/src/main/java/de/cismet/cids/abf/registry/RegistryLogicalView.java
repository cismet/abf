/*
 * RegistryLogicalView.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:26
 *
 */

package de.cismet.cids.abf.registry;

import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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
 */
public class RegistryLogicalView implements LogicalViewProvider
{
    private final transient RegistryProject project;

    public RegistryLogicalView(final RegistryProject project)
    {
        this.project = project;
    }

    @Override
    public Node findPath(final Node node, final Object object)
    {
        return null;
    }

    @Override
    public Node createLogicalView()
    {
        final FileObject main = project.getProjectDirectory();
        //Get the DataObject that represents it
        final DataFolder projDir = DataFolder.findFolder(main);
        final Children children = projDir.createNodeChildren(new DataFilter()
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
            return new RegistryProjectNode(n, project);
        }catch(final DataObjectNotFoundException donfe)
        {
            ErrorManager.getDefault().notify(donfe);
            //Fallback - the directory couldn't be created -
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);
        }
    }
}
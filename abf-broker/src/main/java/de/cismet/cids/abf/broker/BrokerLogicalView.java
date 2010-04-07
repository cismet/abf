/*
 * BrokerLogicalView.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:23
 *
 */
package de.cismet.cids.abf.broker;

import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class BrokerLogicalView implements LogicalViewProvider
{
    private final transient BrokerProject project;

    public BrokerLogicalView(final BrokerProject project)
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
        //Get the DataObject that represents it
        final DataFolder projDirDataObject = DataFolder
                .findFolder(project.getProjectDirectory());
        final Children children = projDirDataObject.createNodeChildren(
                new DataFilter()
        {
            @Override
            public boolean acceptDataObject(final DataObject dataObject)
            {
                // TODO: shall this filter only accept properties files????
                final FileObject fo = dataObject.getPrimaryFile();
                final String name = fo.getName();
                //CVS Folder
                if(fo.isFolder()
                        && ("cvs".equalsIgnoreCase(name)
                                || BrokerProjectFactory.PROJECT_DIR
                                        .equalsIgnoreCase(name)))
                {
                    return false;
                }
                if("properties".equalsIgnoreCase(fo.getExt()))
                {
                    return true;
                }
                return false;
            }

        });
        return new BrokerProjectNode(new AbstractNode(children), project);
    }
}

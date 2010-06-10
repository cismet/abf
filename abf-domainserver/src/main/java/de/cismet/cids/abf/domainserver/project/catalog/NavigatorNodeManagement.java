/*
 * NavigatorNodeManagement.java, encoding: UTF-8
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
 * Created on 22. Juni 2007, 13:31
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import java.awt.EventQueue;
import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author martin.scholl@cismet.de
 * @version 1.12
 */
public final class NavigatorNodeManagement extends ProjectNode implements
        NavigatorNodeManagementContextCookie,
        ConnectionListener,
        Refreshable
{
    private static final transient Logger LOG = Logger.getLogger(
            NavigatorNodeManagement.class);
    
    private final transient Image nodeImage;
    
    public NavigatorNodeManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "tree.png");
        setDisplayName("Navigatorknoten");
        project.addConnectionListener(this);
        getCookieSet().add(this);
    }

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(isConnected)
        {
            setChildren(new NavigatorNodeManagementChildren());
        }else
        {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public void connectionStatusIndeterminate()
    {
        // not needed
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        return nodeImage;
    }

    @Override
    public Image getIcon(final int i)
    {
        return nodeImage;
    }

    @Override
    public void refresh()
    {
        if(project.isConnected())
        {
            ((NavigatorNodeManagementChildren)getChildren()).refreshAll();
        }else
        {
            setChildren(Children.LEAF);
        }
    }

    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[]
        {
            CallableSystemAction.get(NewCatalogNodeWizardAction.class), null,
            CallableSystemAction.get(RefreshAction.class)
        };
    }
    
    private final class NavigatorNodeManagementChildren extends Children.Keys
    {
        private final transient CatalogManagement catalogManagement;
        private transient LoadingNode loadingNode;

        public NavigatorNodeManagementChildren()
        {
            catalogManagement = project.getLookup()
                    .lookup(CatalogManagement.class);
        }

        @Override
        protected Node[] createNodes(final Object key)
        {
            if(key instanceof LoadingNode)
            {
                return new Node[] {(LoadingNode)key};
            }
            final CatNode catNode = (CatNode)key;
            final CatalogNode cn =
                    new CatalogNode(catNode, project, (Refreshable)getNode());
            catalogManagement.addOpenNode(catNode, cn);
            return new Node[] {cn};
        }

        void refreshAll()
        {
            addNotify();
        }
        
        @Override
        protected void addNotify()
        {
            loadingNode = new LoadingNode();
            setKeys(new Object[] {loadingNode});
            refresh();
            final Thread t = new Thread(new Runnable() 
            {
                @Override
                public void run()
                {
                    try
                    {
                        final List<CatNode> rootNodes = project.
                                getCidsDataObjectBackend().getRootNodes(CatNode.
                                Type.ORG);
                        Collections.sort(rootNodes, new Comparators.CatNodes());
                        EventQueue.invokeLater(new Runnable() 
                        {
                            @Override
                            public void run()
                            {
                                setKeys(rootNodes);
                            }
                        });
                    }catch(final Exception ex)
                    {
                        LOG.error("NavNodeManChildren: " // NOI18N
                                + "catnode creation failed", ex); // NOI18N
                        ErrorUtils.showErrorMessage("Es ist ein Fehler beim Laden der Navigatorknoten aus der Datenbank aufgetreten. Bitte melden Sie den Fehler, damit er schnellstmöglich behoben werden kann.\n\nVielen Dank", ex);
                    }finally
                    {
                        if(loadingNode != null)
                        {
                            loadingNode.dispose();
                            loadingNode = null;
                        }
                    }
                }
            }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
            t.start();
        }
    }
}
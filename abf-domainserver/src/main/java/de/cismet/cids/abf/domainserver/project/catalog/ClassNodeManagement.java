/*
 * ClassNodeManagement.java, encoding: UTF-8
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
 * @version 1.11
 */
public final class ClassNodeManagement extends ProjectNode implements
        ClassNodeManagementContextCookie,
        ConnectionListener,
        Refreshable
{
    private static final transient Logger LOG = Logger.getLogger(
            ClassNodeManagement.class);
    
    private final transient Image defaultImage;
    
    public ClassNodeManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        defaultImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "tutorials.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(ClassNodeManagement.class, "Dsc_classNodes"));
        project.addConnectionListener(this);
    }

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(isConnected)
        {
            setChildren(new ClassNodeManagementChildren());
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
        return defaultImage;
    }

    @Override
    public Image getIcon(final int i)
    {
        return defaultImage;
    }

    @Override
    public void refresh()
    {
        ((ClassNodeManagementChildren)getChildren()).refreshAll();
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
    
    private final class ClassNodeManagementChildren extends Children.Keys
    {
        private final transient CatalogManagement catalogManagement;

        public ClassNodeManagementChildren()
        {
            catalogManagement =
                project.getLookup().lookup(CatalogManagement.class);
        }

        @Override
        protected Node[] createNodes(final Object key)
        {
            final CatNode catNode = (CatNode)key;
            final CatalogNode cn = new CatalogNode(
                    catNode, project, (Refreshable)getNode());
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
            final Thread t = new Thread(new Runnable() 
            {
                @Override
                public void run()
                {
                    try
                    {
                        final List<CatNode> roots = project.
                                getCidsDataObjectBackend().getRootNodes(CatNode.
                                Type.CLASS);
                        Collections.sort(roots, new Comparators.CatNodes());
                        EventQueue.invokeLater(new Runnable() 
                        {
                            @Override
                            public void run()
                            {
                                setKeys(roots);
                            }
                        });
                    }catch(final Exception ex)
                    {
                        LOG.error("ClassNodeManChildren: " // NOI18N
                                + "catnode creation failed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    ClassNodeManagement.class, 
                                    "Err_loadingClassNodes"), ex); // NOI18N
                    }
                }
            }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
            t.start();
        }
    }
}
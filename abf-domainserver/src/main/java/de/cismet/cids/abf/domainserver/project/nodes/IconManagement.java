/*
 * ClassManagement.java, encoding: UTF-8
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
 * Created on 24. Oktober 2006, 11:26
 *
 */

package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.icons.IconManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.icons.IconNode;
import de.cismet.cids.abf.domainserver.project.icons.NewIconAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
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
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class IconManagement extends ProjectNode implements 
        ConnectionListener,
        IconManagementContextCookie
{
    private final transient Image image;
    
    public IconManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "icons.png"); // NOI18N
        project.addConnectionListener(this);
        getCookieSet().add(this);
        setDisplayName(org.openide.util.NbBundle.getMessage(IconManagement.class, "IconManagement.IconManagement(DomainserverProject).displayName"));
    }
    
    @Override
    public Image getIcon(final int i)
    {
        return image;
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return image;
    }

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(isConnected)
        {
            setChildren(new IconManagementChildren(project));
        }else
        {
            setChildren(Children.LEAF);
        }
    }
    
    @Override
    public void connectionStatusIndeterminate()
    {
        // do nothing
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[] 
        {
            CallableSystemAction.get(NewIconAction.class)
        };
    }
    
    public void refreshChildren()
    {
        final Children ch = getChildren();
        if(ch instanceof IconManagementChildren)
        {
            ((IconManagementChildren)ch).refreshAll();
        }
    }
}

final class IconManagementChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            IconManagementChildren.class);
    
    private final transient DomainserverProject project;
    private transient LoadingNode loadingNode;
    
    public IconManagementChildren(final DomainserverProject project)
    {
        this.project = project;
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof LoadingNode)
        {
            return new Node[] {(LoadingNode)object};
        }else if(object instanceof Icon)
        {
            return new Node[] {new IconNode((Icon)object, project)};
        }else
        {
            return new Node[] {};
        }
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
                    final List<Icon> icons = project.getCidsDataObjectBackend().
                            getAllEntities(Icon.class);
                    Collections.sort(icons, new Comparators.Icons());
                    EventQueue.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run()
                        {
                            setKeys(icons);
                        }
                    });
                }catch (final Exception ex)
                {
                    LOG.error("could not load icons", ex); // NOI18N
                    ErrorUtils.showErrorMessage(org.openide.util.NbBundle.
                            getMessage(IconManagementChildren.class, 
                            "IconManagement.addNotify().ErrorUtils.message"), ex); // NOI18N
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
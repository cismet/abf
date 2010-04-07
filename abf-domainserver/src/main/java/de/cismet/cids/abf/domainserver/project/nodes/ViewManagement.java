/*
 * ViewManagement.java, encoding: UTF-8
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
 * Created on 12. Februar 2007, 16:45
 *
 */

package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;
import de.cismet.cids.abf.domainserver.project.view.ViewNode;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import java.awt.EventQueue;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.3
 */
public final class ViewManagement extends ProjectNode implements
        ConnectionListener
{
    private final transient Image image;
    
    /** Creates a new instance of ViewManagement */
    public ViewManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        project.addConnectionListener(this);
        image = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "class_management.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(ViewManagement.class, "Dsc_topics"));
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
            setChildren(new ViewManagementChildren(project));
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
    
    public void refreshChildren()
    {
        final Children ch = getChildren();
        if(ch instanceof ViewManagementChildren)
        {
            ((ViewManagementChildren)ch).refreshAll();
        }
    }
}

final class ViewManagementChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            ViewManagementChildren.class);
    
    private final transient DomainserverProject project;
    
    public ViewManagementChildren(final DomainserverProject project)
    {
        this.project = project;
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        if (object instanceof ViewNode)
        {
            return new Node[] {(ViewNode)object};
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
        final Thread t = new Thread(new Runnable() 
        {
            @Override
            public void run()
            {
                try
                {
                    final List<ViewNode> all = new ArrayList<ViewNode>();
                    final SAXReader reader = new SAXReader();
                    project.getProjectDirectory().refresh();
                    for(final Enumeration<? extends FileObject> e = project.
                            getProjectDirectory().getData(false); e.
                            hasMoreElements();)
                    {
                        final FileObject fo = e.nextElement();
                        if(fo.getName().startsWith("csClassView.") // NOI18N
                                && fo.getExt().equalsIgnoreCase("xml"))// NOI18N
                        {
                            final Document d = reader.read(FileUtil.toFile(fo));
                            all.add(new ViewNode(d, project));
                        }
                    }
                    final Set openTCs = TopComponent.getRegistry().getOpened();
                    for(final Object o : openTCs)
                    {
                        if(o instanceof ClassDiagramTopComponent)
                        {
                            final Document d = ((ClassDiagramTopComponent)o).
                                    getViewDocument();
                            final ViewNode viewNode = new ViewNode(d, project);
                            if(!all.contains(viewNode))
                            {
                                viewNode.setHot(true);
                                viewNode.setTopComponent((
                                        ClassDiagramTopComponent)o);
                                all.add(viewNode);
                            }
                        }
                    }
                    Collections.sort(all);
                    EventQueue.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run()
                        {
                            setKeys(all);
                        }
                    });
                }catch(final Exception ex)
                {
                    LOG.error("could not create diagrams", ex); // NOI18N
                    ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                                ViewManagementChildren.class,
                                "Err_duringLoadingDiagrams"), // NOI18N
                            ex);
                }
            }
        }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
        t.start();
    }
}
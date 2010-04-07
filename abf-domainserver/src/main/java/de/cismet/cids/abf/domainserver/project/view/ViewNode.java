/*
 * ViewNode.java, encoding: UTF-8
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
 * Created on 20. Februar 2007, 17:27
 */

package de.cismet.cids.abf.domainserver.project.view;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;
import de.cismet.cids.abf.utilities.ConnectionListener;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.File;
import javax.swing.Action;
import org.dom4j.Document;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.4
 */
public final class ViewNode extends ProjectNode implements
        ConnectionListener,
        Comparable
{
    private final Image viewImage;
    private final Image unstoredViewImage;
    
    private Document view;
    private ClassDiagramTopComponent topComponent = null;
    
    //shows whether a node is only in memory(hot) or not(not hot) ;-)
    private boolean hot;
    
    /** Creates a new instance of ViewNode */
    public ViewNode(final Document view, final DomainserverProject project)
    {
        super(Children.LEAF, project);
        this.view = view;
        getCookieSet().add(new OpenCookie()
        {
            public void open()
            {
                if(topComponent == null)
                {
                    setTopComponent(ClassDiagramTopComponent.getDefault());
                    topComponent.setDomainserverProject(project);
                    topComponent.setViewNode(ViewNode.this);
                    topComponent.restoreFromDocument(ViewNode.this.view);
                    topComponent.open();
                }
                topComponent.requestActive();
            }
        });
        viewImage = Utilities.loadImage(
                "de/cismet/cids/abf/abfcore/res/images/view.png");
        unstoredViewImage = Utilities.loadImage(
                "de/cismet/cids/abf/abfcore/res/images/unstoredView.png");
        super.setName(view.getRootElement().valueOf("//View/@name"));
    }
    
    public void connectionStatusChanged(final boolean isConnected)
    {
    }
    
    public void connectionStatusIndeterminate()
    {
    }
    
    public Image getIcon(final int i)
    {
        if(hot)
            return unstoredViewImage;
        else
            return viewImage;
    }
    
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }
    
//    public String getName()
//    {
//        return view.getRootElement().valueOf("//View/@name");
//    }
    
    public void setName(final String string)
    {
        final String newName = ClassDiagramTopComponent.getFreeViewname(project,
                string);
        final String oldName = getName();
        final String oldFileName = ClassDiagramTopComponent.getFileNameOfView(
                oldName);
        final String newFileName = ClassDiagramTopComponent.getFileNameOfView(
                newName);
        final FileObject base = project.getProjectDirectory();
        final File old = FileUtil.toFile(base.getFileObject(oldFileName));
        old.renameTo(new File(FileUtil.toFile(base), newFileName));
        if(topComponent != null)
            topComponent.setViewName(newName, true);
        super.setName(newName);
        view.getRootElement().selectSingleNode("//View/@name").setText(newName);
    }
    
    public Action[] getActions(final boolean b)
    {
        return new Action[]
        {
            CallableSystemAction.get(OpenAction.class),
            CallableSystemAction.get(RenameAction.class),
            CallableSystemAction.get(DeleteAction.class)
        };
    }
    
    public boolean canRename()
    {
        return true;
    }
    
    public Action getPreferredAction()
    {
        return CallableSystemAction.get(OpenAction.class);
    }
    
    public Document getView()
    {
        return view;
    }
    
    public void setView(final Document view)
    {
        this.view = view;
    }
    
    public ClassDiagramTopComponent getTopComponent()
    {
        return topComponent;
    }
    
    public void setTopComponent(final ClassDiagramTopComponent topComponent)
    {
        this.topComponent = topComponent;
    }
    
    public boolean canDestroy()
    {
        return true;
    }
    
    public void destroy()
    {
        if(topComponent != null)
        {
            EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    topComponent.setSaveChanges(false);
                    topComponent.close();
                    //to ensure the file is deleted after the TC is closed
                    RequestProcessor.getDefault().post(new Runnable()
                    {
                        public void run()
                        {
                            deleteView();
                        }
                    });
                }
            });
        }
        else
        {
            deleteView();
        }
    }
    
    private void deleteView()
    {
        final File f = FileUtil.toFile(project.getProjectDirectory().
                getFileObject(ClassDiagramTopComponent.getFileNameOfView(
                ViewNode.this.getName())));
        f.delete();
        ((ViewManagement)project.getLookup().lookup(ViewManagement.class)).
                refreshChildren();
    }
    
    public boolean isHot()
    {
        return hot;
    }
    
    public void setHot(final boolean hot)
    {
        this.hot = hot;
    }
    
    public boolean equals(final Object object)
    {
        if(object instanceof ViewNode)
            return getName().equals(((ViewNode)object).getName());
        return false;
    }
    
    public int compareTo(final Object o)
    {
        if(o instanceof ViewNode)
            return getName().compareTo(((ViewNode)o).getName());
        return -1;
    }
}

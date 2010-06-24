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
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassManagementContextCookie;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassNode;
import de.cismet.cids.abf.domainserver.project.javaclass.NewJavaClassWizardAction;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
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
public final class JavaClassManagement extends ProjectNode implements 
        ConnectionListener,
        JavaClassManagementContextCookie
{
    private final transient Image nodeImage;
    
    public JavaClassManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        project.addConnectionListener(this);
        nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "java.png"); // NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(
                JavaClassManagement.class, "JavaClassManagement.JavaClassManagement(DomainserverProject).displayName")); // NOI18N
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
        if(project.isConnected())
        {
            setChildren(new JavaClassManagementChildren(project));
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
    public Action[] getActions(final boolean b)
    {
        return new Action[] 
        {
            CallableSystemAction.get(NewJavaClassWizardAction.class)
        };
    }
    
    public void refreshChildren()
    {
        final Children ch = getChildren();
        if(ch instanceof JavaClassManagementChildren)
        {
            ((JavaClassManagementChildren)ch).refreshAll();
        }
    }
}

final class JavaClassManagementChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            JavaClassManagementChildren.class);
    
    private final transient DomainserverProject project;
    
    public JavaClassManagementChildren(final DomainserverProject project)
    {
        this.project = project;
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof JavaClass)
        {
            return new Node[] {new JavaClassNode((JavaClass)object, project)};
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
                    final List<JavaClass> allClasses = project.
                            getCidsDataObjectBackend().getAllEntities(JavaClass.
                            class);
                    Collections.sort(allClasses, new Comparators.JavaClasses());
                    EventQueue.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run()
                        {
                            setKeys(allClasses);
                        }
                    });
                }catch(final Exception ex)
                {
                    LOG.error("could not load javaclasses", ex); // NOI18N
                    ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                                JavaClassManagement.class, 
                                "JavaClassManagement.addNotify().ErrorUtils.message"), ex); // NOI18N
                }
            }
        }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
        t.start();
    }
}
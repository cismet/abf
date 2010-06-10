/*
 * NodeRightPropertyWizardPanel1.java, encoding: UTF-8
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
 * Created on ???
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.permission.NodePermission;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public final class NodeRightPropertyWizardPanel1 implements
        WizardDescriptor.Panel,
        WizardDescriptor.FinishablePanel
{
    private final transient Set<ChangeListener> listeners;
    private transient NodeRightPropertyVisualPanel1 component;
    private transient CatNode catNode;
    private transient Backend backend;
    private transient List<NodePermission> perms;
    private transient DomainserverProject project;

    public NodeRightPropertyWizardPanel1()
    {
        listeners = new HashSet<ChangeListener>(1);
    }

    @Override
    public Component getComponent()
    {
        if (component == null)
        {    
            component = new NodeRightPropertyVisualPanel1(this);
            addChangeListener(component);
        }
        return component;
    }
    
    Backend getBackend()
    {
        return backend;
    }
    
    CatNode getCatNode()
    {
        return catNode;
    }
    
    List<NodePermission> getPermissions()
    {
        return perms;
    }

    DomainserverProject getProject()
    {
        return project;
    }

    @Override
    public HelpCtx getHelp()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l)
    {
        synchronized(listeners)
        {
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener(final ChangeListener l)
    {
        synchronized(listeners)
        {
            listeners.remove(l);
        }
    }
    
    protected void fireChangeEvent()
    {
        final Iterator<ChangeListener> it;
        synchronized (listeners)
        {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while(it.hasNext())
        {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void readSettings(final Object settings)
    {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        backend = (Backend)wizard.getProperty(NewCatalogNodeWizardAction.
                BACKEND_PROP);
        catNode = (CatNode)wizard.getProperty(NewCatalogNodeWizardAction.
                CATNODE_PROP);
        perms = (List<NodePermission>)wizard.getProperty(
                NewCatalogNodeWizardAction.PERM_PROP);
        project = (DomainserverProject)wizard.getProperty(
                NewCatalogNodeWizardAction.PROJECT_PROP);
        catNode.setProspectiveParent((CatNode)wizard.getProperty(
                NewCatalogNodeWizardAction.PARENT_PROP));
        fireChangeEvent();
    }

    @Override
    public void storeSettings(final Object settings)
    {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewCatalogNodeWizardAction.PERM_PROP, 
                component.getPermissions());
    }

    @Override
    public boolean isFinishPanel()
    {
        return true;
    }
}
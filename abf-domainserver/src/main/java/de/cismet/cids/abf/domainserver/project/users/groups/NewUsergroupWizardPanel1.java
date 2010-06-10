/*
 * NewUsergroupWizardPanel1.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.users.groups;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.NameValidator;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public final class NewUsergroupWizardPanel1 implements WizardDescriptor.Panel
{
    private final transient Set<ChangeListener> listeners;
    private final transient NameValidator validator;
    private transient NewUsergroupVisualPanel1 component;
    private transient WizardDescriptor wizard;
    private transient DomainserverProject project;
    private transient UserGroup userGroup;

    public NewUsergroupWizardPanel1()
    {
         listeners = new HashSet<ChangeListener>(1);
         validator = new NameValidator(NameValidator.NAME_MEDIUM_GERMAN);
    }

    @Override
    public Component getComponent()
    {
        if(component == null)
        {
            component = new NewUsergroupVisualPanel1(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean isValid()
    {
        final String name = component.getUserGroup().getName();
        if(name == null
                || name.trim().length() == 0
                || !validator.isValid(name))
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                    NewUsergroupWizardPanel1.class, 
                    "Dsc_nameOfUsergroupInvalid")); // NOI18N
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
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
    
    DomainserverProject getProject()
    {
        return project;
    }
    
    UserGroup getUserGroup()
    {
        return userGroup;
    }
   
    @Override
    public void readSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
        project = (DomainserverProject)wizard.getProperty(
                NewUsergroupWizardAction.PROJECT_PROP);
        userGroup = (UserGroup)wizard.getProperty(NewUsergroupWizardAction.
                USERGROUP_PROP);
        component.init();
        
    }

    @Override
    public void storeSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewUsergroupWizardAction.USERGROUP_PROP, component.
                getUserGroup());
    }
}
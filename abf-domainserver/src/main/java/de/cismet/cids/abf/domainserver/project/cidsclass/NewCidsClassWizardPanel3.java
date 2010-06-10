/*
 * NewCidsClassWizardPanel2.java, encoding: UTF-8
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
 * Created on ???
 *
 */

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class NewCidsClassWizardPanel3 implements WizardDescriptor.Panel
{
    private final transient Set<ChangeListener> listeners;
    private transient NewCidsClassVisualPanel3 component;
    private transient WizardDescriptor wizard;
    private transient DomainserverProject project;
    private transient CidsClass cidsClass;

    public NewCidsClassWizardPanel3()
    {
         listeners = new HashSet<ChangeListener>(1);
    }

    @Override
    public Component getComponent()
    {
        if(component == null)
        {
            component = new NewCidsClassVisualPanel3(this);
        }
        return component;
    }
    
    DomainserverProject getProject()
    {
        return project;
    }
    
    CidsClass getCidsClass()
    {
        return cidsClass;
    }
    
    @Override
    public HelpCtx getHelp()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean isValid()
    {
        final Set<ClassAttribute> cas = component.getCidsClass().
                getClassAttributes();
        final ArrayList<String> keys = new ArrayList<String>(cas.size());
        for(final ClassAttribute ca : cas)
        {
            if(ca.getAttrKey() == null || ca.getAttrKey().trim().equals(""))
            {
                wizard.putProperty("WizardPanel_errorMessage", "Leere Schlüss" +
                        "el sind nicht gültig");
                return false;
            }
            if(keys.contains(ca.getAttrKey()))
            {
                wizard.putProperty("WizardPanel_errorMessage", "Doppelte Schl" +
                        "üssel sind nicht gültig");
                return false;
            }else
                keys.add(ca.getAttrKey());
        }
        wizard.putProperty("WizardPanel_errorMessage", null);
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    @Override
    public final void addChangeListener(final ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.add(l);
        }
    }
    
    @Override
    public final void removeChangeListener(final ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent()
    {
        final Iterator<ChangeListener> it;
        synchronized (listeners)
        {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext())
        {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void readSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
        project = (DomainserverProject)wizard.getProperty(
                NewCidsClassWizardAction.PROJECT_PROP);
        cidsClass = (CidsClass)wizard.getProperty(
                NewCidsClassWizardAction.CIDS_CLASS_PROP);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewCidsClassWizardAction.CIDS_CLASS_PROP, component.
                getCidsClass());
    }
}
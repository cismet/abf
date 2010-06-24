/*
 * NewCidsClassWizardPanel1.java, encoding: UTF-8
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
import de.cismet.cids.abf.utilities.NameValidator;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
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
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class NewCidsClassWizardPanel1 implements WizardDescriptor.Panel
{
    // TODO: eventually medium validator must not be used because only highly
    //       "secure" names are provided
    private final transient Set<ChangeListener> listeners;
    private final transient NameValidator highValidator;
    private transient NewCidsClassVisualPanel1 component;
    private transient WizardDescriptor wizard;
    private transient DomainserverProject project;
    private transient CidsClass cidsClass;
    
    public NewCidsClassWizardPanel1()
    {
        listeners = new HashSet<ChangeListener>(1);
        highValidator = new NameValidator(NameValidator.NAME_HIGH);
    }
    
    @Override
    public Component getComponent()
    {
        if(component == null)
        {
            component = new NewCidsClassVisualPanel1(this);
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
        final ArrayList<String> names = new ArrayList<String>(10);
        final CidsClass cc = component.getCidsClass();
        final String ccName = cc.getName();
        final String ctName = cc.getTableName();
        if(ccName == null || "".equals(ccName.trim())) // NOI18N
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.nameNotValid")); // NOI18N
            return false;
        }
        if(!highValidator.isValid(ccName))
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.classNameInvalid")); // NOI18N
            return false;
        }
        if(ctName == null || "".equals(ctName.trim())) // NOI18N
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.tableW/ONameNotValid")); // NOI18N
            return false;
        }
        if(!highValidator.isValid(ctName))
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                        NewCidsClassWizardPanel1.class,
                        "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.tableNameNotValid")); // NOI18N
            return false;
        }
        for(final Attribute a : cc.getAttributes())
        {
            final String name = a.getFieldName();
            if(name == null || "".equals(name.trim())) // NOI18N
            {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            NewCidsClassWizardPanel1.class,
                            "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.attrW/OFieldnameInvalid")); // NOI18N
                return false;
            }
            if(!highValidator.isValid(name))
            {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            NewCidsClassWizardPanel1.class,
                            "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.attrFieldnameNotValid", name)); // NOI18N
                return false;
            }
            if(names.contains(name))
            {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                            NewCidsClassWizardPanel1.class,
                            "NewCidsClassWizardPanel1.isValid().wizard.PROP_ERROR_MESSAGE.duplAttrFieldNamesInvalid")); // NOI18N
                return false;
            }
            names.add(name);
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
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
        wizard.putProperty(NewCidsClassWizardAction.CIDS_CLASS_PROP,
                component.getCidsClass());
    }
}
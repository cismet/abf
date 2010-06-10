/*
 * NewUserWizardPanel2.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.users;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class NewUserWizardPanel2 implements WizardDescriptor.Panel
{
    private transient NewUserVisualPanel2 component;
    private transient DomainserverProject project;
    
    @Override
    public Component getComponent()
    {
        if(component == null)
        {
            component = new NewUserVisualPanel2(this);
        }
        return component;
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
        // not needed
    }

    @Override
    public void removeChangeListener(final ChangeListener l)
    {
        // not needed
    }

    @Override
    public void readSettings(final Object settings)
    {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        project = (DomainserverProject)wizard.getProperty(
                NewUserWizardAction.PROJECT_PROP);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings)
    {
        final WizardDescriptor wizard = (WizardDescriptor)settings;
        wizard.putProperty(NewUserWizardAction.USERGROUP_PROP, component.
                getUserGroups());
    }
}
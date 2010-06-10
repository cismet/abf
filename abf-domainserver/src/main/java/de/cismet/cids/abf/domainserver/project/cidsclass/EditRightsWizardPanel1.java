/*
 * EditRightsWizardPanel1.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class EditRightsWizardPanel1 implements WizardDescriptor.Panel
{
    private transient EditRightsVisualPanel1 component;
    private transient CidsClass[] classes;
    private transient Backend someBackend;

    @Override
    public Component getComponent()
    {
        if(component == null)
        {
            component = new EditRightsVisualPanel1(this);
        }
        return component;
    }

    CidsClass[] getCidsClasses()
    {
        return classes;
    }

    Backend getBackend()
    {
        return someBackend;
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
        classes = (CidsClass[])wizard.getProperty(EditRightsWizardAction.
                PROP_ARRAY_CIDSCLASSES);
        someBackend = (Backend)wizard.getProperty(EditRightsWizardAction.
                PROP_BACKEND);
        component.init();
    }

    @Override
    public void storeSettings(final Object settings)
    {
        // not needed
    }
}
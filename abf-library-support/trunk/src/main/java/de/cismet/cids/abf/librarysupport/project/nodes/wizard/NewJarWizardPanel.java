/*
 * NewJarWizardPanel.java, encoding: UTF-8
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

package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import de.cismet.cids.abf.utilities.NameValidator;
import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 *
 * @author mscholl
 * @version 1.2
 */
public final class NewJarWizardPanel implements WizardDescriptor.Panel
{
    private static  final transient Logger LOG = Logger.getLogger(
            NewJarWizardPanel.class);
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private transient NewJarVisualPanel visPanel;
    private transient WizardDescriptor wizard;
    private transient File srcDir;
    private final transient NameValidator validator;

    private final transient Set<ChangeListener> listeners;
    
    public NewJarWizardPanel()
    {
        this.validator = new NameValidator(NameValidator.NAME_MEDIUM);
        listeners = new HashSet<ChangeListener>(1);
    }
    
    // Get the visual component for thÏe panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() 
    {
        if (visPanel == null)
        {
            visPanel = new NewJarVisualPanel(this);
        }
        return visPanel;
    }
    
    @Override
    public HelpCtx getHelp() 
    {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    @Override
    public boolean isValid()
    {
        if(!validator.isValid(visPanel.getJarName()))
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                    NewJarWizardPanel.class, "Dsc_groupNameNotValid"));// NOI18N
            return false;
        }
        try
        {
            final File toCreate = new File(srcDir, visPanel.getJarName());
            if(toCreate.exists())
            {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                        NewJarWizardPanel.class, 
                        "Dsc_groupNameAlreadyExists")); // NOI18N
                return false;
            }
        } catch(final Exception e)
        {
            LOG.warn("could not check validity", e); // NOI18N
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                    NewJarWizardPanel.class, "Dsc_nameNotValid")); // NOI18N
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }
    
    @Override
    public void addChangeListener(final ChangeListener l) 
    {
        synchronized (listeners) 
        {
            listeners.add(l);
        }
    }
    
    @Override
    public void removeChangeListener(final ChangeListener l) 
    {
        synchronized (listeners) 
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
        while (it.hasNext())
        {
            it.next().stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(final Object settings)
    {
        wizard = (WizardDescriptor)settings;
        srcDir = FileUtil.toFile((FileObject)wizard.getProperty(
                NewJarWizardAction.PROP_SOURCE_DIR));
        visPanel.init();
    }
    
    @Override
    public void storeSettings(final Object settings)
    {
        ((WizardDescriptor)settings).putProperty(NewJarWizardAction.
                PROP_NEW_JAR_NAME, visPanel.getJarName());
    }
}
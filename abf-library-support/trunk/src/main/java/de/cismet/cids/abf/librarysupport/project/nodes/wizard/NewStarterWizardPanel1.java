/*
 * NewStarterWizardAction.java, encoding: UTF-8
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
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
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
 * @version 1.9
 */
public final class NewStarterWizardPanel1 implements WizardDescriptor.Panel
{
    private static final transient Logger LOG = Logger.getLogger(
            NewStarterWizardPanel1.class);
    
    public static final String NEW_STARTER_NAME_PROPERTY = 
            "newStarterName"; // NOI18N
    public static final String NEW_STARTER_BASE_FILE_PROPERTY = 
            "newStarterBaseFile"; // NOI18N
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private transient NewStarterVisualPanel1 visPanel;
    private transient WizardDescriptor wizard;
    private transient LibrarySupportProject project;
    private final transient NameValidator validator;
    private transient FileObject srcDir;

    private final transient Set<ChangeListener> listeners;
    
    public NewStarterWizardPanel1()
    {
        this.validator = new NameValidator(NameValidator.NAME_MEDIUM);
        listeners = new HashSet<ChangeListener>(1);
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() 
    {
        if (visPanel == null) 
        {
            visPanel = new NewStarterVisualPanel1(this);
        }
        return visPanel;
    }
    
    LibrarySupportProject getProject()
    {
        return project;
    }
    
    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    @Override
    public boolean isValid()
    {
        if(!validator.isValid(visPanel.getStarterName()))
        {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                    NewStarterWizardPanel1.class, 
                    "Dsc_starterNameInvalid")); // NOI18N
            return false;
        }
        try
        {
            final File srcFile = FileUtil.toFile(srcDir);
            final File toCreate = new File(srcFile, visPanel.getStarterName() + 
                    ".mf"); // NOI18N
            if(toCreate.exists())
            {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        org.openide.util.NbBundle.getMessage(
                        NewStarterWizardPanel1.class, 
                        "Dsc_fileAlreadyExists")); // NOI18N
                return false;
            }
        } catch(final Exception e)
        {
            LOG.warn("could not check validity", e); // NOI18N
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    org.openide.util.NbBundle.getMessage(
                    NewStarterWizardPanel1.class, "Dsc_nameNotValid"));// NOI18N
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
        project = (LibrarySupportProject)wizard.getProperty(
                NewStarterWizardAction.PROP_PROJECT);
        srcDir = (FileObject)wizard.getProperty(NewStarterWizardAction.
                PROP_SOURCE_DIR);
        visPanel.init();
    }
    
    @Override
    public void storeSettings(final Object settings)
    {
        ((WizardDescriptor)settings).putProperty(NewStarterWizardAction.
                PROP_NEW_STARTER_NAME, visPanel.getStarterName());
        ((WizardDescriptor)settings).putProperty(NewStarterWizardAction.
                PROP_NEW_STARTER_BASE_FILE, visPanel.getManifestPath());
    }
}
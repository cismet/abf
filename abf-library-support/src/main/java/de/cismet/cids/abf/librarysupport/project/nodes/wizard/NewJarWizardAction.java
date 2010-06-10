/*
 * NewJarWizardAction.java, encoding: UTF-8
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

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import java.awt.Component;
import java.awt.Dialog;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author mscholl
 * @version 1.2
 */
public class NewJarWizardAction extends NodeAction
{
    private static final transient Logger LOG = Logger.getLogger(
            NewJarWizardAction.class);
    
    public static final String PROP_SOURCE_DIR = "property_sourceDir"; // NOI18N
    public static final String PROP_NEW_JAR_NAME = 
            "property_newStarterName"; // NOI18N
    
    private transient WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    protected WizardDescriptor.Panel[] getPanels()
    {
        if (panels == null)
        {
            panels = new WizardDescriptor.Panel[] 
            {
                new NewJarWizardPanel()
            };
            final String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++)
            {
                final Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent)
                { // assume Swing components
                    final JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(
                            WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                            Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
                            steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(
                            WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                            Boolean.TRUE);
                    // Show steps on the left side with the image on the
                    // background
                    jc.putClientProperty(
                            WizardDescriptor.PROP_CONTENT_DISPLAYED,
                            Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED,
                            Boolean.TRUE);
                }
            }
        }
        return Arrays.copyOf(panels, panels.length);
    }
    
    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                NewJarWizardAction.class, "Dsc_addNewGroup"); // NOI18N
    }
    
    @Override
    public String iconResource()
    {
        return LibrarySupportProject.IMAGE_FOLDER + "jar_24.gif"; // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous()
    {
        return false;
    }
    
    @Override
    protected void performAction(final Node[] nodes)
    {
        final LibrarySupportContextCookie lscc = nodes[0].getCookie(
                LibrarySupportContextCookie.class);
        final SourceContextCookie scc = nodes[0].getCookie(SourceContextCookie.
                class);
        assert lscc != null;
        assert scc != null;
        final FileObject srcDir;
        try
        {
            srcDir = scc.getSourceObject();
        }catch(final FileNotFoundException fnfe)
        {
            LOG.error("could not obtain source dir", fnfe); // NOI18N
            return;
        }
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.putProperty(PROP_SOURCE_DIR, srcDir);
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                NewJarWizardAction.class, "Dsc_newGroup")); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.
                FINISH_OPTION;
        if (!cancelled)
        {
            final String jarName = wizard.getProperty(PROP_NEW_JAR_NAME).
                    toString();
            FileLock lock = null;
            try
            {
                lock = srcDir.lock();
                srcDir.createFolder(jarName);
            } catch(final IOException ioe)
            {
                LOG.error("could not create new folder", ioe);// NOI18N
                ErrorManager.getDefault().notify(ioe);
            } finally
            {
                if(lock != null && lock.isValid())
                {
                    lock.releaseLock();
                }
            }
        }
    }
    
    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(nodes == null || nodes.length != 1)
        {
            return false;
        }
        return nodes[0].getCookie(LocalManagementContextCookie.class) != null;
    }
}
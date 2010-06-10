/*
 * NewUsergroupWizardAction.java, encoding: UTF-8
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

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.users.UserManagementContextCookie;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class NewUsergroupWizardAction extends CookieAction 
{
    private static final transient Logger LOG = Logger.getLogger(
            NewUsergroupWizardAction.class);
    
    public static final String PROJECT_PROP = "projectProperty"; // NOI18N
    public static final String USERGROUP_PROP = "usergroupProperty"; // NOI18N
    
    private transient WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels()
    {
        if (panels == null)
        {
            panels = new WizardDescriptor.Panel[] 
            {
                new NewUsergroupWizardPanel1(),
                new NewUsergroupWizardPanel2()
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
                    // Show steps on the left side
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
        return "Neue Benutzergruppe";
    }
    
    @Override
    public String iconResource()
    {
        return DomainserverProject.IMAGE_FOLDER + "add_group.png";
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
    protected int mode()
    {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            UserManagementContextCookie.class,
            DomainserverContext.class
        };
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final DomainserverProject project = nodes[0]
                .getCookie(DomainserverContext.class).getDomainserverProject();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        //{0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizard.setTitle("Neue Benutzergruppe");
        wizard.putProperty(PROJECT_PROP, project);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled =
                wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if(!cancelled)
        {
            final UserGroup newGroup = (UserGroup)wizard.getProperty(
                    USERGROUP_PROP);
            try
            {
                project.getCidsDataObjectBackend().store(newGroup);
            }catch(final Exception e)
            {
                LOG.error("could not store new usergroup", e); // NOI18N
                ErrorManager.getDefault().notify(e);
            }
            project.getLookup().lookup(UserManagement.class).refreshChildren();
        }
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(!super.enable(nodes))
        {
            return false;
        }
        final DomainserverContext dc = nodes[0].getCookie(
                DomainserverContext.class);
        if(dc == null)
        {
            LOG.warn("domainservercontext is null"); // NOI18N
            return false;
        }
        return dc.getDomainserverProject().isConnected();
    }
}
/*
 * NewCidsClassWizardAction.java, encoding: UTF-8
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

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.2
 */
public class NewCidsClassWizardAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            NewCidsClassWizardAction.class);
    
    static final String PROJECT_PROP = "project_property"; // NOI18N
    static final String CIDS_CLASS_PROP = "cidsClass_property"; // NOI18N
    
    private transient WizardDescriptor.Panel[] panels;
    
    private WizardDescriptor.Panel[] getPanels()
    {
        if (panels == null)
        {
            panels = new WizardDescriptor.Panel[] 
            {
                new NewCidsClassWizardPanel1(),
                new NewCidsClassWizardPanel2(),
                new NewCidsClassWizardPanel3()
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
                {
                    // assume Swing components
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
        return org.openide.util.NbBundle.getMessage(NewCidsClassWizardAction.class, "NewCidsClassWizardAction.getName().returnvalue");
    }
    
    @Override
    public String iconResource()
    {
        return null;
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
            ClassManagementContextCookie.class,
            DomainserverContext.class
        };
    }

    @Override
    protected void performAction(final Node[] node)
    {
        performAction(node[0], null);
    }
    
    protected void performAction(final Node node, final CidsClass cidsClass)
    {
        final Type type;
        if(cidsClass == null)
        {
            type = new Type();
        }else
        {
            type = cidsClass.getType();
        }
        if(type == null)
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                NewCidsClassWizardAction.class,
                                "NewCidsClassWizardAction.performAction(Node,CidsClass).JOptionPane.message"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                NewCidsClassWizardAction.class,
                                "NewCidsClassWizardAction.performAction(Node,CidsClass).JOptionPane.title"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            return;
        }
        final DomainserverProject project = node
                .getCookie(DomainserverContext.class).getDomainserverProject();
        final Backend backend = project.getCidsDataObjectBackend();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        //{0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        // TODO: still suboptimal but better than before...
        if(cidsClass == null)
        {
            wizard.setTitle(org.openide.util.NbBundle.getMessage(
                    NewCidsClassWizardAction.class, 
                    "NewCidsClassWizardAction.performAction(Node,CidsClass).wizard.title")); // NOI18N
        }else
        {
            wizard.setTitle(org.openide.util.NbBundle.getMessage(
                    NewCidsClassWizardAction.class, "NewCidsClassWizardAction.performAction(Node,CidsClass).wizard.title.changeClass"));// NOI18N
        }
        wizard.putProperty(PROJECT_PROP, project);
        // use copy of cidsclass, not class itself to avoid changes to remain in
        // transient memory
        wizard.putProperty(CIDS_CLASS_PROP, (cidsClass == null) ? null :
                backend.getEntity(CidsClass.class, cidsClass.getId()));
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.
                FINISH_OPTION;
        if (!cancelled)
        {
            CidsClass newClass = (CidsClass)wizard.getProperty(
                    CIDS_CLASS_PROP);
            final String name = newClass.getName();
            try
            {
                newClass = backend.store(newClass);
                // this may only be the case if cidsclass != null so message of
                // npe is appropriate
                if(type == null)
                {
                    throw new IllegalStateException("type must exist!");//NOI18N
                }
                type.setComplexType(true);
                type.setName(name);
                type.setCidsClass(newClass);
                backend.store(type);
            }catch(final Exception e)
            {
                LOG.error("could not store cidsclass: " + name, e); // NOI18N
                ErrorManager.getDefault().notify(e);
            }
            if(cidsClass == null)
            {
                project.getLookup().lookup(ClassManagement.class).refresh();
            }else
            {
                node.getCookie(Refreshable.class).refresh();
            }
            project.getLookup().lookup(SyncManagement.class).refresh();
        }
    }

    @Override
    protected boolean enable(final Node[] node)
    {
        if(!super.enable(node))
        {
            return false;
        }
        final DomainserverContext dcc =
                node[0].getCookie(DomainserverContext.class);
        return dcc.getDomainserverProject().isConnected();
    }
}
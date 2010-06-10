/*
 * EditRightsWizardAction.java, encoding: UTF-8
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

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class EditRightsWizardAction extends CookieAction
{
    private static final transient Logger LOG = Logger.getLogger(
            EditRightsWizardAction.class);

    public static final String PROP_ARRAY_CIDSCLASSES =
            "__property_array_cidsclasses__"; // NOI18N
    public static final String PROP_BACKEND =
            "__property_backend__"; // NOI18N

    private WizardDescriptor.Panel[] getPanels()
    {
        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[]
                {
                    new EditRightsWizardPanel1()
                };
        final String[] steps = new String[panels.length];
        for(int i = 0; i < panels.length; i++)
        {
            final Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if(c instanceof JComponent)
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
        return panels;
    }

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                EditRightsWizardAction.class, "Dsc_assignRights"); // NOI18N
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
        return MODE_ALL;
    }

    @Override
    protected Class<?>[] cookieClasses()
    {
        return new Class[]
        {
            DomainserverContext.class,
            CidsClassContextCookie.class
        };
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final CidsClass[] classes = new CidsClass[nodes.length];
        for(int i = 0; i < nodes.length; ++i)
        {
            classes[i] = nodes[i].getCookie(CidsClassContextCookie.class).
                    getCidsClass();
        }
        final Backend backend = nodes[0].getCookie(DomainserverContext.class).
                getDomainserverProject().getCidsDataObjectBackend();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                EditRightsWizardAction.class,
                "Dsc_combinedRightsAssignment")); // NOI18N
        wizard.putProperty(PROP_ARRAY_CIDSCLASSES, classes);
        wizard.putProperty(PROP_BACKEND, backend);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.
                FINISH_OPTION;
        // NOTE: currently all changes are reflected directly into the class
        //       objects
        if(!cancelled)
        {
            final CidsClass[] success = new CidsClass[classes.length];
            try
            {
                for(int i = 0; i < classes.length; ++i)
                {
                    success[i] = backend.store(classes[i]);
                }
            }catch(final Exception e)
            {
                LOG.error("could not store classes", e); // NOI18N
                final StringBuffer successful = new StringBuffer();
                for(int i = 0; i < success.length && success[i] != null; ++i)
                {
                    successful.append("\n\t") // NOI18N
                            .append(success[i].toString());
                }
                ErrorUtils.showErrorMessage(
                        org.openide.util.NbBundle.getMessage(
                            EditRightsWizardAction.class, 
                            "Err_storeClassesFollowingSuccessful")
                            + successful.toString(),
                        org.openide.util.NbBundle.getMessage(
                            EditRightsWizardAction.class, 
                            "Err_duringStore"), // NOI18N
                        e);
            }
        }
        // TODO: copy relevant values and refresh only on store
        final Set<ClassManagement> alreadyDone = new HashSet(nodes.length);
        for(final Node node : nodes)
        {
            final ClassManagement current = node.getCookie(DomainserverContext.
                    class).getDomainserverProject().getLookup().lookup(
                    ClassManagement.class);
            if(!alreadyDone.contains(current))
            {
                current.refresh();
                alreadyDone.add(current);
            }
        }
    }
}
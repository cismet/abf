/*
 * RenameJarWizardAction.java, encoding: UTF-8
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

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import java.awt.Dialog;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mscholl
 * @version 1.1
 */
public final class RenameJarWizardAction extends NewJarWizardAction
{
    private static final transient Logger LOG = Logger.getLogger(
            RenameJarWizardAction.class);
    
    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(nodes == null || nodes.length != 1 || !super.enable(nodes))
        {
            return false;
        }
        return nodes[0].getCookie(SourceContextCookie.class) != null;
    }

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                RenameJarWizardAction.class, "RenameJarWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public String iconResource()
    {
        return null;
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
        wizard.putProperty(PROP_SOURCE_DIR, srcDir.getParent());
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                RenameJarWizardAction.class, "RenameJarWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.
                FINISH_OPTION;
        if (!cancelled)
        {
            nodes[0].setName(wizard.getProperty(PROP_NEW_JAR_NAME).toString());
        }
    }
}
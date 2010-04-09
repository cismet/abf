/*
 * DeployChangedJarsAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.librarysupport.project.nodes.actions;

import de.cismet.cids.abf.librarysupport.JarHandler;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.utilities.ModificationStore;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author mscholl
 * @version 1.3
 */
public final class DeployChangedJarsAction extends NodeAction
{
    private static final transient Logger LOG = Logger.getLogger(
            DeployChangedJarsAction.class);
    
    @Override
    public String getName()
    {
        return NbBundle.getMessage(DeployChangedJarsAction.class,
                "CTL_DeployChangedJarsAction"); // NOI18N
    }
    
    @Override
    protected String iconResource()
    {
        return LibrarySupportProject.IMAGE_FOLDER + "einpflegenChanged_24.png";//NOI18N
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous()
    {
        return true;
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        final List<DeployInformation> infos = new LinkedList<DeployInformation>(
                );
        for(final Node n : nodes)
        {
            for(final Node ch : n.getChildren().getNodes())
            {
                final DeployInformation info = DeployInformation.
                        getDeployInformation(ch);
                if(ModificationStore.getInstance().anyModifiedInContext(
                        FileUtil.toFile(info.getSourceDir()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED))
                {
                    infos.add(info);
                }
            }
        }
        try
        {
            JarHandler.deployAllJars(infos, JarHandler.
                    ANT_TARGET_DEPLOY_CHANGED_JARS);
            for(final DeployInformation info : infos)
            {
                ModificationStore.getInstance().removeAllModificationsInContext(
                        FileUtil.toFile(info.getSourceDir()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED);
            }
        }catch(final IOException ex)
        {
            LOG.warn("could not deploy changed jars", ex); // NOI18N
        }
    }
    
    @Override
    protected boolean enable(final Node[] nodes)
    {
        boolean enable = false;
        if(nodes == null || nodes.length < 1)
        {
            return false;
        }
        for(final Node n : nodes)
        {
            final LocalManagementContextCookie l = n.getCookie(
                    LocalManagementContextCookie.class);
            final StarterManagementContextCookie s = n.getCookie(
                    StarterManagementContextCookie.class);
            if(l == null && s == null)
            {
                return false;
            }
            for(final Node ch : n.getChildren().getNodes())
            {
                final DeployInformation info = DeployInformation.
                        getDeployInformation(ch);
                if(ModificationStore.getInstance().anyModifiedInContext(
                        FileUtil.toFile(info.getSourceDir()).getAbsolutePath(),
                        ModificationStore.MOD_CHANGED))
                {
                    enable = true;
                    break;
                }
            }
        }
        if(!enable)
        {
            return false;
        }
        final LibrarySupportContextCookie lscc = nodes[0].getCookie(
                LibrarySupportContextCookie.class);
        if(lscc == null)
        {
            return false;
        }
        final PropertyProvider provider = PropertyProvider.getInstance(lscc.
                getLibrarySupportContext().getProjectProperties());
        final File f = new File(provider.get(PropertyProvider.
                KEY_GENERAL_KEYSTORE));
        return f.exists() && f.canRead();
    }
}
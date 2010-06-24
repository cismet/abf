/*
 * DeployJarsAction.java, encoding: UTF-8
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
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LocalManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
import de.cismet.cids.abf.utilities.ModificationStore;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author mscholl
 * @version 1.7
 */
public final class DeployJarAction extends NodeAction
{
    private static final transient Logger LOG = Logger.getLogger(
            DeployJarAction.class);
    
    private transient boolean enableAction;
    
    @Override
    public String getName()
    {
        final String label = NbBundle.getMessage(DeployJarAction.class, 
                "DeployJarAction.getName().label"); // NOI18N
        if(enableAction)
        {
            return label;
        }else
        {    
            return label + org.openide.util.NbBundle.getMessage(
                    DeployJarAction.class, "DeployJarAction.getName().label.keystoreNotSet"); // NOI18N
        }
    }
    
    @Override
    protected String iconResource()
    {
        return LibrarySupportProject.IMAGE_FOLDER + "einpflegenSingle_24.gif";// NOI18N
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
    
    public void deploy(final Node n)
    {
        try
        {
            JarHandler.deployJar(DeployInformation.getDeployInformation(n));
            // remove all modifications that were created on basis of change of
            // this sourcefoldernode and all its children
            final SourceContextCookie sourceCookie = (SourceContextCookie)n.
                    getCookie(SourceContextCookie.class);
            ModificationStore.getInstance().removeAllModificationsInContext(
                    FileUtil.toFile(sourceCookie.getSourceObject()).
                    getAbsolutePath(), ModificationStore.MOD_CHANGED);
        }catch(final IOException ex)
        {
            LOG.error("could not deploy jar", ex); // NOI18N
            ErrorManager.getDefault().annotate(
                    ex, org.openide.util.NbBundle.getMessage(DeployJarAction
                    .class, "DeployJarAction.deploy().incorporateError")); // NOI18N
        }
    }

    @Override
    protected void performAction(final Node[] nodes)
    {
        for(int i = 0; i < nodes.length; i++)
        {
            deploy(nodes[i]);
        }
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(nodes == null || nodes.length < 1)
        {
            return false;
        }
        for(final Node n : nodes)
        {
            if(n.getCookie(LocalManagementContextCookie.class) != null 
                    || n.getCookie(StarterManagementContextCookie.class) != null
                    || n.getCookie(SourceContextCookie.class) == null)
            {
                return false;
            }
        }
        final LibrarySupportContextCookie lscc = (LibrarySupportContextCookie)
                nodes[0].getCookie(LibrarySupportContextCookie.class);
        if(lscc == null)
        {
            return false;
        }
        final PropertyProvider provider = PropertyProvider.getInstance(lscc.
                getLibrarySupportContext().getProjectProperties());
        final File f = new File(provider.get(PropertyProvider.
                KEY_GENERAL_KEYSTORE));
        enableAction = f.exists() && f.canRead();
        return enableAction;
    }
}
/*
 * RegistryProjectNode.java, encoding: UTF-8
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
 * Created on 4. Dezember 2006, 13:59
 *
 */
package de.cismet.cids.abf.registry;

import de.cismet.cids.abf.registry.messaging.MessagingNode;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.nodes.ConnectAction;
import java.awt.Image;
import java.text.MessageFormat;
import javax.swing.Action;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FileSystemAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class RegistryProjectNode extends FilterNode implements
        ConnectionListener
{
    private final transient String htmlTemplate;
    private final transient RegistryProject project;
    private final transient Image icon;

    public RegistryProjectNode(final Node node, final RegistryProject project)
            throws
            DataObjectNotFoundException
    {
        super(node, new Children(node), new ProxyLookup(new Lookup[]
                {
                    Lookups.singleton(project), node.getLookup()
                }));
        this.project = project;
        icon = ImageUtilities.loadImage(RegistryProject.IMAGE_FOLDER + "registry.png"); // NOI18N
        htmlTemplate = "<font color='!textText'>" // NOI18N
                + project.getProjectDirectory().getName()
                + "</font><font color='!controlShadow'> " // NOI18N
                + "[cidsRegistry] {0}</font>"; // NOI18N
        project.addConnectionListener(this);
        getChildren().add(new Node[] {new MessagingNode(project)});
    }

    @Override
    public Image getIcon(final int type)
    {
        return icon;
    }

    @Override
    public Image getOpenedIcon(final int type)
    {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName()
    {
        if(project.isConnectionInProgress())
        {
            if(project.isConnected())
            {
                return MessageFormat.format(htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                        RegistryProjectNode.class, "Dsc_disconnect")); // NOI18N
            }else
            {
                return MessageFormat.format(htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                        RegistryProjectNode.class, "Dsc_connect")); // NOI18N
            }
        }else
        {
            if(project.isConnected())
            {
                return MessageFormat.format(htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                        RegistryProjectNode.class, "Dsc_connected")); // NOI18N
            }else
            {
                return MessageFormat.format(htmlTemplate,
                        org.openide.util.NbBundle.getMessage(
                        RegistryProjectNode.class, "Dsc_disconnected"));//NOI18N
            }
        }
    }

    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[]
                {
                    CallableSystemAction.get(ConnectAction.class),
                    CommonProjectActions.customizeProjectAction(),
                    SystemAction.get(FileSystemAction.class), null,
                    CommonProjectActions.closeProjectAction()
                };
    }

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        fireDisplayNameChange(null, null);
    }

    @Override
    public void connectionStatusIndeterminate()
    {
        setDisplayName(project.getProjectDirectory().getName() + " ..."); // NOI18N
    }

    @Override
    public String getShortDescription()
    {
        return FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
    }
}
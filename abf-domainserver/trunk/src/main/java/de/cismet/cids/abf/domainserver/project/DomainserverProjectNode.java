/*
 * DomainserverProjectNode.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project;

import de.cismet.cids.abf.domainserver.ConnectAction;
import de.cismet.cids.abf.domainserver.DeleteLockAction;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.IconManagement;
import de.cismet.cids.abf.domainserver.project.nodes.JavaClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.QueryManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import de.cismet.cids.abf.domainserver.project.nodes.TypeManagement;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;
import de.cismet.cids.abf.utilities.ConnectionListener;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class DomainserverProjectNode extends FilterNode implements 
        ConnectionListener,
        DomainserverContext
{
    private static final transient Logger LOG = Logger.getLogger(DomainserverProjectNode.class);

    private static final String NODE_NAME_PATTERN =
            "<font color=''!textText''>{0}</font><font color=''!controlShadow''> [cidsDomainserver] {1}</font>"; // NOI18N

    private final transient DomainserverProject project;
    private final transient Image domainserverImage;

    
    public DomainserverProjectNode(final Node node, final DomainserverProject 
            project) throws 
            DataObjectNotFoundException
    {
        super(node, new Children(node), new ProxyLookup(new Lookup[] 
        {
            Lookups.singleton(project), node.getLookup() 
        }));
        this.project = project;
        project.setDomainserverProjectNode(this);
        project.addConnectionListener(this);
        final UserManagement userManagement = new UserManagement(project);
        final ViewManagement viewManagement = new ViewManagement(project);
        final ClassManagement classManagement = new ClassManagement(project);
        final TypeManagement typeManagement = new TypeManagement(project);
        final JavaClassManagement javaClassManagement = new JavaClassManagement(
                project);
        final IconManagement iconManagement = new IconManagement(project);
        final CatalogManagement catManagement = new CatalogManagement(project);
        final QueryManagement queryManagement = new QueryManagement(project);
        final SyncManagement syncManagement = new SyncManagement(project);
        project.addLookup(new ProxyLookup(new Lookup[]
        {
            getLookup(), Lookups.fixed(new Object[]
            {
                this,
                userManagement,
                viewManagement,
                classManagement,
                typeManagement,
                javaClassManagement,
                iconManagement,
                catManagement,
                queryManagement,
                syncManagement
            })
        }));
        getChildren().add(new Node[]
                {
                    userManagement,
                    classManagement,
                    viewManagement,
                    catManagement,
                    typeManagement,
                    javaClassManagement,
                    iconManagement,
                    queryManagement,
                    syncManagement
                });
        domainserverImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER
                + "domainserver.png"); // NOI18N
        setName(project.getProjectDirectory().getName());
        setShortDescription(FileUtil.toFile(project.getProjectDirectory()).
                getAbsolutePath());
    }
    
    @Override
    public Image getIcon(final int type)
    {
        return domainserverImage;
    }
    
    @Override
    public Image getOpenedIcon(final int type)
    {
        return domainserverImage;
    }
    
    @Override
    public String getHtmlDisplayName()
    {
        final String status;
        // TODO: maybe the status messages could be made better
        if(project.isConnectionInProgress())
        {
            if(project.isConnected())
            {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class, 
                        "Dsc_disconnect"); // NOI18N
            }else
            {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class, "Dsc_connect"); // NOI18N
            }
        }else
        {
            if(project.isConnected())
            {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class, "Dsc_connected");//NOI18N
            }else
            {
                status = org.openide.util.NbBundle.getMessage(
                        DomainserverProjectNode.class, 
                        "Dsc_disconnected");// NOI18N
            }
        }
        return MessageFormat.format(NODE_NAME_PATTERN, getName(), status);
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        final Action closeAction = new ProjectCloseHookAction(
                CommonProjectActions.closeProjectAction());
        final Action deleteLockAction;
        if(project.isConnected() || project.isConnectionInProgress())
        {
            deleteLockAction = null;
        }else
        {
            deleteLockAction = CallableSystemAction.get(DeleteLockAction.class);
        }
        return new Action[]
        {
            CallableSystemAction.get(ConnectAction.class), 
            deleteLockAction, null,
            closeAction, 
            CommonProjectActions.setAsMainProjectAction(), null,
            CommonProjectActions.customizeProjectAction(),
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
        fireDisplayNameChange(null, null);
    }

    @Override
    public DomainserverProject getDomainserverProject()
    {
        return project;
    }
    
    private final class ProjectCloseHookAction implements Action
    {
        private final transient Action delegate;
        
        public ProjectCloseHookAction(final Action delegate)
        {
            this.delegate = delegate;
        }
        
        @Override
        public Object getValue(final String arg0)
        {
            return delegate.getValue(arg0);
        }

        @Override
        public void putValue(final String arg0, final Object arg1)
        {
            delegate.putValue(arg0, arg1);
        }

        @Override
        public void setEnabled(final boolean arg0)
        {
            delegate.setEnabled(arg0);
        }

        @Override
        public boolean isEnabled()
        {
            return delegate.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p)
        {
            delegate.addPropertyChangeListener(p);
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p)
        {
            delegate.removePropertyChangeListener(p);
        }

        @Override
        public void actionPerformed(final ActionEvent arg0)
        {
            project.setConnected(false);
            delegate.actionPerformed(arg0);
        }
    }
}
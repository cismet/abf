/*
 * RegistryProject.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:26
 *
 */

package de.cismet.cids.abf.registry;

import Sirius.server.registry.rmplugin.interfaces.RMForwarder;
import de.cismet.cids.abf.utilities.Connectable;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.project.NotifyProperties;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class RegistryProject implements Project, Connectable
{
    private static final transient Logger LOG = Logger.getLogger(
            RegistryProject.class);

    public static final String RUNTIME_PROPS = "runtime.properties"; // NOI18N
    public static final String WEBINTERFACE_DIR = "webinterface"; //NOI18N
    public static final String IMAGE_FOLDER =
            "de/cismet/cids/abf/registry/images/"; // NOI18N

    private final transient FileObject projectDir;
    private final transient ProjectState state;
    private final transient LogicalViewProvider logicalView;
    private final transient Properties runtimeProps;
    private final transient Set<ConnectionListener> listeners;

    private transient Lookup lkp;
    private transient RMForwarder messageForwarder;
    private transient boolean connectionInProgress;
    private transient ProgressHandle handle;

    public RegistryProject(final FileObject projDir, final ProjectState state)
    {
        this.projectDir = projDir;
        this.state = state;
        final FileObject fob = projDir.getFileObject(RUNTIME_PROPS);
        runtimeProps = new Properties();
        try
        {
            runtimeProps.load(fob.getInputStream());
        }catch(final Exception e)
        {
            final String message = "cannot load runtime.properties"; // NOI18N
            LOG.error(message, e);
            throw new IllegalStateException(message, e);
        }
        logicalView = new RegistryLogicalView(this);
        listeners = new HashSet<ConnectionListener>(1);
    }

    @Override
    public FileObject getProjectDirectory()
    {
        return projectDir;
    }

    FileObject getWebinterfaceFolder(final boolean create)
    {
        FileObject result = projectDir.getFileObject(WEBINTERFACE_DIR);
        if(result == null && create)
        {
            try
            {
                result = projectDir.createFolder(WEBINTERFACE_DIR);
            }catch(final IOException ioe)
            {
                final String message = "cannot create web " // NOI18N
                        + "interface dir '" // NOI18N
                        + WEBINTERFACE_DIR
                        + "' in folder: " // NOI18N
                        + projectDir;
                LOG.error(message, ioe);
                throw new IllegalStateException(message, ioe);
            }
        }
        return result;
    }

    @Override
    public boolean isConnected()
    {
        return messageForwarder != null;
    }

    @Override
    public void setConnected(final boolean connected)
    {
        connectionInProgress = true;
        fireConnectionStatusIndeterminate();
        if(connected)
        {
            final Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        messageForwarder = (RMForwarder)Naming.lookup(
                                "rmi://" // NOI18N
                                + runtimeProps.getProperty("registryIP")//NOI18N
                                + ":1099/RMRegistryServer"); // NOI18N
                    }catch(final NotBoundException ex)
                    {
                        final String message = "rmi server not running";//NOI18N
                        LOG.error(message, ex);
                        throw new IllegalStateException(message, ex);
                    }catch(final MalformedURLException ex)
                    {
                        final String message = "server url not valid"; // NOI18N
                        LOG.error(message, ex);
                        throw new IllegalStateException(message, ex);
                    }catch(final RemoteException ex)
                    {
                        final String message = 
                                "could not connect to server"; // NOI18N
                        LOG.error(message, ex);
                        throw new IllegalStateException(message, ex);
                    }finally
                    {
                        connectionInProgress = false;
                        fireConnectionStatusChanged(isConnected());
                    }
                }
            };
            t.start();
        }else
        {
            messageForwarder = null;
            connectionInProgress = false;
            fireConnectionStatusChanged(isConnected());
        }
    }

    @Override
    public Lookup getLookup()
    {
        if(lkp == null)
        {
            lkp = Lookups.fixed(new Object[]
            {
                this, //project spec requires a project be in its own lookup
                state, //allow outside code to mark the project eg. need saving
                new ActionProviderImpl(), //Provides standard actions
                loadProperties(), //The project properties
                new Info(), //Project information implementation
                logicalView, //Logical view of project implementation
            });
        }
        return lkp;
    }

    private Properties loadProperties()
    {
        final FileObject fob = projectDir.getFileObject(
                RegistryProjectFactory.PROJECT_DIR
                + "/" // NOI18N
                + RegistryProjectFactory.PROJECT_PROPFILE);
        final Properties properties = new NotifyProperties(state);
        if(fob != null)
        {
            try
            {
                properties.load(fob.getInputStream());
            }catch(final IOException e)
            {
                final String message = "could not load project props"; // NOI18N
                LOG.error(message, e);
                throw new IllegalStateException(message, e);
            }
        }
        return properties;
    }

    @Override
    public void addConnectionListener(final ConnectionListener cl)
    {
        synchronized(listeners)
        {
            listeners.add(cl);
        }
    }

    @Override
    public void removeConnectionListener(final ConnectionListener cl)
    {
        synchronized(listeners)
        {
            listeners.remove(cl);
        }
    }

    protected void fireConnectionStatusChanged(final boolean newStatus)
    {
        handle.finish();
        final Iterator<ConnectionListener> it;
        synchronized(listeners)
        {
            it = new HashSet<ConnectionListener>(listeners).iterator();
        }
        final Runnable runner = new Runnable() 
        {
            @Override
            public void run()
            {
                it.next().connectionStatusChanged(newStatus);
            }
        };
        while(it.hasNext())
        {
            EventQueue.invokeLater(runner);
        }
    }

    protected void fireConnectionStatusIndeterminate()
    {
        if(isConnected())
        {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(RegistryProject.class,
                    "Dsc_disconnectFromRegistry", // NOI18N
                    runtimeProps.getProperty("registryIP"))); // NOI18N
        }else
        {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(RegistryProject.class,
                    "Dsc_connectToRegistry", // NOI18N
                    runtimeProps.getProperty("registryIP"))); // NOI18N
        }
        handle.start();
        handle.switchToIndeterminate();
        final Iterator<ConnectionListener> it;
        synchronized(listeners)
        {
            it = new HashSet<ConnectionListener>(listeners).iterator();
        }
        final Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                it.next().connectionStatusIndeterminate();
            }
        };
        while(it.hasNext())
        {
            EventQueue.invokeLater(runner);
        }
    }

    public RMForwarder getMessageForwarder()
    {
        return messageForwarder;
    }

    @Override
    public boolean isConnectionInProgress()
    {
        return connectionInProgress;
    }

    private final class ActionProviderImpl implements ActionProvider
    {
        @Override
        public String[] getSupportedActions()
        {
            return new String[0];
        }

        @Override
        public void invokeAction(final String string, final Lookup lookup)
                throws
                IllegalArgumentException
        {
            //do nothing
        }

        @Override
        public boolean isActionEnabled(final String string, final Lookup lookup)
                throws
                IllegalArgumentException
        {
            return false;
        }
    }

    /** Implementation of project system's ProjectInformation class */
    private final class Info implements ProjectInformation
    {
        private final transient ImageIcon icon;

        Info()
        {
            icon = new ImageIcon(ImageUtilities.loadImage(
                    IMAGE_FOLDER + "registry.png")); // NOI18N
        }

        @Override
        public Icon getIcon()
        {
            return icon;
        }

        @Override
        public String getName()
        {
            return projectDir.getName();
        }

        @Override
        public String getDisplayName()
        {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p)
        {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p)
        {
            //do nothing, won't change
        }

        @Override
        public Project getProject()
        {
            return RegistryProject.this;
        }

    }
}
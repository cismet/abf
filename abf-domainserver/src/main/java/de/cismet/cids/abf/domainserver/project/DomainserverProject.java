/*
 * DomainserverProject.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:25
 *
 */

package de.cismet.cids.abf.domainserver.project;

import de.cismet.cids.abf.domainserver.project.catalog.CatalogNodeContextCookie;
import de.cismet.cids.abf.utilities.Connectable;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.project.NotifyProperties;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.diff.DiffAccessor;
import de.cismet.diff.db.DatabaseConnection;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class DomainserverProject implements Project, Connectable
{
    private static final transient Logger LOG = Logger.getLogger(
            DomainserverProject.class);

    public static final String IMAGE_FOLDER =
            "de/cismet/cids/abf/domainserver/images/"; // NOI18N
    
    public static final String WEBINTERFACE_DIR = "webinterface"; //NOI18N
    public static final String RUNTIME_PROPS = "runtime.properties"; //NOI18N

    public static final String PROP_POLICY_SERVER = "serverPolicy"; // NOI18N
    public static final String PROP_POLICY_ATTR = "attributePolicy"; // NOI18N
    public static final String PROP_POLICY_CLASS_NODE = 
            "classNodePolicy"; // NOI18N
    public static final String PROP_POLICY_ORG_NODE = "pureNodePolicy";// NOI18N

    public static final String DEFAULT_POLICY_SERVER = "WIKI"; // NOI18N
    public static final String DEFAULT_POLICY_ATTR = "WIKI"; // NOI18N
    public static final String DEFAULT_POLICY_CLASS_NODE = "SECURE"; // NOI18N
    public static final String DEFAULT_POLICY_ORG_NODE = "SECURE"; // NOI18N
    
    public static final String LOCK_PREFIX = "ABF_EXCLUSIVE_LOCK_"; // NOI18N
    
    private static final String STMT_BEGIN = "BEGIN WORK"; // NOI18N
    private static final String STMT_COMMIT = "COMMIT WORK"; // NOI18N
    private static final String STMT_ROLLBACK = "ROLLBACK WORK"; // NOI18N
    private static final String STMT_LOCK_TABLE = 
            "LOCK TABLE cs_locks IN ACCESS EXCLUSIVE MODE"; // NOI18N
    public static final String STMT_READ_LOCKS = 
            "SELECT * FROM cs_locks WHERE " + // NOI18N
                    "class_id IS NULL AND " + // NOI18N
                    "object_id IS NULL AND " + // NOI18N
                    "user_string LIKE '" + LOCK_PREFIX + "%'"; // NOI18N
    private static final String STMT_ACQUIRE_LOCK =
            "INSERT INTO cs_locks (" + // NOI18N
                    "class_id, " + // NOI18N
                    "object_id, " + // NOI18N
                    "user_string, " + // NOI18N
                    "additional_info) values (" + // NOI18N
                    "null, " + // NOI18N
                    "null, " + // NOI18N
                    "''{0}'', " + // NOI18N
                    "''{1}'')"; // NOI18N
    private static final String STMT_RELEASE_LOCK = 
            "DELETE FROM cs_locks WHERE user_string = ''{0}''"; // NOI18N
    
    private transient String lockNonce;
    
    private final transient FileObject projectDir;
    private final transient LogicalViewProvider logicalView;
    private final transient ProjectState state;
    private final transient Set<ConnectionListener> listeners;
    private volatile transient boolean connectionInProgress;

    private transient Lookup lkp;
    private transient Properties runtimeProps;
    private transient Properties projectProps;
    private transient Backend backend;
    private transient DomainserverProjectNode domainserverProjectNode;
    private transient DiffAccessor diffAccessor;
    private transient Icon arrayIcon;
    private transient ProgressHandle handle;
    private transient CatalogNodeContextCookie[] catNodeCookies;
    private transient String serverPolicy;
    private transient String classNodePolicy;
    private transient String attrPolicy;
    private transient String orgNodePolicy;
    
    public DomainserverProject(final FileObject projectDir, final ProjectState 
            state)
    {
        this.projectDir = projectDir;
        this.state = state;
        logicalView = new DomainserverLogicalView(this);
        listeners = new HashSet<ConnectionListener>();
        connectionInProgress = false;
        final FileObject fob = getProjectDirectory().getFileObject(
                RUNTIME_PROPS);
        runtimeProps = new Properties();
        try
        {
            runtimeProps.load(fob.getInputStream());
        }catch(final Exception e)
        {
            LOG.error("could not load runtime properties"); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class, 
                    "Err_runtimePropsNotLoaded"), e); // NOI18N
        }
    }

    private void initPolicies()
    {
        if(runtimeProps == null)
        {
            LOG.warn("could not init policies due to missing" // NOI18N
                    + "runtime properties"); // NOI18N
            return;
        }
        serverPolicy = runtimeProps.getProperty(PROP_POLICY_SERVER);
        classNodePolicy = runtimeProps.getProperty(PROP_POLICY_CLASS_NODE);
        orgNodePolicy = runtimeProps.getProperty(PROP_POLICY_ORG_NODE);
        attrPolicy = runtimeProps.getProperty(PROP_POLICY_ATTR);
        if(serverPolicy == null || classNodePolicy == null ||
                orgNodePolicy == null || attrPolicy == null)
        {
            final StringBuffer sb = new StringBuffer("<ul>"); // NOI18N
            if(serverPolicy == null)
            {
                sb.append("<li>") // NOI18N
                        .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "Dsc_serverPolicy")) // NOI18N
                        .append("</li>"); // NOI18N
                runtimeProps.put(PROP_POLICY_SERVER, DEFAULT_POLICY_SERVER);
                serverPolicy = DEFAULT_POLICY_SERVER;
            }
            if(attrPolicy == null)
            {
                sb.append("<li>") // NOI18N
                        .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class, 
                            "Dsc_attributePolicy")) // NOI18N
                        .append("</li>"); // NOI18N
                runtimeProps.put(PROP_POLICY_ATTR, DEFAULT_POLICY_ATTR);
                attrPolicy = DEFAULT_POLICY_ATTR;
            }
            if(classNodePolicy == null)
            {
                sb.append("<li>") // NOI18N
                        .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class, 
                            "Dsc_classNodePolicy")) // NOI18N
                        .append("</li>"); // NOI18N
                runtimeProps.put(PROP_POLICY_CLASS_NODE,
                        DEFAULT_POLICY_CLASS_NODE);
                classNodePolicy = DEFAULT_POLICY_CLASS_NODE;
            }
            if(orgNodePolicy == null)
            {
                sb.append("<li>") // NOI18N
                        .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class, 
                            "Dsc_orgNodePolicy")) // NOI18N
                        .append("</li>"); // NOI18N
                runtimeProps.put(PROP_POLICY_ORG_NODE, DEFAULT_POLICY_ORG_NODE);
                orgNodePolicy = DEFAULT_POLICY_ORG_NODE;
            }
            sb.append("</ul>"); // NOI18N
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class, 
                        "Dsc_missingPropertiesQuestion", // NOI18N
                        sb.toString()),
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class, 
                        "Dsc_missingProperties"), // NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            storeRuntimeProperties();
        }
        // INFO: we cannot check for invalid policies at this point, because
        //       we are not connected to the backend yet.
    }

    @Override
    public FileObject getProjectDirectory()
    {
        return projectDir;
    }
    
    FileObject getWebinterfaceFolder()
    {
        FileObject result = projectDir.getFileObject(WEBINTERFACE_DIR);
        if(result == null)
        {
            try
            {
                result = projectDir.createFolder(WEBINTERFACE_DIR);
            }catch (final IOException ioe)
            {
                ErrorUtils.showErrorMessage(
                        org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class, 
                        "Err_unknownError"), ioe); // NOI18N
            }
        }
        return result;
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
                getProperties(), //The project properties
                new Info(), //Project information implementation
                logicalView, //Logical view of project implementation
            });
            return lkp;
        }
        return lkp;
    }
    
    void addLookup(final Lookup lookup)
    {
        // TODO: investigate: while why did we do that
        if(lookup == null)
        {
            return;
        }
        lkp = new ProxyLookup(new Lookup[] 
        {
            getLookup(), lookup
        });
    }
    
    private Properties getProperties()
    {
        if(projectProps == null)
        {
            final FileObject fob = projectDir.getFileObject(
                    DomainserverProjectFactory.PROJECT_DIR
                    + "/" // NOI18N
                    + DomainserverProjectFactory.PROJECT_PROPFILE);
            projectProps = new NotifyProperties(state);
            if(fob == null)
            {
                LOG.warn("project.properties not found"); // NOI18N
            }else
            {
                try
                {
                    projectProps.load(fob.getInputStream());
                }catch(final IOException ex)
                {
                    LOG.error("could not load project.properties", ex);// NOI18N
                    ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "Err_unknownError"), ex); // NOI18N
                }
            }
        }
        return projectProps;
    }

    // TODO: clean file write
    private void storeRuntimeProperties()
    {
        final FileObject fob = getProjectDirectory().getFileObject(
                RUNTIME_PROPS);
        if(fob != null)
        {
            FileLock lock = null;
            BufferedReader br = null;
            BufferedWriter bw = null;
            try
            {
                lock = fob.lock();
                br = new BufferedReader(new FileReader(FileUtil.toFile(fob)));
                final LinkedList<String> lines = new LinkedList<String>();
                String line = br.readLine();
                boolean inserted = false;
                while(line != null)
                {
                    if(!(line.startsWith(PROP_POLICY_ATTR) ||
                            line.startsWith(PROP_POLICY_CLASS_NODE) ||
                            line.startsWith(PROP_POLICY_ORG_NODE) ||
                            line.startsWith(PROP_POLICY_SERVER)))
                    {
                        if(line.contains("miniServer.properties") // NOI18N
                                && line.startsWith("##")) // NOI18N
                        {
                            lines.add(""); // NOI18N
                            lines.add("## policies"); // NOI18N
                            lines.add(PROP_POLICY_SERVER 
                                    + "=" // NOI18N
                                    + runtimeProps
                                    .getProperty(PROP_POLICY_SERVER));
                            lines.add(PROP_POLICY_ATTR
                                    + "=" // NOI18N
                                    + runtimeProps
                                    .getProperty(PROP_POLICY_ATTR));
                            lines.add(PROP_POLICY_CLASS_NODE 
                                    + "=" // NOI18N
                                    + runtimeProps
                                    .getProperty(PROP_POLICY_CLASS_NODE));
                            lines.add(PROP_POLICY_ORG_NODE 
                                    + "="  // NOI18N
                                    + runtimeProps
                                    .getProperty(PROP_POLICY_ORG_NODE));
                            lines.add(""); // NOI18N
                            inserted = true;
                        }
                        lines.addLast(line);
                    }
                    line = br.readLine();
                }
                if(!inserted)
                {
                    lines.add("## policies"); // NOI18N
                    lines.add(PROP_POLICY_SERVER 
                            + "=" // NOI18N
                            + runtimeProps.getProperty(PROP_POLICY_SERVER));
                    lines.add(PROP_POLICY_ATTR 
                            + "=" // NOI18N
                            + runtimeProps.getProperty(PROP_POLICY_ATTR));
                    lines.add(PROP_POLICY_CLASS_NODE 
                            + "=" // NOI18N
                            + runtimeProps.getProperty(PROP_POLICY_CLASS_NODE));
                    lines.add(PROP_POLICY_ORG_NODE 
                            + "=" // NOI18N
                            + runtimeProps.getProperty(PROP_POLICY_ORG_NODE));
                }
                bw = new BufferedWriter(new FileWriter(FileUtil.toFile(fob)));
                for(final String l : lines)
                {
                    bw.write(l);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            }catch (final Exception e)
            {
                ErrorUtils.showErrorMessage(org.openide.util.NbBundle
                        .getMessage(DomainserverProject.class, 
                        "Err_loadingRuntimeProps"), e); // NOI18N
            }finally
            {
                if(lock != null)// && lock.isValid())
                {
                    lock.releaseLock();
                }
                try
                {
                    if(br != null)
                    {
                        br.close();
                    }
                }catch(final IOException e)
                {
                    LOG.warn("could not close buffered reader", e); // NOI18N
                }
                try
                {
                    if(bw != null)
                    {
                        bw.close();
                    }
                }catch(final IOException e)
                {
                    LOG.warn("could not close buffered writer", e); // NOI18N
                }
            }
        }else
        {
            LOG.error("could not locate runtime.properties file"); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class, 
                    "Err_runtimePropsUnfindable"), null); // NOI18N
        }
    }
    
    @Override
    public boolean isConnected()
    {
        return backend != null;
    }
    
    @Override
    public void setConnected(final boolean connected)
    {
        /* we won't do anything if the backend has not been set and we shall not
         * connect (connected = false)
         */
        if(backend == null && !connected)
        {
            return;
        }
        connectionInProgress = true;
        fireConnectionStatusIndeterminate();
        if(connected)
        {
            final Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    initPolicies();
                    if(!acquireLock())
                    {
                        connectionInProgress = false;
                        fireConnectionStatusChanged(!connected);
                        return;
                    }
                    try
                    {
                        final FileObject fob = getProjectDirectory().
                                getFileObject(DomainserverProject.
                                RUNTIME_PROPS);
                        runtimeProps = new Properties();
                        try
                        {
                            runtimeProps.load(fob.getInputStream());
                        }catch(final Exception e)
                        {
                            ErrorUtils.showErrorMessage(
                                    org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class, 
                                    "Err_unknownError"), e); // NOI18N
                        }
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("new Backend(runtimeProps)"); // NOI18N
                        }
                        backend = new Backend(runtimeProps);
                        connectionInProgress = false;
                        diffAccessor = new DiffAccessor(runtimeProps,
                                backend);
                        fireConnectionStatusChanged(isConnected());
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("setConnected(" // NOI18N
                                    + connected
                                    + ") = " // NOI18N
                                    + isConnected());
                        }
                        final List<Icon> list = backend.getAllEntities(Icon.
                                class);
                        Icon backup = null;
                        for(final Icon ic : list)
                        {
                            if(backup == null)
                            {
                                backup = ic;
                            }
                            if(ic.getName().toLowerCase()
                                    .startsWith("array")) // NOI18N
                            {
                                setArrayIcon(ic);
                            }
                        }
                        if(getArrayIcon() == null)
                        {
                            setArrayIcon(backup);
                        }
                    }catch(final Exception e)
                    {
                        ErrorUtils.showErrorMessage(org.openide.util.NbBundle
                                .getMessage(DomainserverProject.class, 
                                "Err_connectToDB"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class, 
                                "Err_connectionError"), e); // NOI18N
                        fireConnectionStatusChanged(isConnected());
                        connectionInProgress = false;
                    }
                }
            };
            t.start();
        }else
        {
            releaseLock();
            connectionInProgress = false;
            try
            {
                if(backend != null)
                {
                    backend.close();
                }
            }catch(final Exception e)
            {
                LOG.error("could not close backend", e); // NOI18N
                ErrorUtils.showErrorMessage(org.openide.util.NbBundle
                        .getMessage(DomainserverProject.class,
                        "Err_closeDBConnection"), e); // NOI18N
            }
            diffAccessor.freeResources();
            diffAccessor = null;
            backend = null;
            // free resources
            System.gc();
            fireConnectionStatusChanged(isConnected());
            if(LOG.isDebugEnabled())
            {
                LOG.debug("setConnected(" // NOI18N
                        + connected
                        + ") = " // NOI18N
                        + isConnected());
            }
        }
    }
    
    private synchronized boolean acquireLock()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("trying to aquire abf lock"); // NOI18N
        }
        final FileObject fob = getProjectDirectory().getFileObject(
                DomainserverProject.RUNTIME_PROPS);
        runtimeProps = new Properties();
        try
        {
            runtimeProps.load(fob.getInputStream());
        }catch(final Exception e)
        {
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class, 
                    "Err_runtimePropsReload"), e); // NOI18N
            return false;
        }
        Connection con = null;
        try
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("receiving database connection"); // NOI18N
            }
            con = DatabaseConnection.getConnection(runtimeProps);
        }catch(final SQLException sqle)
        {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not acquire connection to database", sqle);//NOI18N
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class, 
                                "Err_noDBConnectionPossibleHTML"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class, 
                                "Err_dbUnreachable"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
            });// </editor-fold>
            return false;
        }
        if(con == null)
        {
            throw new IllegalStateException(
                    "connection must not be null"); // NOI18N
        }
        Statement stmt = null;
        try
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("locking cs_locks"); // NOI18N
            }
            stmt = con.createStatement();
            stmt.execute(STMT_BEGIN);
            stmt.execute(STMT_LOCK_TABLE);
        }catch(final SQLException sqle)
        {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not acquire lock on cs_locks", sqle); // NOI18N
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class, 
                                "Err_dbUnlockableHTML"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class, 
                                "Err_tableUnlockable"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            try
            {
                if(stmt != null)
                {
                    stmt.execute(STMT_ROLLBACK);
                }
            }catch(final SQLException ex)
            {
                LOG.warn("could not rollback statements", ex); // NOI18N
            }finally
            {
                try
                {
                    if(stmt != null)
                    {
                        stmt.close();
                    }
                    con.close();
                }catch(final SQLException e)
                {
                    LOG.warn("could not close connection", e); // NOI18N
                }
            }// </editor-fold>
            return false;
        }
        if(stmt == null)
        {
            throw new IllegalStateException(
                    "statement must not be null"); // NOI18N
        }
        ResultSet set = null;
        try
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("reading abf locks"); // NOI18N
            }
            set = stmt.executeQuery(STMT_READ_LOCKS);
            // if set.next() delivers a row a lock is already acquired
            if(set.next())
            {
                final String when = set.getString("user_string") // NOI18N
                        .replace(LOCK_PREFIX, ""); // NOI18N
                final Date date = new Date(Long.valueOf(when));
                final String who = set.getString("additional_info"); // NOI18N
                if(LOG.isInfoEnabled())
                {
                    LOG.info("lock aquired by " + who + " on " + date);// NOI18N
                }
                EventQueue.invokeLater(new Runnable() 
                {
                    @Override
                    public void run()
                    {
                        JOptionPane.showMessageDialog(
                                WindowManager.getDefault().getMainWindow(),
                                org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class, 
                                    "Dsc_alreadyLocked", // NOI18N
                                    who,
                                    date.toString()),
                                org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class, 
                                    "Dsc_lockPresent"), // NOI18N
                                JOptionPane.WARNING_MESSAGE);
                    }
                });
                stmt.execute(STMT_COMMIT);
                return false;
            }else
            {
                final String user = System.getProperty("user.name"); // NOI18N
                String host = "unknown"; // NOI18N
                try
                {
                    host = InetAddress.getLocalHost().getHostName();
                } catch (final UnknownHostException e)
                {
                    LOG.warn("could not resolve host name", e); // NOI18N
                }
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("writing lock"); // NOI18N
                }
                final String who = user + "@" + host; // NOI18N
                final String update = MessageFormat.format(STMT_ACQUIRE_LOCK, 
                        generateNonce(), who);
                stmt.executeUpdate(update);
                stmt.execute(STMT_COMMIT);
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("successfully locked"); // NOI18N
                }
                return true;
            }
        }catch(final SQLException sqle)
        {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not acquire lock", sqle); // NOI18N
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "Dsc_unlockableHTML"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "Dsc_lockNotSet"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            try
            {
                stmt.execute(STMT_ROLLBACK);
            }catch(final SQLException ex)
            {
                LOG.warn("could not rollback statements", ex); // NOI18N
            }// </editor-fold>
            return false;
        }finally
        {
            // <editor-fold defaultstate="collapsed" desc=" Cleanup ">
            try
            {
                if(set != null)
                {
                    set.close();
                }
                stmt.close();
                con.close();
            }catch(final SQLException sqle)
            {
                LOG.warn("could not cleanup connection", sqle); // NOI18N
            }// </editor-fold>
        }
    }
    
    private String generateNonce()
    {
        lockNonce = LOCK_PREFIX + System.currentTimeMillis();
        return lockNonce;
    }
    
    private synchronized void releaseLock()
    {
        if(lockNonce == null)
        {
            return;
        }
        Connection con = null;
        Statement stmt = null;
        try
        {
            final String update = MessageFormat.format(
                    STMT_RELEASE_LOCK, lockNonce);
            con = DatabaseConnection.getConnection(runtimeProps);
            stmt = con.createStatement();
            stmt.execute(STMT_BEGIN);
            stmt.execute(STMT_LOCK_TABLE);
            stmt.executeUpdate(update);
            stmt.execute(STMT_COMMIT);
        }catch(final Exception ex)
        {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not release lock", ex); // NOI18N
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "Dsc_lockNotDeletableHTML", // NOI18N
                                lockNonce),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "Dsc_errorDuringLockDeletion"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            try
            {
                if(stmt != null)
                {
                    stmt.execute(STMT_ROLLBACK);
                }
            }catch(final SQLException e)
            {
                LOG.warn("could not rollback statements", e); // NOI18N
            }// </editor-fold>
        }finally
        {
            // <editor-fold defaultstate="collapsed" desc=" Cleanup ">
            try
            {
                if(stmt != null)
                {
                    stmt.close();
                }
                if(con != null)
                {
                    con.close();
                }
            }catch(final SQLException ex)
            {
                LOG.error("could not cleanup connection", ex); // NOI18N
            }// </editor-fold>
            lockNonce = null;
        }   
    }
    
    public Properties getRuntimeProps()
    {
        return runtimeProps;
    }

    public String getAttrPolicy()
    {
        return attrPolicy;
    }

    public String getClassNodePolicy()
    {
        return classNodePolicy;
    }

    public String getOrgNodePolicy()
    {
        return orgNodePolicy;
    }

    public String getServerPolicy()
    {
        return serverPolicy;
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
    
    public Backend getCidsDataObjectBackend()
    {
        return backend;
    }
    
    @Override
    public boolean isConnectionInProgress()
    {
        return connectionInProgress;
    }
    
    // TODO: refactor and delete
    public DomainserverProjectNode getDomainserverProjectNode()
    {
        return domainserverProjectNode;
    }

    public void setDomainserverProjectNode(final DomainserverProjectNode node)
    {
        this.domainserverProjectNode = node;
    }

    public DiffAccessor getDiffAccessor()
    {
        return diffAccessor;
    }
    
    public void setDiffAccessor(final DiffAccessor diffAccessor)
    {
        this.diffAccessor = diffAccessor;
    }
    
    public Icon getArrayIcon()
    {
        return arrayIcon;
    }
    
    public void setArrayIcon(final Icon arrayIc)
    {
        this.arrayIcon = arrayIc;
    }

    public void setLinkableCatNodeCookies(final CatalogNodeContextCookie[] cncc)
    {
        this.catNodeCookies = Arrays.copyOf(cncc, cncc.length);
    }

    public CatalogNodeContextCookie[] getLinkableCatNodeCookies()
    {
        return Arrays.copyOf(catNodeCookies, catNodeCookies.length);
    }

    protected void fireConnectionStatusChanged(final boolean newStatus)
    {
        handle.finish();
        final Iterator<ConnectionListener> it;
        synchronized(listeners)
        {
            it = new HashSet<ConnectionListener>(listeners).iterator();
        }
        while(it.hasNext())
        {
            it.next().connectionStatusChanged(newStatus);
        }
    }
    
    protected void fireConnectionStatusIndeterminate()
    {
        if(isConnected())
        {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "Dsc_disconnectFromDomainserver", // NOI18N
                        getProjectDirectory().getName()));
        }else
        {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "Dsc_connectToDomainserver", // NOI18N
                        getProjectDirectory().getName()));
        }
        handle.start();
        handle.switchToIndeterminate();
        final Iterator<ConnectionListener> it;
        synchronized(listeners)
        {
            it = new HashSet<ConnectionListener>(listeners).iterator();
        }
        while(it.hasNext())
        {
            it.next().connectionStatusIndeterminate();
        }
    }
    
    private final class ActionProviderImpl implements ActionProvider
    {
        @Override
        public String[] getSupportedActions()
        {
            return new String[] 
            { 
                ActionProvider.COMMAND_BUILD,
                ActionProvider.COMMAND_CLEAN, 
                ActionProvider.COMMAND_COMPILE_SINGLE 
            };
        }
        
        @Override
        public void invokeAction(final String s, final Lookup l) throws 
                IllegalArgumentException
        {
            //do nothing
        }
        
        @Override
        public boolean isActionEnabled(final String s, final Lookup l) throws 
                IllegalArgumentException
        {
            return true;
        }
    }
    
    /** Implementation of project system's ProjectInformation class */
    private final class Info implements ProjectInformation
    {
        private final transient javax.swing.Icon icon;

        Info()
        {
             icon = new ImageIcon(ImageUtilities.loadImage(IMAGE_FOLDER
                    + "domainserver.png")); // NOI18N
        }

        @Override
        public javax.swing.Icon getIcon()
        {
            return icon;
        }
        
        @Override
        public String getName()
        {
            return getProjectDirectory().getName();
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
            return DomainserverProject.this;
        }
    }
}
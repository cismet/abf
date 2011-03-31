/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.catalog.CatalogNodeContextCookie;
import de.cismet.cids.abf.utilities.Connectable;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.ConnectionSupport;
import de.cismet.cids.abf.utilities.project.NotifyProperties;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.Icon;

import de.cismet.diff.DiffAccessor;

import de.cismet.diff.db.DatabaseConnection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DomainserverProject implements Project, Connectable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DomainserverProject.class);

    public static final String IMAGE_FOLDER = "de/cismet/cids/abf/domainserver/images/"; // NOI18N

    public static final String WEBINTERFACE_DIR = "webinterface";    // NOI18N
    public static final String RUNTIME_PROPS = "runtime.properties"; // NOI18N

    public static final String PROP_POLICY_SERVER = "serverPolicy";        // NOI18N
    public static final String PROP_POLICY_ATTR = "attributePolicy";       // NOI18N
    public static final String PROP_POLICY_CLASS_NODE = "classNodePolicy"; // NOI18N
    public static final String PROP_POLICY_ORG_NODE = "pureNodePolicy";    // NOI18N

    public static final String DEFAULT_POLICY_SERVER = "WIKI";       // NOI18N
    public static final String DEFAULT_POLICY_ATTR = "WIKI";         // NOI18N
    public static final String DEFAULT_POLICY_CLASS_NODE = "SECURE"; // NOI18N
    public static final String DEFAULT_POLICY_ORG_NODE = "SECURE";   // NOI18N

    public static final String LOCK_PREFIX = "ABF_EXCLUSIVE_LOCK_"; // NOI18N

    private static final String STMT_BEGIN = "BEGIN WORK";                                              // NOI18N
    private static final String STMT_COMMIT = "COMMIT WORK";                                            // NOI18N
    private static final String STMT_ROLLBACK = "ROLLBACK WORK";                                        // NOI18N
    private static final String STMT_LOCK_TABLE = "LOCK TABLE cs_locks IN ACCESS EXCLUSIVE MODE";       // NOI18N
    public static final String STMT_READ_LOCKS = "SELECT * FROM cs_locks WHERE "                        // NOI18N
                + "class_id IS NULL AND "                                                               // NOI18N
                + "object_id IS NULL AND "                                                              // NOI18N
                + "user_string LIKE '" + LOCK_PREFIX + "%'";                                            // NOI18N
    private static final String STMT_ACQUIRE_LOCK = "INSERT INTO cs_locks ("                            // NOI18N
                + "class_id, "                                                                          // NOI18N
                + "object_id, "                                                                         // NOI18N
                + "user_string, "                                                                       // NOI18N
                + "additional_info) values ("                                                           // NOI18N
                + "null, "                                                                              // NOI18N
                + "null, "                                                                              // NOI18N
                + "''{0}'', "                                                                           // NOI18N
                + "''{1}'')";                                                                           // NOI18N
    private static final String STMT_RELEASE_LOCK = "DELETE FROM cs_locks WHERE user_string = ''{0}''"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient String lockNonce;

    private final transient FileObject projectDir;
    private final transient LogicalViewProvider logicalView;
    private final transient ProjectState state;
    private final transient ConnectionSupport connectionSupport;
    private transient volatile boolean connectionInProgress;

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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainserverProject object.
     *
     * @param  projectDir  DOCUMENT ME!
     * @param  state       DOCUMENT ME!
     */
    public DomainserverProject(final FileObject projectDir, final ProjectState state) {
        this.projectDir = projectDir;
        this.state = state;
        logicalView = new DomainserverLogicalView(this);
        connectionSupport = new ConnectionSupport();
        connectionInProgress = false;
        final FileObject fob = getProjectDirectory().getFileObject(
                RUNTIME_PROPS);
        fob.addFileChangeListener(new FileChangeListener() {

                @Override
                public void fileFolderCreated(final FileEvent fe) {
                    // do nothing
                }

                @Override
                public void fileDataCreated(final FileEvent fe) {
                    // do nothing
                }

                @Override
                public void fileChanged(final FileEvent fe) {
                    runtimeProps = new Properties();
                    try {
                        runtimeProps.load(fob.getInputStream());
                        initPolicies();
                    } catch (final Exception e) {
                        LOG.error("could not load runtime properties");
                        ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.fileChanged(FileEvent).FileChangeListener.ErrorUtils.runtimePropsNotLoadedError"),
                            e); // NOI18N
                    }
                }

                @Override
                public void fileDeleted(final FileEvent fe) {
                    // do nothing
                }

                @Override
                public void fileRenamed(final FileRenameEvent fe) {
                    // do nothing
                }

                @Override
                public void fileAttributeChanged(final FileAttributeEvent fe) {
                    // do nothing
                }
            });
        runtimeProps = new Properties();
        try {
            runtimeProps.load(fob.getInputStream());
        } catch (final Exception e) {
            LOG.error("could not load runtime properties"); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class,
                    "DomainserverProject.DomainserverProject().ErrorUtils.runtimePropsNotLoadedError"),
                e);                                         // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initPolicies() {
        if (runtimeProps == null) {
            LOG.warn("could not init policies due to missing" // NOI18N
                        + "runtime properties");         // NOI18N
            return;
        }
        serverPolicy = runtimeProps.getProperty(PROP_POLICY_SERVER);
        classNodePolicy = runtimeProps.getProperty(PROP_POLICY_CLASS_NODE);
        orgNodePolicy = runtimeProps.getProperty(PROP_POLICY_ORG_NODE);
        attrPolicy = runtimeProps.getProperty(PROP_POLICY_ATTR);
        if ((serverPolicy == null) || (classNodePolicy == null)
                    || (orgNodePolicy == null)
                    || (attrPolicy == null)) {
            final StringBuffer sb = new StringBuffer("<ul>"); // NOI18N
            if (serverPolicy == null) {
                sb.append("<li>")                        // NOI18N
                .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "DomainserverProject.initPolicy().serverPolicy")) // NOI18N
                .append("</li>");                        // NOI18N
                runtimeProps.put(PROP_POLICY_SERVER, DEFAULT_POLICY_SERVER);
                serverPolicy = DEFAULT_POLICY_SERVER;
            }
            if (attrPolicy == null) {
                sb.append("<li>")                        // NOI18N
                .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "DomainserverProject.initPolicy().attributePolicy")) // NOI18N
                .append("</li>");                        // NOI18N
                runtimeProps.put(PROP_POLICY_ATTR, DEFAULT_POLICY_ATTR);
                attrPolicy = DEFAULT_POLICY_ATTR;
            }
            if (classNodePolicy == null) {
                sb.append("<li>")                        // NOI18N
                .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "DomainserverProject.initPolicy().classNodePolicy")) // NOI18N
                .append("</li>");                        // NOI18N
                runtimeProps.put(PROP_POLICY_CLASS_NODE,
                    DEFAULT_POLICY_CLASS_NODE);
                classNodePolicy = DEFAULT_POLICY_CLASS_NODE;
            }
            if (orgNodePolicy == null) {
                sb.append("<li>")                        // NOI18N
                .append(org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "DomainserverProject.initPolicy().orgNodePolicy")) // NOI18N
                .append("</li>");                        // NOI18N
                runtimeProps.put(PROP_POLICY_ORG_NODE, DEFAULT_POLICY_ORG_NODE);
                orgNodePolicy = DEFAULT_POLICY_ORG_NODE;
            }
            sb.append("</ul>");                          // NOI18N
            JOptionPane.showMessageDialog(
                WindowManager.getDefault().getMainWindow(),
                org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class,
                    "DomainserverProject.initPolicy().JOptionPane.message", // NOI18N
                    sb.toString()),
                org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class,
                    "DomainserverProject.initPolicy().JOptionPane.title"), // NOI18N
                JOptionPane.INFORMATION_MESSAGE);
            storeRuntimeProperties();
        }
        // INFO: we cannot check for invalid policies at this point, because
        // we are not connected to the backend yet.
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public FileObject getDistRoot() {
        FileObject current = projectDir.getParent();
        while (current != null) {
            if (current.getFileObject("lib/cidsLibBase") != null) { // NOI18N
                return current;
            } else {
                current = current.getParent();
            }
        }

        throw new IllegalStateException("cannot locate dist root"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FileObject getWebinterfaceFolder() {
        FileObject result = projectDir.getFileObject(WEBINTERFACE_DIR);
        if (result == null) {
            try {
                result = projectDir.createFolder(WEBINTERFACE_DIR);
            } catch (final IOException ioe) {
                ErrorUtils.showErrorMessage(
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "DomainserverProject.getWebinterfaceFolder().ErrorUtils.unknownErrorMessage"),
                    ioe); // NOI18N
            }
        }
        return result;
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(
                    new Object[] {
                        this,                     // project spec requires a project be in its own lookup
                        state,                    // allow outside code to mark the project eg. need saving
                        new ActionProviderImpl(), // Provides standard actions
                        getProperties(),          // The project properties
                        new Info(),               // Project information implementation
                        logicalView,              // Logical view of project implementation
                    });
        }

        return lkp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lookup  DOCUMENT ME!
     */
    void addLookup(final Lookup lookup) {
        // TODO: investigate: while why did we do that
        if (lookup != null) {
            lkp = new ProxyLookup(new Lookup[] { getLookup(), lookup });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Properties getProperties() {
        if (projectProps == null) {
            final FileObject fob = projectDir.getFileObject(
                    DomainserverProjectFactory.PROJECT_DIR
                            + "/" // NOI18N
                            + DomainserverProjectFactory.PROJECT_PROPFILE);
            projectProps = new NotifyProperties(state);
            if (fob == null) {
                LOG.warn("project.properties not found"); // NOI18N
            } else {
                try {
                    projectProps.load(fob.getInputStream());
                } catch (final IOException ex) {
                    LOG.error("could not load project.properties", ex); // NOI18N
                    ErrorUtils.showErrorMessage(
                        org.openide.util.NbBundle.getMessage(
                            DomainserverProject.class,
                            "DomainserverProject.getProperties().ErrorUtils.unknownErrorMessage"),
                        ex);      // NOI18N
                }
            }
        }
        return projectProps;
    }

    /**
     * TODO: clean file write
     */
    private void storeRuntimeProperties() {
        final FileObject fob = getProjectDirectory().getFileObject(
                RUNTIME_PROPS);
        if (fob != null) {
            FileLock lock = null;
            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                lock = fob.lock();
                br = new BufferedReader(new FileReader(FileUtil.toFile(fob)));
                final LinkedList<String> lines = new LinkedList<String>();
                String line = br.readLine();
                boolean inserted = false;
                while (line != null) {
                    if (!(line.startsWith(PROP_POLICY_ATTR)
                                    || line.startsWith(PROP_POLICY_CLASS_NODE)
                                    || line.startsWith(PROP_POLICY_ORG_NODE)
                                    || line.startsWith(PROP_POLICY_SERVER))) {
                        if (line.contains("miniServer.properties") // NOI18N
                                    && line.startsWith("##"))      // NOI18N
                        {
                            lines.add("");                         // NOI18N
                            lines.add("## policies");              // NOI18N
                            lines.add(PROP_POLICY_SERVER
                                        + "="                      // NOI18N
                                        + runtimeProps.getProperty(PROP_POLICY_SERVER));
                            lines.add(PROP_POLICY_ATTR
                                        + "="                      // NOI18N
                                        + runtimeProps.getProperty(PROP_POLICY_ATTR));
                            lines.add(PROP_POLICY_CLASS_NODE
                                        + "="                      // NOI18N
                                        + runtimeProps.getProperty(PROP_POLICY_CLASS_NODE));
                            lines.add(PROP_POLICY_ORG_NODE
                                        + "="                      // NOI18N
                                        + runtimeProps.getProperty(PROP_POLICY_ORG_NODE));
                            lines.add("");                         // NOI18N
                            inserted = true;
                        }
                        lines.addLast(line);
                    }
                    line = br.readLine();
                }
                if (!inserted) {
                    lines.add("## policies");                      // NOI18N
                    lines.add(PROP_POLICY_SERVER
                                + "="                              // NOI18N
                                + runtimeProps.getProperty(PROP_POLICY_SERVER));
                    lines.add(PROP_POLICY_ATTR
                                + "="                              // NOI18N
                                + runtimeProps.getProperty(PROP_POLICY_ATTR));
                    lines.add(PROP_POLICY_CLASS_NODE
                                + "="                              // NOI18N
                                + runtimeProps.getProperty(PROP_POLICY_CLASS_NODE));
                    lines.add(PROP_POLICY_ORG_NODE
                                + "="                              // NOI18N
                                + runtimeProps.getProperty(PROP_POLICY_ORG_NODE));
                }
                bw = new BufferedWriter(new FileWriter(FileUtil.toFile(fob)));
                for (final String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch (final Exception e) {
                ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "DomainserverProject.storeRuntimeProperties().ErrorUtils.loadingRuntimeProps"),
                    e);                                            // NOI18N
            } finally {
                if (lock != null)                                  // && lock.isValid())
                {
                    lock.releaseLock();
                }
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (final IOException e) {
                    LOG.warn("could not close buffered reader", e); // NOI18N
                }
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (final IOException e) {
                    LOG.warn("could not close buffered writer", e); // NOI18N
                }
            }
        } else {
            LOG.error("could not locate runtime.properties file"); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class,
                    "DomainserverProject.storeRuntimeProperties().ErrorUtils.runtimePropsUnfindable"),
                null);                                             // NOI18N
        }
    }

    @Override
    public boolean isConnected() {
        return backend != null;
    }

    @Override
    public void setConnected(final boolean connected) {
        /* we won't do anything if the backend has not been set and we shall not
         * connect (connected = false)
         */
        if ((backend == null) && !connected) {
            return;
        }
        connectionInProgress = true;
        fireConnectionStatusIndeterminate();
        if (connected) {
            final Thread t = new Thread() {

                    @Override
                    public void run() {
                        initPolicies();
                        if (!acquireLock()) {
                            connectionInProgress = false;
                            fireConnectionStatusChanged();
                            return;
                        }
                        try {
                            final FileObject fob = getProjectDirectory().getFileObject(
                                    DomainserverProject.RUNTIME_PROPS);
                            runtimeProps = new Properties();
                            try {
                                runtimeProps.load(fob.getInputStream());
                            } catch (final Exception e) {
                                ErrorUtils.showErrorMessage(
                                    org.openide.util.NbBundle.getMessage(
                                        DomainserverProject.class,
                                        "DomainserverProject.setConnected().ErrorUtils.unknownErrorMessage"),
                                    e); // NOI18N
                            }

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("new Backend(runtimeProps)"); // NOI18N
                            }

                            backend = new Backend(runtimeProps);
                            connectionInProgress = false;
                            diffAccessor = new DiffAccessor(runtimeProps, backend);
                            fireConnectionStatusChanged();

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("setConnected(" + connected + ") = " + isConnected()); // NOI18N
                            }

                            final List<Icon> list = backend.getAllEntities(Icon.class);
                            Icon backup = null;
                            for (final Icon ic : list) {
                                if (backup == null) {
                                    backup = ic;
                                }
                                if (ic.getName().toLowerCase().startsWith("array")) {             // NOI18N
                                    setArrayIcon(ic);
                                }
                            }
                            if (getArrayIcon() == null) {
                                setArrayIcon(backup);
                            }
                        } catch (final Exception e) {
                            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class,
                                    "DomainserverProject.setConnected().ErrorUtils.connectToDB"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class,
                                    "DomainserverProject.setConnected().ErrorUtils.connectionError"),
                                e);                                                               // NOI18N
                            backend = null;
                            fireConnectionStatusChanged();
                            connectionInProgress = false;
                        }
                    }
                };
            t.start();
        } else {
            releaseLock();
            connectionInProgress = false;
            try {
                if (backend != null) {
                    backend.close();
                }
            } catch (final Exception e) {
                LOG.error("could not close backend", e);                                          // NOI18N
                ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "DomainserverProject.setConnected().ErrorUtils.closeDBConnection"),
                    e);                                                                           // NOI18N
            }
            diffAccessor.freeResources();
            diffAccessor = null;
            backend = null;
            // free resources
            fireConnectionStatusChanged();

            if (LOG.isDebugEnabled()) {
                LOG.debug("setConnected(" + connected + ") = " + isConnected()); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private synchronized boolean acquireLock() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to aquire abf lock");         // NOI18N
        }
        final FileObject fob = getProjectDirectory().getFileObject(
                DomainserverProject.RUNTIME_PROPS);
        runtimeProps = new Properties();
        try {
            runtimeProps.load(fob.getInputStream());
        } catch (final Exception e) {
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DomainserverProject.class,
                    "DomainserverProject.acquireLock().ErrorUtils.runtimePropsReload"),
                e);                                         // NOI18N
            return false;
        }
        Connection con = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("receiving database connection"); // NOI18N
            }
            con = DatabaseConnection.getConnection(runtimeProps);
        } catch (final SQLException sqle) {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not acquire connection to database", sqle); // NOI18N
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.acquireLock().ErrorUtils.noDBConnectionPossibleHTML"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.acquireLock().ErrorUtils.dbUnreachable"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    }
                });                                                                            // </editor-fold>
            return false;
        }
        if (con == null) {
            throw new IllegalStateException("connection must not be null");                    // NOI18N
        }
        Statement stmt = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("locking cs_locks");                                                 // NOI18N
            }
            stmt = con.createStatement();
            stmt.execute(STMT_BEGIN);
            stmt.execute(STMT_LOCK_TABLE);
        } catch (final SQLException sqle) {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not acquire lock on cs_locks", sqle); // NOI18N
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.acquireLock().ErrorUtils.dbUnlockableHTML"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.acquireLock().ErrorUtils.tableUnlockable"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    }
                });
            try {
                if (stmt != null) {
                    stmt.execute(STMT_ROLLBACK);
                }
            } catch (final SQLException ex) {
                LOG.warn("could not rollback statements", ex);                                   // NOI18N
            } finally {
                DatabaseConnection.closeStatement(stmt);
                DatabaseConnection.closeConnection(con);
            }
            // </editor-fold>
            return false;
        }
        if (stmt == null) {
            throw new IllegalStateException("statement must not be null");                       // NOI18N
        }
        ResultSet set = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("reading abf locks");                                                  // NOI18N
            }
            set = stmt.executeQuery(STMT_READ_LOCKS);
            // if set.next() delivers a row a lock is already acquired
            if (set.next()) {
                final String when = set.getString("user_string").replace(LOCK_PREFIX, ""); // NOI18N
                final Date date = new Date(Long.valueOf(when));
                final String who = set.getString("additional_info");                       // NOI18N
                if (LOG.isInfoEnabled()) {
                    LOG.info("lock aquired by " + who + " on " + date);                    // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(
                                WindowManager.getDefault().getMainWindow(),
                                org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class,
                                    "DomainserverProject.acquireLock().ErrorUtils.alreadyLocked", // NOI18N
                                    who,
                                    date.toString()),
                                org.openide.util.NbBundle.getMessage(
                                    DomainserverProject.class,
                                    "DomainserverProject.acquireLock().ErrorUtils.lockPresent"), // NOI18N
                                JOptionPane.WARNING_MESSAGE);
                        }
                    });
                stmt.execute(STMT_COMMIT);

                return false;
            } else {
                final String user = System.getProperty("user.name"); // NOI18N
                String host = "unknown";                             // NOI18N

                try {
                    host = InetAddress.getLocalHost().getHostName();
                } catch (final UnknownHostException e) {
                    LOG.warn("could not resolve host name", e); // NOI18N
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("writing lock"); // NOI18N
                }

                final String who = user + "@" + host; // NOI18N
                final String update = MessageFormat.format(STMT_ACQUIRE_LOCK, generateNonce(), who);
                stmt.executeUpdate(update);
                stmt.execute(STMT_COMMIT);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("successfully locked"); // NOI18N
                }

                return true;
            }
        } catch (final SQLException sqle) {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not acquire lock", sqle); // NOI18N
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.acquireLock().ErrorUtils.unlockableHTML"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.acquireLock().ErrorUtils.lockNotSet"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    }
                });
            try {
                stmt.execute(STMT_ROLLBACK);
            } catch (final SQLException ex) {
                LOG.warn("could not rollback statements", ex);                              // NOI18N
            }                                                                               // </editor-fold>
            return false;
        } finally {
            DatabaseConnection.closeResultSet(set);
            DatabaseConnection.closeStatement(stmt);
            DatabaseConnection.closeConnection(con);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String generateNonce() {
        lockNonce = LOCK_PREFIX + System.currentTimeMillis();
        return lockNonce;
    }

    /**
     * DOCUMENT ME!
     */
    private synchronized void releaseLock() {
        if (lockNonce == null) {
            return;
        }
        Connection con = null;
        Statement stmt = null;
        try {
            final String update = MessageFormat.format(
                    STMT_RELEASE_LOCK,
                    lockNonce);
            con = DatabaseConnection.getConnection(runtimeProps);
            stmt = con.createStatement();
            stmt.execute(STMT_BEGIN);
            stmt.execute(STMT_LOCK_TABLE);
            stmt.executeUpdate(update);
            stmt.execute(STMT_COMMIT);
        } catch (final Exception ex) {
            // <editor-fold defaultstate="collapsed" desc=" Errorhandling ">
            LOG.error("could not release lock", ex); // NOI18N
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.releaseLock().ErrorUtils.lockNotDeletableHTMLL", // NOI18N
                                lockNonce),
                            org.openide.util.NbBundle.getMessage(
                                DomainserverProject.class,
                                "DomainserverProject.releaseLock().ErrorUtils.errorDuringLockDeletion"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    }
                });
            try {
                if (stmt != null) {
                    stmt.execute(STMT_ROLLBACK);
                }
            } catch (final SQLException e) {
                LOG.warn("could not rollback statements", e); // NOI18N
            }                     // </editor-fold>
        } finally {
            // <editor-fold defaultstate="collapsed" desc=" Cleanup ">
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (final SQLException ex) {
                LOG.error("could not cleanup connection", ex); // NOI18N
            }                                                  // </editor-fold>
            lockNonce = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Properties getRuntimeProps() {
        return runtimeProps;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAttrPolicy() {
        return attrPolicy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClassNodePolicy() {
        return classNodePolicy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOrgNodePolicy() {
        return orgNodePolicy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getServerPolicy() {
        return serverPolicy;
    }

    @Override
    public void addConnectionListener(final ConnectionListener cl) {
        connectionSupport.addConnectionListener(cl);
    }

    @Override
    public void removeConnectionListener(final ConnectionListener cl) {
        connectionSupport.addConnectionListener(cl);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Backend getCidsDataObjectBackend() {
        return backend;
    }

    @Override
    public boolean isConnectionInProgress() {
        return connectionInProgress;
    }

    /**
     * TODO: refactor and delete
     *
     * @return  DOCUMENT ME!
     */
    public DomainserverProjectNode getDomainserverProjectNode() {
        return domainserverProjectNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  node  DOCUMENT ME!
     */
    public void setDomainserverProjectNode(final DomainserverProjectNode node) {
        this.domainserverProjectNode = node;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DiffAccessor getDiffAccessor() {
        return diffAccessor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  diffAccessor  DOCUMENT ME!
     */
    public void setDiffAccessor(final DiffAccessor diffAccessor) {
        this.diffAccessor = diffAccessor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Icon getArrayIcon() {
        return arrayIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  arrayIc  DOCUMENT ME!
     */
    public void setArrayIcon(final Icon arrayIc) {
        this.arrayIcon = arrayIc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cncc  DOCUMENT ME!
     */
    public void setLinkableCatNodeCookies(final CatalogNodeContextCookie[] cncc) {
        this.catNodeCookies = Arrays.copyOf(cncc, cncc.length);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CatalogNodeContextCookie[] getLinkableCatNodeCookies() {
        if (catNodeCookies == null) {
            return new CatalogNodeContextCookie[0];
        } else {
            return Arrays.copyOf(catNodeCookies, catNodeCookies.length);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void fireConnectionStatusChanged() {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    handle.finish();
                }
            });

        final ConnectionEvent event = new ConnectionEvent(this, isConnected(), isConnectionInProgress());
        connectionSupport.fireConnectionStatusChanged(event);
    }

    /**
     * DOCUMENT ME!
     */
    private void fireConnectionStatusIndeterminate() {
        if (isConnected()) {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "DomainserverProject.fireConnectionStatusIndeterminate().handle.disconnectFromDomainserver", // NOI18N
                        getProjectDirectory().getName()));
        } else {
            handle = ProgressHandleFactory.createHandle(
                    org.openide.util.NbBundle.getMessage(
                        DomainserverProject.class,
                        "DomainserverProject.fireConnectionStatusIndeterminate().handle.connectToDomainserver", // NOI18N
                        getProjectDirectory().getName()));
        }

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    handle.start();
                    handle.switchToIndeterminate();
                }
            });

        final ConnectionEvent event = new ConnectionEvent(this, isConnected(), isConnectionInProgress());
        connectionSupport.fireConnectionStatusChanged(event);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ActionProviderImpl implements ActionProvider {

        //~ Methods ------------------------------------------------------------

        @Override
        public String[] getSupportedActions() {
            return new String[] {
                    ActionProvider.COMMAND_BUILD,
                    ActionProvider.COMMAND_CLEAN,
                    ActionProvider.COMMAND_COMPILE_SINGLE
                };
        }

        @Override
        public void invokeAction(final String s, final Lookup l) throws IllegalArgumentException {
            // do nothing
        }

        @Override
        public boolean isActionEnabled(final String s, final Lookup l) throws IllegalArgumentException {
            return true;
        }
    }

    /**
     * Implementation of project system's ProjectInformation class.
     *
     * @version  $Revision$, $Date$
     */
    private final class Info implements ProjectInformation {

        //~ Instance fields ----------------------------------------------------

        private final transient javax.swing.Icon icon;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Info object.
         */
        Info() {
            icon = new ImageIcon(ImageUtilities.loadImage(IMAGE_FOLDER + "domainserver.png")); // NOI18N
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public javax.swing.Icon getIcon() {
            return icon;
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p) {
            // do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p) {
            // do nothing, won't change
        }

        @Override
        public Project getProject() {
            return DomainserverProject.this;
        }
    }
}

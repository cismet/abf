/*
 * SyncManagement.java, encoding: UTF-8
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
 * Created on 22. März 2007, 09:53
 *
 */
package de.cismet.cids.abf.domainserver.project.nodes;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.sync.SyncAction;
import de.cismet.cids.abf.domainserver.project.sync.SyncingSqlTopComponent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.diff.container.PSQLStatement;
import de.cismet.diff.container.PSQLStatementGroup;
import de.cismet.diff.exception.ScriptGeneratorException;
import de.cismet.diff.exception.TableLoaderException;
import java.awt.EventQueue;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityNotFoundException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author hell
 */
public class SyncManagement extends ProjectNode implements
        ConnectionListener,
        DomainserverContext,
        Refreshable
{
    private static final transient Logger LOG = Logger.getLogger(
            SyncManagement.class);

    private static final String SYNC_STATUS_CHECK = NbBundle.getMessage(
            SyncManagement.class, "SyncManagement_syncStatusCheck"); // NOI18N
    private static final String SYNC_STATUS_ERROR = NbBundle.getMessage(
            SyncManagement.class, "SyncManagement_syncStatusError"); // NOI18N
    private static final String SYNC = NbBundle.getMessage(SyncManagement.class,
            "SyncManagement_sync"); // NOI18N
    private static final String PROPERTIES = NbBundle.getMessage(SyncManagement.
            class, "SyncManagement_properties"); // NOI18N
    private static final String PEDANTIC = NbBundle.getMessage(SyncManagement.
            class, "SyncManagement_pedantic"); // NOI18N
    private static final String PEDANTIC_DETAIL = NbBundle.getMessage(
            SyncManagement.class, "SyncManagement_pedanticDetail"); // NOI18N

    private final transient Image noChangesNeededImage;
    private final transient Image inProgressImage;
    private final transient Image changesNeeded;
    private final transient Image errorImage;

    private transient PSQLStatementGroup[] statementGroupsNotPedantic;
    private transient PSQLStatementGroup[] allStatementGroups;
    private transient boolean inProgress;
    private transient boolean hasErrors;
    private transient boolean pedantic;
    private transient String errorMessage;
    private transient SyncingSqlTopComponent topComponent;

    public SyncManagement(final DomainserverProject project)
    {
        super(Children.LEAF, project);
        noChangesNeededImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "db2.png"); // NOI18N
        inProgressImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "db_status.png"); // NOI18N
        changesNeeded = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "db_update.png"); // NOI18N
        errorImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "db_error.png"); // NOI18N
        statementGroupsNotPedantic = new PSQLStatementGroup[0];
        allStatementGroups = new PSQLStatementGroup[0];
        inProgress = false;
        hasErrors = false;
        pedantic = false;
        project.addConnectionListener(this);
        getCookieSet().add(new OpenCookie()
        {
            @Override
            public void open()
            {
                final SyncingSqlTopComponent tc = getTopComponent();
                if(!tc.isOpened())
                {
                    topComponent.open();
                }
                tc.requestActive();
                tc.setSql(getSqlScriptFromGroups());
            }

        });
    }

    @Override
    public Image getIcon(final int i)
    {
        final PSQLStatementGroup[] statementGroups;
        if(pedantic)
        {
            statementGroups = allStatementGroups;
        }else
        {
            statementGroups = statementGroupsNotPedantic;
        }
        if(project != null && project.isConnected() && hasErrors)
        {
            return errorImage;
        }else if(project == null
                || !project.isConnected()
                || statementGroups == null
                && !inProgress
                || statementGroups != null
                && statementGroups.length == 0
                && !inProgress)
        {
            return noChangesNeededImage;
        }else if(inProgress)
        {
            return inProgressImage;
        }else
        {
            return changesNeeded;
        }
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }

    @Override
    public String getDisplayName()
    {
        final PSQLStatementGroup[] statementGroups;
        if(pedantic)
        {
            statementGroups = allStatementGroups;
        }else
        {
            statementGroups = statementGroupsNotPedantic;
        }
        if(project != null && project.isConnected() && inProgress)
        {
            return SYNC + SYNC_STATUS_CHECK;
        }else if(project != null && project.isConnected() && hasErrors)
        {
            return SYNC + MessageFormat.format(SYNC_STATUS_ERROR, errorMessage);
        }else if(project == null ||
                !project.isConnected() ||
                statementGroups == null &&
                !inProgress ||
                statementGroups != null &&
                statementGroups.length == 0 &&
                !inProgress)
        {
            return SYNC;
        }else
        {
            return SYNC + "(" + statementGroups.length + ")"; // NOI18N
        }
    }

    @Override
    public void connectionStatusChanged(final boolean isConnected)
    {
        if(isConnected)
        {
            refresh();
        }
        else
        {
            allStatementGroups = new PSQLStatementGroup[0];
            statementGroupsNotPedantic = new PSQLStatementGroup[0];
            inProgress = false;
            fireIconChange();
            fireDisplayNameChange(null, getDisplayName());
            getTopComponent().setSql(getSqlScriptFromGroups());
        }
    }

    @Override
    public void connectionStatusIndeterminate()
    {
        // do nothing
    }

    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set main = Sheet.createPropertiesSet();
        main.setName(PROPERTIES);
        main.setDisplayName(PROPERTIES);
        try
        {                                                      
            final Property pedanticCheck = new PropertySupport(
                    "pedantic", // NOI18N
                    Boolean.class, PEDANTIC, PEDANTIC_DETAIL, true, true)
            {

                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return pedantic;
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    try
                    {
                        setPedantic(((Boolean) object).booleanValue());
                    }catch(final Exception e)
                    {
                        ErrorUtils.showErrorMessage(NbBundle.getMessage(
                                SyncManagement.class, 
                                "SyncManagement_unknownError"), e); // NOI18N
                    }
                }

            };
            main.put(pedanticCheck);
            sheet.put(main);
        }catch(final Exception ex)
        {
            ErrorUtils.showErrorMessage(NbBundle.getMessage(SyncManagement.
                    class, "SyncManagement_unknownError"), ex); // NOI18N
        }
        return sheet;
    }

    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[]
        {
            CallableSystemAction.get(RefreshAction.class),
            CallableSystemAction.get(OpenAction.class),
            CallableSystemAction.get(SyncAction.class)
        };
    }

    @Override
    public Action getPreferredAction()
    {
        return CallableSystemAction.get(OpenAction.class);
    }

    @Override
    public void refresh()
    {
        if(project.isConnected())
        {
            final Thread retrieval = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        getTopComponent().setEnabled(false);
                        hasErrors = false;
                        errorMessage = ""; // NOI18N
                        inProgress = true;
                        fireIconChange();
                        allStatementGroups = project.getDiffAccessor().
                                getStatementGroups();
                        final List<PSQLStatementGroup> notpedantics = new
                                ArrayList<PSQLStatementGroup>();
                        for(final PSQLStatementGroup g : allStatementGroups)
                        {
                            final PSQLStatement[] sa = g.getPSQLStatements();
                            boolean allPedantics = true;
                            for(final PSQLStatement s : sa)
                            {
                                if(!s.isPedantic())
                                {
                                    allPedantics = false;
                                    break;
                                }
                            }
                            if(!allPedantics)
                            {
                                notpedantics.add(g);
                            }
                        }
                        statementGroupsNotPedantic = notpedantics.toArray(new
                                PSQLStatementGroup[notpedantics.size()]);
                    }catch(final ScriptGeneratorException sge)
                    {
                        hasErrors = true;
                        errorMessage = sge.getMessage() 
                                + " Table: " + sge.getTable() // NOI18N
                                + ", Type: " + sge.getType() // NOI18N
                                + " Column: " + sge.getColumn(); // NOI18N
                        LOG.error("Error: " + errorMessage, sge); // NOI18N
                    }catch(final TableLoaderException tle)
                    {
                        LOG.error("Error " + tle, tle); // NOI18N
                        LOG.error("Cause " + tle.getCause(), // NOI18N
                                tle.getCause());
                        hasErrors = true;
                        errorMessage = tle.toString();
                        final Throwable cause = tle.getCause();
                        if(cause instanceof EntityNotFoundException)
                        {
                            ErrorUtils.showErrorMessage(
                                    org.openide.util.NbBundle.getMessage(
                                    SyncManagement.class, 
                                    "Err_metaInfoInconsistency"), tle);// NOI18N
                        }else
                        {    ErrorUtils.showErrorMessage(
                                     org.openide.util.NbBundle.getMessage(
                                     SyncManagement.class, 
                                     "Err_loadingMetaInfo"), // NOI18N
                                    (cause == null) ? tle : cause);
                        }
                    }catch(final Exception e)
                    {
                        LOG.error("unexpected error", e); // NOI18N
                        hasErrors = true;
                        errorMessage = e.toString();
                        ErrorUtils.showErrorMessage(org.openide.util.NbBundle
                                .getMessage(SyncManagement.class, 
                                "Err_syncGeneric"), e); // NOI18N
                    }finally
                    {
                        inProgress = false;
                    }
                    getTopComponent().setEnabled(true);
                    fireIconChange();
                    fireDisplayNameChange(null, getDisplayName());
                    getTopComponent().setSql(getSqlScriptFromGroups());
                }

            });
            retrieval.start();
        }
    }

    public void executeStatements()
    {
        final InputOutput io = IOProvider.getDefault().getIO(getTopComponent().
                getName(), false);
        io.select();
        final Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                getTopComponent().setEnabled(false);
                Connection connection = null;
                final Properties runtime = project.getRuntimeProps();
                final String dbURL = 
                        runtime.getProperty("connection.url"); // NOI18N
                final String user = 
                        runtime.getProperty("connection.username"); // NOI18N
                final String pass = 
                        runtime.getProperty("connection.password"); // NOI18N
                final String driver = 
                        runtime.getProperty("connection.driver_class");// NOI18N
                try
                {
                    final PSQLStatementGroup[] statementGroups;
                    if(pedantic)
                    {
                        statementGroups = allStatementGroups;
                    }else
                    {
                        statementGroups = statementGroupsNotPedantic;
                    }
                    Class.forName(driver).newInstance();
                    connection = DriverManager.getConnection(dbURL, user, pass);
                    io.getOut().println(org.openide.util.NbBundle.getMessage(
                            SyncManagement.class, "Dsc_syncingDB")); // NOI18N
                    io.getOut().println(org.openide.util.NbBundle.getMessage(
                            SyncManagement.class, "Dsc_connectedToDB"));//NOI18N
                    for(final PSQLStatementGroup g : statementGroups)
                    {
                        connection.setAutoCommit(!g.isTransaction());
                        io.getOut().println();
                        final PSQLStatement[] sa = g.getPSQLStatements();
                        for(final PSQLStatement p : sa)
                        {
                            String currentStatement = ""; // NOI18N
                            Statement statement = null;
                            try
                            {
                                statement = connection.createStatement();
                                currentStatement = p.getStatement();
                                if(LOG.isDebugEnabled())
                                {
                                    LOG.debug("current stmt: " // NOI18N
                                            + currentStatement);
                                }
                                statement.execute(currentStatement);
                                io.getOut().println(
                                        org.openide.util.NbBundle.getMessage(
                                        SyncManagement.class, 
                                        "Dsc_stmtSuccessful", // NOI18N
                                        currentStatement));
                                io.getOut().flush();
                            }catch(final SQLException e)
                            {
                                io.getErr().println(org.openide.util.NbBundle
                                        .getMessage(SyncManagement.class, 
                                        "Dsc_stmtErroneous", // NOI18N
                                        currentStatement));
                                LOG.error("Error at: " // NOI18N
                                        + currentStatement, e);
                                io.getErr().println(
                                        "\t\t" + e.toString()); // NOI18N
                                io.getErr().flush();
                            }finally
                            {
                                try
                                {
                                    if(statement != null)
                                    {
                                        statement.close();
                                    }
                                }catch(final SQLException e)
                                {
                                    LOG.warn("could not close stmt", e);//NOI18N
                                }
                            }
                        }
                    }
                    io.getOut().println(org.openide.util.NbBundle.getMessage(
                            SyncManagement.class, "Dsc_syncDone")); // NOI18N
                }catch(final Exception e)
                {
                    io.getErr().println(org.openide.util.NbBundle.getMessage(
                            SyncManagement.class, "Err_abort", // NOI18N
                            e.getMessage()));
                    e.printStackTrace(io.getErr());
                    io.getErr().flush();
                    LOG.error("error during synchronization", e); // NOI18N
                }finally
                {
                    try
                    {
                        if(connection != null)
                        {
                            connection.close();
                        }
                    }catch(final SQLException ex)
                    {
                        LOG.error("could not close connection", ex); // NOI18N
                    }
                }
                refresh();
            }
        });
        t.start();
    }

    private String getSqlScriptFromGroups()
    {
        final PSQLStatementGroup[] stmtGroups;
        if(pedantic)
        {
            stmtGroups = allStatementGroups;
        }else
        {
            stmtGroups = statementGroupsNotPedantic;
        }
        final StringBuffer s = new StringBuffer(50);
        s.append(org.openide.util.NbBundle.getMessage(SyncManagement.class, 
                "Dsc_stmtCount", stmtGroups.length));
        for(final PSQLStatementGroup g : stmtGroups)
        {
            final PSQLStatement[] sa = g.getPSQLStatements();
            s.append('\n'); // NOI18N
            if(g.getWarning() != null && g.getWarning().trim().length() != 0)
            {
                s.append(org.openide.util.NbBundle.getMessage(
                        SyncManagement.class, "Dsc_warning")) // NOI18N
                        .append(g.getWarning());
            }
            if(g.getDescription() != null &&
                    g.getDescription().trim().length() != 0)
            {
                s.append("\n-- ").append(g.getDescription()); // NOI18N
            }
            for(final PSQLStatement p : sa)
            {
                if(p.getWarning() != null &&
                        p.getWarning().trim().length() != 0)
                {
                    s.append(org.openide.util.NbBundle.getMessage(
                            SyncManagement.class, "Dsc_warning"))
                            .append(p.getWarning());
                }
                if(p.getDescription() != null &&
                        p.getDescription().trim().length() != 0)
                {
                    s.append("\n-- ").append(p.getDescription()); // NOI18N
                }
                s.append('\n').append(p.getStatement()); // NOI18N
            }
        }
        return s.toString();
    }

    public boolean isPedantic()
    {
        return pedantic;
    }

    public void setPedantic(final boolean pedantic)
    {
        this.pedantic = pedantic;
        firePropertyChange("pedantic", // NOI18N
                Boolean.valueOf(!pedantic), Boolean.valueOf(pedantic));
        fireDisplayNameChange(null, getDisplayName());
        getTopComponent().setSql(getSqlScriptFromGroups());
        getTopComponent().setPedantic(pedantic);
    }

    public SyncingSqlTopComponent getTopComponent()
    {
        if(topComponent == null)
        {
            // the component has to be created in EDT
            if(SwingUtilities.isEventDispatchThread())
            {
                topComponent = new SyncingSqlTopComponent(project);
            }else
            {
                try
                {
                    EventQueue.invokeAndWait(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            topComponent = new SyncingSqlTopComponent(project);
                        }
                    });
                }catch(final Exception ex)
                {
                    final String message = "could no create syncing top component";
                    LOG.error(message, ex);
                    throw new IllegalStateException(message, ex);
                }
            }
            topComponent.setSyncManager(this);
        }
        return topComponent;
    }
}
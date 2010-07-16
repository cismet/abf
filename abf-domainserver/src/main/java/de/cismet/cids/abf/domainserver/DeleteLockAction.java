/*
 * DeleteLockAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.diff.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class DeleteLockAction extends NodeAction
{
    private static final transient Logger LOG = Logger.getLogger(
            DeleteLockAction.class);
    
    private static final String DELETE_LOCK_STMT = 
            "DELETE FROM cs_locks WHERE id = "; // NOI18N
    
    @Override
    protected void performAction(final Node[] activatedNodes)
    {
        final DomainserverProject project = activatedNodes[0].getLookup().
                lookup(DomainserverProject.class);
        final int answer = JOptionPane.showOptionDialog(
                WindowManager.getDefault().getMainWindow(), 
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class, "DeleteLockAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class, "DeleteLockAction.performAction(Node[]).JOptionPane.title"),// NOI18N
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[] 
                    {
                        org.openide.util.NbBundle.getMessage(
                                DeleteLockAction.class, "DeleteLockAction.performAction(Node[]).JOptionPane.yesOption"), // NOI18N
                        org.openide.util.NbBundle.getMessage(
                                DeleteLockAction.class,
                                "DeleteLockAction.performAction(Node[]).JOptionPane.noOption") // NOI18N
                    },
                org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class, "DeleteLockAction.performAction(Node[]).JOptionPane.noOption")); // NOI18N
        if(answer != JOptionPane.YES_OPTION)
        {
            return;
        }
        Connection con = null;
        ResultSet set = null;
        try        
        {
            con = DatabaseConnection.getConnection(project.getRuntimeProps());
            set = con.createStatement().executeQuery(DomainserverProject.
                    STMT_READ_LOCKS);
            while(set.next())
            {
                final Integer id = set.getInt("id"); // NOI18N
                con.createStatement().executeUpdate(DELETE_LOCK_STMT + id);
            }
        }catch(final Exception e)        
        {
            LOG.error("could not remove lock", e); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class, 
                    "DeleteLockAction.performAction(Node[]).ErrorUtils.atLeastOneLockUndeleteableError.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                    DeleteLockAction.class, 
                    "DeleteLockAction.performAction(Node[]).ErrorUtils.atLeastOneLockUndeleteableError.title"), e); // NOI18N
        }finally
        {
            try            
            {
                if(set != null)
                {
                    set.close();
                }
            }catch(final SQLException e)            
            {
                LOG.warn("could not close resultset", e); // NOI18N
            }
            try
            {
                if(con != null)
                {
                    con.close();
                }
            }catch(final SQLException e)
            {
                LOG.warn("could not close connection", e); // NOI18N
            }
        }
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(DeleteLockAction.class, 
                "DeleteLockAction.getName().returnvalue"); // NOI18N
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
    protected String iconResource()
    {
        return DomainserverProject.IMAGE_FOLDER + "unlock.png"; // NOI18N
    }

    @Override
    protected boolean enable(final Node[] nodes)
    {
        if(nodes.length != 1)
        {
            return false;
        }
        final DomainserverProject project = nodes[0].getLookup().lookup(
                DomainserverProject.class);
        if(project == null)
        {
            return false;
        }
        if(project.isConnected() || project.isConnectionInProgress())
        {
            return false;
        }
        if(!nodes[0].getName().equals(project.getProjectDirectory().getName()))
        {
            return false;
        }
        return lockExists(project);
    }
    
    private boolean lockExists(final DomainserverProject project)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("checking for locks"); // NOI18N
        }
        Connection con = null;
        ResultSet set = null;
        try        
        {
            final FutureTask<Connection> task = new FutureTask(new Callable<
                    Connection>()
            {
                @Override
                public Connection call() throws Exception
                {
                    return DatabaseConnection.getConnection(project.
                            getRuntimeProps(), 2);
                }
            });
            RequestProcessor.getDefault().post(task);
            con = task.get(300, TimeUnit.MILLISECONDS);
            set = con.createStatement().executeQuery(DomainserverProject.
                    STMT_READ_LOCKS);
            return set.next();
        }catch(final Exception e)        
        {
            LOG.warn("could not check for locks", e); // NOI18N
            // possibly notify the user
            return false;
        }finally
        {
            try
            {
                if(set != null)
                {
                    set.close();
                }
            }catch(final SQLException e)
            {
                LOG.warn("could not close set", e); // NOI18N
            }
            try
            {
                if(con != null)
                {
                    con.close();
                }
            }catch(final SQLException e)
            {
                LOG.warn("could not close connection", e); // NOI18N
            }
        }
    }
}
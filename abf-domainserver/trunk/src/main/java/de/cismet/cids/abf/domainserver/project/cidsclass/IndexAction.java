/*
 * IndexAction.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.util.Cancelable;
import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 * 
 * @author martin.scholl@cismet.de
 */
public final class IndexAction extends CookieAction implements Cancelable
{
    private static final transient Logger LOG = Logger.getLogger(
            IndexAction.class);

    private transient Backend backend;
    private transient boolean canceled;

    @Override
    protected void performAction(final Node[] nodes)
    {
        canceled = false;
        final LinkedList<CidsClass> classes = new LinkedList<CidsClass>();
        for(final Node n : nodes)
        {
            final CidsClassContextCookie classCookie = 
                    n.getCookie(CidsClassContextCookie.class);
            classes.add(classCookie.getCidsClass());
        }
        final DomainserverContext domainCookie =
                nodes[0].getCookie(DomainserverContext.class);
        backend = domainCookie.getDomainserverProject().
                getCidsDataObjectBackend();
        final IndexActionDialog dialog = new IndexActionDialog(
                WindowManager.getDefault().getMainWindow(),
                true,
                classes.size(),
                (Cancelable)this);
        backend.addProgressListener(dialog);
        final Thread indexActionThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final Iterator<CidsClass> it = classes.iterator();
                    while(it.hasNext() && !canceled)
                    {
                        backend.refreshIndex(it.next());
                        if(!canceled)
                        {
                            dialog.nextClass();
                        }
                    }
                }catch(final SQLException ex)
                {
                    LOG.error("could not index class", ex); // NOI18N
                    LOG.error("next ex", ex.getNextException()); // NOI18N
                    dialog.setError(org.openide.util.NbBundle.getMessage(
                            IndexAction.class, "Err_duringIndexing"),// NOI18N
                            ex);
                }
            }
        });
        indexActionThread.setPriority(6);
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                dialog.setVisible(true);
            }
        });
        indexActionThread.start();
    }

    @Override
    protected int mode()
    {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName()
    {
        return NbBundle.getMessage(
                IndexAction.class, "CTL_IndexAction"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses()
    {
        return new Class[]
        {
            CidsClassContextCookie.class,
            DomainserverContext.class
        };
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
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
    protected boolean enable(final Node[] nodes)
    {
        if(!super.enable(nodes))
        {
            return false;
        }
        return nodes[0].getCookie(DomainserverContext.class)
                .getDomainserverProject().isConnected();
    }

    @Override
    public void cancel()
    {
        canceled = true;
        backend.cancel();
    }
}
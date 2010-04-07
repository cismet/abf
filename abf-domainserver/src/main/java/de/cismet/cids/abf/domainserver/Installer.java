/*
 * Installer.java, encoding: UTF-8
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

package de.cismet.cids.abf.domainserver;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class Installer extends ModuleInstall
{
    @Override
    public void restored()
    {
        // TODO: use external log4j config
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        // explicit configuration of level per package causes dublicate log
        // messages, should globally set non additive then, how?
        // cids stuff
        //p.put("log4j.logger.de.cismet.cids", "INFO,Remote");
        // hibernate main, configure individually using the variables below
        p.put("log4j.logger.org.hibernate", "WARN,Remote");
        // hql parser activity
//        p.put("log4j.logger.org.hibernate.hql.ast.AST", "WARN,Remote");
//        // sql
//        p.put("log4j.logger.org.hibernate.SQL", "WARN,Remote");
//        // jdbc bind parameters
//        p.put("log4j.logger.org.hibernate.type", "WARN,Remote");
//        // schema export/update
//        p.put("log4j.logger.org.hibernate.tool.hbm2ddl", "WARN,Remote");
//        // hql parse trees
//        p.put("log4j.logger.org.hibernate.hql", "WARN,Remote");
//        // cache activity
//        p.put("log4j.logger.org.hibernate.cache", "WARN,Remote");
//        // transaction activity
//        p.put("log4j.logger.org.hibernate.transaction", "WARN,Remote");
//        //jdbc resource acquisition
//        p.put("log4j.logger.org.hibernate.jdbc", "WARN,Remote");
        // c3p0 connection pool logging
        p.put("log4j.logger.com.mchange.v2", "WARN,Remote");
        org.apache.log4j.PropertyConfigurator.configure(p);
    }
    
    @Override
    public boolean closing()
    {
        for(final Object o : TopComponent.getRegistry().getOpened())
        {
            if(o instanceof ClassDiagramTopComponent)
            {
                ((ClassDiagramTopComponent)o).componentClosed();
            }
        }
        for(final Project p : OpenProjects.getDefault().getOpenProjects())
        {
            if(p instanceof DomainserverProject)
            {
                ((DomainserverProject)p).setConnected(false);
            }
        }
        return super.closing();
    }
}
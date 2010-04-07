/*
 * DomainserverProjectFactory.java, encoding: UTF-8
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

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class DomainserverProjectFactory implements ProjectFactory
{
    private static final transient Logger LOG = Logger.getLogger(DomainserverProjectFactory.class);
    
    public static final String PROJECT_DIR = "cidsDomainServer"; // NOI18N
    public static final String PROJECT_PROPFILE = "project.properties";// NOI18N
    
    public DomainserverProjectFactory()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("new DomainserverProjectFactory created"); // NOI18N
        }
    }
    
    @Override
    public Project loadProject(final FileObject dir, final ProjectState state) 
            throws 
            IOException
    {
        return isProject(dir) ? new DomainserverProject(dir, state) : null;
    }
    
    @Override
    public void saveProject(final Project project) throws 
            IOException, 
            ClassCastException
    {
        final Properties properties = project.getLookup()
                .lookup(Properties.class);
        final FileObject fob = project.getProjectDirectory().getFileObject(
                    PROJECT_DIR
                    + "/" // NOI18N
                    + PROJECT_PROPFILE);
        properties.store(fob.getOutputStream(),
                "Cids Domainserver Project Properties"); // NOI18N
    }
    
    @Override
    public boolean isProject(final FileObject projectDirectory)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("isProject: " // NOI18N
                    + projectDirectory.getPath()
                    + " :: " // NOI18N
                    + projectDirectory.getFileObject(PROJECT_DIR) != null);
        }
        return projectDirectory.getFileObject(PROJECT_DIR) != null;
    }
}
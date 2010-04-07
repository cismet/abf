/*
 * ClientProjectFactory.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:24
 *
 */
package de.cismet.cids.abf.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class ClientProjectFactory implements ProjectFactory
{
    public static final String PROJECT_DIR = "cidsClient"; // NOI18N
    public static final String PROJECT_PROPFILE = "project.properties";// NOI18N

    @Override
    public Project loadProject(final FileObject dir, final ProjectState state)
            throws
            IOException
    {
        return isProject(dir) ? new ClientProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws
            IOException,
            ClassCastException
    {
        final FileObject projectRoot = project.getProjectDirectory();
        if(projectRoot.getFileObject(PROJECT_DIR) == null)
        {
            throw new IOException("Project dir " // NOI18N
                    + projectRoot.getPath()
                    + " deleted, cannot save project"); // NOI18N
        }
        //Force creation of the scenes/ dir if it was deleted
        ((ClientProject) project).getWebinterfaceFolder(true);
        //Find the properties file pvproject/project.properties,
        //creating it if necessary
        final String propsPath = PROJECT_DIR + "/" + PROJECT_PROPFILE; // NOI18N
        FileObject propertiesFile = projectRoot.getFileObject(propsPath);
        if(propertiesFile == null)
        {
            //Recreate the properties file if needed
            propertiesFile = projectRoot.createData(propsPath);
        }
        final Properties properties = (Properties)project.getLookup().lookup(
                Properties.class);
        properties.store(new FileOutputStream(FileUtil.toFile(propertiesFile)),
                "Cids Client Project Properties"); // NOI18N
    }

    @Override
    public boolean isProject(final FileObject projectDirectory)
    {
        return projectDirectory.getFileObject(PROJECT_DIR) != null;
    }
}
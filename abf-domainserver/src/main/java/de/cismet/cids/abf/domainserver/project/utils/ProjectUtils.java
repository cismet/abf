/*
 * ProjectUtils.java, encoding: UTF-8
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
 * Created on 20. Februar 2008, 15:54
 *
 */

package de.cismet.cids.abf.domainserver.project.utils;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class ProjectUtils
{
    private static final transient Logger LOG = Logger.getLogger(
            ProjectUtils.class);
    
    private ProjectUtils()
    {
    }
    
    public static Image getImageForIconAndProject(final Icon icon, final
            DomainserverProject project)
    {
        String iconDir = project.getRuntimeProps()
                .get("iconDirectory").toString(); // NOI18N
        // maybe use of FileObject would be nicer, but as long as one cannot
        // ensure that the iconDir does not contain . or .. the use of 
        // FileObject is not recommended. That is because MasterFileObject 
        // cannot handle these paths correctely at least when trying to resolve
        // a FileObject using the getFileObject method
        final File baseFile = FileUtil.toFile(project.getProjectDirectory());
        final String internalSeparator = project.getRuntimeProps().get(
                    "fileSeparator").toString(); // NOI18N
        iconDir = iconDir.replace(internalSeparator, File.separator);
        final File imageFile = new File(baseFile, iconDir + File.separator + 
                icon.getFileName());
        try
        {
            return ImageIO.read(imageFile);
        } catch(final Exception ex)
        {
            LOG.warn("image retrieval failed:" // NOI18N
                    + icon.getFileName(), ex);
        }
        return null;
    }
    
    public static boolean isRemoteGroup(final UserGroup ug, final 
            DomainserverProject p)
    {
        final Domain d = ug.getDomain();
        final String domainname = p.getRuntimeProps().getProperty(
                "serverName"); // NOI18N
        return !(d.getName().equalsIgnoreCase("LOCAL") || // NOI18N
                 d.getName().equalsIgnoreCase(domainname));
    }
}
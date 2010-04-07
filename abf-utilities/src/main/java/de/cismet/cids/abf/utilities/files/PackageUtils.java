/*
 * PackageUtils.java, encoding: UTF-8
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
 * Created on 21. August 2007, 10:12
 */

package de.cismet.cids.abf.utilities.files;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mscholl
 * @version 1.1
 */
public final class PackageUtils
{
    // TODO: add checks like is pakkage children of srcroot etc, null checks and
    //       think of possibilities you have if a folder contains spaces
    //       (exception??)
    
    public static final String ROOT_PACKAGE = "<package root>"; // NOI18N
    
    /**
     * Creates a new instance of PackageUtils
     */
    private PackageUtils()
    {
    }
    
    public static String toPackage(final FileObject srcRoot, final FileObject 
            pakkage)
    {
        if(srcRoot.equals(pakkage))
        {
            return ROOT_PACKAGE;
        }
        return pakkage.getPath()
                .replace(srcRoot.getPath(), "") // NOI18N
                .substring(1).replace("/", "."); // NOI18N
    }
    
    public static String toRelativePath(final String pakkage, final boolean 
            systemDependant)
    {
        if(pakkage.equals(ROOT_PACKAGE))
        {
            return ""; // NOI18N
        }
        if(systemDependant)
        {
            return pakkage.replace(".", File.separator); // NOI18N
        }
        return pakkage.replace(".", "/"); // NOI18N
    }
    
    public static String toAbsolutePath(final FileObject srcRoot,
            final String pakkage, final boolean systemDependant)
    {
        String srcPath;
        final String separator;
        if(systemDependant)
        {
            srcPath = FileUtil.toFile(srcRoot).getAbsolutePath();
            separator = File.separator;
        }else
        {
            separator = "/"; // NOI18N
            srcPath = srcRoot.getPath();
            if(!srcPath.startsWith(separator))
            {
                srcPath = separator + srcPath;
            }
        }
        if(pakkage.equals(ROOT_PACKAGE))
        {
            return srcPath;
        }
        return srcPath + separator + toRelativePath(pakkage, true);
    }
    
    public static FileObject toFileObject(final FileObject srcRoot,
            final String pakkage)
    {
        return srcRoot.getFileObject(toRelativePath(pakkage, false));
    }
}
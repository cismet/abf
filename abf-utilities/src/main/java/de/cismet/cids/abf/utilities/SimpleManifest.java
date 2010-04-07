/*
 * SimpleManifest.java, encoding: UTF-8
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
 * Created on 21. Dezember 2007, 13:40
 *
 */

package de.cismet.cids.abf.utilities;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author mscholl
 */
public final class SimpleManifest
{
    private String manifestName;
    private String manifestPath;
    private String[] classPath;
    
    public SimpleManifest(final String manifestName, final String manifestPath, 
            final String[] classPath)
    {
        setManifestName(manifestName);
        this.manifestPath = manifestPath;
        this.classPath = Arrays.copyOf(classPath, classPath.length);
    }
    
    public SimpleManifest(final String manifestName, final String manifestPath, 
            final Collection<String> classPath)
    {
        this.manifestPath = manifestPath;
        setManifestName(manifestName);
        setClassPath(classPath);
    }

    public String getManifestName()
    {
        return manifestName;
    }

    public void setManifestName(final String manifestName)
    {
        this.manifestName = manifestName.substring(0, 1).toUpperCase() + 
                manifestName.substring(1);
    }

    public String[] getClassPath()
    {
        return Arrays.copyOf(classPath, classPath.length);
    }

    public void setClassPath(final String[] classPath)
    {
        this.classPath = Arrays.copyOf(classPath, classPath.length);
    }
    
    public void setClassPath(final Collection<String> classPath)
    {
        this.classPath = new String[classPath.size()];
        classPath.toArray(this.classPath);
    }
    
    public String getManifestPath()
    {
        return manifestPath;
    }
    
    public void setManifestPath(final String manifestPath)
    {
        this.manifestPath = manifestPath;
    }
    
    @Override
    public String toString()
    {
        return manifestName + " :: " + manifestPath; // NOI18N
    }
}
/*
 * DeployInformation.java, encoding: UTF-8
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
 * Created on 27. August 2007, 17:37
 *
 */

package de.cismet.cids.abf.librarysupport.project.util;

import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.ManifestProviderCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author mscholl
 * @version 1.6
 */
public final class DeployInformation
{
    private static final transient Logger LOG = Logger.getLogger(
            DeployInformation.class);
    
    private FileObject buildXML;
    private FileObject sourceDir;
    private FileObject keystore;
    private FileObject manifest;
    private String destFilePath;
    private String alias;
    private String storepass;
    
    
    public DeployInformation(
            final FileObject buildXML, 
            final FileObject sourceDir, 
            final String destFilePath, 
            final FileObject keystore, 
            final String alias, 
            final String storepass, 
            final FileObject manifest)
    {
        this.buildXML = buildXML;
        this.sourceDir = sourceDir;
        this.keystore = keystore;
        this.manifest = manifest;
        this.destFilePath = destFilePath;
        this.alias = alias;
        this.storepass = storepass;
    }

    public FileObject getBuildXML()
    {
        return buildXML;
    }

    public void setBuildXML(final FileObject buildXML)
    {
        this.buildXML = buildXML;
    }

    public FileObject getSourceDir()
    {
        return sourceDir;
    }

    public void setSourceDir(final FileObject sourceDir)
    {
        this.sourceDir = sourceDir;
    }

    public FileObject getKeystore()
    {
        return keystore;
    }

    public void setKeystore(final FileObject keystore)
    {
        this.keystore = keystore;
    }

    public FileObject getManifest()
    {
        return manifest;
    }

    public void setManifest(final FileObject manifest)
    {
        this.manifest = manifest;
    }

    public String getDestFilePath()
    {
        return destFilePath;
    }

    public void setDestFilePath(final String destFilePath)
    {
        this.destFilePath = destFilePath;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(final String alias)
    {
        this.alias = alias;
    }

    public String getStorepass()
    {
        return storepass;
    }

    public void setStorepass(final String storepass)
    {
        this.storepass = storepass;
    }
    
    public static DeployInformation getDeployInformation(final Node n)
    {
        try
        {
            final SourceContextCookie sourceCookie = (SourceContextCookie)n.
                    getCookie(SourceContextCookie.class);
            final LibrarySupportContextCookie libCC= (
                    LibrarySupportContextCookie)n.getCookie(
                    LibrarySupportContextCookie.class);
            final ManifestProviderCookie manifestProvider = (
                    ManifestProviderCookie)n.getCookie(ManifestProviderCookie.
                    class);
            final FileObject manifest;
            if(manifestProvider == null)
            {
                manifest = libCC.getLibrarySupportContext().
                        getDefaultManifest();
            }else
            {
                manifest = manifestProvider.getManifest();
            }
            final FileObject srcFile = sourceCookie.getSourceObject();
            final File destFilePath = FileUtil.toFile(srcFile.getParent().
                    getParent().getParent());
            final PropertyProvider provider = PropertyProvider.getInstance(
                    libCC.getLibrarySupportContext().getProjectProperties());
            final FileObject keystore = FileUtil.toFileObject(new File(provider.
                    get(PropertyProvider.KEY_GENERAL_KEYSTORE)));
            final String passwd = provider.get(PropertyProvider.
                    KEY_GENERAL_KEYSTORE_PW);
            final String destFile = destFilePath.getAbsolutePath() 
                    + System.getProperty("file.separator") // NOI18N
                    + srcFile.getName() + ".jar"; // NOI18N
            return new DeployInformation(
                        libCC.getLibrarySupportContext().getBuildXML(), 
                        srcFile,
                        destFile,
                        keystore,
                        null, 
                        passwd,
                        manifest);
        } catch (final FileNotFoundException ex)
        {
            LOG.error("could not create deploy information", ex); // NOI18N
            return null;
        }
    }
}
/*
 * PropertyProvider.java, encoding: UTF-8
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
 * Created on 25. August 2007, 15:50
 *
 */

package de.cismet.cids.abf.librarysupport.project.customizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mscholl
 * @version 1.2
 */
public final class PropertyProvider
{
    private static final transient Logger LOG = Logger.getLogger(
            PropertyProvider.class);
    
    public static final String KEY_AUTORELOAD = "autoreload"; // NOI18N
    public static final String KEY_GENERAL_KEYSTORE = 
            "generalKeystorePath"; // NOI18N
    public static final String KEY_USE_INDIVIDUAL_KEYSTORES = 
            "useIndividualKeystores"; // NOI18N
    public static final String KEY_GENERAL_MANIFEST = 
            "generalManifest"; // NOI18N
    public static final String KEY_GENERAL_KEYSTORE_PW = 
            "generalKeystorePW"; // NOI18N
    
    private final transient FileObject propFile;

    private transient Properties properties;

    private static PropertyProvider provider;
    
    /** Creates a new instance of PropertyProvider */
    private PropertyProvider(final FileObject propFile)
    {
        this.propFile = propFile;
    }
    
    public synchronized static PropertyProvider getInstance(
            final FileObject propFile)
    {
        if(provider == null)
        {
            provider = new PropertyProvider(propFile);
        }
        return provider;
    }
    
    public void load()
    {
        synchronized(this)
        {
            properties = new Properties();
            try
            {
                properties.load(new BufferedInputStream(
                        propFile.getInputStream()));
            }catch(final Exception ex)
            {
                LOG.fatal("project properties could not be loaded", ex);//NOI18N
                ErrorManager.getDefault().annotate(
                        ex, "project properties could not be loaded"); // NOI18N
                properties = null;
            }
        }
    }
    
    public void save()
    {
        synchronized(this)
        {
            assert properties != null;
            OutputStream out = null;
            final File file = FileUtil.toFile(propFile);
            try
            {
                out = new BufferedOutputStream(new FileOutputStream(file));
                properties.store(out, ""); // NOI18N
            }catch(final IOException ex)
            {
                LOG.fatal("project properties could not be saved", ex);// NOI18N
                ErrorManager.getDefault().annotate(
                        ex, "project properties could not be saved"); // NOI18N
            }finally
            {
                if(out != null)
                {
                    try
                    {
                        out.close();
                    }catch(final IOException ex)
                    {
                        // do nothing
                        LOG.warn(
                                "outputstream could not be closed", ex);//NOI18N
                    }
                }
                clearInternal();
            }
        }
    }
    
    public String get(final String key)
    {
        if(properties == null)
        {
            load();
        }
        assert properties != null;
        return properties.getProperty(key);
    }
    
    public void put(final String key, final String value)
    {
        if(properties == null)
        {
            load();
        }
        assert properties != null;
        properties.put(key, value);
    }
    
    void clearInternal()
    {
        properties = null;
    }
}
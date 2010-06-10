/*
 * LibrarySupportProject.java, encoding: UTF-8
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
 * Created on 10. Juli 2007, 11:02
 *
 */

package de.cismet.cids.abf.librarysupport.project;

import de.cismet.cids.abf.librarysupport.project.customizer.LibrarySupportProjectCustomizer;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author mscholl
 * @version 1.6
 */
public final class LibrarySupportProject implements Project
{
    private static final transient Logger LOG = Logger.getLogger(
            LibrarySupportProject.class);
    
    public static final String EXT_DIR = "ext"; // NOI18N
    public static final String INT_DIR = "int"; // NOI18N
    public static final String STARTER_DIR = "starter"; // NOI18N
    public static final String LOCAL_DIR = "local"; // NOI18N

    public static final String IMAGE_FOLDER =
            "de/cismet/cids/abf/librarysupport/images/"; // NOI18N
    
    private final transient FileObject distDir;
    private final transient ProjectState state;
    private final transient LibrarySupportLogicalView view;
    private transient Lookup lookup;
    
    /**
     * Creates a new instance of LibrarySupportProject
     */
    public LibrarySupportProject(final FileObject dir, final ProjectState state)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("constructing..."); // NOI18N
        }
        this.distDir = dir;
        this.state = state;
        this.view = new LibrarySupportLogicalView(this);
        if(LOG.isDebugEnabled())
        {
            LOG.debug("construction finished"); // NOI18N
        }
    }
    
    @Override
    public FileObject getProjectDirectory()
    {
        return distDir;
    }
    
    public FileObject getProjectProperties()
    {
        return distDir.getFileObject(
                "cidsLibBase/project.properties"); // NOI18N
    }
    
    public FileObject getBuildXML()
    {
        return distDir.getFileObject(
                "cidsLibBase/resource/build.xml"); // NOI18N
    }
    
    public FileObject getDefaultManifest()
    {
        return distDir.getFileObject(
                "cidsLibBase/resource/default.mf"); // NOI18N
    }
    
    /**
     * @depricated removed for safety reasons, you have to provide your own 
     *      keystore
     */
    public FileObject getKeystore()
    {
        return distDir.getFileObject(
                "cidsLibBase/resource/.keystore"); // NOI18N
    }
    
    @Override
    public Lookup getLookup()
    {
        if(lookup == null)
        {
            lookup = Lookups.fixed(new Object[] 
            {
               this,
               state,
               distDir,
               new Info(),
               new LibrarySupportProjectCustomizer(this),
               view
            });
        }
        return lookup;
    }
    
    void addLookup(final Lookup lkp)
    {
        if(lkp != null)
        {
            lookup = new ProxyLookup(getLookup(), lkp);
        }
    }
    
    private final class Info implements ProjectInformation
    {
        private final transient ImageIcon icon = new ImageIcon(
                ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                        + "libbase.png")); // NOI18N

        @Override
        public Icon getIcon()
        {
            return icon;
        }

        @Override
        public String getName()
        {
            return java.util.ResourceBundle.getBundle(
                    "de/cismet/cids/abf/librarysupport/project/Bundle")// NOI18N
                    .getString("LibraryManagementProjectName"); // NOI18N
        }

        @Override
        public String getDisplayName()
        {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener p)
        {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener p)
        {
            //do nothing, won't change
        }

        @Override
        public Project getProject()
        {
            return LibrarySupportProject.this;
        }
    }
}
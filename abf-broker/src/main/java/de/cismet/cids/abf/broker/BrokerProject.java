/*
 * BrokerProject.java, encoding: UTF-8
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
 * Created on 29. September 2006, 10:23
 *
 */
package de.cismet.cids.abf.broker;

import de.cismet.cids.abf.utilities.project.NotifyProperties;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class BrokerProject implements Project
{
    public static final String IMAGE_FOLDER =
            "de/cismet/cids/abf/broker/images/"; // NOI18N
    public static final String WEBINTERFACE_DIR = "webinterface"; //NOI18N
    
    private final transient FileObject projectDir;
    private final transient ProjectState state;
    private final transient LogicalViewProvider logicalView;
    private transient Lookup lkp;

    public BrokerProject(final FileObject projectDir, final ProjectState state)
    {
        this.projectDir = projectDir;
        this.state = state;
        logicalView = new BrokerLogicalView(this);
    }

    @Override
    public FileObject getProjectDirectory()
    {
        return projectDir;
    }

    FileObject getWebinterfaceFolder(final boolean create)
    {
        FileObject result =
                projectDir.getFileObject(WEBINTERFACE_DIR);
        if(result == null && create)
        {
            try
            {
                result = projectDir.createFolder(WEBINTERFACE_DIR);
            }catch(final IOException ioe)
            {
                ErrorUtils.showErrorMessage(
                        org.openide.util.NbBundle.getMessage(
                        BrokerProject.class,
                        "Err_couldNotCreateWebFolder"), ioe); // NOI18N
            }
        }
        return result;
    }

    @Override
    public Lookup getLookup()
    {
        if(lkp == null)
        {
            lkp = Lookups.fixed(new Object[]
            {
                this, //project spec requires a project be in its own lookup
                state, //allow outside code to mark the project eg. need saving
                new ActionProviderImpl(), //Provides standard actions
                loadProperties(), //The project properties
                new Info(), //Project information implementation
                logicalView, //Logical view of project implementation
            });
            return lkp;
        }
        return lkp;
    }

    private Properties loadProperties()
    {
        final FileObject fob = projectDir.getFileObject(
                BrokerProjectFactory.PROJECT_DIR
                + "/" // NOI18N
                + BrokerProjectFactory.PROJECT_PROPFILE);
        final Properties properties = new NotifyProperties(state);
        if(fob != null)
        {
            try
            {
                properties.load(fob.getInputStream());
            }catch(final IOException e)
            {
                ErrorUtils.showErrorMessage(
                        org.openide.util.NbBundle.getMessage(
                        BrokerProject.class, 
                        "Err_loadingProjectProps"), e); // NOI18N
            }
        }
        return properties;
    }

    private final class ActionProviderImpl implements ActionProvider
    {
        @Override
        public String[] getSupportedActions()
        {
            return new String[0];
        }

        @Override
        public void invokeAction(final String string, final Lookup lookup)
                throws
                IllegalArgumentException
        {
            //do nothing
        }

        @Override
        public boolean isActionEnabled(final String string, final Lookup lookup)
                throws
                IllegalArgumentException
        {
            return false;
        }
    }

    /** Implementation of project system's ProjectInformation class */
    private final class Info implements ProjectInformation
    {
        private final transient Icon icon;

        Info()
        {
            icon = new ImageIcon(ImageUtilities.loadImage(
                    IMAGE_FOLDER + "broker.png")); // NOI18N
        }

        @Override
        public Icon getIcon()
        {
            return icon;
        }

        @Override
        public String getName()
        {
            return getProjectDirectory().getName();
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
            return BrokerProject.this;
        }
    }
}
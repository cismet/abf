/*
 * IconNode.java, encoding: UTF-8
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
 * Created on 10. Januar 2007, 14:51
 *
 */

package de.cismet.cids.abf.domainserver.project.icons;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.nodes.IconManagement;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.12
 */
public final class IconNode extends ProjectNode
{
    private static final transient Logger LOG = Logger.getLogger(
            IconNode.class);
    
    private transient Icon icon;
    private final transient Image defaultIcon;
    
    public IconNode(final Icon icon, final DomainserverProject project)
    {
        super(Children.LEAF, project);
        this.icon = icon;
        defaultIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "javaclass.png"); // NOI18N
    }

    // overriden because the icon may change
    @Override
    public String getName()
    {
        return icon.getName();
    }

    // overriden because the icon may change
    @Override
    public String getDisplayName()
    {
        return icon.getName();
    }
    
    @Override
    public Image getIcon(final int i)
    {
        Image image = ProjectUtils.getImageForIconAndProject(icon, project);
        if(image == null)
        {
            image = defaultIcon;
        }
        return image;
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[] 
        {
            CallableSystemAction.get(DeleteAction.class)
        };
    }
    
    @Override
    public boolean canDestroy()
    {
        return true;
    }
    
    @Override
    public void destroy() throws IOException
    {
        if(project.getCidsDataObjectBackend().stillReferenced(icon))
        {
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                IconNode.class, "IconNode.destroy().JOptionPane.iconNotDeletable.message"),//NOI18N
                            org.openide.util.NbBundle.getMessage(
                                IconNode.class, "IconNode.destroy().JOptionPane.iconNotDeletable.title"),//NOI18N
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
            return;
        }
        try
        {
            project.getCidsDataObjectBackend().deleteIcon(icon);
            ((IconManagement)project.getLookup()
                    .lookup(IconManagement.class)).refreshChildren();
        }catch(final Exception e)
        {
            LOG.error("error during icon deletion", e); // NOI18N
            ErrorManager.getDefault().notify(e);
        }
    }
    
    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set main = Sheet.createPropertiesSet();
        main.setName(org.openide.util.NbBundle.getMessage(
                IconNode.class, "IconNode.createSheet().main.name")); // NOI18N
        main.setDisplayName(org.openide.util.NbBundle.getMessage(
                IconNode.class, "IconNode.createSheet().main.displayName")); // NOI18N
        try
        {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ID ">
            final Property idProp = new PropertySupport.Reflection(icon, 
                    Integer.class, "getId", null); // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    IconNode.class, "IconNode.createSheet().idProp.name"));  // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
            final Property nameProp = new PropertySupport(
                    "name", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        IconNode.class, "IconNode.createSheet().nameProp.description"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        IconNode.class, "IconNode.createSheet().nameProp.descriptionOfIcon"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return icon.getName();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Icon old = icon;
                    try
                    {
                        icon.setName(object.toString());
                        icon = project.getCidsDataObjectBackend().store(icon);
                        fireDisplayNameChange(null, getDisplayName());
                    }catch(final Exception e)
                    {
                        LOG.error("could not store icon", e); // NOI18N
                        icon = old;
                        ErrorManager.getDefault().notify(e);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: PathProp ">
            final Property pathProp = new PropertySupport(
                    "path", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        IconNode.class, "IconNode.createSheet().pathProp.filename"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        IconNode.class, "IconNode.createSheet().pathProp.filenameOfIcon"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return icon.getFileName();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Icon old = icon;
                    try
                    {
                        icon.setFileName(object.toString());
                        icon = project.getCidsDataObjectBackend().store(icon);
                    }catch(final Exception e)
                    {
                        LOG.error("could not store icon", e); // NOI18N
                        icon = old;
                        ErrorManager.getDefault().notify(e);
                    }
                }
            };// </editor-fold>
            main.put(idProp);
            main.put(nameProp);
            main.put(pathProp);
            sheet.put(main);
        }catch (final Exception ex)
        {
            LOG.error("could not create sheet for icon: " // NOI18N
                    + icon.getName(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        return sheet;
    }
}
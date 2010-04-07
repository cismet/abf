/*
 * Renderers.java, encoding: UTF-8
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
 * Created on 26. November 2007, 12:50
 *
 */

package de.cismet.cids.abf.domainserver.project.utils;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.common.CommonEntity;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Component;
import java.awt.Image;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.openide.util.ImageUtilities;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class Renderers
{
    // TODO: be aware of performance issues
    private static final DefaultListCellRenderer LIST_R = new
            DefaultListCellRenderer();
    private static final DefaultTableCellRenderer TABLE_R = new
            DefaultTableCellRenderer();

    private Renderers()
    {
    }
    
    public final static class UnifiedCellRenderer implements 
            ListCellRenderer,
            TableCellRenderer
    {
        private static final transient Logger LOG = Logger.getLogger(
                UnifiedCellRenderer.class);

        private final transient ImageIcon groupIcon = new ImageIcon(
                ImageUtilities.loadImage(DomainserverProject.class
                + "group.png")); // NOI18N
        private final transient ImageIcon remotegroupIcon = new ImageIcon(
                ImageUtilities.loadImage(DomainserverProject.class
                + "remotegroup.png")); // NOI18N
        
        private final transient PermissionResolver resolver;
        private final transient CommonEntity entity;

        public UnifiedCellRenderer()
        {
            this(null, null);
        };

        public UnifiedCellRenderer(final DomainserverProject project, final
                CommonEntity entity)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("creating new UnifiedCellRenderer: project: "// NOI18N
                        + project + " :: entity: " + entity); // NOI18N
            }
            this.entity = entity;
            if(project == null || entity == null)
            {
                resolver = null;
            }else
            {
                resolver = PermissionResolver.getInstance(project);
            }
        }
        
        @Override
        public Component getListCellRendererComponent(final JList list, final 
                Object value, final int index, final boolean isSelected, final 
                boolean cellHasFocus)
        {
            final JLabel label = (JLabel)LIST_R.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            modifyLabel(label, value);
            return label;
        }

        @Override
        public Component getTableCellRendererComponent(final JTable t, final 
                Object value, final boolean isSelected, final boolean hasFocus, 
                final int row, final int column)
        {
            final JLabel label = (JLabel)TABLE_R.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, column);
            modifyLabel(label, value);
            return label;
        }
        
        private void modifyLabel(final JLabel label, final Object value)
        {
            if(value != null)
            {
                if(value instanceof Permission)
                {
                    final Permission perm = (Permission)value;
                    String s;
                    if(resolver == null)
                    {
                        s = perm.getDescription();
                    }else
                    {
                        s = resolver.getPermString(entity, perm)
                                .getPermissionString();
                    }
                    if(s == null)
                    {
                        s = perm.getKey();
                    }
                    label.setText(s);
                    label.setIcon(null);
                }else if(value instanceof UserGroup)
                {
                    final UserGroup ug = (UserGroup)value;
                    final StringBuffer name = new StringBuffer(ug.getName());
                    Icon icon = groupIcon;
                    if(!"local".equalsIgnoreCase( // NOI18N
                            ug.getDomain().getName()))
                    {
                        name.append('@') // NOI18N
                                .append(ug.getDomain().getName());
                        icon = remotegroupIcon;
                    }
                    label.setText(name.toString());
                    label.setIcon(icon);
                }else if(value instanceof Domain)
                {
                    label.setText(((Domain)value).getName());
                    label.setIcon(null);
                }else if(value instanceof CidsClass)
                {
                    label.setText(((CidsClass)value).getTableName());
                    label.setIcon(null);
                }else if(value instanceof Policy)
                {
                    throw new UnsupportedOperationException("tbd"); // NOI18N
                }else if(value instanceof JLabel)
                {
                    final JLabel lbl = (JLabel)value;
                    label.setText(lbl.getText());
                    label.setIcon(lbl.getIcon());
                }
            }
        }
    }
    
    public final static class JavaClassTypeRenderer implements ListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(final JList list, final 
                Object value, final int index, final boolean isSelected, final 
                boolean cellHasFocus)
        {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            if(value == null)
            {
                return l;
            }
            final String type = value.toString();
            if(type.equals(JavaClass.Type.TO_STRING.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_toStringClass")); // NOI18N
            }else if(type.equals(JavaClass.Type.RENDERER.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_renderer")); // NOI18N
            }else if(type.equals(JavaClass.Type.SIMPLE_EDITOR.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_simpleEditor")); // NOI18N
            }else if(type.equals(JavaClass.Type.COMPLEX_EDITOR.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_complexEditor")); // NOI18N
            }else if(type.equals(JavaClass.Type.UNKNOWN.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_unknownClass")); // NOI18N
            }
            return l;
        }
    }
    
    public final static class NodeTypeRenderer implements ListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(final JList list, final 
                Object value, final int index, final boolean isSelected, final 
                boolean cellHasFocus)
        {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            if(value == null)
            {
                return l;
            }
            final String type = value.toString();
            if(type.equals(CatNode.Type.CLASS.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_classNode")); // NOI18N
            }else if(type.equals(CatNode.Type.OBJECT.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_objectNode")); // NOI18N
            }else if(type.equals(CatNode.Type.ORG.getType()))
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_organisationalNode")); // NOI18N
            }else
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_unknownType")); // NOI18N
            }
            return l;
        }
    }
    
    public final static class UserGroupListRenderer implements ListCellRenderer
    {
        private final transient ImageIcon groupIcon;
        private final transient ImageIcon remotegroupIcon;

        private final transient DomainserverProject project;

        public UserGroupListRenderer(final DomainserverProject project)
        {
            groupIcon = new ImageIcon(ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "group.png")); // NOI18N
            remotegroupIcon = new ImageIcon(ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER 
                + "remotegroup.png")); // NOI18N
            this.project = project;
        }

        @Override
        public Component getListCellRendererComponent(final JList list, final 
                Object value, final int index, final boolean isSelected, final 
                boolean cellHasFocus)
        {
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            final UserGroup ug = ((UserGroup)value);
            final StringBuffer name = new StringBuffer(ug.getName());
            Icon icon = groupIcon;
            if(ProjectUtils.isRemoteGroup(ug, project))
            {
                icon = remotegroupIcon;
                name.append('@').append(ug.getDomain()); // NOI18N
            }
            l.setText(name.toString());
            l.setIcon(icon);
            return l;
        }
    }
    
    public final static class IconCellRenderer implements ListCellRenderer 
    {
        private final transient DomainserverProject project;
       
        public IconCellRenderer(final DomainserverProject project) 
        {
            this.project = project;
        }

        @Override
        public Component getListCellRendererComponent(final JList list, final 
                Object value, final int index, final boolean isSelected, final 
                boolean cellHasFocus) 
        {
            final de.cismet.cids.jpa.entity.cidsclass.Icon i = 
                    (de.cismet.cids.jpa.entity.cidsclass.Icon)value;
            final JLabel l = (JLabel)LIST_R.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            if(i == null)
            {
                l.setText(org.openide.util.NbBundle.getMessage(
                        Renderers.class, "Dsc_noIcon")); // NOI18N
                l.setIcon(null);
            }else 
            {
                final Image ii = ProjectUtils.getImageForIconAndProject(
                        i, project);
                l.setText(i.getName());
                if(ii != null)
                {
                    l.setIcon(new ImageIcon(ii));
                }
            }
            return l;
        }
    }
}
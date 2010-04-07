/*
 * TypeNode.java, encoding: UTF-8
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
 * Created on 13. März 2007, 13:00
 *
 */

package de.cismet.cids.abf.domainserver.project.types;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.7
 */
public final class TypeNode extends ProjectNode
{
    private static final transient Logger LOG = Logger.getLogger(
            TypeNode.class);
    
    private transient Type type;
    private final transient Image defaultImage;
    
    public TypeNode(final Type type, final DomainserverProject project)
    {
        super(Children.LEAF, project);
        this.type = type;
        defaultImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "datatype.png"); // NOI18N
        setName(type.getName());
        setDisplayName(type.getName());
    }

    @Override
    public Image getIcon(final int i)
    {
        Image image = null;
        try
        {
            final CidsClass c = type.getCidsClass();
            image = ProjectUtils.getImageForIconAndProject(c.getClassIcon(),
                    project);
        }catch (final Exception e)
        {
            LOG.warn("could not get image", e); // NOI18N
        }
        if(image == null)
        {
            image = defaultImage;
        }
        return image;
    }
    
    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set main = Sheet.createPropertiesSet();
        try
        {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: TypeID ">
            final Property idProp = new PropertySupport.Reflection(type, 
                    Integer.class, "getId", null); // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    TypeNode.class, "Dsc_id")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
            final Property nameProp = new PropertySupport(
                    "name", // NOI18N
                    String.class, 
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_name"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_nameOfType"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return type.getName();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Type old = type;
                    try
                    {
                        type.setName(object.toString());
                        type = project.getCidsDataObjectBackend().store(type);
                        fireDisplayNameChange(null, getDisplayName());
                    }catch(final Exception e)
                    {
                        LOG.error("could not store type", e); // NOI18N
                        type = old;
                        ErrorManager.getDefault().notify(e);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ComplexType ">
            final Property complexProp = new PropertySupport.Reflection(type, 
                    Boolean.class, "isComplexType", null); // NOI18N
            complexProp.setName(org.openide.util.NbBundle.getMessage(
                    TypeNode.class, "Dsc_complexType")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Class ">
            final Property classProp = new PropertySupport(
                    "class", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_class"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_classOfType"), // NOI18N
                    true,
                    false)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    try
                    {
                        return type.getCidsClass();
                    }catch (final Exception e)
                    {
                        return org.openide.util.NbBundle.getMessage(
                                TypeNode.class,
                                "Err_erroneousAssignment"); // NOI18N
                    }
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    // not needed
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Description ">
            final Property descriptionProp = new PropertySupport(
                    "description", String.class, // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_description"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_descriptionOfType"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    final String val = type.getDescription();
                    return val == null ? "" : val; // NOI18N
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Type old = type;
                    try
                    {
                        type.setDescription(object.toString());
                        type = project.getCidsDataObjectBackend().store(type);
                    }catch(final Exception t)
                    {
                        LOG.error("could not store type", t); // NOI18N
                        type = old;
                        ErrorManager.getDefault().notify(t);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" PropertyGroup: JavaClass ">
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Editor ">
            final JavaClassPropertyEditor editorPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.
                    SIMPLE_EDITOR, JavaClass.Type.COMPLEX_EDITOR);
            final Property editorProp = new PropertySupport(
                    "editor", // NOI18N
                    JavaClass.class, 
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_editor"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_editorClassOfType"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return type.getEditor();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Type old = type;
                    try
                    {
                        type.setEditor((JavaClass)object);
                        type = project.getCidsDataObjectBackend().store(type);
                    }catch(final Exception e)
                    {
                        LOG.error("could not store type", e); // NOI18N
                        type = old;
                        ErrorManager.getDefault().notify(e);
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return editorPropertyEditor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Renderer ">
            final JavaClassPropertyEditor rendererPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.RENDERER);
            final Property rendererProp = new PropertySupport(
                    "rendererProp",  // NOI18N
                    JavaClass.class,
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_renderer"),  // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        TypeNode.class, "Dsc_rendererClassOfType"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return type.getRenderer();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Type old = type;
                    try
                    {
                        type.setRenderer((JavaClass)object);
                        type = project.getCidsDataObjectBackend().store(type);
                    }catch(final Exception e)
                    {
                        LOG.error("could not store type", e); // NOI18N
                        type = old;
                        ErrorManager.getDefault().notify(e);
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return rendererPropertyEditor;
                }
            };// </editor-fold>
            // </editor-fold>
            main.setName(org.openide.util.NbBundle.getMessage(
                    TypeNode.class, "Dsc_properties")); // NOI18N
            main.setDisplayName(org.openide.util.NbBundle.getMessage(
                    TypeNode.class, "Dsc_properties")); // NOI18N
            main.put(idProp);
            main.put(nameProp);
            main.put(descriptionProp);
            main.put(classProp);
            main.put(complexProp);
            main.put(editorProp);
            main.put(rendererProp);
            sheet.put(main);
        }
        catch (final Exception ex)
        {
            LOG.error("could not create propertysheet", ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        }
        return sheet;
    }
}
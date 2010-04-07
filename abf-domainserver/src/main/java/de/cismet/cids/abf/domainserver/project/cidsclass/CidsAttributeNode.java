/*
 * CidsAttributeNode.java, encoding: UTF-8
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
 * Created on 5. Januar 2007, 15:09
 *
 */

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.permission.AttributePermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.apache.log4j.Logger;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public final class CidsAttributeNode extends ProjectNode implements
        CidsClassContextCookie,
        CidsAttributeContextCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            CidsAttributeNode.class);
    
    private final transient Image attributeImage;
    private final transient Image arrayBadge;
    private final transient Image foreignKeyBadge;
    private final transient Image primaryKeyBadge;
    private final transient Image indexBadge;

    private final transient PermissionResolver permResolve;

    private transient CidsClass cidsClass;
    private transient Attribute cidsAttribute;

    public CidsAttributeNode(final Attribute cidsAttribute, final CidsClass 
            cidsClass, final DomainserverProject project)
    {
        super(Children.LEAF, project);
        this.cidsClass = cidsClass;
        this.cidsAttribute = cidsAttribute;
        attributeImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "attribute.png"); // NOI18N
        arrayBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "badge_array.png"); // NOI18N
        foreignKeyBadge = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER
                + "badge_foreign_key.png"); // NOI18N
        primaryKeyBadge = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "badge_key.png"); // NOI18N
        indexBadge = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                + "badge_search.png"); // NOI18N
        permResolve = PermissionResolver.getInstance(project);
    }
    
    @Override
    public Image getIcon(final int i)
    {
        Image ret = attributeImage;
        int count = 0;
        if(cidsAttribute.isIndexed())
        {
            ret = ImageUtilities.mergeImages(ret, indexBadge, 16, 8);
            count++;
        }
        if(cidsClass.getPrimaryKeyField().equals(cidsAttribute.getFieldName()))
        {
            ret = ImageUtilities.mergeImages(ret, primaryKeyBadge, 16, 8);
            count++;
        }
        if(cidsAttribute.isForeignKey())
        {
            if(count == 0)
            {
                ret = ImageUtilities.mergeImages(ret, foreignKeyBadge, 16, 8);
            }else
            {
                ret = ImageUtilities.mergeImages(ret, foreignKeyBadge, 16, 0);
            }
            count++;
        }
        if(cidsAttribute.isArray())
        {
            switch(count)
            {
                case 0:
                    ret = ImageUtilities.mergeImages(ret, arrayBadge, 16, 8);
                    break;
                case 1:
                    ret = ImageUtilities.mergeImages(ret, arrayBadge, 16, 0);
                    break;
                default:
                    ret = ImageUtilities.mergeImages(ret, arrayBadge, 8, 8);
            }
        }
        return ret;
    }
    
    /**
     * Gets the programmatic name of this feature.
     *
     *
     * @return The programmatic name of the property/method/event
     */
    @Override
    public String getName()
    {
        return cidsAttribute.getFieldName();
    }
    
    @Override
    public String getHtmlDisplayName()
    {
        if(cidsAttribute.isVisible() == null || cidsAttribute.isVisible())
        {
            return "<font color='!textText'>" + getName() + "</font>"; // NOI18N
        }else
        {
            return "<font color='!controlShadow'>" + getName() // NOI18N
                    + "</font>"; // NOI18N
        }
    }
    
    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set main = Sheet.createPropertiesSet();
        final Sheet.Set relations = Sheet.createPropertiesSet();
        final Sheet.Set classes = Sheet.createPropertiesSet();
        final Sheet.Set rightAttributes = Sheet.createPropertiesSet();
        try
        {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: CidsAttrID ">
            final Property idProp = new PropertySupport.Reflection(
                    cidsAttribute, Integer.class, "getId", null); // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Dsc_id")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
            final Property nameProp = new PropertySupport(
                    "name", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_attrName"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_nameOfAttr"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getName();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setName(object.toString());
                        project.getCidsDataObjectBackend().store(
                                getCidsAttribute());
                        fireDisplayNameChange(null, object.toString());
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store cidsAttribute name",// NOI18N
                                e);
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeAttrName"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: FieldName ">
            final Property fieldnameProp = new PropertySupport(
                    "fieldname", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_fieldName"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_fieldNameOfAttr"),//NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getFieldName();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setFieldName(object.toString());
                        project.getCidsDataObjectBackend().store(
                                getCidsAttribute());
                        refreshInDiagram();
                        project.getLookup().lookup(SyncManagement.class)
                                .refresh();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attribute fieldname",//NOI18N
                                e);
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class, 
                                    "Err_storeFieldName"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: DefaultValue ">
            final Property defaultValueProp = new PropertySupport(
                    "defaultValue", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_defaultValue"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, 
                        "Dsc_defaultValueOfAttr"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getDefaultValue();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setDefaultValue(object.toString());
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        refreshInDiagram();
                        project.getLookup().lookup(SyncManagement.class)
                                .refresh();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr default value",// NOI18N
                                e);
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeDefaultValue"), e); // NOI18N
                    }
                }
                
                @Override
                public boolean canWrite()
                {
                    return cidsAttribute.isOptional();
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Position ">
            final Property posProp = new PropertySupport(
                    "pos", // NOI18N
                    Integer.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_position"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_positionOfAttr"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getPosition();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setPosition((Integer)object);
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr position", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storePositionAttr"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Substitute ">
            final Property substituteProp = new PropertySupport(
                    "substitute", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_replace"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_replaceOfAttr"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.isSubstitute();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setSubstitute((Boolean)object);
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr substitute", e);//NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeReplacement"), e); // NOI18N
                    }
                }
                
                @Override
                public boolean canWrite()
                {
                    return getCidsAttribute().isForeignKey();
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Optional ">
            final Property optionalProp = new PropertySupport(
                    "optional", // NOI18N
                    Boolean.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_optional"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_optionalTooltip"),//NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.isOptional();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setOptional((Boolean)object);
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        refreshInDiagram();
                        project.getLookup().lookup(SyncManagement.class)
                                .refresh();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr optional", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeOptional"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Visible ">
            final Property visibleProp = new PropertySupport(
                    "visible", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_visible"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_visibleTooltip"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.isVisible();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setVisible((Boolean)object);
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        fireDisplayNameChange(null, object.toString());
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr visible", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeVisibility"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Indexed ">
            final Property indexedProp = new PropertySupport(
                    "indexed", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_indexed"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_indexedTooltip"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.isIndexed();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setIndexed((Boolean)object);
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        fireOpenedIconChange();
                        fireIconChange();
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr indexed", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeIndexed"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Description ">
            final Property descriptionProp = new PropertySupport(
                    "description", // NOI18N
                    String.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_description"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_descOfAttr"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getFieldName();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setFieldName(object.toString());
                        project.getCidsDataObjectBackend().store(
                                cidsAttribute);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr description", // NOI18N
                                e);
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class, 
                                    "Err_storeDesc"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" PropertyGroup: Java Classes ">
            // <editor-fold defaultstate="collapsed" desc=" Create Property: toString ">
            final JavaClassPropertyEditor toStringPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.TO_STRING);
            final Property toStringProp = new PropertySupport(
                    "toString", // NOI18N
                    JavaClass.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_toString"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_toStringOfAttr"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getToString();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setToString((JavaClass)object);
                        project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store cidsClass", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                CidsAttributeNode.class,
                                "Err_storeCidsclass"), e); // NOI18N
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return toStringPropertyEditor;
                }
                
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: fromString ">
//            final JavaClassPropertyEditor fromStringPropertyEditor = new 
//                    JavaClassPropertyEditor(project, JavaClassPropertyEditor.
//                    MODE_FROMSTRING);
//            Property fromStringProp = new PropertySupport("fromString", 
//                    JavaClass.class, "fromString", "fromString-Klasse des " +
//                    "Attributes", true, true)
//            {
//                public Object getValue() throws 
//                        IllegalAccessException,
//                        InvocationTargetException
//                {
//                    Object val;
//                    try
//                    {
//                        // TODO: we probably do not need that anymore because
//                        //       no lazy fetching is performed
//                        if(cidsAttribute.getFromString() != null)
//                            //zum fetchen gezwungen
//                            cidsAttribute.getFromString().toString(); 
//                        val = cidsAttribute.getFromString();
//                    }
//                    catch (Throwable e)
//                    {
//                        log.error("could not retrieve fromString property", e);
//                        ErrorManager.getDefault().annotate(e,
//                                ErrorManager.EXCEPTION,
//                                "An error occured during the loading of " +
//                                "fromString. Value has to be set to null.\n\n" +
//                                e.getMessage(),
//                                "Beim Laden von fromString ist ein Fehler " +
//                                "aufgetreten. Der entsprechende Wert wird auf" +
//                                " null gesetzt:\n\n" + e.getMessage(),
//                                e.getCause(), new Date());
//                        ErrorManager.getDefault().notify(e);
//                        val = null;
//                        setValue(null);
//                    }
//                    return val;
//                }
//                
//                public void setValue(Object object) throws 
//                        IllegalAccessException,
//                        IllegalArgumentException,
//                        InvocationTargetException
//                {
//                    CidsClass old = cidsClass;
//                    try
//                    {
//                        cidsAttribute.setFromString((JavaClass)object);
//                        // TODO: why is the class object stored here???
//                        //       cannot understand this because the cidsclass is
//                        //       not modified -.-
//                        cidsClass = (CidsClass)project.getCidsDataObjectBackend(
//                                ).storeClass(cidsClass);
//                        refreshInDiagram();
//                    }
//                    catch (Throwable t)
//                    {
//                        log.error("could not store attribute", t);
//                        cidsClass = old;
//                        ErrorManager.getDefault().notify(t);
//                    }
//                }
//                
//                public PropertyEditor getPropertyEditor()
//                {
//                    return fromStringPropertyEditor;
//                }
//            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Editor ">
            final JavaClassPropertyEditor editorPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.
                    SIMPLE_EDITOR);
            final Property editorProp = new PropertySupport(
                    "editor", // NOI18N
                    JavaClass.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_editor"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, 
                        "Dsc_editorClassOfAttr"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getEditor();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final CidsClass old = cidsClass;
                    try
                    {
                        cidsAttribute.setEditor((JavaClass)object);
                        // TODO: why is the class object stored here???
                        //       cannot understand this because the cidsclass is
                        //       not modified -.-
                        cidsClass = project.getCidsDataObjectBackend()
                                .store(cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr editor", e); // NOI18N
                        cidsClass = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeEditor"), e); // NOI18N
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return editorPropertyEditor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ComplexEditor ">
            final JavaClassPropertyEditor complexEditorPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.
                    COMPLEX_EDITOR);
            final Property complexEditorProp = new PropertySupport(
                    "complexeditor", // NOI18N
                    JavaClass.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_complexEditor"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class,
                        "Dsc_complexEditorOfAttr"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getComplexEditor();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final CidsClass old = cidsClass;
                    try
                    {
                        cidsAttribute.setComplexEditor((JavaClass)object);
                        // TODO: why is the class object stored here???
                        //       cannot understand this because the cidsclass is
                        //       not modified -.-
                        cidsClass = project.getCidsDataObjectBackend()
                                .store(cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attr complex editor",//NOI18N
                                e);
                        cidsClass = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class, 
                                    "Err_storeComplexEditorAttr"), e); // NOI18N
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return complexEditorPropertyEditor;
                }
            };// </editor-fold>
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" PropertyGroup: References ">
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Type ">
            final Property typeProp = new PropertySupport(
                    "type", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_type"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_typeOfAttr"), // NOI18N
                    true,
                    false)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getType().getName();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    // read-only
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ForeignKeyReference ">
            final Property foreignKeyReferencesToProp = new PropertySupport(
                    "foreignKeyReferencesTo", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_foreignKeyTable"),//NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, 
                        "Dsc_foreignKeyTableOfAttr"), // NOI18N
                    true,
                    false)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    final Integer fkClass = cidsAttribute.getForeignKeyClass();
                    if(fkClass != null)
                    {
                        final CidsClass c = project.getCidsDataObjectBackend().
                                getEntity(CidsClass.class, fkClass);
                        if(c != null)
                        {
                            return c.getName();
                        }
                    }
                    return null;
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    // read-only
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ArrayKey ">
            final Property arrayKeyProp = new PropertySupport(
                    "arraykey", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_arraykey"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_arrayKeyOfAttr"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.getArrayKey();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setArrayKey((String)object);
                        project.getCidsDataObjectBackend().store(cidsAttribute);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attribute", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class, 
                                    "Err_storeArrayKeyAttr"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ForeignKey ">
            final Property foreignKeyProp = new PropertySupport(
                    "foreignkey", // NOI18N
                    Boolean.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_foreignKey"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, 
                        "Dsc_attrIsForeignKey"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.isForeignKey();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setForeignKey((Boolean)object);
                        project.getCidsDataObjectBackend().store(cidsAttribute);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attribute", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class,
                                    "Err_storeForeignKey"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Array ">
            final Property arrayProp = new PropertySupport(
                    "array", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_array"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsAttributeNode.class, "Dsc_attrIsArray"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsAttribute.isArray();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Attribute old = cidsAttribute;
                    try
                    {
                        cidsAttribute.setArray((Boolean)object);
                        project.getCidsDataObjectBackend().store(cidsAttribute);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error("could not store attribute", e); // NOI18N
                        cidsAttribute = old;
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsAttributeNode.class, 
                                    "Err_storeArray"), e); // NOI18N
                    }
                }
            };// </editor-fold>
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Rights ">
            final List<AttributePermission> allAttributePermissions = new
                    ArrayList<AttributePermission>(cidsAttribute.
                    getAttributePermissions());
            for(final AttributePermission perm : allAttributePermissions)
            {
                final UserGroup ug = perm.getUserGroup();
                final String name;
                if(ProjectUtils.isRemoteGroup(ug, project))
                {
                    name = ug.getName() + "@" + ug.getDomain(); // NOI18N
                }else
                {
                    name = ug.getName();
                }
                final Property attributePermissionProp = new PropertySupport(
                        "attributePerm" + perm.getId(), // NOI18N
                        String.class,
                        name,
                        "", // NOI18N
                        true,
                        false)
                {
                    @Override
                    public Object getValue() throws 
                            IllegalAccessException,
                            InvocationTargetException
                    {
                        final Permission p = perm.getPermission();
                        String s = permResolve.getPermString(cidsAttribute, p).
                                getPermissionString();
                        if(s == null)
                        {
                            s = p.getKey();
                        }
                        return s;
                    }

                    @Override
                    public void setValue(final Object object) throws 
                            IllegalAccessException,
                            IllegalArgumentException,
                            InvocationTargetException
                    {
                        // read-only
                    }
                };
                rightAttributes.put(attributePermissionProp);
            }// </editor-fold>
            main.setName("properties"); // NOI18N
            relations.setName("relations"); // NOI18N
            classes.setName("java"); // NOI18N
            rightAttributes.setName("rights"); // NOI18N
            main.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Dsc_properties")); // NOI18N
            relations.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Dsc_relationship")); // NOI18N
            classes.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Dsc_java")); // NOI18N
            rightAttributes.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Dsc_rights")); // NOI18N
            main.put(idProp);
            main.put(nameProp);
            main.put(fieldnameProp);
            main.put(defaultValueProp);
            main.put(posProp);
            main.put(descriptionProp);
            main.put(substituteProp);
            main.put(optionalProp);
            main.put(visibleProp);
            main.put(indexedProp);
            classes.put(toStringProp);
//            classes.put(fromStringProp);
            classes.put(editorProp);
            classes.put(complexEditorProp);
            relations.put(foreignKeyProp);
            relations.put(foreignKeyReferencesToProp);
            relations.put(typeProp);
            relations.put(arrayProp);
            relations.put(arrayKeyProp);
            sheet.put(main);
            sheet.put(classes);
            sheet.put(relations);
            if(rightAttributes.getProperties().length > 0)
            {
                sheet.put(rightAttributes);
            }
        }
        catch (final Exception ex)
        {
            LOG.error("could not create property sheet", ex); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Err_createSheet"), ex); // NOI18N
        }
        return sheet;
    }
    
    void refreshInDiagram()
    {
        final Node n = getParentNode();
        if(n instanceof CidsClassNode)
        {
            ((CidsClassNode)n).refreshInDiagram();
        }
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[] 
        {
            CallableSystemAction.get(AttributePermissionWizardAction.class),
            null,
            CallableSystemAction.get(DeleteAction.class)
        };
    }
    
    @Override
    public boolean canDestroy()
    {
        return true;
    }
    
    @Override
    public void destroy()
    {
        try
        {
            cidsClass.getAttributes().remove(cidsAttribute);
            project.getCidsDataObjectBackend().store(cidsClass);
            cidsAttribute = null;
            // p will always be null -.-
            final Node p = getParentNode();
            if(p instanceof CidsClassNode)
            {
                ((CidsClassNode)p).refreshChildren();
            }
        }catch(final Exception e)
        {
            LOG.error("error during deletion", e); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    CidsAttributeNode.class, "Err_deleteAttr"), e); // NOI18N
        }
        project.getLookup().lookup(ClassManagement.class).refresh();
        project.getLookup().lookup(SyncManagement.class).refresh();
    }

    @Override
    public void setCidsAttribute(final Attribute cidsAttribute)
    {
        this.cidsAttribute = cidsAttribute;
    }

    @Override
    public Attribute getCidsAttribute()
    {
        return cidsAttribute;
    }

    @Override
    public CidsClass getCidsClass()
    {
        return cidsClass;
    }
}
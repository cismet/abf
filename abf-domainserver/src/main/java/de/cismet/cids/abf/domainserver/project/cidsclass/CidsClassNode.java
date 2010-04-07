/*
 * CidsClassNode.java, encoding: UTF-8
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
 * Created on 5. Januar 2007, 14:50
 *
 */

package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.PolicyPropertyEditor;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.icons.IconPropertyEditor;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
import de.cismet.cids.abf.domainserver.project.nodes.ClassManagement;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;
import de.cismet.cids.abf.domainserver.project.nodes.TypeManagement;
import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.ClassAttribute;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.permission.ClassPermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 * @version 1.30
 */
public final class CidsClassNode extends ProjectNode implements
        Refreshable,
        CidsClassContextCookie
{
    private static final transient Logger LOG = Logger.getLogger(CidsClassNode.
            class);
    
    private final transient Image defaultClassImage;
    private final transient PermissionResolver permResolve;

    // TODO: cannot be final because is new assigned when class is stored,
    // can possibly be refactored as after storage the children will be
    // refreshed and newly loaded
    private transient CidsClass cidsClass;

    public CidsClassNode(final CidsClass cidsClass, final DomainserverProject 
            project)
    {
        super(new CidsClassNodeChildren(cidsClass, project), project);
        this.cidsClass = cidsClass;
        defaultClassImage = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "class.png"); // NOI18N
        permResolve = PermissionResolver.getInstance(project);
        getCookieSet().add((CidsClassNodeChildren)getChildren());
        getCookieSet().add(this);
    }
    
    @Override
    public Image getIcon(final int i)
    {
        if(cidsClass.getClassIcon() == null)
        {
            return defaultClassImage;
        }
        final Image ic = ProjectUtils.getImageForIconAndProject(cidsClass.
                getClassIcon(), project);
        if(ic == null)
        {
            return defaultClassImage;
        }else
        {
            final Image image = ic.getScaledInstance(10, 10, Image.
                    SCALE_SMOOTH);
            final BufferedImage result = new BufferedImage(10, 10, 
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = result.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            final Image badged = ImageUtilities.mergeImages(
                    defaultClassImage, result, 11, 7);
            return badged;
        }
    }
    
    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }
    
    @Override
    public String getDisplayName()
    {
        return cidsClass.getTableName();
    }
    
    @Override
    protected Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set main = Sheet.createPropertiesSet();
        final Sheet.Set icons = Sheet.createPropertiesSet();
        final Sheet.Set classes = Sheet.createPropertiesSet();
        final Sheet.Set classAttributes = Sheet.createPropertiesSet();
        final Sheet.Set rightAttributes = Sheet.createPropertiesSet();
        final String storeError = "could not store class: "; // NOI18N
        try
        {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ID ">
            final Property idProp = new PropertySupport.Reflection(
                    cidsClass,
                    Integer.class, 
                    "getId", // NOI18N
                    null);
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    CidsClassNode.class, "Dsc_id")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Name ">
            final Property nameProp = new PropertySupport(
                    "name", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_classname"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_nameOfClass"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getName();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final String old = cidsClass.getName();
                    try
                    {
                        cidsClass.setName(object.toString());
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        fireDisplayNameChange(null,object.toString());
                        refresh();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setName(old);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Tablename ">
            final Property tablenameProp = new PropertySupport(
                    "tablename", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_tablename"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_nameOfTable"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getTableName();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final String old = cidsClass.getTableName();
                    try
                    {
                        cidsClass.setTableName(object.toString());
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refresh();
                        project.getLookup().lookup(SyncManagement.class)
                                .refresh();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setTableName(old);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: PrimaryKeyField ">
            final Property primaryKeyFieldProp = new PropertySupport(
                    "primaryKeyField", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_fieldnameOfPrimKEy"),// NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class,
                        "Dsc_fieldnameOfPrimKeyOfTable"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getPrimaryKeyField();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final String old = cidsClass.getPrimaryKeyField();
                    try
                    {
                        cidsClass.setPrimaryKeyField(object.toString());
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                        project.getLookup().lookup(SyncManagement.class)
                                .refresh();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setPrimaryKeyField(old);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Indexed ">
            final Property indexedProp = new PropertySupport(
                    "indexed", // NOI18N
                    Boolean.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_inSearchIndex"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_addedToSearchIndex"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.isIndexed();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Boolean old = cidsClass.isIndexed();
                    try
                    {
                        fireIconChange();
                        cidsClass.setIndexed((Boolean)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setIndexed(old);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Arraylink ">
            final Property arraylinkProp = new PropertySupport(
                    "arrayLink", // NOI18N
                    Boolean.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_arrayclass"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_classIsArrayclass"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.isArrayLink();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Boolean old = cidsClass.isArrayLink();
                    try
                    {
                        fireIconChange();
                        cidsClass.setArrayLink((Boolean)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setArrayLink(old);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Description ">
            final Property descriptionProp = new PropertySupport(
                    "description", // NOI18N
                    String.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_description"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_descriptionOfClass"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    final String val = cidsClass.getDescription();
                    return val == null ? "" : val; // NOI18N
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final String old = cidsClass.getDescription();
                    try
                    {
                        cidsClass.setDescription(object.toString());
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setDescription(old);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ClassPolicy ">
            final Property policyProp = new PropertySupport(
                    "classPolicy", // NOI18N
                    String.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_classPolicy"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class,
                        "Dsc_basicPolicyOnClassLevel"), // NOI18N
                    true,
                    true)
            {
                private PolicyPropertyEditor editor;

                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    Policy p = cidsClass.getPolicy();
                    if(p == null)
                    {
                        final PermissionResolver.Result r = permResolve.
                                getPermString(cidsClass, null);
                        if(r.getInheritanceString() == null)
                        {
                            // should never occur
                            p = Policy.NO_POLICY;
                        }else
                        {
                            p = new Policy();
                            p.setName("<" // NOI18N
                                    + r.getInheritanceString()
                                    + ">"); // NOI18N
                        }
                    }
                    return p;
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    if(!(object instanceof Policy))
                    {
                        throw new IllegalArgumentException(
                                "object must be of type Policy"); // NOI18N
                    }
                    final Policy policy = (Policy)object;
                    final Policy oldPolicy = cidsClass.getPolicy();
                    try
                    {
                        cidsClass.setPolicy((policy.getId() == null) ?
                            null : policy);
                        project.getCidsDataObjectBackend().store(cidsClass);
                        refresh();
                        firePropertySetsChange(null, getPropertySets());
                    }catch(final Exception ex)
                    {
                        LOG.error("policy could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsClassNode.class,
                                    "Err_changeRights"), // NOI18N
                                ex);
                        cidsClass.setPolicy(oldPolicy);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new PolicyPropertyEditor(project);
                    }
                    return editor;
                }

            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: AttributePolicy ">
            final Property attrPolicyProp = new PropertySupport(
                    "attrPolicy", // NOI18N
                    String.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_attrPolicy"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class,
                        "Dsc_basicPolicyForRightEval"), // NOI18N
                    true,
                    true)
            {
                private PolicyPropertyEditor editor;

                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    Policy p = cidsClass.getAttributePolicy();
                    if(p == null)
                    {
                        final Set<Attribute> attr = cidsClass.getAttributes();
                        if(attr.isEmpty())
                        {
                            p = Policy.NO_POLICY;
                        }else
                        {
                            final PermissionResolver.Result r = permResolve.
                                    getPermString(attr.iterator().next(), null);
                            if(r.getInheritanceString() == null)
                            {
                                // should never occur
                                p = Policy.NO_POLICY;
                            }else
                            {
                                p = new Policy();
                                p.setName("<" // NOI18N
                                        + r.getInheritanceString()
                                        + ">"); // NOI18N
                            }
                        }
                    }
                    return p;
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    if(!(object instanceof Policy))
                    {
                        throw new IllegalArgumentException(
                                "object must be of type Policy"); // NOI18N
                    }
                    final Policy policy = (Policy)object;
                    final Policy oldPolicy = cidsClass.getAttributePolicy();
                    try
                    {
                        cidsClass.setAttributePolicy((policy.getId() == null) ?
                                null : policy);
                        project.getCidsDataObjectBackend().store(cidsClass);
                        refreshChildren();
                    }catch(final Exception ex)
                    {
                        LOG.error("policy could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CidsClassNode.class,
                                    "Err_changeRights"), // NOI18N
                                ex);
                        cidsClass.setAttributePolicy(oldPolicy);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new PolicyPropertyEditor(project);
                    }
                    return editor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ClassIcon ">
            final IconPropertyEditor iconPropertyEditor = new 
                    IconPropertyEditor(project);
            final Property classIconProp = new PropertySupport(
                    "classIcon", // NOI18N
                    Icon.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_classIcon"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_iconOfClass"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getClassIcon();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Icon old = cidsClass.getClassIcon();
                    try
                    {
                        cidsClass.setClassIcon((Icon)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        fireOpenedIconChange();
                        fireIconChange();
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setClassIcon(old);
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return iconPropertyEditor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ObjectIcon ">
            final Property objectIconProp = new PropertySupport(
                    "objectIcon", // NOI18N
                    Icon.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_objectIcon"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_iconOfClass"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getObjectIcon();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Icon old = cidsClass.getObjectIcon();
                    try
                    {
                        cidsClass.setObjectIcon((Icon)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setObjectIcon(old);
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return iconPropertyEditor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: ToString ">
            final JavaClassPropertyEditor toStringPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.TO_STRING);
            final Property toStringProp = new PropertySupport(
                    "toString", // NOI18N
                    JavaClass.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_toString"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class,
                        "Dsc_toStringClassOfClass"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getToString();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final JavaClass old = cidsClass.getToString();
                    if(object == null)
                    {
                        final int answer = JOptionPane.showConfirmDialog(
                                WindowManager.getDefault().getMainWindow(), 
                                org.openide.util.NbBundle.getMessage(
                                    CidsClassNode.class,
                                    "Dsc_reallyRemJavaclassQuestion"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    CidsClassNode.class,
                                    "Dsc_setJavaClassToNull"), // NOI18N
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if(answer == JOptionPane.NO_OPTION)
                        {
                            return;
                        }
                    }
                    try
                    {
                        cidsClass.setToString((JavaClass)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setToString(old);
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return toStringPropertyEditor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: Editor ">
            final JavaClassPropertyEditor editorPropertyEditor = new 
                    JavaClassPropertyEditor(project, JavaClass.Type.
                    SIMPLE_EDITOR, JavaClass.Type.COMPLEX_EDITOR);
            final Property editorProp = new PropertySupport(
                    "editor", // NOI18N
                    JavaClass.class,
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_editor"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_editorClassOfClass"),// NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getEditor();
                }

                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final JavaClass old = cidsClass.getEditor();
                    try
                    {
                        cidsClass.setEditor((JavaClass)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setEditor(old);
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
                    "rendererProp", // NOI18N
                    JavaClass.class, 
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class, "Dsc_renderer"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CidsClassNode.class,
                        "Dsc_rendererClassOfClass"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return cidsClass.getRenderer();
                }
                
                @Override
                public void setValue(final Object object) throws 
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final JavaClass old = cidsClass.getRenderer();
                    try
                    {
                        cidsClass.setRenderer((JavaClass)object);
                        cidsClass = project.getCidsDataObjectBackend().store(
                                cidsClass);
                        refreshInDiagram();
                    }catch(final Exception e)
                    {
                        LOG.error(storeError + cidsClass.getName(), e);
                        ErrorManager.getDefault().notify(e);
                        cidsClass.setRenderer(old);
                    }
                }
                
                @Override
                public PropertyEditor getPropertyEditor()
                {
                    return rendererPropertyEditor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Properties: ClassAttribute ">
            final List<ClassAttribute> allClassAttributes = new
                    ArrayList<ClassAttribute>(cidsClass.getClassAttributes());
            for(final ClassAttribute classAttr : allClassAttributes)
            {
                final Property classAttrProp = new PropertySupport(
                        "classAttrProp" + classAttr.getAttrKey(), // NOI18N
                        String.class,
                        classAttr.getAttrKey(),
                        org.openide.util.NbBundle.getMessage(
                            CidsClassNode.class, "Dsc_classAttr") // NOI18N
                            + classAttr.getAttrKey(),
                        true,
                        true)
                {
                    @Override
                    public Object getValue() throws 
                            IllegalAccessException,
                            InvocationTargetException
                    {
                        return classAttr.getAttrValue();
                    }

                    @Override
                    public void setValue(final Object object) throws 
                            IllegalAccessException,
                            IllegalArgumentException,
                            InvocationTargetException
                    {
                        final String old = classAttr.getAttrValue();
                        try
                        {
                            classAttr.setAttrValue(object.toString());
                            project.getCidsDataObjectBackend().store(classAttr);
                            refreshInDiagram();
                        }catch(final Exception e)
                        {
                            LOG.error("could not store class attribute",//NOI18N
                                    e);
                            ErrorManager.getDefault().notify(e);
                            classAttr.setAttrValue(old);
                        }
                    }
                };
                classAttributes.put(classAttrProp);
            }// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Properties: ClassPermission ">
            final List<ClassPermission> allClassPermissions = new
                    ArrayList<ClassPermission>(cidsClass.getClassPermissions());
            for(final ClassPermission perm : allClassPermissions)
            {
                final String ug;
                if(ProjectUtils.isRemoteGroup(perm.getUserGroup(), project))
                {
                    ug = perm.getUserGroup().getName()
                            + "@" // NOI18N
                            + perm.getUserGroup().getDomain();
                }else
                {
                    ug = perm.getUserGroup().getName();
                }
                final Property classPermissionProp = new PropertySupport(
                        "classPerm" + perm.getId(), // NOI18N
                        String.class,
                        ug,
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
                        String s = permResolve.getPermString(cidsClass, p).
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
                rightAttributes.put(classPermissionProp);
            }// </editor-fold>
            main.setName("properties"); // NOI18N
            icons.setName("icons"); // NOI18N
            classes.setName("java"); // NOI18N
            classAttributes.setName("classattributes"); // NOI18N
            rightAttributes.setName("rights"); // NOI18N
            main.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsClassNode.class, "Dsc_properties")); // NOI18N
            icons.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsClassNode.class, "Dsc_icons")); // NOI18N
            classes.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsClassNode.class, "Dsc_java")); // NOI18N
            classAttributes.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsClassNode.class, "Dsc_classAttrs")); // NOI18N
            rightAttributes.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CidsClassNode.class, "Dsc_rights")); // NOI18N
            main.put(idProp);
            main.put(nameProp);
            main.put(tablenameProp);
            main.put(primaryKeyFieldProp);
            main.put(indexedProp);
            main.put(arraylinkProp);
            main.put(descriptionProp);
            main.put(policyProp);
            main.put(attrPolicyProp);
            icons.put(classIconProp);
            icons.put(objectIconProp);
            classes.put(toStringProp);
            classes.put(editorProp);
            classes.put(rendererProp);
            sheet.put(main);
            sheet.put(icons);
            sheet.put(classes);
            if(classAttributes.getProperties().length > 0)
            {
                sheet.put(classAttributes);
            }
            if(rightAttributes.getProperties().length > 0)
            {
                sheet.put(rightAttributes);
            }
        }catch(final Exception ex)
        {
            LOG.debug("error during sheet creation", ex); // NOI18N
            ErrorManager.getDefault().notify(ex);
        }
        return sheet;
    }
    
    @Override
    public CidsClass getCidsClass()
    {
        return cidsClass;
    }
    
    @Override
    public Action[] getActions(final boolean b)
    {
        return new Action[]
        {
            CallableSystemAction.get(RefreshAction.class), null,
            CallableSystemAction.get(EditCidsClassWizardAction.class),
            CallableSystemAction.get(EditRightsWizardAction.class), null,
            CallableSystemAction.get(
                    AddClassWithRelationsToCurrentDiagramAction.class),
            CallableSystemAction.get(AddClassToCurrentDiagramAction.class),null,
            CallableSystemAction.get(
                    CreateDiagramFromClassWithRelationsAction.class),
            CallableSystemAction.get(CreateDiagramFromClassAction.class), null,
            CallableSystemAction.get(DeleteAction.class), null,
            CallableSystemAction.get(ExportClassesAction.class), null,
            CallableSystemAction.get(IndexAction.class)
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
        if(project.getCidsDataObjectBackend().stillReferenced(cidsClass.
                getType()))
        {
            EventQueue.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(
                            WindowManager.getDefault().getMainWindow(),
                            org.openide.util.NbBundle.getMessage(
                                CidsClassNode.class, 
                                "Dsc_typeStillReferencedUndeletable"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                CidsClassNode.class, 
                                "Dsc_referencePresent"), // NOI18N
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
            return;
        }
        try
        {
            project.getDiffAccessor().putDropAction(cidsClass.getTableName());
            project.getCidsDataObjectBackend().delete(cidsClass);
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    project.getLookup().lookup(ClassManagement.class).refresh();
                    project.getLookup().lookup(SyncManagement.class).refresh();
                }
            });
        }catch(final Exception e)
        {
            LOG.error("error during deletion", e); // NOI18N
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public void refreshChildren()
    {
        ((CidsClassNodeChildren)getChildren()).setCidsClass(cidsClass);
    }
    
    void refreshInDiagram()
    {
        final Set opened = TopComponent.getRegistry().getOpened();
        for(final Object o : opened)
        {
            if(o instanceof ClassDiagramTopComponent)
            {
                ((ClassDiagramTopComponent)o).refreshClassWidget(this);
            }
        }
    }
    
    @Override
    public void refresh()
    {
        cidsClass = project.getCidsDataObjectBackend().getEntity(CidsClass.
                class, cidsClass.getId());
        refreshChildren();
        refreshInDiagram();
        fireIconChange();
        fireOpenedIconChange();
        // as long as the props will not be refreshed...
        setSheet(createSheet());
        project.getLookup().lookup(TypeManagement.class).refreshChildren();
    }
}

final class CidsClassNodeChildren extends Children.Keys implements Index
{
    private static final transient Logger LOG = Logger.getLogger(
            CidsClassNodeChildren.class);
    
    private final transient DomainserverProject project;
    private final transient Index indexSupport;
    private transient CidsClass cidsClass;
    private transient boolean doReorder;
    
    public CidsClassNodeChildren(final CidsClass cidsClass,
            final DomainserverProject project)
    {
        this.project = project;
        this.cidsClass = cidsClass;
        this.doReorder = false;
        indexSupport = new IndexSupport();
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof Attribute)
        {
            final CidsAttributeNode can = new CidsAttributeNode((
                    Attribute)object, cidsClass, project);
            return new Node[] {can};
        }else
        {
            return new Node[] {};
        }
    }
    
    void setCidsClass(final CidsClass cidsClass)
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("setting cidsclass"); // NOI18N
        }
        this.cidsClass = cidsClass;
        if(LOG.isDebugEnabled())
        {
            LOG.debug("resetting keys"); // NOI18N
        }
        setKeys(new Object[0]);
        if(LOG.isDebugEnabled())
        {
            LOG.debug("calling addNotify"); // NOI18N
        }
        addNotify();
    }
    
    @Override
    protected void addNotify()
    {
        final Thread t = new Thread(new Runnable() 
        {
            @Override
            public void run()
            {
                try
                {
                    final LinkedList<Attribute> childs = new LinkedList(
                            cidsClass.getAttributes());
                    doReorder = false;
                    final Comparator comp = new Comparator()
                    {
                        @Override
                        public int compare(final Object o1, final Object o2)
                        {
                            final Attribute a1 = (Attribute)o1;
                            final Attribute a2 = (Attribute)o2;
                            if(a1.getPosition() != null
                                    && a2.getPosition() != null)
                            {
                                final int comp = a1.getPosition().compareTo(
                                        a2.getPosition());
                                if (comp != 0)
                                {
                                    return comp;
                                }
                                doReorder = true;
                                return a1.getName().compareTo(a2.getName());
                            }else if(a1.getPosition() == null
                                    && a2.getPosition() != null)
                            {
                                doReorder = true;
                                return 1;
                            }else if(a1.getPosition() != null
                                    && a2.getPosition() == null)
                            {
                                doReorder = true;
                                return -1;
                            }else
                            {
                                doReorder = true;
                                return a1.getName().compareTo(a2.getName());
                            }
                        }

                        @Override
                        public boolean equals(final Object obj)
                        {
                            return false;
                        }
                    };
                    Collections.sort(childs, comp);
                    try
                    {
                        if(doReorder)
                        {
                            for(int i = 0; i < childs.size(); ++i)
                            {
                                final Attribute a = childs.get(i);
                                if(a.getPosition() == null || 
                                        !a.getPosition().equals(i))
                                {
                                    a.setPosition(i);
                                    project.getCidsDataObjectBackend().store(a);
                                }
                            }
                            Collections.sort(childs, comp);
                        }
                    }catch(final Exception e)
                    {
                        LOG.warn("error during reorder", e); // NOI18N
                        ErrorManager.getDefault().notify(e);
                    }
                    EventQueue.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run()
                        {
                            setKeys(childs);
                        }
                    });
                }catch(final Exception ex)
                {
                    LOG.error("could not create children", ex); // NOI18N
                }
            }
        }, getClass().getSimpleName() + "::addNotifyRunner"); // NOI18N
        t.start();
    }
    
    @Override
    public void removeChangeListener(final ChangeListener changeListener)
    {
        indexSupport.removeChangeListener(changeListener);
    }

    @Override
    public void addChangeListener(final ChangeListener changeListener)
    {
        indexSupport.addChangeListener(changeListener);
    }

    @Override
    public void reorder(final int[] i)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("REORDER"); // NOI18N
        }
        indexSupport.reorder(i);
    }
    
    @Override
    public void moveUp(final int i)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("moveUp"); // NOI18N
        }
        indexSupport.moveUp(i);
    }
    
    @Override
    public void moveDown(final int i)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("moveDown"); // NOI18N
        }
        indexSupport.moveDown(i);
    }
    
    @Override
    public int indexOf(final Node node)
    {
        final int ret = indexSupport.indexOf(node);
        if(LOG.isDebugEnabled())
        {
            LOG.debug("indexof = " + ret); // NOI18N
        }
        return ret;
    }
    
    @Override
    public void reorder()
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("reorder"); // NOI18N
        }
        indexSupport.reorder();
    }
    
    @Override
    public void move(final int from, final int to)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("move"); // NOI18N
        }
        indexSupport.move(from, to);
    }

    @Override
    public void exchange(final int x, final int y)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("exchange"); // NOI18N
        }
        indexSupport.exchange(x, y);
    }
    
    private final class IndexSupport extends Index.Support
    {
        @Override
        public void reorder(final int[] perm)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("EMPTY REORDER"); // NOI18N
            }
        }

        @Override
        public int getNodesCount()
        {
            return CidsClassNodeChildren.this.getNodesCount();
        }

        @Override
        public Node[] getNodes()
        {
            return CidsClassNodeChildren.this.getNodes();
        }
    }
}
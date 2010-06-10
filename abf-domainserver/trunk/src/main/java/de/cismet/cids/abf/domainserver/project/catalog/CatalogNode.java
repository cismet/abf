/*
 * CatalogNode.java, encoding: UTF-8
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
 * Created on 21. Juni 2007, 16:20
 *
 */

package de.cismet.cids.abf.domainserver.project.catalog;

import de.cismet.cids.abf.domainserver.RefreshAction;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.PolicyPropertyEditor;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.domainserver.project.javaclass.JavaClassPropertyEditor;
import de.cismet.cids.abf.domainserver.project.nodes.CatalogManagement;
import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
import de.cismet.cids.abf.utilities.Comparators;
import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.LoadingNode;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.common.URL;
import de.cismet.cids.jpa.entity.common.URLBase;
import de.cismet.cids.jpa.entity.permission.NodePermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.diff.db.DatabaseConnection;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.NoResultException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.WindowManager;



/**
 *
 * @author martin.scholl@cismet.de
 * @version 1.29
 */
public class CatalogNode extends ProjectNode implements 
        Refreshable,
        CatalogNodeContextCookie
{
    private static final transient Logger LOG = Logger.getLogger(
            CatalogNode.class);

    private static final Image IMAGE_OPEN;
    private static final Image IMAGE_CLOSED;
    private static final Image BADGE_ORG;
    private static final Image BADGE_CLASS;
    private static final Image BADGE_DYN;
    private static final Image BADGE_OBJ;
    private static final String NULL;

    private final transient PermissionResolver permResolve;

    static
    {
        IMAGE_OPEN = ImageUtilities
                .icon2Image(UIManager.getIcon("Tree.openIcon")); // NOI18N
        IMAGE_CLOSED = ImageUtilities
                .icon2Image(UIManager.getIcon("Tree.closedIcon")); // NOI18N
        BADGE_ORG = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "badge_org.png"); // NOI18N
        BADGE_OBJ = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "badge_object.png");// NOI18N
        BADGE_DYN = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "badge_dynamic.png");//NOI18N
        BADGE_CLASS = ImageUtilities.loadImage(
                DomainserverProject.IMAGE_FOLDER + "badge_class.png"); // NOI18N
        NULL = "null"; // NOI18N
    }

    transient CatNode catNode;
    transient Refreshable parent;
    
    public CatalogNode(final CatNode catNode, final DomainserverProject project,
            final Refreshable parent)
    {
        super(Children.LEAF, project);
        this.catNode = catNode;
        this.parent = parent;
        permResolve = PermissionResolver.getInstance(project);
        setDisplayName(catNode.getName());
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                refresh();
            }
        });
    }

    @Override
    public String getHtmlDisplayName()
    {
        return getDisplayName();
    }

    @Override
    public Action[] getActions(final boolean b) 
    {
        return new Action[] 
        {
            CallableSystemAction.get(NewCatalogNodeWizardAction.class), null,
            CallableSystemAction.get(ModifyNodeRightsWizardAction.class), null,
            CallableSystemAction.get(CopyAction.class),
            CallableSystemAction.get(CutAction.class),
            CallableSystemAction.get(PasteAction.class),
            CallableSystemAction.get(CreateLinkAction.class),
            CallableSystemAction.get(InsertLinkAction.class), null,
            CallableSystemAction.get(DeleteAction.class), null,
            CallableSystemAction.get(RefreshAction.class)
        };
    }
    
    @Override
    public boolean canDestroy()
    {
        return true;
    }
    
    @Override
    public boolean canCopy()
    {
        return true;
    }
    
    @Override
    public boolean canCut()
    {
        // cannot cut dynamically created nodes
        if(catNode.getId() == -1)
        {
            return false;
        }
        return true;
    }
    
    @Override
    public void destroy()
    {
        try
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("CatalogNode: destroy requested"); // NOI18N
            }
            if(parent instanceof CatalogNode)
            {
                final CatalogNode parentNode = (CatalogNode)parent;
                try
                {
                    if(project.getCidsDataObjectBackend().deleteNode(
                            parentNode.catNode, catNode))
                    {
                        project.getLookup().lookup(CatalogManagement.class).
                                destroyedNode(catNode, this);
                    }else
                    {
                        project.getLookup().lookup(CatalogManagement.class).
                                removedNode(catNode, this);
                    }
                }catch(final NoResultException nre)
                {
                    LOG.debug("possibly already deleted", nre); // NOI18N
                    ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                                CatalogNode.class,
                                "Err_nodeDeletionProbablyAlreadyDel"), // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                CatalogNode.class,
                                "Err_duringDeletion"), nre); // NOI18N
                }
            }else
            {
                project.getCidsDataObjectBackend().deleteRootNode(catNode);
                project.getLookup().lookup(CatalogManagement.class).
                                removedNode(catNode, this);
            }
            parent.refresh();
        }catch(final Exception e)
        {
            LOG.error("could not destroy node", e); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Err_duringNodeDeletion"), e); // NOI18N
        }
    }

    @Override
    public void refresh()
    {
        if(project.isConnected())
        {
            catNode.setIsLeaf(project.getCidsDataObjectBackend().isLeaf(catNode,
                    true));
            final Children c = getChildren();
            if(catNode.isLeaf()
                    && (catNode.getDynamicChildren() == null
                        || NULL.equalsIgnoreCase(catNode.getDynamicChildren())))
            {
                EventQueue.invokeLater(new Runnable() 
                {
                    @Override
                    public void run()
                    {
                        setChildren(Children.LEAF);
                    }
                });
            }else if(c instanceof CatalogNodeChildren)
            {
                if(catNode.getDynamicChildren() == null 
                        || NULL.equalsIgnoreCase(catNode.getDynamicChildren()))
                {
                    ((CatalogNodeChildren)c).refreshAll();
                }else
                {
                    setChildren(new DynamicCatalogNodeChildren(catNode,
                            project));
                }
            }else if(c instanceof DynamicCatalogNodeChildren)
            {
                if(catNode.getDynamicChildren() == null
                        || NULL.equalsIgnoreCase(catNode.getDynamicChildren()))
                {
                    setChildren(new CatalogNodeChildren(catNode, project));
                }else
                {
                    ((DynamicCatalogNodeChildren)c).refreshAll();
                }
            }else if(c == Children.LEAF)
            {
                if(catNode.getDynamicChildren() == null)
                {
                    setChildren(new CatalogNodeChildren(catNode, project));
                }else
                {
                    setChildren(new DynamicCatalogNodeChildren(
                            catNode, project));
                }
            }
            // TODO: fire property change to display possibly changed rights
            //firePropertySetsChange(null, getPropertySets());
            // TODO: as long as i did not find out about the mechanism to fire
            // an appropriate property change and/or how to register an 
            // appropriate listener at the right place if this is necessary, 
            // this workaround is acceptable
            setSheet(createSheet());
        }
        else
        {
            setChildren(Children.LEAF);
        }
    }
    
    @Override
    public Sheet createSheet()
    {
        final Sheet sheet = Sheet.createDefault();
        final boolean mayWrite;
        if(parent instanceof CatalogNode)
        {
            final CatalogNode cn = (CatalogNode)parent;
            mayWrite = (cn.catNode.getDynamicChildren() == null);
        }else
        {
            mayWrite = true;
        }
        try
        {
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeID ">
            final Property idProp = new PropertySupport.Reflection(catNode, 
                    Integer.class, "getId", null); // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Dsc_id")); // NOI18N
            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeName ">
            final Property nameProp = new PropertySupport(
                    "nodeName", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_nodeName"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_nameOfNode"), // NOI18N
                    true,
                    mayWrite)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    return catNode.getName();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException 
                {
                    final String name = object.toString();
                    final String oldName = catNode.getName();
                    try
                    {
                        catNode.setName(name);
                        project.getCidsDataObjectBackend().store(catNode);
                        CatalogNode.this.setDisplayName(name);
                    }catch(final Exception ex)
                    {
                        LOG.error("name could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringNameChange"), ex); // NOI18N
                        catNode.setName(oldName);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeDescription ">
            final Property urlProp = new PropertySupport(
                    "nodeUrl", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_desc"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_urlDescribingTheNode"),// NOI18N
                    true,
                    mayWrite)
            {
                private NodeURLPropertyEditor editor;

                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    final URL url = catNode.getUrl();
                    return url == null ? URL.NO_DESCRIPTION : url;
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException 
                {
                    if(!(object instanceof URL))
                    {
                        throw new IllegalArgumentException(
                                "object must be of type URL"); // NOI18N
                    }
                    final URL url = (URL)object;
                    final URL oldURL = catNode.getUrl();
                    try
                    {
                        catNode.setUrl((url.equals(URL.NO_DESCRIPTION)) ?
                            null : url);
                        project.getCidsDataObjectBackend().store(catNode);
                    }catch(final Exception ex)
                    {
                        LOG.error("url could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringURLchange"), ex); // NOI18N
                        catNode.setUrl(oldURL);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new NodeURLPropertyEditor(project);
                    }
                    return editor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeType ">
            final Property nodeTypeProp = new PropertySupport(
                    "nodeType", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_nodeType"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_typeOfNode"), // NOI18N
                    true,
                    mayWrite)
            {
                private NodeTypePropertyEditor editor;

                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    final String type = catNode.getNodeType();
                    if(type.equals(CatNode.Type.CLASS.getType()))
                    {
                        return org.openide.util.NbBundle.getMessage(
                                CatalogNode.class, "Dsc_classNode"); // NOI18N
                    }else if(type.equals(CatNode.Type.OBJECT.getType()))
                    {
                        return org.openide.util.NbBundle.getMessage(
                                CatalogNode.class, "Dsc_objectNode"); // NOI18N
                    }else if(type.equals(CatNode.Type.ORG.getType()))
                    {
                        return org.openide.util.NbBundle.getMessage(
                                CatalogNode.class, "Dsc_orgNode"); // NOI18N
                    }else
                    {
                        return org.openide.util.NbBundle.getMessage(
                                CatalogNode.class, "Dsc_unknownType"); // NOI18N
                    }
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException 
                {
                    final String oldType = catNode.getNodeType();
                    final String type = object.toString();
                    try
                    {
                        catNode.setNodeType(type);
                        project.getCidsDataObjectBackend().store(catNode);
                        fireIconChange();
                    }catch(final Exception ex)
                    {
                        LOG.error("type could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringTypeChange"), ex); // NOI18N
                        catNode.setNodeType(oldType);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new NodeTypePropertyEditor();
                    }
                    return editor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeIsRoot ">
            final Property rootProp = new PropertySupport(
                    "nodeIsRoot", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_rootNode"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_isNodeRootNode"), // NOI18N
                    true,
                    mayWrite)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    if(catNode.getIsRoot() == null)
                    {
                        return Boolean.FALSE;
                    }
                    return catNode.getIsRoot();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException 
                {
                    final Boolean isRoot = (Boolean)object;
                    final Boolean oldIsRoot = catNode.getIsRoot();
                    try
                    {
                        catNode.setIsRoot(isRoot);
                        project.getCidsDataObjectBackend().store(catNode);
                    }catch(final Exception ex)
                    {
                        LOG.error("isRoot could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringRootFlagChange"), ex); // NOI18N
                        catNode.setIsRoot(oldIsRoot);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodePolicy ">
            final Property policyProp = new PropertySupport(
                    "nodePolicy",// NOI18N
                    String.class, 
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_policy"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_policyTooltip"), // NOI18N
                    true,
                    mayWrite)
            {
                private PolicyPropertyEditor editor;

                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    Policy p = catNode.getPolicy();
                    if(p == null)
                    {
                        final PermissionResolver.Result r = permResolve.
                                getPermString(catNode, null);
                        if(r.getInheritanceString() == null)
                        {
                            // should never occur
                            p = Policy.NO_POLICY;
                        }else
                        {
                            p = new Policy();
                            p.setName("<" // NOI18N
                                    + r.getInheritanceString() + ">"); // NOI18N
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
                    final Policy oldPolicy = catNode.getPolicy();
                    try
                    {
                        catNode.setPolicy((policy.getId() == null) ?
                            null : policy);
                        project.getCidsDataObjectBackend().store(catNode);
                        refresh();
                    }catch(final Exception ex)
                    {
                        LOG.error("policy could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringRightsChange"), ex); // NOI18N
                        catNode.setPolicy(oldPolicy);
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
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeDerivePerm ">
            final Property derivePermProp = new PropertySupport(
                    "nodeDerivePerm", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_deriveRightsFromClass"),//NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class,
                        "Dsc_deriveRightsFromClassTooltip"), // NOI18N
                    true,
                    mayWrite)
            {
                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    if(catNode.getDerivePermFromClass() == null)
                    {
                        return Boolean.FALSE;
                    }
                    return catNode.getDerivePermFromClass();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final Boolean derive = (Boolean)object;
                    final Boolean oldDerive = catNode.getDerivePermFromClass();
                    if(derive != null && catNode.getCidsClass() == null)
                    {
                        final int answer = JOptionPane.showConfirmDialog(
                                WindowManager.getDefault().getMainWindow(),
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Dsc_cidsclassNotSetChangeQuest"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Dsc_confirmChanges"), // NOI18N
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if(answer == JOptionPane.CANCEL_OPTION)
                        {
                            return;
                        }
                    }
                    try
                    {
                        catNode.setDerivePermFromClass(derive);
                        project.getCidsDataObjectBackend().store(catNode);
                    }catch(final Exception ex)
                    {
                        LOG.error("derivePerm could not be changed", // NOI18N
                                ex);
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_deriveFromClassFlagChange"), // NOI18N
                                ex);
                        catNode.setDerivePermFromClass(oldDerive);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeIcon ">
            final Property iconProp = new PropertySupport(
                    "nodeIcon", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_icon"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_iconTooltip"), // NOI18N
                    true,
                    true)
            {
                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    final String icon = catNode.getIcon();
                    return icon == null ? org.openide.util.NbBundle.getMessage(
                            CatalogNode.class,
                            "Dsc_notIconBrackets") : icon; // NOI18N
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final String icon = object.toString();
                    final String oldIcon = catNode.getIcon();
                    try
                    {
                        if(NULL.equals(icon) || "".equals(icon) // NOI18N
                                || org.openide.util.NbBundle.getMessage(
                                        CatalogNode.class,
                                        "Dsc_notIconBrackets")
                                   .equals(icon)) // NOI18N
                        {
                            catNode.setIcon(null);
                        }else
                        {
                            catNode.setIcon(icon);
                        }
                        project.getCidsDataObjectBackend().store(catNode);
                        fireIconChange();
                        refresh();
                    }catch(final Exception ex)
                    {
                        LOG.error("icon could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringIconChange"), ex); // NOI18N
                        catNode.setIcon(oldIcon);
                    }
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeIconFactory ">
            final Property factoryProp = new PropertySupport(
                    "nodeIconFactory", // NOI18N
                    JavaClass.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_iconFactory"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_iconFactoryTooltip"), // NOI18N
                    true,
                    mayWrite)
            {
                private JavaClassPropertyEditor editor;

                @Override
                public Object getValue() throws
                        IllegalAccessException,
                        InvocationTargetException
                {
                    return catNode.getIconFactory();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    final JavaClass old = catNode.getIconFactory();
                    if(object == null)
                    {
                        final int answer = JOptionPane.showConfirmDialog(
                                WindowManager.getDefault().getMainWindow(),
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Dsc_reallyRemAssignedJCQuestion"),// NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Dsc_setJCtoNull"), // NOI18N
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if(answer == JOptionPane.NO_OPTION)
                        {
                            return;
                        }
                    }
                    try
                    {
                        catNode.setIconFactory((JavaClass)object);
                        project.getCidsDataObjectBackend().store(catNode);
                        fireIconChange();
                    }catch(final Exception e)
                    {
                        LOG.error("iconfactory could not be changed", // NOI18N
                                e);
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringIconFactoryChange"), e);// NOI18N
                        catNode.setIconFactory(old);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new JavaClassPropertyEditor(project, JavaClass.
                                Type.UNKNOWN);
                    }
                    return editor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeObjectSet ">
            final Map<String, String> objectInfo =
                    project.getCidsDataObjectBackend().getSimpleObjectInformation(catNode);
            final HashSet<Property> objectProps = new HashSet<Property>();
            if(objectInfo != null)
            {
                for(final Iterator<Entry<String, String>> entries = objectInfo.
                        entrySet().iterator(); entries.hasNext();)
                {
                    final Entry<String, String> entry = entries.next();
                    objectProps.add(new PropertySupport.ReadOnly(
                            "node" + entry.getKey(), // NOI18N
                            String.class,
                            entry.getKey(),
                            entry.getKey())
                    {
                        @Override
                        public Object getValue() throws 
                                IllegalAccessException, 
                                InvocationTargetException
                        {
                            final String value = entry.getValue();
                            if(value == null || NULL.equalsIgnoreCase(value))
                            {
                                return org.openide.util.NbBundle.getMessage(
                                        CatalogNode.class,
                                        "Dsc_valueNotSetBrackets"); // NOI18N
                            }
                            return entry.getValue();
                        }
                    });
                }
            }// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodeClass ">
            final Property classProp = new PropertySupport(
                    "nodeClass", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_linkedClass"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_linkedClassTooltip"), // NOI18N
                    true,
                    mayWrite)
            {
                private NodeClassPropertyEditor editor;

                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    final CidsClass cc = catNode.getCidsClass();
                    return cc == null ? CidsClass.NO_CLASS : cc;
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException
                {
                    if(!(object instanceof CidsClass))
                    {
                        throw new IllegalArgumentException(
                                "object must be instanceof CidsClass");// NOI18N
                    }
                    final CidsClass clazz = (CidsClass)object;
                    final CidsClass oldClass = catNode.getCidsClass();
                    try
                    {
                        catNode.setCidsClass((clazz.equals(CidsClass.NO_CLASS)) 
                                ? null : clazz);
                        project.getCidsDataObjectBackend().store(catNode);
                    }catch(final Exception ex)
                    {
                        LOG.error("class could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringClassChange"), ex); // NOI18N
                        catNode.setCidsClass(oldClass);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new NodeClassPropertyEditor(project);
                    }
                    return editor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: NodePermissions ">
            final LinkedList<Property> rightProps = new LinkedList<Property>();
            if(catNode.getNodePermissions() != null)
            {
                final List<NodePermission> perms = new ArrayList<
                        NodePermission>(catNode.getNodePermissions());
                for(final NodePermission perm : perms)
                {
                    rightProps.add(new PropertySupport(
                            "nodeRight" + perm.toString(), // NOI18N
                            String.class,
                            perm.getUserGroup().getName(),
                            org.openide.util.NbBundle.getMessage(
                                CatalogNode.class, "Dsc_aUsergroup"), // NOI18N
                            true,
                            false)
                    {
                        @Override
                        public Object getValue() throws 
                                IllegalAccessException, 
                                InvocationTargetException
                        {
                            final Permission p = perm.getPermission();
                            String s = permResolve.getPermString(catNode, p).
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
                            // not needed
                        }
                    });
                }
            }// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: DynamicChildrenSQL ">
            final Property dynaChildrenSQL = new PropertySupport(
                    "nodeDynaChildrenSQL", // NOI18N
                    String.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_sqlQuery"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_sqlQueryTooltip"), // NOI18N
                    true,
                    mayWrite)
            {
                private PropertyEditor editor;

                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    return catNode.getDynamicChildren();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException 
                {
                    final String sql = object.toString();
                    final String oldSql = catNode.getDynamicChildren();
                    try
                    {
                        if(NULL.equals(sql) || "".equals(sql)) // NOI18N
                        {
                            catNode.setDynamicChildren(null);
                        }else
                        {
                            catNode.setDynamicChildren(sql);
                        }
                        project.getCidsDataObjectBackend().store(catNode);
                        fireIconChange();
                        refresh();
                    }catch(final Exception ex)
                    {
                        LOG.error("dynamic children statement could" // NOI18N
                                + " not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringDynChildrenChange"), ex);//NOI18N
                        catNode.setDynamicChildren(oldSql);
                    }
                }

                @Override
                public PropertyEditor getPropertyEditor()
                {
                    if(editor == null)
                    {
                        editor = new DynamicChildrenPropertyEditor();
                    }
                    return editor;
                }
            };// </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Create Property: DynamicChildrenSQLSort ">
            final Property dynaChildrenSQLSort = new PropertySupport(
                    "nodeDynaChildrenSQLSort", // NOI18N
                    Boolean.class,
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_sqlSort"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CatalogNode.class, "Dsc_sqlSortTooltip"), // NOI18N
                    true,
                    mayWrite)
            {
                @Override
                public Object getValue() throws 
                        IllegalAccessException, 
                        InvocationTargetException
                {
                    if(catNode.getSqlSort() == null)
                    {
                        return Boolean.FALSE;
                    }
                    return catNode.getSqlSort();
                }

                @Override
                public void setValue(final Object object) throws
                        IllegalAccessException,
                        IllegalArgumentException,
                        InvocationTargetException 
                {
                    final Boolean sqlSort = (Boolean)object;
                    final Boolean oldSqlSort = catNode.getSqlSort();
                    try
                    {
                        catNode.setSqlSort(sqlSort);
                        project.getCidsDataObjectBackend().store(catNode);
                    }catch(final Exception ex)
                    {
                        LOG.error("SQLSort could not be changed", ex); // NOI18N
                        ErrorUtils.showErrorMessage(
                                org.openide.util.NbBundle.getMessage(
                                    CatalogNode.class,
                                    "Err_duringSqlSortChange"), ex); // NOI18N
                        catNode.setSqlSort(oldSqlSort);
                    }
                }
            };// </editor-fold>
            final Sheet.Set main = Sheet.createPropertiesSet();
            final Sheet.Set clazz = Sheet.createPropertiesSet();
            final Sheet.Set object = Sheet.createPropertiesSet();
            final Sheet.Set rights = Sheet.createPropertiesSet();
            final Sheet.Set dynaChildren = Sheet.createPropertiesSet();
            main.setName("nodeProps"); // NOI18N
            clazz.setName("nodeClassProps"); // NOI18N
            object.setName("nodeObjectProps"); // NOI18N
            rights.setName("nodeRights"); // NOI18N
            dynaChildren.setName("nodeDynaChildrenProps"); // NOI18N
            main.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Dsc_properties")); // NOI18N
            clazz.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Dsc_cidsclass")); // NOI18N
            object.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Dsc_object")); // NOI18N
            rights.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Dsc_rights")); // NOI18N
            dynaChildren.setDisplayName(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class, "Dsc_dynChildren")); // NOI18N
            main.put(idProp);
            main.put(nameProp);
            main.put(urlProp);
            main.put(nodeTypeProp);
            main.put(rootProp);
            main.put(policyProp);
            main.put(derivePermProp);
            main.put(iconProp);
            //main.put(factoryProp);
            clazz.put(classProp);
            for(final Iterator<Property> props = objectProps.iterator(); props.
                    hasNext();)
            {
                object.put(props.next());
            }
            for(final Iterator<Property> props = rightProps.iterator(); props.
                    hasNext();)
            {
                rights.put(props.next());
            }
            sheet.put(main);
            sheet.put(clazz);
            sheet.put(object);
            sheet.put(rights);
            dynaChildren.put(dynaChildrenSQL);
            dynaChildren.put(dynaChildrenSQLSort);
            sheet.put(dynaChildren);
        } catch(final Exception ex)
        {
            LOG.error("could not create property sheet", ex); // NOI18N
            ErrorUtils.showErrorMessage(org.openide.util.NbBundle.getMessage(
                    CatalogNode.class,
                    "Err_duringPropViewCreation"), ex); // NOI18N
        }
        return sheet;
    }

    // TODO: use iconfactory if set or get icon from icon resource
    @Override
    public Image getIcon(final int i)
    {
        if(catNode.getDynamicChildren() != null)
        {
            return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_DYN, 0, 0);
        }else if("N".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_ORG, 0, 0);
        }else if("C".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_CLASS, 0, 0);
        }else if("O".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return ImageUtilities.mergeImages(IMAGE_CLOSED, BADGE_OBJ, 0, 0);
        }else if("none".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return null;
        }else
        {
            return IMAGE_CLOSED;
        }
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        if(catNode.getDynamicChildren() != null)
        {
            return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_DYN, 0, 0);
        }else if("N".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_ORG, 0, 0);
        }else if("C".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_CLASS, 0, 0);
        }else if("O".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return ImageUtilities.mergeImages(IMAGE_OPEN, BADGE_OBJ, 0, 0);
        }else if("none".equalsIgnoreCase(catNode.getNodeType())) // NOI18N
        {
            return null;
        }else
        {
            return IMAGE_OPEN;
        }
    }

    @Override
    protected void createPasteTypes(final Transferable transferable, final List 
            list)
    {
        super.createPasteTypes(transferable, list);
        int mode = NodeTransfer.COPY;
        Node node = NodeTransfer.node(transferable, mode);
        if(!(node instanceof CatalogNode))
        {
            mode = NodeTransfer.MOVE;
            node = NodeTransfer.node(transferable, mode);
            if(!(node instanceof CatalogNode))
            {
                return;
            }
        }
        final CatalogNode pasteNode = (CatalogNode)node;
                // one cannot add children to object nodes
        if(!catNode.getNodeType().equals(CatNode.Type.OBJECT.getType()) &&
                // one can only perform copy & paste/dnd within a project
                pasteNode.project.getProjectDirectory().equals(project.
                        getProjectDirectory()) &&
                // one cannot paste a node in itself
                //!pasteNode.equals(this) &&
                // one cannot paste a node where it already is
                //!(pasteNode.getParent() != null && pasteNode.getParent().
                //        equals(this.catNode)) &&
                // one cannot insert if this is dynamic node
                catNode.getDynamicChildren() == null) // &&
                // this restriction must not be present
                // one cannot insert dynamically created nodes 
                //(pasteNode.getParent() == null || pasteNode.getParent().
                //        getDynamicChildren() == null))
        {
            list.add(new CatalogPasteType(pasteNode, mode));
        }
    }

    @Override
    public CatNode getCatNode()
    {
        return catNode;
    }

    @Override
    public void setCatNode(final CatNode catNode)
    {
        this.catNode = catNode;
    }

    @Override
    public CatNode getParent()
    {
        return (parent instanceof CatalogNode) ? 
            ((CatalogNode)parent).catNode : null;
    }
    
    private final class CatalogPasteType extends PasteType
    {
        private final transient CatalogNode node;
        private final transient int mode;
        
        public CatalogPasteType(final CatalogNode node, final int mode)
        {
            this.node = node;
            this.mode = mode;
        }

        @Override
        public Transferable paste() throws IOException
        {
            if(node.catNode.getId() == -1)
            {
                final int answer = JOptionPane.showOptionDialog(
                        WindowManager.getDefault().getMainWindow(),
                        org.openide.util.NbBundle.getMessage(
                            CatalogPasteType.class,
                            "Dsc_copyDynNodeQuestion"), // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            CatalogPasteType.class,
                            "Dsc_copyDynChildren"), // NOI18N
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new Object[] 
                            {
                                org.openide.util.NbBundle.getMessage(
                                        CatalogPasteType.class,
                                        "Dsc_yes"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                        CatalogNode.class,
                                        "Dsc_noCapitalised") // NOI18N
                            },
                        org.openide.util.NbBundle.getMessage(
                            CatalogPasteType.class,
                            "Dsc_noCapitalised")); // NOI18N
                if(answer != JOptionPane.YES_OPTION)
                {
                    return null;
                }
            }
            final CatNode parent;
            if(node.parent instanceof CatalogNode)
            {
                parent = ((CatalogNode)node.parent).catNode;
            }else
            {
                parent = null;
            }
            final Backend backend = project.getCidsDataObjectBackend();
            if(mode == NodeTransfer.COPY)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("copyNode"); // NOI18N
                }
                backend.copyNode(parent, catNode, node.catNode);
            }
            else
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("moveNode"); // NOI18N
                }
                backend.moveNode(parent, catNode, node.catNode);
                node.parent.refresh();
            }
            refresh();
            node.refresh();
            return null;
        }
    }
}

final class CatalogNodeChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            CatalogNodeChildren.class);
    
    private final transient CatNode catNode;
    private final transient DomainserverProject project;
    private final transient CatalogManagement catalogManagement;
    
    public CatalogNodeChildren(final CatNode node, final DomainserverProject 
            project)
    {
        this.project = project;
        this.catNode = node;
        catalogManagement = project.getLookup().lookup(CatalogManagement.class);
    }

    @Override
    protected Node[] createNodes(final Object key)
    {
        if(key instanceof LoadingNode)
        {
            return new Node[] {(LoadingNode)key};
        }
        if(key instanceof CatNode)
        {
            final CatNode node = (CatNode)key;
            final CatalogNode cn = new CatalogNode(node, project,
                    (Refreshable)getNode());
            catalogManagement.addOpenNode(node, cn);
            return new Node[] {cn};
        }else
        {
            return new Node[] {};
        }
    }
    
    void refreshAll()
    {
        addNotify();
    }
    
    @Override
    protected void addNotify()
    {
        final Thread builder = new Thread(new ChildrenBuilder());
        setKeys(new Object[] {new LoadingNode()});
        builder.start();
    }
    
    final class ChildrenBuilder implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                final List<CatNode> l = project.
                        getCidsDataObjectBackend().getNodeChildren(
                        catNode);
                Collections.sort(l, new Comparators.CatNodes());
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run()
                    {
                        setKeys(l);
                    }
                });
            }catch(final ObjectNotFoundException ex)
            {
                final CatNode node = new CatNode();
                node.setName(org.openide.util.NbBundle.getMessage(
                        ChildrenBuilder.class,
                        "Dsc_dataInconsistency")); // NOI18N
                node.setNodeType("none"); // NOI18N
                node.setIsLeaf(true);
                node.setIsRoot(false);
                node.setId(-1);
                if(catNode.getCidsClass() == null)
                {
                    ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                                ChildrenBuilder.class,
                                "Err_dataInconsistencyCheckDB"), ex); // NOI18N
                }else
                {
                    ErrorUtils.showErrorMessage(
                            org.openide.util.NbBundle.getMessage(
                                ChildrenBuilder.class,
                                "Dsc_dataInconsistencyCheckTable", // NOI18N
                                catNode.getCidsClass().getTableName()),
                            ex);
                }
                LOG.error("data inconsistency", ex); // NOI18N
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setKeys(new Object[] {node});
                    }
                });
            }
        }
    }
}

final class DynamicCatalogNodeChildren extends Children.Keys
{
    private static final transient Logger LOG = Logger.getLogger(
            DynamicCatalogNodeChildren.class);
    
    private final transient DomainserverProject project;
    private final transient CatNode parentNode;
    private transient LoadingNode loadingNode;
    
    public DynamicCatalogNodeChildren(final CatNode node, final 
            DomainserverProject project)
    {
        this.parentNode = node;
        this.project = project;
    }
    
    @Override
    protected Node[] createNodes(final Object object)
    {
        if(object instanceof LoadingNode)
        {
            return new Node[] {(LoadingNode)object};
        }else if(object instanceof CatNode)
        {
            final CatNode catNode = (CatNode)object;
            return new Node[] 
            {
                new CatalogNode(catNode, project, (Refreshable)getNode())
            };
        }else
        {
            return new Node[]{};
        }
    }

    void refreshAll()
    {
        addNotify();
    }
    
    @Override
    protected void addNotify()
    {
        loadingNode = new LoadingNode();
        setKeys(new Object[] {loadingNode});
        refresh();
        final Backend backend = project.getCidsDataObjectBackend();
        final Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Connection con = null;
                try
                {
                    con = DatabaseConnection.getConnection(project.
                            getRuntimeProps());
                } catch (final SQLException ex)
                {
                    LOG.error("could not connect to database", ex); // NOI18N
                    setKeys(new Object[]
                    {
                        org.openide.util.NbBundle.getMessage(
                                DynamicCatalogNodeChildren.class,
                                "Err_duringConToDB")// NOI18N
                    });
                    refresh();
                    if(loadingNode != null)
                    {
                        loadingNode.dispose();
                        loadingNode = null;
                    }
                    return;
                }
                ResultSet set = null;
                try
                {
                    set = con.createStatement().executeQuery(parentNode.
                            getDynamicChildren());
                } catch (final SQLException ex)
                {
                    LOG.error("could not fetch resultset", ex); // NOI18N
                    setKeys(new Object[]
                    {
                        org.openide.util.NbBundle.getMessage(
                                DynamicCatalogNodeChildren.class,
                                "Err_queryUnsuccessful") // NOI18N
                    });
                    refresh();
                    if(loadingNode != null)
                    {
                        loadingNode.dispose();
                        loadingNode = null;
                    }
                    return;
                } finally
                {
                    try
                    {
                        set.close();
                        con.close();
                    } catch (final SQLException sqle)
                    {
                        LOG.warn("could not close connection", sqle); // NOI18N
                    }
                }
                try
                {
                    final List<CatNode> nodes = new LinkedList<CatNode>();
                    final HashMap<Integer, CidsClass> classCache = new HashMap<
                            Integer, CidsClass>();
                    while(set.next())
                    {
                        final CatNode c = new CatNode();
                        c.setName(set.getString("name")); // NOI18N
                        c.setDynamicChildren(
                                set.getString("dynamic_children")); // NOI18N
                        c.setSqlSort(set.getBoolean("sql_sort")); // NOI18N
                        try
                        {
                            c.setId(set.getInt("id")); // NOI18N
                        }catch(final SQLException ex)
                        {
                            LOG.warn("id could not be set", ex); // NOI18N
                        }
                        try
                        {
                            c.setObjectId(set.getInt("object_id")); // NOI18N
                        }catch(final SQLException ex)
                        {
                            LOG.warn("object_id could not be set", ex);// NOI18N
                        }
                        try
                        {
                            c.setNodeType(set.getString("node_type")); // NOI18N
                        }catch(final SQLException ex)
                        {
                            LOG.warn("node_type could not be set", ex);// NOI18N
                        }
                        try
                        {
                            final int classId = set.getInt("class_id");// NOI18N
                            final CidsClass clazz;
                            if(classCache.containsKey(classId))
                            {
                                clazz = classCache.get(classId);
                            }else
                            {
                                clazz = backend.getEntity(CidsClass.class,
                                        classId);
                                classCache.put(classId, clazz);
                            }
                            c.setCidsClass(clazz);
                        }catch(final NoResultException ex)
                        {
                            LOG.warn("cidsclass could not be set", ex);// NOI18N
                        }catch(final SQLException ex)
                        {
                            LOG.warn("cidsclass could not be set", ex);// NOI18N
                        }
                        try
                        {
                            final URL url = 
                                    getURL(set.getString("url")); // NOI18N
                            c.setUrl(url);
                        }catch(final SQLException ex)
                        {
                            LOG.warn("url could not be set", ex); // NOI18N
                        }
                        c.setIsLeaf(true);
                        nodes.add(c);
                    }
                    if(parentNode.getSqlSort() == null
                            || !parentNode.getSqlSort())
                    {
                        Collections.sort(nodes, new Comparators.CatNodes());
                    }
                    setKeys(nodes);
                    refresh();
                }catch(final SQLException ex)
                {
                    LOG.error("could not evaluate resultset", ex); // NOI18N
                    setKeys(new Object[]
                    {
                        org.openide.util.NbBundle.getMessage(
                                DynamicCatalogNodeChildren.class,
                                "Err_queryResultEvaluation") // NOI18N
                    });
                    refresh();
                    return;
                }finally
                {
                    if(loadingNode != null)
                    {
                        loadingNode.dispose();
                        loadingNode = null;
                    }
                }
            }
            
            private URL getURL(final String s)
            {
                final URL url = new URL();
                url.setObjectName(s);
                final URLBase base = new URLBase();
                base.setPath(""); // NOI18N
                base.setProtocolPrefix(""); // NOI18N
                base.setServer(""); // NOI18N
                url.setUrlbase(base);
                return url;
            }
        });
        t.setPriority(7);
        t.start();
    }
}
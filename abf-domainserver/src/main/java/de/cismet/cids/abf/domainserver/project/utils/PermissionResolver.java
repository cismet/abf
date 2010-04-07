/*
 * PermissionResolver.java, encoding: UTF-8
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
 * Created on ???
 *
 */

package de.cismet.cids.abf.domainserver.project.utils;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.jpa.backend.service.impl.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.common.CommonEntity;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.permission.PolicyRule;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class PermissionResolver
{
    private static final transient Logger LOG = Logger.getLogger(
            PermissionResolver.class);
    
    private static final String PERM_STRING_READ;
    private static final String PERM_STRING_WRITE;
    private static final String PERM_STRING_NOT;
    private static final String INH_STRING_SERVER_POLICY;
    private static final String INH_STRING_CLASS_POLICY;
    private static final String INH_STRING_SERVER_CLASS_POLICY;
    private static final String INH_STRING_SERVER_ATTR_POLICY;
    private static final String INH_STRING_NODE_POLICY;
    private static final String INH_STRING_SERVER_NODE_POLICY;
    private static final String USE_STRING_CLASS_POLICY;
    private static final String USE_STRING_ATTR_POLICY;
    private static final String USE_STRING_NODE_POLICY;

    private static final Map<DomainserverProject, PermissionResolver> MAP;
    
    private final transient DomainserverProject project;
    private final transient List<Policy> policies;
    // policyid, hashmap<permissionid, default value>
    private final transient Map<Integer, Map<Integer, Boolean>> ruleMap;
    private final transient Permission readPerm;
    private final transient ThreadLocal<Result> result;

    static
    {
        PERM_STRING_READ = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_permStringRead"); // NOI18N
        PERM_STRING_WRITE = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_permStringWrite"); // NOI18N
        PERM_STRING_NOT = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_permStringNot"); // NOI18N
        INH_STRING_CLASS_POLICY = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_inhStringClassPolicy"); // NOI18N
        INH_STRING_NODE_POLICY = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_inhStringNodePolicy"); // NOI18N
        INH_STRING_SERVER_ATTR_POLICY = NbBundle.getMessage(PermissionResolver.
                class, "PermissionResolver_inhStringServerAttrPolicy");// NOI18N
        INH_STRING_SERVER_CLASS_POLICY = NbBundle.getMessage(PermissionResolver.
                class, "PermissionResolver_inhStringServerClassPolicy");//NOI18N
        INH_STRING_SERVER_NODE_POLICY = NbBundle.getMessage(PermissionResolver.
                class, "PermissionResolver_inhStringServerNodePolicy");// NOI18N
        INH_STRING_SERVER_POLICY = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_inhStringServerPolicy"); // NOI18N
        USE_STRING_ATTR_POLICY = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_useStringAttrPolicy"); // NOI18N
        USE_STRING_CLASS_POLICY = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_useStringClassPolicy"); // NOI18N
        USE_STRING_NODE_POLICY = NbBundle.getMessage(PermissionResolver.class,
                "PermissionResolver_useStringNodePolicy"); // NOI18N
        MAP = new Hashtable<DomainserverProject, PermissionResolver>();
    }

    /**
     * should be used instead of constructor
     *
     * @param p Domainserver to create a PermissionResolver for
     * @return a PermissionResolver corresponding to the given Domainserver
     */
    public static PermissionResolver getInstance(final DomainserverProject p)
    {
        if(p == null)
        {
            throw new IllegalArgumentException(
                    "project must not be null"); // NOI18N
        }
        PermissionResolver resolver = MAP.get(p);
        if(resolver == null)
        {
            resolver = new PermissionResolver(p);
            MAP.put(p, resolver);
        }
        return resolver;
    }

    /**
     * should not be accessed directely
     *
     * @param project Domainserver to create a PermissionResolver for
     */
    public PermissionResolver(final DomainserverProject project)
    {
        if(project == null)
        {
            throw new IllegalArgumentException(
                    "project must not be null"); // NOI18N
        }
        if(!project.isConnected())
        {
            throw new IllegalStateException(
                    "the project is not connected"); // NOI18N
        }
        this.project = project;
        final Backend backend = project.getCidsDataObjectBackend();
        ruleMap = new HashMap<Integer, Map<Integer, Boolean>>();
        for(final PolicyRule rule : backend.getAllEntities(PolicyRule.class))
        {
            Map permMap = ruleMap.get(rule.getPolicy().getId());
            if(permMap == null)
            {
                permMap = new HashMap<Integer, Boolean>();
                ruleMap.put(rule.getPolicy().getId(), permMap);
            }
            permMap.put(rule.getPermission().getId(), rule.getDefaultValue());
        }
        policies = backend.getAllEntities(Policy.class);
        final List<Permission> perms = backend.getAllEntities(Permission.class);
        Permission read = null;
        Permission write = null;
        for(final Permission perm : perms)
        {
            if(perm.getKey().equalsIgnoreCase("read")) // NOI18N
            {
                read = perm;
            }
            if(perm.getKey().equalsIgnoreCase("write")) // NOI18N
            {
                write = perm;
            }
            // get out as early as possible
            if(read != null && write != null)
            {
                break;
            }
        }
        if(read == null || write == null)
        {
            throw new IllegalStateException(
                    "could not find r/w permission"); // NOI18N
        }
        readPerm = read;
        result = new ThreadLocal<Result>()
        {
            @Override
            protected Result initialValue()
            {
                return new Result();
            }

            @Override
            public void set(final Result value)
            {
                throw new UnsupportedOperationException(
                        "shall not set result"); // NOI18N
            }
        };
        validateFallbackPolicies();
    }

    private void validateFallbackPolicies() throws IllegalStateException
    {
        boolean serverFound = false;
        boolean attrFound = false;
        boolean cNodeFound = false;
        boolean nNodeFound = false;
        for(final Policy p : policies)
        {
            final String pName = p.getName();
            if(project.getServerPolicy().equals(pName))
            {
                serverFound = true;
            }
            if(project.getAttrPolicy().equals(pName))
            {
                attrFound = true;
            }
            if(project.getClassNodePolicy().equals(pName))
            {
                cNodeFound = true;
            }
            if(project.getOrgNodePolicy().equals(pName))
            {
                nNodeFound = true;
            }
            // getting out as early as possible
            if(serverFound && attrFound && cNodeFound && nNodeFound)
            {
                return;
            }
        }
        if(!serverFound)
        {
            throw new IllegalStateException(DomainserverProject
                    .PROP_POLICY_SERVER + " is not valid: " // NOI18N
                    + project.getServerPolicy());
        }
        if(!attrFound)
        {
            throw new IllegalStateException(DomainserverProject
                    .PROP_POLICY_ATTR + " is not valid: " // NOI18N
                    + project.getAttrPolicy());
        }
        if(!cNodeFound)
        {
            throw new IllegalStateException(DomainserverProject
                    .PROP_POLICY_CLASS_NODE + " is not valid: " // NOI18N
                    + project.getClassNodePolicy());
        }
        if(!nNodeFound)
        {    throw new IllegalStateException(DomainserverProject
                     .PROP_POLICY_ORG_NODE + " is not valid: " // NOI18N
                     + project.getOrgNodePolicy());
        }
    }

    public Result getPermString(final CommonEntity entity, final Permission p)
    {
        if(entity instanceof CidsClass)
        {
            return getPermString((CidsClass)entity, p);
        }else if(entity instanceof Attribute)
        {
            return getPermString((Attribute)entity, p);
        }else if(entity instanceof CatNode)
        {
            return getPermString((CatNode)entity, p);
        }else
        {
            throw new IllegalArgumentException(
                    "entity not known: " + entity); // NOI18N
        }
    }

    private Result getPermString(final CidsClass cidsClass, final Permission p)
    {
        Policy policy = cidsClass.getPolicy();
        final Result r = result.get();
        if(policy == null)
        {
            for(final Policy pol : policies)
            {
                if(project.getServerPolicy().equals(pol.getName()))
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("no policy found so far, " // NOI18N
                                + "enforcing server's policy"); // NOI18N
                    }
                    r.inheritanceString = INH_STRING_SERVER_POLICY;
                    policy = pol;
                    break;
                }
            }
            if(policy == null)
            {
                throw new IllegalStateException(
                        "could not find server policy"); // NOI18N
            }
        }else
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("enforcing class' policy"); // NOI18N
            }
            r.inheritanceString = USE_STRING_CLASS_POLICY;
        }
        try
        {
            r.permissionString = evaluatePerms(policy, p);
        }finally
        {
            result.remove();
        }
        return r;
    }

    private Result getPermString(final Attribute attribute, final Permission p)
    {
        Policy policy = attribute.getCidsClass().getAttributePolicy();
        final Result r = result.get();
        if(policy == null)
        {
            for(final Policy pol : policies)
            {
                if(project.getAttrPolicy().equals(pol.getName()))
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("no policy found so far, enforcing " // NOI18N
                                + "server's attribute policy"); // NOI18N
                    }
                    r.inheritanceString = INH_STRING_SERVER_ATTR_POLICY;
                    policy = pol;
                    break;
                }
            }
            if(policy == null)
            {
                throw new IllegalStateException(
                        "could not find attr policy"); // NOI18N
            }
        }else
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("enforcing class' attribute policy"); // NOI18N
            }
            r.inheritanceString = USE_STRING_ATTR_POLICY;
        }
        try
        {
            r.permissionString = evaluatePerms(policy, p);
        }finally
        {
            result.remove();
        }
        return r;
    }

    private Result getPermString(final CatNode catNode, final Permission p)
    {
        Policy policy = catNode.getPolicy();
        final Result r = result.get();
        if(policy == null)
        {
            policy = getNodePolicy(catNode);
            if(policy == null)
            {
                for(final Policy pol : policies)
                {
                    if(project.getServerPolicy().equals(pol.getName()))
                    {
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug("no policy found so far, " // NOI18N
                                    + "enforcing serverpolicy"); // NOI18N
                        }
                        r.inheritanceString = INH_STRING_SERVER_POLICY;
                        policy = pol;
                        break;
                    }
                }
            }
            if(policy == null)
            {
                throw new IllegalStateException(
                        "could not find server policy"); // NOI18N
            }
        }else
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("enforcing catnode's policy"); // NOI18N
            }
            r.inheritanceString = USE_STRING_NODE_POLICY;
        }
        try
        {
            r.permissionString = evaluatePerms(policy, p);
        }finally
        {
            result.remove();
        }
        return r;
    }

    private Policy getNodePolicy(final CatNode node)
    {
        if(node.getNodeType().equals(CatNode.Type.CLASS.getType()))
        {
            return getClassNodePolicy(node);
        }else if(node.getNodeType().equals(CatNode.Type.OBJECT.getType()))
        {
            return getObjectNodePolicy(node);
        }else if(node.getNodeType().equals(CatNode.Type.ORG.getType()))
        {
            return getOrgNodePolicy(node);
        }else
        {
            return null;
        }
    }

    private Policy getClassNodePolicy(final CatNode node)
    {
        if(!node.getNodeType().equals(CatNode.Type.CLASS.getType()))
        {
            throw new IllegalArgumentException(
                    "node is not of type ClassNode"); // NOI18N
        }
        for(final Policy pol : policies)
        {
            if(project.getClassNodePolicy().equals(pol.getName()))
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("no policy found so far, returning " // NOI18N
                            + "server's class node policy"); // NOI18N
                }
                result.get().inheritanceString = INH_STRING_SERVER_CLASS_POLICY;
                return pol;
            }
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("server's class node policy " // NOI18N
                    + "unknown/not enforcable"); // NOI18N
        }
        return null;
    }

    private Policy getObjectNodePolicy(final CatNode node)
    {
        if(!node.getNodeType().equals(CatNode.Type.OBJECT.getType()))
        {
            throw new IllegalArgumentException(
                    "node is not of type ObjectNode"); // NOI18N
        }
        if(node.getCidsClass() != null)
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug("no policy found so far, returning cids " // NOI18N
                        + "class' policy for object node"); // NOI18N
            }
            result.get().inheritanceString = INH_STRING_CLASS_POLICY;
            return node.getCidsClass().getPolicy();
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("no cids class attached to object node"); // NOI18N
        }
        return null;
    }

    private Policy getOrgNodePolicy(final CatNode node)
    {
        if(!node.getNodeType().equals(CatNode.Type.ORG.getType()))
        {
            throw new IllegalArgumentException(
                    "node is not of type OrgNode"); // NOI18N
        }
        final List<CatNode> parents = project.getCidsDataObjectBackend().
                getNodeParents(node);
        if(parents.size() > 1)
        {
            throw new IllegalStateException(
                    "node has more than one parent: " + node); // NOI18N
        }else if(parents.size() < 1)
        {
            // INFO: special routine for transient nodes (nodes during creation)
            if(node.getId() == null)
            {
                if(node.getProspectiveParent() == null)
                {
                    throw new IllegalStateException("could not find " // NOI18N
                            + "prospective parent for transient node");// NOI18N
                }else
                {
                    if(LOG.isDebugEnabled())
                    {
                        LOG.debug("using prospective parent"); // NOI18N
                    }
                    parents.add(node.getProspectiveParent());
                }
            }else
            {
                for(final Policy pol : policies)
                {
                    if(project.getOrgNodePolicy().equals(pol.getName()))
                    {
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug(
                                    "no policy found so far, returning"// NOI18N
                                    + " server's pure node policy"); // NOI18N
                        }
                        result.get().inheritanceString =
                                INH_STRING_SERVER_NODE_POLICY;
                        return pol;
                    }
                }
                return null;
            }
        }
        Policy policy = parents.get(0).getPolicy();
        if(policy == null)
        {
            policy = getOrgNodePolicy(parents.get(0));
            if(policy == null)
            {
                for(final Policy pol : policies)
                {
                    if(project.getOrgNodePolicy().equals(pol.getName()))
                    {
                        if(LOG.isDebugEnabled())
                        {
                            LOG.debug(
                                    "no policy found so far, returning"// NOI18N
                                    + " server's pure node policy"); // NOI18N
                        }
                        result.get().inheritanceString =
                                INH_STRING_SERVER_NODE_POLICY;
                        return pol;
                    }
                }
            }
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("returning parent's policy for orgnode"); // NOI18N
        }
        result.get().inheritanceString = INH_STRING_NODE_POLICY;
        return policy;
    }

    private String evaluatePerms(final Policy policy, final Permission p)
    {
        final boolean defaultPerm;
        if(p == null)
        {
            if(LOG.isInfoEnabled())
            {
                LOG.info("permission is null, no permission " // NOI18N
                        + "string attached"); // NOI18N
            }
            return null;
        }
        final Integer policyId = policy.getId();
        final Integer permId = p.getId();
        if(policyId == null || permId == null)
        {
            // unknown policy
            LOG.warn("unknown policy or permission: policyid = " // NOI18N
                    + policy.getId() + " :: permissionid = " // NOI18N
                    + p.getId());
            return null;
        }
        defaultPerm = ruleMap.get(policyId).get(permId);
        final String permString;
        if(readPerm.getId().equals(p.getId()))
        {
            permString = PERM_STRING_READ;
        }else
        {
            permString = PERM_STRING_WRITE;
        }
        final String modifier;
        if(defaultPerm)
        {
            modifier = PERM_STRING_NOT;
        }else
        {
            modifier = ""; // NOI18N
        }
        return MessageFormat.format(permString, modifier);
    }

    public static final class Result
    {
        private String permissionString;
        private String inheritanceString;

        public Result()
        {
            this(null, null);
        }

        public Result(final String permString)
        {
            this(permString, null);
        }

        public Result(final String permString, final String inheritanceString)
        {
            this.permissionString = permString;
            this.inheritanceString = inheritanceString;
        }

        public String getPermissionString()
        {
            return permissionString;
        }

        public String getInheritanceString()
        {
            return inheritanceString;
        }

        void setPermissionString(final String permissionString)
        {
            this.permissionString = permissionString;
        }

        void setInheritanceString(final String inheritanceString)
        {
            this.inheritanceString = inheritanceString;
        }
    }
}
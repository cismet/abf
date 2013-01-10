/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.utils;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.text.MessageFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.Attribute;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.common.PermissionAwareEntity;
import de.cismet.cids.jpa.entity.permission.AbstractPermission;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.permission.PolicyRule;
import de.cismet.cids.jpa.entity.user.UserGroup;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class PermissionResolver {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(PermissionResolver.class);

    private static final String PERM_STRING_READ;
    private static final String PERM_STRING_WRITE;
    private static final String PERM_STRING_NOT;
    private static final String PERM_STRING_DERIVED_FROM_CLASS;
    private static final String PERM_STRING_UNSUPPORTED_PERMISSION;
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
    private static final ReentrantLock INITLOCK;

    static {
        PERM_STRING_READ = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.PERM_STRING_READ");                   // NOI18N
        PERM_STRING_WRITE = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.PERM_STRING_WRITE");                  // NOI18N
        PERM_STRING_NOT = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.PERM_STRING_NOT");                    // NOI18N
        INH_STRING_CLASS_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.INH_STRING_CLASS_POLICY");            // NOI18N
        INH_STRING_NODE_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.INH_STRING_NODE_POLICY");             // NOI18N
        INH_STRING_SERVER_ATTR_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.INH_STRING_SERVER_ATTR_POLICY");      // NOI18N
        INH_STRING_SERVER_CLASS_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.INH_STRING_SERVER_CLASS_POLICY");     // NOI18N
        INH_STRING_SERVER_NODE_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.INH_STRING_SERVER_NODE_POLICY");      // NOI18N
        INH_STRING_SERVER_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.INH_STRING_SERVER_POLICY");           // NOI18N
        USE_STRING_ATTR_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.USE_STRING_ATTR_POLICY");             // NOI18N
        USE_STRING_CLASS_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.USE_STRING_CLASS_POLICY");            // NOI18N
        USE_STRING_NODE_POLICY = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.USE_STRING_NODE_POLICY");             // NOI18N
        PERM_STRING_DERIVED_FROM_CLASS = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.PERM_STRING_DERIVED_FROM_CLASS");     // NOI18N
        PERM_STRING_UNSUPPORTED_PERMISSION = NbBundle.getMessage(
                PermissionResolver.class,
                "PermissionResolver.PERM_STRING_UNSUPPORTED_PERMISSION"); // NOI18N
        MAP = new HashMap<DomainserverProject, PermissionResolver>();
        INITLOCK = new ReentrantLock(false);
    }

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;
    private final transient List<Policy> policies;
    // map<policyid, map<permissionid, default value>>
    private final transient Map<Integer, Map<Integer, Boolean>> ruleMap;
    private final transient Permission readPerm;
    private final transient Permission writePerm;
    private final transient Permission noPerm;
    private final transient ThreadLocal<Result> result;

    //~ Constructors -----------------------------------------------------------

    /**
     * should not be accessed directely.
     *
     * @param   project  Domainserver to create a PermissionResolver for
     *
     * @throws  IllegalArgumentException       DOCUMENT ME!
     * @throws  IllegalStateException          DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    private PermissionResolver(final DomainserverProject project) {
        if (project == null) {
            throw new IllegalArgumentException("project must not be null");  // NOI18N
        }
        if (!project.isConnected()) {
            throw new IllegalStateException("the project is not connected"); // NOI18N
        }
        this.project = project;
        final Backend backend = project.getCidsDataObjectBackend();
        ruleMap = new HashMap<Integer, Map<Integer, Boolean>>();
        for (final PolicyRule rule : backend.getAllEntities(PolicyRule.class)) {
            Map permMap = ruleMap.get(rule.getPolicy().getId());
            if (permMap == null) {
                permMap = new HashMap<Integer, Boolean>();
                ruleMap.put(rule.getPolicy().getId(), permMap);
            }
            permMap.put(rule.getPermission().getId(), rule.getDefaultValue());
        }
        policies = backend.getAllEntities(Policy.class);
        final List<Permission> perms = backend.getAllEntities(Permission.class);
        Permission read = null;
        Permission write = null;
        for (final Permission perm : perms) {
            if (perm.getKey().equalsIgnoreCase("read"))                      // NOI18N
            {
                read = perm;
            }
            if (perm.getKey().equalsIgnoreCase("write"))                     // NOI18N
            {
                write = perm;
            }
            // get out as early as possible
            if ((read != null) && (write != null)) {
                break;
            }
        }
        if ((read == null) || (write == null)) {
            throw new IllegalStateException("could not find r/w permission"); // NOI18N
        }
        readPerm = read;
        writePerm = write;
        noPerm = null;
        result = new ThreadLocal<Result>() {

                @Override
                protected Result initialValue() {
                    return new Result();
                }

                @Override
                public void set(final Result value) {
                    throw new UnsupportedOperationException("shall not set result"); // NOI18N
                }
            };
        validateFallbackPolicies();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * should be used instead of constructor.
     *
     * @param   p  Domainserver to create a PermissionResolver for
     *
     * @return  a PermissionResolver corresponding to the given Domainserver
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public static PermissionResolver getInstance(final DomainserverProject p) {
        if (p == null) {
            throw new IllegalArgumentException("project must not be null"); // NOI18N
        }

        // we won't use double checked locking as we neither know if it really works at all
        // (see http://www.javaworld.com/javaworld/jw-02-2001/jw-0209-double.html)
        INITLOCK.lock();
        try {
            PermissionResolver resolver = MAP.get(p);
            if (resolver == null) {
                resolver = new PermissionResolver(p);
                MAP.put(p, resolver);
            }

            return resolver;
        } finally {
            INITLOCK.unlock();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void validateFallbackPolicies() throws IllegalStateException {
        boolean serverFound = false;
        boolean attrFound = false;
        boolean cNodeFound = false;
        boolean nNodeFound = false;
        for (final Policy p : policies) {
            final String pName = p.getName();
            if (project.getServerPolicy().equals(pName)) {
                serverFound = true;
            }
            if (project.getAttrPolicy().equals(pName)) {
                attrFound = true;
            }
            if (project.getClassNodePolicy().equals(pName)) {
                cNodeFound = true;
            }
            if (project.getOrgNodePolicy().equals(pName)) {
                nNodeFound = true;
            }
            // getting out as early as possible
            if (serverFound && attrFound && cNodeFound && nNodeFound) {
                return;
            }
        }
        if (!serverFound) {
            throw new IllegalStateException(
                DomainserverProject.PROP_POLICY_SERVER
                        + " is not valid: "
                        + project.getServerPolicy());    // NOI18N
        }
        if (!attrFound) {
            throw new IllegalStateException(
                DomainserverProject.PROP_POLICY_ATTR
                        + " is not valid: "
                        + project.getAttrPolicy());      // NOI18N
        }
        if (!cNodeFound) {
            throw new IllegalStateException(
                DomainserverProject.PROP_POLICY_CLASS_NODE
                        + " is not valid: "
                        + project.getClassNodePolicy()); // NOI18N
        }
        if (!nNodeFound) {
            throw new IllegalStateException(
                DomainserverProject.PROP_POLICY_ORG_NODE
                        + " is not valid: "
                        + project.getOrgNodePolicy());   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usergroup  DOCUMENT ME!
     * @param   entity     DOCUMENT ME!
     * @param   desired    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException       DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public boolean hasPerm(final UserGroup usergroup, final PermissionAwareEntity entity, final Permission desired) {
        if (usergroup == null) {
            throw new IllegalArgumentException("usergroup may not be null");                        // NOI18N
        }
        if (desired == null) {
            throw new IllegalArgumentException("permission may not be null");                       // NOI18N
        }
        final Policy policy = getEffectivePolicy(entity);
        if (policy == null) {
            throw new UnsupportedOperationException(
                "hasPerm has been called for catnode that has 'derivePermFromClass' flag or "       // NOI18N
                        + "effective policy was not found! Both operations are not supported yet"); // NOI18N
        }
        for (final AbstractPermission ap : entity.getPermissions()) {
            if (usergroup.equals(ap.getUserGroup())) {
                final Result r = evaluatePerms(policy, ap.getPermission());
                if (desired.equals(r.effectivePerm)) {
                    return true;
                } else if ((r.effectivePerm == null) && ap.getPermission().equals(desired)) {
                    // this is the case if the permission is set and results in effective permission loss for the
                    // permission we want to be informed about
                    // e.g. policy == WIKI, ug.perm == write -> result.effectivePerm == null
                    return false;
                }
            }
        }
        // no permission has been set for this usergroup, let's see whether the policy implicitely sets the permission
        final Boolean perm = ruleMap.get(policy.getId()).get(desired.getId());
        if (perm == null) {
            LOG.warn("unsupported permission in cids system present: " + desired); // NOI18N
            return false;
        } else {
            return perm;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   entity  DOCUMENT ME!
     * @param   p       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Result getPermString(final PermissionAwareEntity entity, final Permission p) {
        try {
            final Policy policy = getEffectivePolicy(entity);
            if (policy == null) {
                return result.get();
            } else {
                return evaluatePerms(policy, p);
            }
        } finally {
            result.remove();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   entity  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Policy getEffectivePolicy(final PermissionAwareEntity entity) {
        // policy of PermissionAwareEntity cannot be used as nodes may have derive perm from class flag
        if (entity instanceof CidsClass) {
            return getClassPolicy((CidsClass)entity);
        } else if (entity instanceof Attribute) {
            return getAttributePolicy((Attribute)entity);
        } else if (entity instanceof CatNode) {
            return getNodePolicy((CatNode)entity);
        } else {
            LOG.warn("returning entity's policy for unknown entity: " + entity); // NOI18N
            return entity.getPolicy();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private Policy getClassPolicy(final CidsClass cidsClass) {
        final Result r = result.get();
        Policy policy = cidsClass.getPolicy();
        if (policy == null) {
            for (final Policy pol : policies) {
                if (project.getServerPolicy().equals(pol.getName())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("no policy found so far, enforcing server's policy"); // NOI18N
                    }
                    r.inheritanceString = INH_STRING_SERVER_POLICY;
                    policy = pol;
                    break;
                }
            }
            if (policy == null) {
                throw new IllegalStateException("could not find server's policy");      // NOI18N
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enforcing class' policy");                                   // NOI18N
            }
            r.inheritanceString = USE_STRING_CLASS_POLICY;
        }
        return policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attribute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private Policy getAttributePolicy(final Attribute attribute) {
        Policy policy = attribute.getCidsClass().getAttributePolicy();
        final Result r = result.get();
        if (policy == null) {
            for (final Policy pol : policies) {
                if (project.getAttrPolicy().equals(pol.getName())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("no policy found so far, enforcing server's attribute policy"); // NOI18N
                    }
                    r.inheritanceString = INH_STRING_SERVER_ATTR_POLICY;
                    policy = pol;
                    break;
                }
            }
            if (policy == null) {
                throw new IllegalStateException("could not find attr policy");                    // NOI18N
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enforcing class' attribute policy");                                   // NOI18N
            }
            r.inheritanceString = USE_STRING_ATTR_POLICY;
        }
        return policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     * @throws  IllegalStateException     DOCUMENT ME!
     */
    private Policy getNodePolicy(final CatNode node) {
        final Result r = result.get();
        Policy policy;
        if (node.getDerivePermFromClass()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("derive permission from class set, return null");          // NOI18N
            }
            r.inheritanceString = USE_STRING_CLASS_POLICY;
            r.permissionString = PERM_STRING_DERIVED_FROM_CLASS;
            return null;
        } else if (node.getPolicy() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enforcing catnode's policy");                             // NOI18N
            }
            r.inheritanceString = USE_STRING_NODE_POLICY;
            policy = node.getPolicy();
        } else if (node.getNodeType().equals(CatNode.Type.CLASS.getType())) {
            policy = getClassNodePolicy(node);
        } else if (node.getNodeType().equals(CatNode.Type.OBJECT.getType())) {
            policy = getObjectNodePolicy(node);
        } else if (node.getNodeType().equals(CatNode.Type.ORG.getType())) {
            policy = getOrgNodePolicy(node);
        } else {
            throw new IllegalArgumentException("unknown catnode type: " + node.getNodeType());
        }
        if (policy == null) {
            for (final Policy pol : policies) {
                if (project.getServerPolicy().equals(pol.getName())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("no policy found so far, enforcing serverpolicy"); // NOI18N
                    }
                    r.inheritanceString = INH_STRING_SERVER_POLICY;
                    policy = pol;
                    break;
                }
            }
            if (policy == null) {
                throw new IllegalStateException("could not find server policy");     // NOI18N
            }
        }
        return policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private Policy getClassNodePolicy(final CatNode node) {
        if (!node.getNodeType().equals(CatNode.Type.CLASS.getType())) {
            throw new IllegalArgumentException("node is not of type ClassNode");               // NOI18N
        }
        for (final Policy pol : policies) {
            if (project.getClassNodePolicy().equals(pol.getName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no policy found so far, returning server's class node policy"); // NOI18N
                }
                result.get().inheritanceString = INH_STRING_SERVER_CLASS_POLICY;
                return pol;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("server's class node policy unknown/not enforcable");                    // NOI18N
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private Policy getObjectNodePolicy(final CatNode node) {
        if (!node.getNodeType().equals(CatNode.Type.OBJECT.getType())) {
            throw new IllegalArgumentException("node is not of type ObjectNode");                  // NOI18N
        }
        if (node.getCidsClass() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no policy found so far, returning cids class' policy for object node"); // NOI18N
            }
            result.get().inheritanceString = INH_STRING_CLASS_POLICY;
            return node.getCidsClass().getPolicy();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("no cids class attached to object node");                                    // NOI18N
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     * @throws  IllegalStateException     DOCUMENT ME!
     */
    // TODO: refactor since this implementation is rather hard to understand
    private Policy getOrgNodePolicy(final CatNode node) {
        if (!node.getNodeType().equals(CatNode.Type.ORG.getType())) {
            throw new IllegalArgumentException("node is not of type OrgNode");         // NOI18N
        }
        final List<CatNode> parents = project.getCidsDataObjectBackend().getNodeParents(node);
        if (parents.size() > 1) {
            throw new IllegalStateException("node has more than one parent: " + node); // NOI18N
        } else if (parents.isEmpty()) {
            // INFO: special routine for transient nodes (nodes during creation)
            if ((node.getId() == null) && (node.getProspectiveParent() != null)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("using prospective parent for transient node");                         // NOI18N
                }
                parents.add(node.getProspectiveParent());
            } else {
                for (final Policy pol : policies) {
                    if (project.getOrgNodePolicy().equals(pol.getName())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("no policy found so far, returning server's pure node policy"); // NOI18N
                        }
                        result.get().inheritanceString = INH_STRING_SERVER_NODE_POLICY;

                        return pol;
                    }
                }

                return null;
            }
        }
        Policy policy = parents.get(0).getPolicy();
        if (policy == null) {
            policy = getOrgNodePolicy(parents.get(0));
            if (policy == null) {
                for (final Policy pol : policies) {
                    if (project.getOrgNodePolicy().equals(pol.getName())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("no policy found so far, returning server's pure node policy"); // NOI18N
                        }
                        result.get().inheritanceString = INH_STRING_SERVER_NODE_POLICY;

                        return pol;
                    }
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("returning parent's policy for orgnode"); // NOI18N
        }
        result.get().inheritanceString = INH_STRING_NODE_POLICY;

        return policy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   policy  DOCUMENT ME!
     * @param   p       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Result evaluatePerms(final Policy policy, final Permission p) {
        final Result r = result.get();
        if (p == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("permission is null, no permission string attached"); // NOI18N
            }
            return r;
        }
        final Integer policyId = policy.getId();
        final Integer permId = p.getId();
        if ((policyId == null) || (permId == null)) {
            // unknown policy
            LOG.warn("unknown policy or permission: policyid = " + policy.getId() + " :: permissionid = " + p.getId()); // NOI18N
            return r;
        }
        final Boolean defaultPerm = ruleMap.get(policyId).get(permId);

        if (defaultPerm == null) {
            r.permissionString = p.getKey() + ": " + PERM_STRING_UNSUPPORTED_PERMISSION;
        } else {
            final String permString;
            if (readPerm.getId().equals(p.getId())) {
                permString = PERM_STRING_READ;
                r.effectivePerm = readPerm;
            } else {
                permString = PERM_STRING_WRITE;
                r.effectivePerm = writePerm;
            }
            final String modifier;
            if (defaultPerm) {
                // assigning a specific right, if defaultperm set, means inverting the right
                modifier = PERM_STRING_NOT;
                r.effectivePerm = noPerm;
            } else {
                modifier = ""; // NOI18N
            }
            r.permissionString = MessageFormat.format(permString, modifier);
        }
        return r;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class Result {

        //~ Instance fields ----------------------------------------------------

        private String permissionString;
        private String inheritanceString;
        private Permission effectivePerm;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Result object.
         */
        public Result() {
            this(null, null, null);
        }

        /**
         * Creates a new Result object.
         *
         * @param  permString  DOCUMENT ME!
         */
        public Result(final String permString) {
            this(permString, null, null);
        }

        /**
         * Creates a new Result object.
         *
         * @param  permString         DOCUMENT ME!
         * @param  inheritanceString  DOCUMENT ME!
         */
        public Result(final String permString, final String inheritanceString) {
            this(permString, inheritanceString, null);
        }

        /**
         * Creates a new Result object.
         *
         * @param  permString         DOCUMENT ME!
         * @param  inheritanceString  DOCUMENT ME!
         * @param  effectivePerm      DOCUMENT ME!
         */
        public Result(final String permString, final String inheritanceString, final Permission effectivePerm) {
            this.permissionString = permString;
            this.inheritanceString = inheritanceString;
            this.effectivePerm = effectivePerm;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getPermissionString() {
            return permissionString;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getInheritanceString() {
            return inheritanceString;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Permission getEffectivePerm() {
            return effectivePerm;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  permissionString  DOCUMENT ME!
         */
        void setPermissionString(final String permissionString) {
            this.permissionString = permissionString;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  inheritanceString  DOCUMENT ME!
         */
        void setInheritanceString(final String inheritanceString) {
            this.inheritanceString = inheritanceString;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  effectivePerm  DOCUMENT ME!
         */
        void setEffectivePerm(final Permission effectivePerm) {
            this.effectivePerm = effectivePerm;
        }
    }
}

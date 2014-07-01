/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver;

import java.sql.SQLException;

import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import de.cismet.cids.jpa.backend.service.Backend;
import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.catalog.CatNode.Type;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.common.CommonEntity;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.common.URL;
import de.cismet.cids.jpa.entity.common.URLBase;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;
import de.cismet.cids.jpa.entity.permission.AbstractPermission;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;

import de.cismet.commons.ref.PurgingCache;

import de.cismet.commons.utils.ProgressListener;

import de.cismet.tools.Calculator;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ReadCacheBackend implements Backend {

    //~ Instance fields --------------------------------------------------------

    private final Backend delegate;
    private final PurgingCache<Class<? extends CommonEntity>, List<? extends CommonEntity>> readCache;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ReadCacheBackend object.
     *
     * @param  delegate  DOCUMENT ME!
     */
    public ReadCacheBackend(final Backend delegate) {
        this.delegate = delegate;
        this.readCache = new PurgingCache<Class<? extends CommonEntity>, List<? extends CommonEntity>>(
                new Calculator<Class<? extends CommonEntity>, List<? extends CommonEntity>>() {

                    @Override
                    public List<? extends CommonEntity> calculate(final Class<? extends CommonEntity> input)
                            throws Exception {
                        return ReadCacheBackend.this.delegate.getAllEntities(input);
                    }
                },
                0,
                0,
                false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public <T extends CommonEntity> T store(final T entity) {
        readCache.clear();
        return delegate.store(entity);
    }

    @Override
    public void delete(final CommonEntity ce) {
        delegate.delete(ce);
    }

    @Override
    public void delete(final List<CommonEntity> entities) {
        delegate.delete(entities);
    }

    @Override
    public <T extends CommonEntity> T getEntity(final Class<T> entity, final int id) throws NoResultException {
        final List<? extends CommonEntity> entities = readCache.get(entity);
        for (final CommonEntity ce : entities) {
            if (ce.getId() == id) {
                return (T)ce;
            }
        }

        throw new NoResultException(entity.toString() + " id=" + id + " c=" + entities.size());
    }

    @Override
    public <T extends CommonEntity> List<T> getAllEntities(final Class<T> entity) {
        return (List<T>)readCache.get(entity);
    }

    @Override
    public <T extends CommonEntity> T getEntity(final Class<T> entity, final String name) throws NoResultException {
        return delegate.getEntity(entity, name);
    }

    @Override
    public <T extends CommonEntity> boolean contains(final Class<T> entity, final String name) {
        return delegate.contains(entity, name);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final ConfigAttrKey key) {
        return delegate.getEntries(key);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final ConfigAttrKey key, final Types type) {
        return delegate.getEntries(key, type);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final Types type) {
        return delegate.getEntries(type);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final Domain dom,
            final UserGroup ug,
            final User user,
            final boolean collect) {
        return delegate.getEntries(dom, ug, user, collect);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final Domain dom,
            final UserGroup ug,
            final User user,
            final String localDomainName,
            final boolean collect) {
        return delegate.getEntries(dom, ug, user, localDomainName, collect);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final User user, final String localDomainName) {
        return delegate.getEntries(user, localDomainName);
    }

    @Override
    public List<ConfigAttrEntry> getEntries(final User user,
            final String localDomainName,
            final boolean newCollect,
            final boolean includeConflicts) {
        return delegate.getEntries(user, localDomainName, newCollect, includeConflicts);
    }

    @Override
    public List<Object[]> getEntriesNewCollect(final User user, final boolean includeConflicts) {
        return delegate.getEntriesNewCollect(user, includeConflicts);
    }

    @Override
    public ConfigAttrEntry storeEntry(final ConfigAttrEntry entry) {
        return delegate.storeEntry(entry);
    }

    @Override
    public boolean contains(final ConfigAttrEntry entry) {
        return delegate.contains(entry);
    }

    @Override
    public boolean contains(final ConfigAttrKey key) {
        return delegate.contains(key);
    }

    @Override
    public void cleanAttributeTables() {
        delegate.cleanAttributeTables();
    }

    @Override
    public void refreshIndex(final CidsClass cidsClass) throws SQLException {
        delegate.refreshIndex(cidsClass);
    }

    @Override
    public List<CatNode> getNodeChildren(final CatNode node) {
        return delegate.getNodeChildren(node);
    }

    @Override
    public List<CatNode> getNodeParents(final CatNode node) {
        return delegate.getNodeParents(node);
    }

    @Override
    public List<CatNode> getRootNodes(final Type type) {
        return delegate.getRootNodes(type);
    }

    @Override
    public List<CatNode> getRootNodes() {
        return delegate.getRootNodes();
    }

    @Override
    public Domain getLinkDomain(final CatNode from, final CatNode to) {
        return delegate.getLinkDomain(from, to);
    }

    @Override
    public void setLinkDomain(final CatNode from, final CatNode to, final Domain domainTo) {
        delegate.setLinkDomain(from, to, domainTo);
    }

    @Override
    public Map<String, String> getSimpleObjectInformation(final CatNode node) {
        return delegate.getSimpleObjectInformation(node);
    }

    @Override
    public boolean deleteNode(final CatNode parent, final CatNode node) {
        return delegate.deleteNode(parent, node);
    }

    @Override
    public void deleteRootNode(final CatNode node) {
        delegate.deleteRootNode(node);
    }

    @Override
    public CatNode addNode(final CatNode parent, final CatNode newNode, final Domain domainTo) {
        return delegate.addNode(parent, newNode, domainTo);
    }

    @Override
    public void moveNode(final CatNode oldParent, final CatNode newParent, final CatNode node) {
        delegate.moveNode(oldParent, newParent, node);
    }

    @Override
    public void copyNode(final CatNode oldParent, final CatNode newParent, final CatNode node) {
        delegate.copyNode(oldParent, newParent, node);
    }

    @Override
    public void linkNode(final CatNode oldParent, final CatNode newParent, final CatNode node) {
        delegate.linkNode(oldParent, newParent, node);
    }

    @Override
    public boolean isLeaf(final CatNode node, final boolean useCache) {
        return delegate.isLeaf(node, useCache);
    }

    @Override
    public void reloadNonLeafNodeCache() {
        delegate.reloadNonLeafNodeCache();
    }

    @Override
    public void deleteIcon(final Icon i) {
        delegate.deleteIcon(i);
    }

    @Override
    public boolean stillReferenced(final Icon icon) {
        return delegate.stillReferenced(icon);
    }

    @Override
    public void deleteJavaClass(final JavaClass jc) {
        delegate.deleteJavaClass(jc);
    }

    @Override
    public boolean contains(final JavaClass jc) {
        return delegate.contains(jc);
    }

    @Override
    public JavaClass getJavaClass(final String qualifier) {
        return delegate.getJavaClass(qualifier);
    }

    @Override
    public List getSortedTypes() {
        return delegate.getSortedTypes();
    }

    @Override
    public boolean stillReferenced(final de.cismet.cids.jpa.entity.cidsclass.Type t) {
        return delegate.stillReferenced(t);
    }

    @Override
    public List<URL> getURLsLikeURL(final URL url) {
        return delegate.getURLsLikeURL(url);
    }

    @Override
    public URL storeURL(final URL url) {
        return delegate.storeURL(url);
    }

    @Override
    public List<URL> storeURLs(final List<URL> urls) {
        return delegate.storeURLs(urls);
    }

    @Override
    public void deleteURL(final URL url) {
        delegate.deleteURL(url);
    }

    @Override
    public void deleteURLs(final List<URL> urls) {
        delegate.deleteURLs(urls);
    }

    @Override
    public void deleteURLBaseIfUnused(final URLBase urlbase) {
        delegate.deleteURLBaseIfUnused(urlbase);
    }

    @Override
    public void deleteURLBasesIfUnused(final List<URLBase> urlbases) {
        delegate.deleteURLBasesIfUnused(urlbases);
    }

    @Override
    public void delete(final UserGroup ug) {
        delegate.delete(ug);
    }

    @Override
    public void delete(final User user) {
        delegate.delete(user);
    }

    @Override
    public void removeMembership(final User user, final UserGroup ug) {
        delegate.removeMembership(user, ug);
    }

    @Override
    public UserGroup copy(final UserGroup original) {
        return delegate.copy(original);
    }

    @Override
    public UserGroup copy(final UserGroup original, final UserGroup newGroup) {
        return delegate.copy(original, newGroup);
    }

    @Override
    public User getUser(final String userName, final String password) {
        return delegate.getUser(userName, password);
    }

    @Override
    public <T extends AbstractPermission> List<T> getPermissions(final Class<T> permType, final UserGroup ug) {
        return delegate.getPermissions(permType, ug);
    }

    @Override
    public Integer getLowestUGPrio() {
        return delegate.getLowestUGPrio();
    }

    @Override
    public void close() throws Exception {
        readCache.clear();
        delegate.close();
    }

    @Override
    public void addProgressListener(final ProgressListener pl) {
        delegate.addProgressListener(pl);
    }

    @Override
    public void removeProgressListener(final ProgressListener pl) {
        delegate.removeProgressListener(pl);
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }
}

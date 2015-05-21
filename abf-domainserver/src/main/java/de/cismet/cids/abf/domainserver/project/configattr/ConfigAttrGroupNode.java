/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.abf.domainserver.project.configattr;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.awt.EventQueue;

import java.io.IOException;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.KeyContainer;
import de.cismet.cids.abf.domainserver.project.ProjectChildren;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class ConfigAttrGroupNode extends ProjectNode {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConfigAttrGroupNode.class);

    public static final String NO_GROUP_DISPLAYNAME = NbBundle.getMessage(
            ConfigAttrGroupNode.class,
            "ConfigAttrGroupNode.<init>.nogroup.name"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final Types type;
    private final String group;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigAttrGroupNode object.
     *
     * @param  group    DOCUMENT ME!
     * @param  type     DOCUMENT ME!
     * @param  project  DOCUMENT ME!
     */
    public ConfigAttrGroupNode(final String group, final Types type, final DomainserverProject project) {
        super(new GenericConfigAttrGroupNodeChildren(group, type, project), project);

        this.group = group;
        this.type = type;

        if (ConfigAttrKey.NO_GROUP.equals(group)) {
            setName(NO_GROUP_DISPLAYNAME);
        } else {
            setName(group);
        }

        getCookieSet().add(new RefreshableImpl());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class RefreshableImpl implements Refreshable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void refresh() {
            if (LOG.isTraceEnabled()) {
                LOG.trace("requesting refresh", new Throwable("trace")); // NOI18N
            }

            final Future<?> refreshing = ((ProjectChildren)getChildren()).refreshByNotify();

            try {
                refreshing.get(10, TimeUnit.SECONDS);
                final Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            final Node[] childNodes = getChildren().getNodes(false);
                            for (final Node childNode : childNodes) {
                                final Refreshable refreshableChild = childNode.getCookie(Refreshable.class);
                                if (refreshableChild != null) {
                                    refreshableChild.refresh();
                                }
                            }
                        }
                    };

                if (EventQueue.isDispatchThread()) {
                    r.run();
                } else {
                    EventQueue.invokeLater(r);
                }
            } catch (final Exception e) {
                LOG.warn("cannot wait for finish of refresh of config attr group node children", e); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class GenericConfigAttrGroupNodeChildren extends ProjectChildren {

        //~ Instance fields ----------------------------------------------------

        private final transient Types type;
        private final transient String group;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new GenericConfigAttrRootNodeChildren object.
         *
         * @param  group    DOCUMENT ME!
         * @param  type     DOCUMENT ME!
         * @param  project  backend DOCUMENT ME!
         */
        public GenericConfigAttrGroupNodeChildren(final String group,
                final Types type,
                final DomainserverProject project) {
            super(project);
            this.type = type;
            this.group = group;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected void threadedNotify() throws IOException {
            final List<ConfigAttrEntry> entries = project.getCidsDataObjectBackend().getEntries(type, group);
            Collections.sort(entries, new Comparator<ConfigAttrEntry>() {

                    @Override
                    public int compare(final ConfigAttrEntry o1, final ConfigAttrEntry o2) {
                        return o1.getKey().getKey().compareTo(o2.getKey().getKey());
                    }
                });

            final Set<ConfigAttrKey> keys = new LinkedHashSet<ConfigAttrKey>(entries.size());
            for (final ConfigAttrEntry entry : entries) {
                keys.add(entry.getKey());
            }

            setKeysEDT(KeyContainer.convertCollection(ConfigAttrKey.class, keys));
        }

        @Override
        protected Node[] createUserNodes(final Object o) {
            if ((o instanceof KeyContainer) && (((KeyContainer)o).getObject() instanceof ConfigAttrKey)) {
                return new Node[] {
                        new ConfigAttrKeyNode((ConfigAttrKey)((KeyContainer)o).getObject(), type, project)
                    };
            } else {
                return new Node[] {};
            }
        }
    }
}

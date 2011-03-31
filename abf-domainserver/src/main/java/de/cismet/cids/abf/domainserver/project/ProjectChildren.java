/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.util.WeakListeners;

import java.awt.EventQueue;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;

import de.cismet.cids.abf.utilities.nodes.LoadingNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class ProjectChildren extends Children.Keys {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ProjectChildren.class);

    //~ Instance fields --------------------------------------------------------

    protected final transient DomainserverProject project;
    protected final transient NodeListener nodeL;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProjectChildren object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ProjectChildren(final DomainserverProject project) {
        super(true);
        this.project = project;
        this.nodeL = new NodeListenerImpl();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void addNotify() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNotify caller debug, initialized: " + isInitialized(), new Throwable("trace: " + this)); // NOI18N
        }

        final LoadingNode loadingNode = new LoadingNode();
        if (!isInitialized()) {
            setKeys(new Object[] { loadingNode });
        }
        final Thread loader = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            threadedNotify();
                        } catch (final Exception e) {
                            LOG.error("could not generate keys", e); // NOI18N
                            setKeysEDT(e);
                        } finally {
                            loadingNode.dispose();
                        }
                    }
                });
        loader.start();
    }

    @Override
    protected Node[] createNodes(final Object o) {
        final Node[] created;

        if (o instanceof Exception) {
            final AbstractNode node = new AbstractNode(LEAF);
            node.setName(((Exception)o).getLocalizedMessage());

            created = new Node[] { node };
        } else if (o instanceof LoadingNode) {
            created = new Node[] { (Node)o };
        } else {
            created = createUserNodes(o);
            for (final Node node : created) {
                node.addNodeListener(WeakListeners.create(NodeListener.class, nodeL, node));
            }
        }

        return created;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract Node[] createUserNodes(final Object o);

    /**
     * DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    protected abstract void threadedNotify() throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @param  keys  DOCUMENT ME!
     */
    protected void setKeysEDT(final Collection keys) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setKeys(keys);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  keys  DOCUMENT ME!
     */
    protected void setKeysEDT(final Object... keys) {
        setKeysEDT(Arrays.asList(keys));
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshByNotify() {
        addNotify();
        // we do not refresh the already present nodes anymore since we don't want all sub-objects to query the db for
        // changes. it is decided that the user has to use the ABF to do any db changes or he is responsible for data
        // consistency to himself.
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class NodeListenerImpl extends NodeAdapter {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  ev  DOCUMENT ME!
         */
        @Override
        public void nodeDestroyed(final NodeEvent ev) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("node destroyed, event: " + ev + " || refreshByNotify: " + ProjectChildren.this);
            }
//            refreshByNotify();
        }
    }
}

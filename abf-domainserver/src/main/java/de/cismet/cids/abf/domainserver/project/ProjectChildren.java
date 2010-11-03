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

import de.cismet.cids.abf.utilities.Refreshable;
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
        this.project = project;
        this.nodeL = new NodeListenerImpl();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void addNotify() {
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
        final Node[] before = getNodes();
        addNotify();
        // We refresh the nodes that were already present before the notify, too. This is to propagate the refresh to
        // all sub nodes
        for (final Node node : getNodes()) {
            for (final Node old : before) {
                if (node.equals(old)) {
                    final Refreshable cookie = node.getCookie(Refreshable.class);
                    if (cookie != null) {
                        cookie.refresh();
                    }
                    break;
                }
            }
        }
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
            refreshByNotify();
        }
    }
}

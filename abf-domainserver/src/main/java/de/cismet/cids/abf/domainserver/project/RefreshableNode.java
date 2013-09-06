/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Mutex.Action;

import java.awt.EventQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.cismet.cids.abf.utilities.Refreshable;
import de.cismet.cids.abf.utilities.nodes.PropertyRefresh;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class RefreshableNode extends ProjectNode implements Refreshable, PropertyRefresh {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(RefreshableNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient ExecutorService refreshDispatcher;
    private final transient ReentrantReadWriteLock sheetLock;

    private transient boolean sheetInitialised;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RefreshableNode object.
     *
     * @param  c                  DOCUMENT ME!
     * @param  p                  DOCUMENT ME!
     * @param  refreshDispatcher  DOCUMENT ME!
     */
    public RefreshableNode(final Children c, final DomainserverProject p, final ExecutorService refreshDispatcher) {
        super(c, p);

        this.refreshDispatcher = refreshDispatcher;
        this.sheetLock = new ReentrantReadWriteLock();
        this.sheetInitialised = false;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isSheetInitialised() {
        sheetLock.readLock().lock();
        try {
            return sheetInitialised;
        } finally {
            sheetLock.readLock().unlock();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sheetInitialised  DOCUMENT ME!
     */
    public final void setSheetInitialised(final boolean sheetInitialised) {
        sheetLock.writeLock().lock();
        try {
            this.sheetInitialised = sheetInitialised;
        } finally {
            sheetLock.writeLock().unlock();
        }
    }

    @Override
    public final void refresh() {
        refreshDispatcher.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        threadedRefresh();

                        final Node[] nodes = Children.MUTEX.readAccess(new Action<Node[]>() {

                                    @Override
                                    public Node[] run() {
                                        return getChildren().getNodes();
                                    }
                                });
                        for (final Node node : nodes) {
                            final Refreshable refreshable = node.getCookie(Refreshable.class);
                            if (refreshable != null) {
                                refreshable.refresh();
                            }

                            final PropertyRefresh propertyRefresh = node.getCookie(PropertyRefresh.class);
                            if (propertyRefresh != null) {
                                propertyRefresh.refreshProperties(false);
                            }
                        }

                        if (isSheetInitialised()) {
                            final Sheet sheet = createSheet();

                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        setSheet(sheet);
                                    }
                                });
                        }
                    } catch (final Exception ex) {
                        LOG.warn("unsuccessful refresh: " + this, ex); // NOI18N
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void threadedRefresh() throws Exception {
        final Children c = Children.MUTEX.readAccess(new Action<Children>() {

                    @Override
                    public Children run() {
                        return getChildren();
                    }
                });
        if (c instanceof ProjectChildren) {
            final Future<?> future = ((ProjectChildren)c).refreshByNotify();

            // if future == null refresh is already finished
            if(future != null){
                future.get(30, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public final void refreshProperties(final boolean forceInit) {
        refreshDispatcher.execute(new Runnable() {

                @Override
                public void run() {
                    if (sheetInitialised || forceInit) {
                        final Sheet sheet = createSheet();

                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    setSheet(sheet);
                                }
                            });
                    }

                    final Node[] nodes = Children.MUTEX.readAccess(new Action<Node[]>() {

                                @Override
                                public Node[] run() {
                                    return getChildren().getNodes();
                                }
                            });
                    for (final Node n : nodes) {
                        final PropertyRefresh pr = n.getCookie(PropertyRefresh.class);
                        if (pr != null) {
                            pr.refreshProperties(forceInit);
                        }
                    }
                }
            });
    }
}

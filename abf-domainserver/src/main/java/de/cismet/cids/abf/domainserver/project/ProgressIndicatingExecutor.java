/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.apache.log4j.Logger;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import java.awt.EventQueue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ProgressIndicatingExecutor implements ExecutorService {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(ProgressIndicatingExecutor.class);

    public static final String ABF_THREAD_GROUP_NAME = "ABF"; // NOI18N

    private static final Timer TIMER = new Timer("progress-indicating-executor"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient ExecutorService executor;
    private final transient Set<Future<?>> running;
    private final transient ReentrantReadWriteLock rwLock;
    private final transient String displayName;

    private transient TimerTask progressCheck;
    private transient ProgressHandle handle;
    private transient int total;
    private transient int finished;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProgressIndicatingExecutor object.
     *
     * @param  displayName  DOCUMENT ME!
     * @param  prefix       DOCUMENT ME!
     * @param  noOfThreads  DOCUMENT ME!
     */
    public ProgressIndicatingExecutor(final String displayName, final String prefix, final int noOfThreads) {
        final ThreadFactory tg = CismetConcurrency.getInstance(ABF_THREAD_GROUP_NAME).createThreadFactory(prefix);

        this.executor = CismetExecutors.newFixedThreadPool(noOfThreads, tg);
        this.running = new HashSet<Future<?>>();
        this.rwLock = new ReentrantReadWriteLock();
        this.displayName = displayName;
        this.handle = null;
        this.progressCheck = null;
        this.total = 0;
        this.finished = 0;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean tasksInProgress() {
        rwLock.readLock().lock();

        try {
            return !running.isEmpty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   task  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void scheduleProgressCheck(final Future<?> task) {
        rwLock.writeLock().lock();
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("scheduling new future (total before=" + total + "): " + task + ""); // NOI18N
            }

            running.add(task);
            total++;

            if (progressCheck == null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("new ProgressCheck new future (total=" + total + "): " + task + ""); // NOI18N
                }
                progressCheck = new ProgressCheck();

                try {
                    final Runnable r = new Runnable() {

                            @Override
                            public void run() {
                                handle = ProgressHandleFactory.createHandle(displayName);
                                handle.start(100);

                                if (LOG.isTraceEnabled()) {
                                    LOG.trace("EDT handle started"); // NOI18N
                                }
                            }
                        };
                    if (EventQueue.isDispatchThread()) {
                        r.run();
                    } else {
                        EventQueue.invokeAndWait(r);
                    }
                } catch (final Exception ex) {
                    final String message = "cannot start progress handle"; // NOI18N
                    LOG.error(message, ex);
                    throw new IllegalStateException(message, ex);
                }

                TIMER.scheduleAtFixedRate(progressCheck, 333, 333);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void execute(final Runnable command) {
        final Future<?> future = executor.submit(command);

        scheduleProgressCheck(future);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        final Future<?> future = executor.submit(task);

        scheduleProgressCheck(future);

        return future;
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        final Future<T> future = executor.submit(task, result);

        scheduleProgressCheck(future);

        return future;
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        final Future<T> future = executor.submit(task);

        scheduleProgressCheck(future);

        return future;
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> arg0, final long arg1, final TimeUnit arg2)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(arg0, arg1, arg2);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> arg0) throws InterruptedException,
        ExecutionException {
        return executor.invokeAny(arg0);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> arg0,
            final long arg1,
            final TimeUnit arg2) throws InterruptedException {
        return executor.invokeAll(arg0, arg1, arg2);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> arg0) throws InterruptedException {
        return executor.invokeAll(arg0);
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ProgressCheck extends TimerTask {

        //~ Instance fields ----------------------------------------------------

        private transient int maxPercent = 0;
        private transient int noOfExecutions = 0;

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            Lock lock = rwLock.readLock();
            lock.lock();

            try {
                noOfExecutions++;

                if (LOG.isTraceEnabled()) {
                    LOG.trace("progresscheck enter: " // NOI18N
                                + "[total=" + total // NOI18N
                                + "|finished=" + finished // NOI18N
                                + "|running=" + running.size() // NOI18N
                                + "|noOfExecs=" + noOfExecutions + "]"); // NOI18N
                }

                if (running.isEmpty()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("progresscheck handle finish: " // NOI18N
                                    + "[total=" + total     // NOI18N
                                    + "|finished=" + finished // NOI18N
                                    + "|running=" + running.size() // NOI18N
                                    + "|noOfExecs=" + noOfExecutions + "]"); // NOI18N
                    }

                    // lock upgrade
                    lock.unlock();
                    lock = rwLock.writeLock();
                    lock.lock();

                    // check again after upgrade, maybe more tasks were added, don't do anything if still running
                    if (running.isEmpty()) {
                        try {
                            EventQueue.invokeAndWait(new Runnable() {

                                    @Override
                                    public void run() {
                                        handle.finish();
                                        handle = null;

                                        if (LOG.isTraceEnabled()) {
                                            LOG.trace("EDT handle finished"); // NOI18N
                                        }
                                    }
                                });

                            progressCheck.cancel();
                            progressCheck = null;
                            running.clear();
                            total = 0;
                            finished = 0;
                        } catch (final Exception e) {
                            final String message = "cannot finish progress handle"; // NOI18N
                            LOG.error(message, e);
                            throw new IllegalStateException(message, e);
                        }
                    }
                } else {
                    // we don't use an iterator to avoid lock up- and downgrade overhead
                    final Set<Future<?>> toRemove = new HashSet<Future<?>>();
                    for (final Future<?> future : running) {
                        if (future.isDone()) {
                            toRemove.add(future);
                            // finished is only used here so no need for (write)lock
                            finished++;
                        }
                    }

                    final int percent = (int)(((double)finished / (double)total) * 100);

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("progresscheck handle progress: " // NOI18N
                                    + "[total=" + total       // NOI18N
                                    + "|finished=" + finished // NOI18N
                                    + "|running=" + running.size() // NOI18N
                                    + "|progress=" + percent  // NOI18N
                                    + "|maxpercent=" + maxPercent // NOI18N
                                    + "|noOfExecs=" + noOfExecutions + "]"); // NOI18N
                    }

                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                // maxpercent is stored in the outer instance, however, only accessed here it is likely,
                                // and in case that maxPercent is higher than percent we create a new handle since
                                // "stepping back" is not allowed by the handle implementation
                                if (maxPercent > percent) {
                                    handle.finish();
                                    handle = ProgressHandleFactory.createHandle(displayName);
                                    handle.start(100);
                                }

                                handle.progress(percent);
                                maxPercent = percent;

                                if (LOG.isTraceEnabled()) {
                                    LOG.trace("EDT handle progressed (" + percent + ")"); // NOI18N
                                }
                            }
                        });

                    if (!toRemove.isEmpty()) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("progresscheck finished removal (before): " // NOI18N
                                        + "[total=" + total                 // NOI18N
                                        + "|finished=" + finished           // NOI18N
                                        + "|running=" + running.size()      // NOI18N
                                        + "|noOfExecs=" + noOfExecutions + "]"); // NOI18N
                        }

                        // lock upgrade
                        lock.unlock();
                        lock = rwLock.writeLock();
                        lock.lock();

                        for (final Future<?> future : toRemove) {
                            running.remove(future);
                        }

                        if (LOG.isTraceEnabled()) {
                            LOG.trace("progresscheck finished removal (after): " // NOI18N
                                        + "[total=" + total                // NOI18N
                                        + "|finished=" + finished          // NOI18N
                                        + "|running=" + running.size() + "]"); // NOI18N
                        }
                    }
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("progresscheck leave: " // NOI18N
                                + "[total=" + total // NOI18N
                                + "|finished=" + finished // NOI18N
                                + "|running=" + running.size() + "]"); // NOI18N
                }
            } finally {
                lock.unlock();
            }
        }
    }
}

package org.anarres.weaklistener.core;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
/* pp */ class WeakListenerReferenceQueue extends ReferenceQueue<Object> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WeakListenerReferenceQueue.class);

    /** This keeps a strong reference to the WeakListenerReferenceQueue as long as it is alive. */
    public static class CollectorThread extends Thread {

        public CollectorThread(@Nonnull WeakListenerReferenceQueue queue) {
            super(queue, "WeakListenerReferenceQueue CollectorThread");
            setDaemon(true);
        }
    }

    public static interface Entry extends Runnable {

        @CheckForNull
        public Executor getExecutor();
    }

    private static Reference<WeakListenerReferenceQueue> QUEUE;
    private static final Object LOCK = new Object();

    @Nonnull
    public static WeakListenerReferenceQueue getInstance() {
        synchronized (LOCK) {
            WeakListenerReferenceQueue queue = (QUEUE == null) ? null : QUEUE.get();
            if (queue == null) {
                queue = new WeakListenerReferenceQueue();
                CollectorThread thread = new CollectorThread(queue);
                thread.start();
                QUEUE = new WeakReference<WeakListenerReferenceQueue>(queue);
            }
            return queue;
        }
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Entry reference = (Entry) remove();
                try {
                    Executor executor = reference.getExecutor();
                    if (executor == null)
                        reference.run();
                    else
                        executor.execute(reference);
                } catch (Throwable t) {
                    LOG.error("Failed to clean up reference", t);
                } finally {
                    reference = null;
                }
            }
        } catch (InterruptedException e) {
            LOG.error("WeakListenerReferenceQueue interrupted", e);
        }
    }
}

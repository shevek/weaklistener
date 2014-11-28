package org.anarres.weaklistener.core;

import org.anarres.weaklistener.executor.DirectExecutor;
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
/* pp */ class WeakListenerReference<L> extends WeakReference<L> implements Runnable, WeakListenerReferenceQueue.Entry {

    private static final Logger LOG = LoggerFactory.getLogger(WeakListenerReference.class);
    private final WeakListener<?> weakListener;
    /**
     * If the source GCs, we generally GC as well.
     * If we don't, it's because the source passed our reference on to
     * somebody else, so knowing the previous source won't help us.
     */
    private final WeakReference<Object> sourceReference;

    /**
     * If we are cleaning up from an auxiliary source, we need a dummy referent.
     * So we cannot bind both WeakListener[L] and L referent to the generic parameter L.
     * Since it is useful to have WeakListenerReference extend WeakReference[L],
     * we make the slightly odd choice of discarding the type parameter on the WeakListener.
     */
    public WeakListenerReference(@Nonnull WeakListener<?> weakListener, @Nonnull Object source, @Nonnull L referent, @Nonnull ReferenceQueue<? super L> queue) {
        super(referent, queue);
        this.weakListener = weakListener;
        this.sourceReference = new WeakReference<Object>(source);
    }

    @CheckForNull
    public Object getSource() {
        return sourceReference.get();
    }

    @CheckForNull
    public L getStrongListener() {
        return get();
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void run() {
        Object source = getSource();
        if (source == null)
            return;
        weakListener.remove(source);
    }
}

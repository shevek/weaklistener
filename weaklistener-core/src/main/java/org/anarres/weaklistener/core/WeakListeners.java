package org.anarres.weaklistener.core;

import java.beans.PropertyChangeListener;
import java.util.EventListener;
import javax.annotation.Nonnull;
import javax.swing.event.ChangeListener;

/**
 *
 * @author shevek
 */
public class WeakListeners {

    @Nonnull
    public static <L extends EventListener> L create(@Nonnull Object source, @Nonnull Class<L> listenerType, @Nonnull L listener) {
        WeakListenerImpl.Proxy<L> proxy = new WeakListenerImpl.Proxy<L>(source, listenerType, listener);
        return proxy.getWeakListener();
    }

    @Nonnull
    public static ChangeListener change(@Nonnull Object source, @Nonnull ChangeListener listener) {
        return new WeakListenerImpl.Change(source, listener);
    }

    @Nonnull
    public static PropertyChangeListener propertyChange(@Nonnull Object source, @Nonnull PropertyChangeListener listener) {
        return new WeakListenerImpl.PropertyChange(source, listener);
    }
}

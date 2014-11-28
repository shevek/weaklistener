package org.anarres.weaklistener.core;

import java.lang.reflect.Method;
import java.util.EventObject;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
/* pp */ abstract class WeakListener<L> {

    private static final Logger LOG = LoggerFactory.getLogger(WeakListener.class);
    private final WeakListenerReference<L> listenerReference;

    public WeakListener(@Nonnull Object source, @Nonnull L strongListener) {
        this.listenerReference = new WeakListenerReference<L>(this, source, strongListener, WeakListenerReferenceQueue.getInstance());
    }

    @Nonnull
    protected abstract Class<? extends L> getListenerType();

    @CheckForNull
    protected Object getSource() {
        return listenerReference.getSource();
    }

    @CheckForNull
    protected L getStrongListener() {
        return listenerReference.getStrongListener();
    }

    @Nonnull
    protected abstract L getWeakListener();

    @Nonnull
    protected abstract String getRemoveMethodName();

    @CheckForNull
    private Method getRemoveMethod(@Nonnull Object source) {
        Class<?> removeMethodClass = source.getClass();
        String removeMethodName = getRemoveMethodName();
        Class<?>[] removeMethodParameters = new Class<?>[]{getListenerType()};

        try {
            Method removeMethod = removeMethodClass.getMethod(removeMethodName, removeMethodParameters);
            WeakListenerUtils.makeAccessible(removeMethod);
            return removeMethod;
        } catch (NoSuchMethodException e) {
        }

        while (removeMethodClass != Object.class) {
            try {
                Method removeMethod = removeMethodClass.getDeclaredMethod(removeMethodName, removeMethodParameters);
                WeakListenerUtils.makeAccessible(removeMethod);
                return removeMethod;
            } catch (NoSuchMethodException e) {
            }
            removeMethodClass = removeMethodClass.getSuperclass();
        }

        LOG.debug("No method {}.{}(...)", source.getClass().getName(), removeMethodName);
        return null;
    }

    public void remove(@Nonnull Object source) {
        Method removeMethod = getRemoveMethod(source);
        if (removeMethod == null)
            return;
        try {
            removeMethod.invoke(source, getWeakListener());
        } catch (Exception e) {
            LOG.warn("Failed to invoke " + removeMethod, e);
        }
    }

}

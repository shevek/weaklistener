package org.anarres.weaklistener.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author shevek
 */
/* pp */ abstract class WeakListenerImpl<L> extends WeakListener<L> {

    public WeakListenerImpl(@Nonnull Object source, @Nonnull L strongListener) {
        super(source, strongListener);
    }

    protected Object getEventSource(@CheckForNull EventObject event) {
        if (event == null)
            return null;
        Object source = event.getSource();
        if (source == null)
            return null;
        if (source == getSource())
            return null;
        return source;
    }

    public static class PropertyChange extends WeakListenerImpl<PropertyChangeListener> implements PropertyChangeListener {

        public PropertyChange(@Nonnull Object source, @Nonnull PropertyChangeListener listener) {
            super(source, listener);
        }

        @Override
        protected Class<PropertyChangeListener> getListenerType() {
            return PropertyChangeListener.class;
        }

        @Override
        protected PropertyChangeListener getWeakListener() {
            return this;
        }

        @Override
        protected String getRemoveMethodName() {
            return "removePropertyChangeListener";
        }

        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener listener = getStrongListener();
            if (listener != null)
                listener.propertyChange(evt);
            else
                remove(evt.getSource());
        }
    }

    public static class Change extends WeakListenerImpl<ChangeListener> implements ChangeListener {

        public Change(@Nonnull Object source, @Nonnull ChangeListener listener) {
            super(source, listener);
        }

        @Override
        protected Class<ChangeListener> getListenerType() {
            return ChangeListener.class;
        }

        @Override
        protected ChangeListener getWeakListener() {
            return this;
        }

        @Override
        protected String getRemoveMethodName() {
            return "removeChangeListener";
        }

        public void stateChanged(ChangeEvent evt) {
            ChangeListener listener = getStrongListener();
            if (listener != null)
                listener.stateChanged(evt);
            else
                remove(evt.getSource());
        }
    }

    public static class Proxy<L extends EventListener> extends WeakListenerImpl<L> implements InvocationHandler {

        private final Class<L> listenerType;
        private final L listenerImplementation;

        public Proxy(@Nonnull Object source,
                @Nonnull Class<L> listenerType, @Nonnull L listener) {
            super(source, listener);
            Object impl = java.lang.reflect.Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType}, this);
            this.listenerType = listenerType;
            this.listenerImplementation = listenerType.cast(impl);
        }

        @Override
        public Class<L> getListenerType() {
            return listenerType;
        }

        @Override
        protected L getWeakListener() {
            return listenerImplementation;
        }

        @Override
        protected String getRemoveMethodName() {
            return "remove".concat(listenerType.getSimpleName());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (WeakListenerUtils.isEqualsMethod(method))
                    return equals(args[0]);
                if (method.getDeclaringClass() == Object.class)
                    return method.invoke(this, args);
                EventObject evt;
                if (args == null || args.length == 0)
                    evt = null;
                else if (args[0] instanceof EventObject)
                    evt = (EventObject) args[0];
                else
                    evt = null;
                L listener = getStrongListener();
                if (listener != null)
                    return method.invoke(listener, args);
                Object source = getEventSource(evt);
                if (source != null)
                    remove(source);
                return null;
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) || listenerImplementation == obj;
        }
    }
}

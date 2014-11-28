package org.anarres.weaklistener.core;

import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class ProxyListenerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyListenerTest.class);

    public static interface EventListener extends java.util.EventListener {

        public void event(EventObject e);
    }

    public static class EventSource {

        private final List<EventListener> listeners = new CopyOnWriteArrayList<EventListener>();

        public void addEventListener(EventListener listener) {
            listeners.add(listener);
        }

        public void removeEventListener(EventListener listener) {
            listeners.remove(listener);
        }

        public void fireEvent() {
            for (EventListener listener : listeners)
                listener.event(new EventObject(this));
        }
    }

    @Test
    public void testProxyEventListener() throws Exception {
        EventSource source = new EventSource();

        final AtomicInteger count = new AtomicInteger();
        EventListener strongListener = new EventListener() {

            public void event(EventObject e) {
                LOG.info("Event: " + e);
                count.getAndIncrement();
            }
        };
        EventListener weakListener = WeakListeners.create(source, EventListener.class, strongListener);
        source.addEventListener(weakListener);
        source.fireEvent();
        strongListener = null;
        WeakListenerTestUtils.gc();
        assertTrue("Source still has listeners.", source.listeners.isEmpty());

        source.fireEvent();
        assertEquals("Got wrong number of calls.", 1, count.get());

        EventSource copy = new EventSource();
        copy.addEventListener(weakListener);
        copy.fireEvent();
        assertTrue("Copy still has listeners.", copy.listeners.isEmpty());
    }

    public static interface PureListener extends java.util.EventListener {

        public void event();
    }

    public static class PureSource {

        private final List<PureListener> listeners = new CopyOnWriteArrayList<PureListener>();

        public void addPureListener(PureListener listener) {
            listeners.add(listener);
        }

        public void removePureListener(PureListener listener) {
            listeners.remove(listener);
        }

        public void firePure() {
            for (PureListener listener : listeners)
                listener.event();
        }
    }

    @Test
    public void testProxyPureListener() throws Exception {
        PureSource source = new PureSource();

        final AtomicInteger count = new AtomicInteger();
        PureListener strongListener = new PureListener() {

            public void event() {
                LOG.info("Event");
                count.getAndIncrement();
            }
        };
        PureListener weakListener = WeakListeners.create(source, PureListener.class, strongListener);
        source.addPureListener(weakListener);
        source.firePure();
        strongListener = null;
        WeakListenerTestUtils.gc();
        assertTrue("Source still has listeners.", source.listeners.isEmpty());

        source.firePure();
        assertEquals("Got wrong number of calls.", 1, count.get());

        PureSource copy = new PureSource();
        copy.addPureListener(weakListener);
        copy.firePure();
        assertFalse("Copy has no listeners.", copy.listeners.isEmpty());    // We can't remove it. :-(
    }

    public static interface ObjectListener extends java.util.EventListener {

        public void event(String value);
    }

    public static class ObjectSource {

        private final List<ObjectListener> listeners = new CopyOnWriteArrayList<ObjectListener>();

        public void addObjectListener(ObjectListener listener) {
            listeners.add(listener);
        }

        public void removeObjectListener(ObjectListener listener) {
            listeners.remove(listener);
        }

        public void fireObject() {
            for (ObjectListener listener : listeners)
                listener.event("My Value");
        }
    }

    @Test
    public void testProxyObjectListener() throws Exception {
        ObjectSource source = new ObjectSource();

        final AtomicInteger count = new AtomicInteger();
        ObjectListener strongListener = new ObjectListener() {

            public void event(String value) {
                LOG.info("Event: " + value);
                count.getAndIncrement();
            }
        };
        ObjectListener weakListener = WeakListeners.create(source, ObjectListener.class, strongListener);
        source.addObjectListener(weakListener);
        source.fireObject();
        strongListener = null;
        WeakListenerTestUtils.gc();
        assertTrue("Source still has listeners.", source.listeners.isEmpty());

        source.fireObject();
        assertEquals("Got wrong number of calls.", 1, count.get());

        ObjectSource copy = new ObjectSource();
        copy.addObjectListener(weakListener);
        copy.fireObject();
        assertFalse("Copy has no listeners.", copy.listeners.isEmpty());    // We can't remove it. :-(
    }
}

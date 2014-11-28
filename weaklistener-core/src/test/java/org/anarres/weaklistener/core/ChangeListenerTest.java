package org.anarres.weaklistener.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class ChangeListenerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeListenerTest.class);

    public static abstract class Base {

        protected List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

        public void addChangeListener(ChangeListener listener) {
            listeners.add(listener);
        }

        public void fireChange() {
            for (ChangeListener listener : listeners)
                listener.stateChanged(new ChangeEvent(this));
        }
    }

    public static class Remove extends Base {

        public void removeChangeListener(ChangeListener listener) {
            listeners.remove(listener);
        }
    }

    public static class NoRemove extends Base {
    }

    private static class RemovePrivateClass extends Base {

        public void removeChangeListener(ChangeListener listener) {
            listeners.remove(listener);
        }
    }

    public static class RemovePrivateMethod extends Base {

        private void removeChangeListener(ChangeListener listener) {
            listeners.remove(listener);
        }
    }

    public static class RemoveThrows extends Base {

        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }
    }

    private void testWeakListener(Base source, boolean removed) throws Exception {
        final AtomicInteger count = new AtomicInteger();
        ChangeListener strongListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                LOG.info("Change: " + e);
                count.getAndIncrement();
            }
        };
        ChangeListener weakListener = WeakListeners.change(source, strongListener);
        source.addChangeListener(weakListener);
        source.fireChange();
        strongListener = null;
        WeakListenerTestUtils.gc();
        assertEquals("Source " + source.getClass().getSimpleName() + " still has listeners.", removed, source.listeners.isEmpty());

        source.fireChange();
        assertEquals("Got wrong number of calls.", 1, count.get());

        Base copy = new Remove();
        copy.addChangeListener(weakListener);
        copy.fireChange();
        assertTrue("Copy " + copy.getClass().getSimpleName() + " still has listeners.", copy.listeners.isEmpty());
    }

    @Test
    public void testListener() throws Exception {
        testWeakListener(new Remove(), true);
        testWeakListener(new NoRemove(), false);
        testWeakListener(new RemovePrivateClass(), true);
        testWeakListener(new RemovePrivateMethod(), true);
        testWeakListener(new RemoveThrows(), false);
    }

}

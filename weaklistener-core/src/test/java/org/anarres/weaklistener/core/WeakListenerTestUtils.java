package org.anarres.weaklistener.core;

/**
 *
 * @author shevek
 */
public class WeakListenerTestUtils {

    public static void gc() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            System.gc();
            Thread.sleep(50);   // Let the ReferenceQueue kick in.
        }
    }
}

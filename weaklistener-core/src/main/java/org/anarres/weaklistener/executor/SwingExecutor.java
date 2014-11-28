package org.anarres.weaklistener.executor;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;

/**
 *
 * @author shevek
 */
public enum SwingExecutor implements Executor {

    INSTANCE;

    @Override
    public void execute(Runnable command) {
        try {
            EventQueue.invokeAndWait(command);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            if (t instanceof Error)
                throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}

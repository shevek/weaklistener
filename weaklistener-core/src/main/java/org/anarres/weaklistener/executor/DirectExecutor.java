package org.anarres.weaklistener.executor;

import java.util.concurrent.Executor;

/**
 *
 * @author shevek
 */
public enum DirectExecutor implements Executor {

    INSTANCE;

    @Override
    public void execute(Runnable command) {
        command.run();
    }

}

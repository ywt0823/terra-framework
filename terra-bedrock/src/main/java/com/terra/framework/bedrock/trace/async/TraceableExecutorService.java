package com.terra.framework.bedrock.trace.async;



import com.terra.framework.bedrock.trace.TraceHelper;

import java.util.concurrent.Executor;

/**
 * TraceableExecutorService
 *
 * @author Shawn
 * @version 1.0
 * @since 2020/10/27 15:55
 **/
public class TraceableExecutorService implements Executor {

    final Executor delegate;

    public TraceableExecutorService(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            return;
        }
        this.delegate.execute(new TraceRunnable(command, TraceHelper.getTraceId()));
    }
}

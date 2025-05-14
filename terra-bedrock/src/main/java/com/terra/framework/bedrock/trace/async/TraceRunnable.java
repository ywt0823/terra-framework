package com.terra.framework.bedrock.trace.async;


import com.terra.framework.bedrock.trace.TraceHelper;

/**
 * 包装 TraceableExecutorService 中 Tread
 *
 * @author Shawn
 * @version 1.0
 * @since 2020/10/27 16:00
 **/
public class TraceRunnable implements Runnable {

    private final Runnable delegate;

    private final String tracerId;


    public TraceRunnable(Runnable delegate, String tracerId) {
        this.delegate = delegate;
        this.tracerId = tracerId;
    }

    @Override
    public void run() {
        TraceHelper.markChildTraceId(tracerId);
        this.delegate.run();
    }
}

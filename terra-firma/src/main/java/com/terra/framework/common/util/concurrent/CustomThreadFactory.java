package com.terra.framework.common.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yangwt
 * @date 2023/6/2 09:36
 **/
public class CustomThreadFactory implements ThreadFactory {

    private final ThreadGroup group;

    private final AtomicInteger index = new AtomicInteger(1);

    protected CustomThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, "TERRA-THREAD-GROUP-" + index.getAndIncrement());
        t.setDaemon(false);
        return t;
    }
}

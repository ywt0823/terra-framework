package com.terra.framework.bedrock.trace.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * CompletableFuture 封装，支持tracerId传递
 *
 * @author ywt
 * @version 2.0
 * @since 2020/10/27 14:50
 **/
public class TerraCompletableFuture {

    private static Executor taskExecutor;

    // 线程池
    public static void setTaskExecutor(Executor taskExecutor) {
        if (taskExecutor == null) {
            return;
        }
        TerraCompletableFuture.taskExecutor = taskExecutor;
    }

    static final class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {

        Executor asyncPool = taskExecutor == null ? new ThreadPerTaskExecutor() : taskExecutor;

        TraceableExecutorService traceableExecutor;

        if (asyncPool instanceof TraceableExecutorService) {
            traceableExecutor = (TraceableExecutorService) asyncPool;
        } else {
            traceableExecutor = new TraceableExecutorService(asyncPool);
        }

        return CompletableFuture.supplyAsync(supplier, traceableExecutor);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return CompletableFuture.supplyAsync(supplier, new TraceableExecutorService(executor));
    }
}

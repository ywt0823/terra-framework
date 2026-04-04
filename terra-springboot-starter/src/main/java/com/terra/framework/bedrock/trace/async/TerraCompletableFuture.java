package com.terra.framework.bedrock.trace.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * CompletableFuture wrapper that propagates trace context.
 */
public class TerraCompletableFuture {

    private static volatile Executor taskExecutor;

    /**
     * Optional default executor (e.g. application thread pool). If unset, {@link ForkJoinPool#commonPool()} is used.
     */
    public static void setTaskExecutor(Executor taskExecutor) {
        if (taskExecutor == null) {
            return;
        }
        TerraCompletableFuture.taskExecutor = taskExecutor;
    }

    private static Executor defaultExecutor() {
        Executor configured = taskExecutor;
        if (configured != null) {
            return configured;
        }
        return ForkJoinPool.commonPool();
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        Executor asyncPool = defaultExecutor();
        TraceableExecutorService traceableExecutor =
            asyncPool instanceof TraceableExecutorService traceable
                ? traceable
                : new TraceableExecutorService(asyncPool);
        return CompletableFuture.supplyAsync(supplier, traceableExecutor);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return CompletableFuture.supplyAsync(supplier, new TraceableExecutorService(executor));
    }
}

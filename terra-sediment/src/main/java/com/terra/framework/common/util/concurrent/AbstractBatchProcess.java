package com.terra.framework.common.util.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author yangwt
 * @date 2023/6/2 09:37
 **/
@Slf4j
public abstract class AbstractBatchProcess<V> implements Runnable {
    private final List<V> cache = new CopyOnWriteArrayList<>();

    protected final Object lock = new Object();

    private ScheduledExecutorService taskRunner;

    /**
     * 单次消息写入最大数
     */
    protected int messageCacheCapacity = 2000;

    /**
     * 定时写入事件
     */
    protected Duration persistDuration = Duration.parse("PT10S");

    protected ExecutorService executor;

    {
        executor = Executors.newFixedThreadPool(4, new CustomThreadFactory());
        taskRunner = Executors.newSingleThreadScheduledExecutor();
    }

    public AbstractBatchProcess() {

    }

    public AbstractBatchProcess(int messageCacheCapacity, Duration persistDuration) {
        this.messageCacheCapacity = messageCacheCapacity;
        this.persistDuration = persistDuration;
    }

    public AbstractBatchProcess(CustomThreadFactory threadPoolTaskScheduler, int messageCacheCapacity, Duration persistDuration) {
        this.taskRunner = Executors.newSingleThreadScheduledExecutor(threadPoolTaskScheduler);
        this.messageCacheCapacity = messageCacheCapacity;
        this.persistDuration = persistDuration;
    }

    public AbstractBatchProcess(CustomThreadFactory threadPoolTaskScheduler, int messageCacheCapacity, Duration persistDuration, ExecutorService executor) {
        this.taskRunner = Executors.newSingleThreadScheduledExecutor(threadPoolTaskScheduler);
        this.messageCacheCapacity = messageCacheCapacity;
        this.persistDuration = persistDuration;
        this.executor = executor;
    }

    public void process(V message) {
        if (!support(message)) {
            return;
        }
        synchronized (lock) {
            cache.add(message);
            if (cache.size() < messageCacheCapacity) {
                return;
            }
            this.run();
        }
    }

    @Override
    public void run() {
        log.debug("定时批量处理消息-开始");
        List<V> pendingMessages;
        synchronized (lock) {
            if (cache.isEmpty()) {
                return;
            }
            pendingMessages = new CopyOnWriteArrayList<>(cache);
            cache.clear();
        }
        executor.execute(() -> {
            if (pendingMessages.isEmpty()) {
                return;
            }
            try {
                log.info("批量处理消息，消息长度:{}", pendingMessages.size());
                this.batchInsert(pendingMessages);
                log.info("批量处理消息完成");
            } catch (Exception e) {
                log.error("批量处理消息异常", e);
                throw e;
            }
        });
        log.debug("定时批量处理消息-结束");
    }

    protected void schedule() {
        this.taskRunner.scheduleAtFixedRate(this, 10, this.persistDuration.getSeconds(), TimeUnit.SECONDS);
    }

    protected void destroy() throws Exception {
        this.run();
        taskRunner.awaitTermination(3L, TimeUnit.SECONDS);
        taskRunner.shutdown();
        taskRunner.awaitTermination(3L, TimeUnit.SECONDS);
        executor.shutdown();
    }

    protected abstract Boolean support(V message);

    protected abstract Boolean batchInsert(List<V> messages);
}

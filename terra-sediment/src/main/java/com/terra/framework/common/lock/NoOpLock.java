package com.terra.framework.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * 一个空操作的锁实现，用于单机环境或不需要分布式锁的场景.
 * <p>
 * 该实现的所有方法都是无操作的，并且总是返回成功.
 * 它可以作为分布式锁的默认回退选项，以避免在缺少锁配置时应用程序无法启动.
 * </p>
 *
 * @author Terra Framework Team
 */
public class NoOpLock implements ILock {

    /**
     * 单例实例
     */
    public static final NoOpLock INSTANCE = new NoOpLock();

    private NoOpLock() {
        // 私有构造，确保单例
    }

    /**
     * 模拟获取锁，总是返回true.
     *
     * @param lockId       锁的唯一标识.
     * @param heldTimeUnit 持有时间的单位.
     * @param heldTimes    持有时间.
     * @return 总是返回 true
     */
    @Override
    public boolean acquireLock(String lockId, TimeUnit heldTimeUnit, long heldTimes) {
        return true;
    }

    /**
     * 模拟释放锁，总是返回true.
     *
     * @param lockId 锁的唯一标识.
     * @return 总是返回 true
     */
    @Override
    public boolean releaseLock(String lockId) {
        return true;
    }
} 
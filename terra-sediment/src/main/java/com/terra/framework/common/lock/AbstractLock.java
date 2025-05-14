package com.terra.framework.common.lock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author Zeus
 * @date 2025/1/7 14:15
 * @description AbstractLock
 */
public abstract class AbstractLock<T extends Lock> implements ILock {

    @Override
    public boolean acquireLock(String lockId, TimeUnit heldTimeUnit, long heldTimes) {
        return acquireLock(getLock(lockId), Duration.ofMillis(heldTimeUnit.convert(heldTimes, TimeUnit.MILLISECONDS)));
    }

    @Override
    public boolean releaseLock(String lockId) {
        return releaseLock(getLock(lockId));
    }

    protected abstract boolean acquireLock(T lockEntity, Duration heldDuration);

    protected abstract boolean releaseLock(T lockEntity);

    protected abstract T getLock(String lockId);

}

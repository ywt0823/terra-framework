package com.terra.framework.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author Zeus
 * @date 2025/1/7 14:13
 * @description ILock
 */
public interface ILock {

    boolean acquireLock(String lockId, TimeUnit heldTimeUnit, long heldTimes);

    boolean releaseLock(String lockId);
}

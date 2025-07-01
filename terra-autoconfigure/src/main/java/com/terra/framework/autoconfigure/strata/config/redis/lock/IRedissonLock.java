package com.terra.framework.autoconfigure.strata.config.redis.lock;

import com.terra.framework.common.lock.AbstractLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;


@Slf4j
@AllArgsConstructor
public class IRedissonLock extends AbstractLock<RLock> {

    private RedissonClient redissonClient;

    @Override
    protected boolean acquireLock(RLock redissonLock, Duration duration) {
        try {
            return redissonLock.tryLock(duration.get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("acquireLock false ", e);
            return false;
        }
    }

    @Override
    protected boolean releaseLock(RLock redissonLock) {
        if (redissonLock.isHeldByCurrentThread() && redissonLock.isLocked()) {
            redissonLock.unlock();
        }
        return false;
    }

    @Override
    protected RLock getLock(String lockId) {
        return redissonClient.getLock(lockId);
    }
}

package com.terra.framework.strata.config;

import com.terra.framework.common.lock.AbstractLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Zeus
 * @date 2025/1/7 14:44
 * @description RedissionLockAutoconfiguration
 */
@ConditionalOnClass(RedissonClient.class)
@AutoConfigureAfter(TerraRedisAutoConfiguration.class)
@Slf4j
public class RedissonLockAutoConfiguration {

    @Bean
    public IRedissonLock redissonLock(RedissonClient redissonClient) {
        return new IRedissonLock(redissonClient);
    }


    @AllArgsConstructor
    public static class IRedissonLock extends AbstractLock<RLock> {

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
}

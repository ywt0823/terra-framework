package com.terra.framework.strata.config;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

@AutoConfigureAfter(TerraRedisAutoConfiguration.class)
public class RedisQueueConfig {

    @Bean
    public RBlockingQueue<String> blockingQueue(RedissonClient redissonClient) {
        //队列名称可以自己定义
        return redissonClient.getBlockingQueue("terra-delayedQueue");
    }

    @Bean
    public RDelayedQueue<String> delayedQueue(RBlockingQueue<String> blockingQueue,
                                              RedissonClient redissonClient) {
        return redissonClient.getDelayedQueue(blockingQueue);
    }
}

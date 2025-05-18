package com.terra.framework.nova.llm.retry;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

import com.terra.framework.nova.llm.model.RetryConfig;

/**
 * 重试执行器接口
 *
 * @author terra-nova
 */
public interface RetryExecutor {

    /**
     * 执行可重试的任务
     *
     * @param <T> 返回值类型
     * @param task 需要执行的任务
     * @param retryConfig 重试配置
     * @param retryCondition 重试条件判断
     * @return 任务执行结果
     * @throws Exception 执行异常
     */
    <T> T execute(Callable<T> task, RetryConfig retryConfig, Predicate<Throwable> retryCondition) throws Exception;

    /**
     * 执行可重试的任务，使用默认的重试条件
     *
     * @param <T> 返回值类型
     * @param task 需要执行的任务
     * @param retryConfig 重试配置
     * @return 任务执行结果
     * @throws Exception 执行异常
     */
    <T> T execute(Callable<T> task, RetryConfig retryConfig) throws Exception;
}

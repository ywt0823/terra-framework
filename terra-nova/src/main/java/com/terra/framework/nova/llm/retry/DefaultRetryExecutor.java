package com.terra.framework.nova.llm.retry;

import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * 默认重试执行器实现
 *
 * @author terra-nova
 */
public class DefaultRetryExecutor implements RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultRetryExecutor.class);

    @Override
    public <T> T execute(Callable<T> task, RetryConfig retryConfig, Predicate<Throwable> retryCondition) throws Exception {
        int attempts = 0;
        long delay = retryConfig.getInitialDelayMs();
        Exception lastException = null;

        while (attempts <= retryConfig.getMaxRetries()) {
            try {
                if (attempts > 0) {
                    log.info("重试第 {} 次执行任务", attempts);
                }
                return task.call();
            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (attempts > retryConfig.getMaxRetries() || !shouldRetry(e, retryCondition)) {
                    log.warn("任务执行失败，不再重试: {}", e.getMessage());
                    throw e;
                }

                log.info("任务执行失败，将在 {} 毫秒后重试: {}", delay, e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ModelException("重试过程被中断", ie, ErrorType.INTERRUPTED_ERROR);
                }

                // 计算下一次重试的延迟时间，使用指数退避策略
                delay = calculateNextDelay(delay, retryConfig);
            }
        }

        // 这里通常不会到达，因为最后一次重试失败时会在循环中抛出异常
        throw lastException;
    }

    @Override
    public <T> T execute(Callable<T> task, RetryConfig retryConfig) throws Exception {
        return execute(task, retryConfig, this::isRetryableException);
    }

    /**
     * 计算下一次重试的延迟时间
     *
     * @param currentDelay 当前延迟时间
     * @param retryConfig  重试配置
     * @return 下一次延迟时间
     */
    private long calculateNextDelay(long currentDelay, RetryConfig retryConfig) {
        // 指数退避策略，每次延迟时间乘以退避乘数
        long nextDelay = (long) (currentDelay * retryConfig.getBackoffMultiplier());

        // 确保不超过最大延迟时间
        return Math.min(nextDelay, retryConfig.getMaxDelayMs());
    }

    /**
     * 判断异常是否可重试
     *
     * @param exception      异常
     * @param retryCondition 重试条件
     * @return 是否可重试
     */
    private boolean shouldRetry(Exception exception, Predicate<Throwable> retryCondition) {
        if (retryCondition != null) {
            return retryCondition.test(exception);
        }
        return false;
    }

    /**
     * 默认的重试条件判断
     *
     * @param throwable 异常
     * @return 是否可重试
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof ModelException) {
            ModelException modelException = (ModelException) throwable;
            ErrorType errorType = modelException.getErrorType();

            // 这些错误类型通常是可以重试的
            return errorType == ErrorType.RATE_LIMIT_ERROR
                || errorType == ErrorType.SERVER_ERROR
                || errorType == ErrorType.TIMEOUT_ERROR
                || errorType == ErrorType.SERVICE_UNAVAILABLE_ERROR;
        }

        // 对于网络超时和连接异常，通常也可以重试
        return throwable instanceof java.net.SocketTimeoutException
            || throwable instanceof java.net.ConnectException
            || throwable instanceof java.io.IOException;
    }
}

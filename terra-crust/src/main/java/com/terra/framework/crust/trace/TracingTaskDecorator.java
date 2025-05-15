package com.terra.framework.crust.trace;

import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 线程池任务装饰器，用于保持异步任务的链路追踪上下文
 */
public class TracingTaskDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        // 捕获当前线程的追踪上下文
        Map<String, String> traceContext = MDCTraceManager.captureTraceContext();
        
        return () -> {
            try {
                // 在新线程中恢复追踪上下文
                MDCTraceManager.restoreTraceContext(traceContext);
                
                // 执行原始任务
                runnable.run();
            } finally {
                // 清理追踪上下文
                MDCTraceManager.clearTraceInfo();
            }
        };
    }
} 
package com.terra.framework.stream.annotation;

import java.lang.annotation.*;

/**
 * 消息发布注解
 * 用于标记方法返回值将被发布到指定队列
 * 
 * @author terra
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StreamPublish {
    /**
     * 目标队列名称
     */
    String destination();
    
    /**
     * 队列类型，留空则使用默认队列类型
     */
    String type() default ""; // 默认使用配置中的defaultQueueType
} 
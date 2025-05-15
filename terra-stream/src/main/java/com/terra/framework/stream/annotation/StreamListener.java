package com.terra.framework.stream.annotation;

import java.lang.annotation.*;

/**
 * 消息监听注解
 * 用于标记方法为消息监听器
 * 
 * @author terra
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StreamListener {
    /**
     * 目标队列名称
     */
    String destination();
    
    /**
     * 消费组ID
     */
    String group() default "";
    
    /**
     * 队列类型，留空则使用默认队列类型
     */
    String type() default ""; // 默认使用配置中的defaultQueueType
} 
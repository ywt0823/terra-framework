package com.terra.framework.stream.config;

import com.terra.framework.stream.annotation.StreamListenerAnnotationBeanPostProcessor;
import com.terra.framework.stream.annotation.StreamPublishAnnotationBeanPostProcessor;
import com.terra.framework.stream.core.MessageQueue;
import com.terra.framework.stream.factory.DefaultMessageQueueFactory;
import com.terra.framework.stream.factory.MessageQueueFactory;
import com.terra.framework.stream.rabbitmq.RabbitMqMessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息流自动配置类
 * 
 * @author terra
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StreamProperties.class)
@ConditionalOnProperty(prefix = "terra.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StreamAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageQueueFactory messageQueueFactory(StreamProperties properties) {
        Map<String, MessageQueue> queueMap = new HashMap<>();
        
        // 将在子配置类中添加具体实现
        return new DefaultMessageQueueFactory(queueMap, properties.getDefaultQueueType());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public StreamListenerAnnotationBeanPostProcessor streamListenerAnnotationBeanPostProcessor(
            MessageQueueFactory messageQueueFactory) {
        return new StreamListenerAnnotationBeanPostProcessor(messageQueueFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamPublishAnnotationBeanPostProcessor streamPublishAnnotationBeanPostProcessor(
            MessageQueueFactory messageQueueFactory) {
        return new StreamPublishAnnotationBeanPostProcessor(messageQueueFactory);
    }
    
    /**
     * RabbitMQ配置
     */
    @Configuration
    @ConditionalOnClass({RabbitTemplate.class, AmqpAdmin.class})
    @ConditionalOnProperty(prefix = "terra.stream.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class RabbitMqConfiguration {
        
        @Bean("rabbitMqMessageQueue")
        public MessageQueue rabbitMqMessageQueue(RabbitTemplate rabbitTemplate, 
                                                AmqpAdmin amqpAdmin,
                                                StreamProperties properties) {
            log.info("初始化RabbitMQ消息队列");
            return new RabbitMqMessageQueue(rabbitTemplate, amqpAdmin, properties.getRabbitMq());
        }
        
        @Bean
        public void configureRabbitMqMessageQueue(MessageQueueFactory factory, 
                                                 MessageQueue rabbitMqMessageQueue) {
            if (factory instanceof DefaultMessageQueueFactory) {
                ((DefaultMessageQueueFactory) factory).registerMessageQueue("rabbitmq", rabbitMqMessageQueue);
                log.info("已向工厂注册RabbitMQ消息队列");
            }
        }
    }
} 
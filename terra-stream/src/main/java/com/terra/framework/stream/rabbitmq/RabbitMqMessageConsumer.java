package com.terra.framework.stream.rabbitmq;

import com.terra.framework.stream.config.StreamProperties;
import com.terra.framework.stream.core.DefaultStreamMessage;
import com.terra.framework.stream.core.MessageConsumer;
import com.terra.framework.stream.core.MessageListener;
import com.terra.framework.stream.core.StreamMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RabbitMQ消息消费者实现
 * 
 * @author terra
 */
@Slf4j
public class RabbitMqMessageConsumer implements MessageConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;
    private final StreamProperties.RabbitMq properties;
    private final String groupId;
    private final Map<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();

    public RabbitMqMessageConsumer(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin, 
                                 StreamProperties.RabbitMq properties, String groupId) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
        this.properties = properties;
        this.groupId = groupId;
    }

    @Override
    public <T> void subscribe(String destination, MessageListener<T> listener) {
        // 确保队列存在
        Queue queue = new Queue(destination, 
                properties.isDurable(), 
                properties.isExclusive(), 
                properties.isAutoDelete());
        amqpAdmin.declareQueue(queue);
        
        // 创建消息监听容器
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(destination);
        container.setConcurrentConsumers(properties.getConcurrentConsumers());
        container.setMaxConcurrentConsumers(properties.getMaxConcurrentConsumers());
        container.setPrefetchCount(properties.getPrefetchCount());
        
        container.setMessageListener((org.springframework.amqp.core.MessageListener) message -> {
            try {
                processMessage(message, listener, messageConverter);
            } catch (Exception e) {
                log.error("处理来自队列 {} 的消息时出错: {}", destination, e.getMessage(), e);
                // 可以在这里添加重试策略或死信处理
                throw new RuntimeException("消息处理失败", e);
            }
        });
        
        container.start();
        containers.put(destination, container);
        log.info("已订阅RabbitMQ队列: {}, 消费组: {}", destination, groupId);
    }

    @SuppressWarnings("unchecked")
    private <T> void processMessage(Message message, MessageListener<T> listener, MessageConverter messageConverter) {
        T payload;
        try {
            payload = (T) messageConverter.fromMessage(message);
        } catch (Exception e) {
            log.error("消息转换失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息转换失败", e);
        }
        
        Map<String, Object> headers = new HashMap<>(message.getMessageProperties().getHeaders());
        
        StreamMessage<T> streamMessage = DefaultStreamMessage.<T>builder()
                .id(message.getMessageProperties().getMessageId() != null ? 
                        message.getMessageProperties().getMessageId() : UUID.randomUUID().toString())
                .payload(payload)
                .headers(headers)
                .timestamp(message.getMessageProperties().getTimestamp() != null ? 
                        message.getMessageProperties().getTimestamp().getTime() : System.currentTimeMillis())
                .build();
        
        listener.onMessage(streamMessage);
    }

    @Override
    public void unsubscribe(String destination) {
        SimpleMessageListenerContainer container = containers.remove(destination);
        if (container != null) {
            container.stop();
            log.info("已取消订阅RabbitMQ队列: {}", destination);
        } else {
            log.warn("尝试取消订阅未订阅的队列: {}", destination);
        }
    }
} 
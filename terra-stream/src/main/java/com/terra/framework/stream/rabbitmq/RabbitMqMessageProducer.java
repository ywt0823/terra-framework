package com.terra.framework.stream.rabbitmq;

import com.terra.framework.stream.core.DefaultStreamMessage;
import com.terra.framework.stream.core.MessageProducer;
import com.terra.framework.stream.core.StreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

/**
 * RabbitMQ消息生产者实现
 * 
 * @author terra
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqMessageProducer implements MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public <T> void send(String destination, StreamMessage<T> message) {
        try {
            rabbitTemplate.convertAndSend(destination, message);
            log.debug("发送消息到队列 {}: {}", destination, message);
        } catch (Exception e) {
            log.error("发送消息到队列 {} 失败: {}", destination, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    @Override
    public <T> void send(String destination, T payload) {
        send(destination, DefaultStreamMessage.create(payload));
    }

    @Override
    public <T> void send(String destination, T payload, Map<String, Object> headers) {
        send(destination, DefaultStreamMessage.create(payload, headers));
    }
} 
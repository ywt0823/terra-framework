package com.terra.framework.stream.rabbitmq;

import com.terra.framework.stream.config.StreamProperties;
import com.terra.framework.stream.core.MessageConsumer;
import com.terra.framework.stream.core.MessageProducer;
import com.terra.framework.stream.core.MessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * RabbitMQ消息队列实现
 * 
 * @author terra
 */
@Slf4j
public class RabbitMqMessageQueue implements MessageQueue {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;
    private final StreamProperties.RabbitMq properties;

    public RabbitMqMessageQueue(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin, 
                                StreamProperties.RabbitMq properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
        this.properties = properties;
    }

    @Override
    public MessageProducer createProducer() {
        return new RabbitMqMessageProducer(rabbitTemplate);
    }

    @Override
    public MessageConsumer createConsumer(String groupId) {
        return new RabbitMqMessageConsumer(rabbitTemplate, amqpAdmin, properties, groupId);
    }

    @Override
    public void createQueue(String name) {
        Queue queue = new Queue(name, 
                properties.isDurable(), 
                properties.isExclusive(), 
                properties.isAutoDelete());
        amqpAdmin.declareQueue(queue);
        log.info("已创建RabbitMQ队列: {}", name);
    }

    @Override
    public void deleteQueue(String name) {
        amqpAdmin.deleteQueue(name);
        log.info("已删除RabbitMQ队列: {}", name);
    }

    @Override
    public List<String> listQueues() {
        // 这是一个简化实现，实际上需要通过RabbitMQ HTTP API或其他方式获取队列列表
        log.warn("listQueues方法对RabbitMQ尚未完全实现");
        return new ArrayList<>();
    }
} 
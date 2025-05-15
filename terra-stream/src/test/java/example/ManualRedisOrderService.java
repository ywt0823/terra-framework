package example;

import com.terra.framework.stream.core.MessageConsumer;
import com.terra.framework.stream.core.MessageProducer;
import com.terra.framework.stream.factory.MessageQueueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * 订单服务示例 - 使用API方式操作Redis Stream
 * 演示如何直接使用API操作Redis Stream
 * 
 * @author terra
 */
@Slf4j
@Service
public class ManualRedisOrderService implements ApplicationContextAware {

    private final MessageQueueFactory messageQueueFactory;
    private final MessageProducer messageProducer;
    
    public ManualRedisOrderService(MessageQueueFactory messageQueueFactory) {
        this.messageQueueFactory = messageQueueFactory;
        this.messageProducer = messageQueueFactory.getMessageQueue("redis").createProducer();
    }
    
    /**
     * 创建订单并发送到Redis Stream
     *
     * @param request 订单请求
     * @return 创建的订单
     */
    public OrderService.Order createOrder(OrderService.OrderRequest request) {
        log.info("Redis API方式 - 创建订单: {}", request);
        // 模拟订单创建
        OrderService.Order order = new OrderService.Order();
        order.setId(System.currentTimeMillis());
        order.setCustomerId(request.getCustomerId());
        order.setAmount(request.getAmount());
        order.setStatus("CREATED");

        // 手动发送消息到Redis Stream
        messageProducer.send("orders-redis-manual", order);
        log.info("Redis API方式 - 发送订单到Stream: {}", order);

        return order;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MessageConsumer consumer = messageQueueFactory.getMessageQueue("redis")
                .createConsumer("redis-order-processors-manual");

        consumer.subscribe("orders-redis-manual", message -> {
            OrderService.Order order = (OrderService.Order) message.getPayload();
            log.info("Redis API方式 - 处理订单: {}", order);
            // 模拟订单处理
            order.setStatus("PROCESSED");
            log.info("Redis API方式 - 订单处理完成: {}", order);
        });

        log.info("Redis API方式 - 已初始化订单消费者");
    }
} 
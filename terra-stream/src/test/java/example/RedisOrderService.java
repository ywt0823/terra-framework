package example;

import com.terra.framework.stream.annotation.StreamListener;
import com.terra.framework.stream.annotation.StreamPublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Redis Stream订单服务示例
 * 演示如何使用terra-stream的Redis Stream功能
 * 
 * @author terra
 */
@Slf4j
@Service
public class RedisOrderService {
    
    /**
     * 创建订单并发布到Redis Stream
     * 
     * @param request 订单请求
     * @return 创建的订单
     */
    @StreamPublish(destination = "orders-redis", type = "redis")
    public OrderService.Order createOrder(OrderService.OrderRequest request) {
        log.info("Redis Stream - 创建订单: {}", request);
        // 模拟订单创建
        OrderService.Order order = new OrderService.Order();
        order.setId(System.currentTimeMillis());
        order.setCustomerId(request.getCustomerId());
        order.setAmount(request.getAmount());
        order.setStatus("CREATED");
        
        return order; // 返回值将自动发布到Redis Stream
    }
    
    /**
     * 处理Redis Stream中的订单消息
     * 
     * @param order 订单对象
     */
    @StreamListener(destination = "orders-redis", group = "redis-order-processors", type = "redis")
    public void processOrder(OrderService.Order order) {
        log.info("Redis Stream - 处理订单: {}", order);
        // 模拟订单处理
        order.setStatus("PROCESSED");
        log.info("Redis Stream - 订单处理完成: {}", order);
    }
} 
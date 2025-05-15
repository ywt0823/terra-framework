package example;

import com.terra.framework.stream.annotation.StreamListener;
import com.terra.framework.stream.annotation.StreamPublish;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单服务示例
 * 演示如何使用terra-stream
 * 
 * @author terra
 */
@Slf4j
@Service
public class OrderService {
    
    /**
     * 创建订单并发布到消息队列
     * 
     * @param request 订单请求
     * @return 创建的订单
     */
    @StreamPublish(destination = "orders")
    public Order createOrder(OrderRequest request) {
        log.info("创建订单: {}", request);
        // 模拟订单创建
        Order order = new Order();
        order.setId(System.currentTimeMillis());
        order.setCustomerId(request.getCustomerId());
        order.setAmount(request.getAmount());
        order.setStatus("CREATED");
        
        return order; // 返回值将自动发布到orders队列
    }
    
    /**
     * 处理订单消息
     * 
     * @param order 订单对象
     */
    @StreamListener(destination = "orders", group = "order-processors")
    public void processOrder(Order order) {
        log.info("处理订单: {}", order);
        // 模拟订单处理
        order.setStatus("PROCESSED");
        log.info("订单处理完成: {}", order);
    }
    
    /**
     * 订单请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRequest {
        private Long customerId;
        private Double amount;
    }
    
    /**
     * 订单对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private Long id;
        private Long customerId;
        private Double amount;
        private String status;
    }
} 
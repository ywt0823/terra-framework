package com.terra.framework.nova.model.router;

import com.terra.framework.nova.model.client.ModelClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器实现
 *
 * @author terra-nova
 */
@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer {
    
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public ModelClient selectClient(List<ModelClient> eligibleClients, RoutingContext context) {
        if (eligibleClients == null || eligibleClients.isEmpty()) {
            log.warn("没有可用的客户端");
            return null;
        }
        
        int size = eligibleClients.size();
        int index = counter.getAndIncrement() % size;
        
        // 处理整数溢出
        if (index < 0) {
            counter.set(0);
            index = 0;
        }
        
        ModelClient selectedClient = eligibleClients.get(index);
        log.debug("轮询选择客户端: {}, 索引: {}/{}", 
                selectedClient.getClass().getSimpleName(), index, size);
        
        return selectedClient;
    }
} 
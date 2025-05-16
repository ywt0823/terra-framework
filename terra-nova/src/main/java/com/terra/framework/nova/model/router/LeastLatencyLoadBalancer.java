package com.terra.framework.nova.model.router;

import com.terra.framework.nova.model.client.ModelClient;
import com.terra.framework.nova.model.health.HealthStatus;
import com.terra.framework.nova.model.health.ModelHealthMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 最小延迟负载均衡器实现
 * 选择具有最低响应延迟的可用客户端
 *
 * @author terra-nova
 */
@Slf4j
public class LeastLatencyLoadBalancer implements LoadBalancer {
    
    private final ModelHealthMonitor healthMonitor;
    private final RoundRobinLoadBalancer fallbackLoadBalancer;

    public LeastLatencyLoadBalancer(ModelHealthMonitor healthMonitor) {
        this.healthMonitor = healthMonitor;
        this.fallbackLoadBalancer = new RoundRobinLoadBalancer();
    }
    
    @Override
    public ModelClient selectClient(List<ModelClient> eligibleClients, RoutingContext context) {
        if (eligibleClients == null || eligibleClients.isEmpty()) {
            log.warn("没有可用的客户端");
            return null;
        }
        
        // 首先过滤出健康状态良好的客户端
        List<String> eligibleClientIds = eligibleClients.stream()
                .map(client -> getClientId(client))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (eligibleClientIds.isEmpty()) {
            // 如果无法确定客户端ID，则回退到轮询策略
            log.warn("无法确定客户端ID，回退到轮询策略");
            return fallbackLoadBalancer.selectClient(eligibleClients, context);
        }
        
        // 获取所有健康状态并按延迟排序
        List<HealthStatus> healthStatuses = eligibleClientIds.stream()
                .map(clientId -> healthMonitor.getHealthStatus(clientId))
                .filter(HealthStatus::isAvailable)  // 只选择可用的客户端
                .sorted(Comparator.comparingDouble(HealthStatus::getLatency))  // 按延迟排序
                .collect(Collectors.toList());
        
        if (healthStatuses.isEmpty()) {
            // 如果没有健康的客户端，则回退到轮询策略
            log.warn("没有健康的客户端，回退到轮询策略");
            return fallbackLoadBalancer.selectClient(eligibleClients, context);
        }
        
        // 获取延迟最低的客户端ID
        String selectedClientId = healthStatuses.get(0).getClientId();
        
        // 查找对应的客户端实例
        for (ModelClient client : eligibleClients) {
            String clientId = getClientId(client);
            if (selectedClientId.equals(clientId)) {
                log.debug("选择延迟最低的客户端: {}, 延迟: {}ms", 
                        client.getClass().getSimpleName(), 
                        healthStatuses.get(0).getLatency());
                return client;
            }
        }
        
        // 如果找不到匹配的客户端（不应该发生），回退到轮询
        log.warn("无法找到匹配的客户端实例，回退到轮询策略");
        return fallbackLoadBalancer.selectClient(eligibleClients, context);
    }
    
    /**
     * 获取客户端ID
     */
    private String getClientId(ModelClient client) {
        // 尝试使用getName方法获取客户端ID
        try {
            java.lang.reflect.Method method = client.getClass().getMethod("getName");
            return (String) method.invoke(client);
        } catch (Exception e) {
            // 如果没有getName方法或调用失败，则使用类名作为ID
            return client.getClass().getSimpleName().toLowerCase();
        }
    }
} 
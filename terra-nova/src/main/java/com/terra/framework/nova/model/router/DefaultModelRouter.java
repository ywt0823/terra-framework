package com.terra.framework.nova.model.router;

import com.terra.framework.nova.model.client.ClientStatus;
import com.terra.framework.nova.model.client.ModelClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认模型路由器实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultModelRouter implements ModelRouter {
    
    private final Map<String, ModelClient> clients = new ConcurrentHashMap<>();
    private String defaultClientKey;
    private final RoutingStrategy strategy;
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private LoadBalancer loadBalancer;
    
    /**
     * 构造函数
     *
     * @param strategy 路由策略
     * @param loadBalancer 负载均衡器
     */
    public DefaultModelRouter(RoutingStrategy strategy, LoadBalancer loadBalancer) {
        this.strategy = strategy;
        this.loadBalancer = loadBalancer;
    }
    
    /**
     * 构造函数
     *
     * @param strategy 路由策略
     */
    public DefaultModelRouter(RoutingStrategy strategy) {
        this(strategy, null);
    }
    
    @Override
    public ModelClient route(RoutingContext context) {
        ModelClient selectedClient = null;
        
        // 根据不同策略选择模型
        switch (strategy) {
            case DEFAULT_ONLY:
                selectedClient = getDefaultClient();
                break;
                
            case USER_PREFERRED:
                selectedClient = routeByUserPreference(context);
                break;
                
            case COST_OPTIMIZED:
                selectedClient = routeByCost(context);
                break;
                
            case PERFORMANCE_OPTIMIZED:
                selectedClient = routeByPerformance(context);
                break;
                
            case COST_PERFORMANCE_BALANCED:
                selectedClient = routeByBalanced(context);
                break;
                
            case AVAILABILITY_OPTIMIZED:
                selectedClient = routeByAvailability(context);
                break;
                
            case ROUND_ROBIN:
                selectedClient = routeByRoundRobin(context);
                break;
                
            default:
                selectedClient = getDefaultClient();
                break;
        }
        
        // 如果有多个符合条件的客户端并且负载均衡器可用，则使用负载均衡器选择一个
        if (selectedClient == null && loadBalancer != null) {
            List<ModelClient> eligibleClients = new ArrayList<>(clients.values());
            selectedClient = loadBalancer.selectClient(eligibleClients, context);
        }
        
        // 如果仍然没有选到客户端，则使用默认客户端
        if (selectedClient == null) {
            selectedClient = getDefaultClient();
        }
        
        return selectedClient;
    }
    
    private ModelClient routeByUserPreference(RoutingContext context) {
        // 尝试根据用户偏好选择模型
        if (StringUtils.isNotBlank(context.getPreferredProvider())) {
            // 查找匹配提供商的客户端
            for (Map.Entry<String, ModelClient> entry : clients.entrySet()) {
                if (entry.getKey().toLowerCase().contains(context.getPreferredProvider().toLowerCase())) {
                    ModelClient client = entry.getValue();
                    if (client.getStatus() == ClientStatus.READY) {
                        log.debug("使用用户偏好的提供商: {}", entry.getKey());
                        return client;
                    }
                }
            }
        }
        
        if (StringUtils.isNotBlank(context.getPreferredModel())) {
            // 查找匹配模型的客户端
            for (Map.Entry<String, ModelClient> entry : clients.entrySet()) {
                ModelClient client = entry.getValue();
                for (String model : client.getSupportedModels()) {
                    if (model.equalsIgnoreCase(context.getPreferredModel()) && 
                            client.getStatus() == ClientStatus.READY) {
                        log.debug("使用用户偏好的模型: {}", model);
                        return client;
                    }
                }
            }
        }
        
        // 如果允许回退且找不到匹配项，使用默认客户端
        if (context.isFallbackEnabled()) {
            log.debug("回退到默认客户端");
            return getDefaultClient();
        }
        
        log.warn("无法找到匹配的客户端且不允许回退");
        return null;
    }
    
    private ModelClient routeByCost(RoutingContext context) {
        // 为简化实现，这里假设Ollama是成本最低的选项
        for (Map.Entry<String, ModelClient> entry : clients.entrySet()) {
            if (entry.getKey().toLowerCase().contains("ollama") && 
                    entry.getValue().getStatus() == ClientStatus.READY) {
                return entry.getValue();
            }
        }
        return getDefaultClient();
    }
    
    private ModelClient routeByPerformance(RoutingContext context) {
        // 为简化实现，这里假设OpenAI是性能最好的选项
        for (Map.Entry<String, ModelClient> entry : clients.entrySet()) {
            if (entry.getKey().toLowerCase().contains("openai") && 
                    entry.getValue().getStatus() == ClientStatus.READY) {
                return entry.getValue();
            }
        }
        return getDefaultClient();
    }
    
    private ModelClient routeByBalanced(RoutingContext context) {
        // 在实际实现中，应该根据成本和性能的权重进行选择
        // 这里简单实现为随机选择一个可用的客户端
        if (Math.random() > 0.5) {
            return routeByCost(context);
        } else {
            return routeByPerformance(context);
        }
    }
    
    private ModelClient routeByAvailability(RoutingContext context) {
        // 选择状态为READY的客户端
        for (ModelClient client : clients.values()) {
            if (client.getStatus() == ClientStatus.READY) {
                return client;
            }
        }
        return getDefaultClient();
    }
    
    private ModelClient routeByRoundRobin(RoutingContext context) {
        // 轮询所有可用客户端
        int count = clients.size();
        if (count == 0) {
            return null;
        }
        
        int index = roundRobinCounter.getAndIncrement() % count;
        if (index < 0) {
            // 处理整数溢出情况
            roundRobinCounter.set(0);
            index = 0;
        }
        
        // 获取第index个客户端
        ModelClient[] clientArray = clients.values().toArray(new ModelClient[0]);
        return clientArray[index];
    }
    
    @Override
    public void addClient(ModelClient client) {
        // 生成客户端键
        String clientKey = generateClientKey(client);
        clients.put(clientKey, client);
        
        // 如果是第一个客户端，则设为默认
        if (defaultClientKey == null) {
            defaultClientKey = clientKey;
        }
        
        log.info("添加模型客户端: {}", clientKey);
    }
    
    @Override
    public void removeClient(String clientKey) {
        ModelClient client = clients.remove(clientKey);
        if (client != null) {
            client.close();
            log.info("移除模型客户端: {}", clientKey);
        }
        
        // 如果移除的是默认客户端，则重新选择一个默认客户端
        if (clientKey.equals(defaultClientKey) && !clients.isEmpty()) {
            defaultClientKey = clients.keySet().iterator().next();
            log.info("更新默认客户端: {}", defaultClientKey);
        }
    }
    
    @Override
    public Map<String, ModelClient> getAllClients() {
        return clients;
    }
    
    @Override
    public ModelClient getDefaultClient() {
        if (defaultClientKey == null || !clients.containsKey(defaultClientKey)) {
            if (!clients.isEmpty()) {
                defaultClientKey = clients.keySet().iterator().next();
            } else {
                return null;
            }
        }
        return clients.get(defaultClientKey);
    }
    
    @Override
    public void setDefaultClient(String clientKey) {
        if (clients.containsKey(clientKey)) {
            defaultClientKey = clientKey;
            log.info("设置默认客户端: {}", clientKey);
        } else {
            log.warn("尝试设置不存在的客户端为默认: {}", clientKey);
        }
    }
    
    /**
     * 生成客户端键
     *
     * @param client 模型客户端
     * @return 客户端键
     */
    private String generateClientKey(ModelClient client) {
        return client.getClass().getSimpleName() + "-" + 
                clients.size() + "-" + 
                System.currentTimeMillis() % 1000;
    }
} 
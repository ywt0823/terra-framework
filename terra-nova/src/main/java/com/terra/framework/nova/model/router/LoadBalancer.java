package com.terra.framework.nova.model.router;

import com.terra.framework.nova.model.client.ModelClient;

import java.util.List;

/**
 * 负载均衡器接口
 *
 * @author terra-nova
 */
public interface LoadBalancer {
    
    /**
     * 从可用的客户端中选择一个
     *
     * @param eligibleClients 可选的客户端列表
     * @param context 路由上下文
     * @return 选择的模型客户端
     */
    ModelClient selectClient(List<ModelClient> eligibleClients, RoutingContext context);
} 
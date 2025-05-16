package com.terra.framework.nova.model.router;

import com.terra.framework.nova.model.client.ModelClient;

import java.util.Map;

/**
 * 模型路由器接口
 *
 * @author terra-nova
 */
public interface ModelRouter {
    
    /**
     * 基于路由上下文选择合适的模型客户端
     *
     * @param context 路由上下文
     * @return 选择的模型客户端
     */
    ModelClient route(RoutingContext context);
    
    /**
     * 添加模型客户端
     *
     * @param client 模型客户端
     */
    void addClient(ModelClient client);
    
    /**
     * 移除模型客户端
     *
     * @param clientKey 客户端标识
     */
    void removeClient(String clientKey);
    
    /**
     * 获取所有可用的模型客户端
     *
     * @return 模型客户端映射
     */
    Map<String, ModelClient> getAllClients();
    
    /**
     * 获取默认模型客户端
     *
     * @return 默认模型客户端
     */
    ModelClient getDefaultClient();
    
    /**
     * 设置默认模型客户端
     *
     * @param clientKey 客户端标识
     */
    void setDefaultClient(String clientKey);
} 
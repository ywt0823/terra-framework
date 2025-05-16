package com.terra.framework.nova.model.health;

import java.util.List;

/**
 * 模型健康监控器接口
 *
 * @author terra-nova
 */
public interface ModelHealthMonitor {
    
    /**
     * 开始监控指定客户端
     *
     * @param clientId 客户端ID
     * @param intervalSeconds 监控间隔（秒）
     */
    void startMonitoring(String clientId, int intervalSeconds);
    
    /**
     * 停止监控指定客户端
     *
     * @param clientId 客户端ID
     */
    void stopMonitoring(String clientId);
    
    /**
     * 获取客户端当前健康状态
     *
     * @param clientId 客户端ID
     * @return 健康状态
     */
    HealthStatus getHealthStatus(String clientId);
    
    /**
     * 获取客户端历史健康报告
     *
     * @param clientId 客户端ID
     * @param limit 返回数量限制
     * @return 健康报告列表
     */
    List<HealthReport> getHistoricalHealth(String clientId, int limit);
    
    /**
     * 执行一次健康检查
     *
     * @param clientId 客户端ID
     * @return 健康报告
     */
    HealthReport checkHealth(String clientId);
    
    /**
     * 判断客户端是否可用
     *
     * @param clientId 客户端ID
     * @return 是否可用
     */
    boolean isAvailable(String clientId);
} 
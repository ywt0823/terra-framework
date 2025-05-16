package com.terra.framework.nova.model.health;

import com.terra.framework.nova.model.client.ModelClient;
import com.terra.framework.nova.model.router.ModelRouter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 模型健康监控器默认实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultModelHealthMonitor implements ModelHealthMonitor {

    private final ModelRouter modelRouter;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> monitoringTasks;
    private final Map<String, HealthStatus> healthStatusMap;
    private final Map<String, List<HealthReport>> healthHistoryMap;
    private final int maxHistoryPerClient;

    public DefaultModelHealthMonitor(ModelRouter modelRouter) {
        this(modelRouter, 100);
    }

    public DefaultModelHealthMonitor(ModelRouter modelRouter, int maxHistoryPerClient) {
        this.modelRouter = modelRouter;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.monitoringTasks = new ConcurrentHashMap<>();
        this.healthStatusMap = new ConcurrentHashMap<>();
        this.healthHistoryMap = new ConcurrentHashMap<>();
        this.maxHistoryPerClient = maxHistoryPerClient;
    }

    @Override
    public void startMonitoring(String clientId, int intervalSeconds) {
        if (monitoringTasks.containsKey(clientId)) {
            log.info("已经在监控客户端: {}", clientId);
            return;
        }

        log.info("开始监控客户端: {}, 间隔: {}秒", clientId, intervalSeconds);
        
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        checkHealth(clientId);
                    } catch (Exception e) {
                        log.error("监控客户端时发生错误: {}", clientId, e);
                    }
                },
                0, 
                intervalSeconds, 
                TimeUnit.SECONDS
        );
        monitoringTasks.put(clientId, task);
    }

    @Override
    public void stopMonitoring(String clientId) {
        ScheduledFuture<?> task = monitoringTasks.remove(clientId);
        if (task != null) {
            task.cancel(false);
            log.info("已停止监控客户端: {}", clientId);
        }
    }

    @Override
    public HealthStatus getHealthStatus(String clientId) {
        return healthStatusMap.getOrDefault(clientId, HealthStatus.builder()
                .clientId(clientId)
                .available(false)
                .message("未监控此客户端")
                .lastCheckedTime(ZonedDateTime.now())
                .build());
    }

    @Override
    public List<HealthReport> getHistoricalHealth(String clientId, int limit) {
        List<HealthReport> reports = healthHistoryMap.getOrDefault(clientId, new ArrayList<>());
        if (reports.isEmpty()) {
            return reports;
        }
        
        int actualLimit = Math.min(limit, reports.size());
        return reports.subList(reports.size() - actualLimit, reports.size());
    }

    @Override
    public HealthReport checkHealth(String clientId) {
        long startTime = System.currentTimeMillis();
        ModelClient client = getClient(clientId);
        
        if (client == null) {
            HealthReport report = buildErrorReport(clientId, "客户端不存在");
            updateStatus(clientId, report);
            return report;
        }

        try {
            // 使用简单请求测试客户端状态
            String response = client.generate("这是一个健康检查测试", Map.of("max_tokens", 10));
            
            long latency = System.currentTimeMillis() - startTime;
            
            HealthReport report = HealthReport.builder()
                    .reportId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .checkTime(ZonedDateTime.now())
                    .available(true)
                    .latency(latency)
                    .hasError(false)
                    .statusCode(200)
                    .build();
            
            updateStatus(clientId, report);
            return report;
            
        } catch (Exception e) {
            log.error("健康检查失败: {}", clientId, e);
            HealthReport report = buildErrorReport(clientId, e.getMessage());
            updateStatus(clientId, report);
            return report;
        }
    }

    @Override
    public boolean isAvailable(String clientId) {
        HealthStatus status = getHealthStatus(clientId);
        return status.isAvailable();
    }
    
    private HealthReport buildErrorReport(String clientId, String errorMessage) {
        return HealthReport.builder()
                .reportId(UUID.randomUUID().toString())
                .clientId(clientId)
                .checkTime(ZonedDateTime.now())
                .available(false)
                .latency(0)
                .hasError(true)
                .errorMessage(errorMessage)
                .statusCode(500)
                .build();
    }
    
    private void updateStatus(String clientId, HealthReport report) {
        // 更新健康历史
        healthHistoryMap.computeIfAbsent(clientId, k -> new ArrayList<>()).add(report);
        
        // 保持历史记录大小限制
        List<HealthReport> reports = healthHistoryMap.get(clientId);
        if (reports.size() > maxHistoryPerClient) {
            reports.remove(0);
        }
        
        // 更新当前状态
        HealthStatus currentStatus = healthStatusMap.getOrDefault(clientId, HealthStatus.builder()
                .clientId(clientId)
                .successCount(0)
                .errorCount(0)
                .build());
        
        // 计算平均延迟
        double currentLatency = currentStatus.getLatency();
        int totalCount = currentStatus.getSuccessCount() + currentStatus.getErrorCount();
        double newLatency;
        
        if (totalCount > 0) {
            newLatency = (currentLatency * totalCount + report.getLatency()) / (totalCount + 1);
        } else {
            newLatency = report.getLatency();
        }
        
        HealthStatus newStatus = HealthStatus.builder()
                .clientId(clientId)
                .available(report.isAvailable())
                .latency(newLatency)
                .successCount(report.isAvailable() ? currentStatus.getSuccessCount() + 1 : currentStatus.getSuccessCount())
                .errorCount(report.isAvailable() ? currentStatus.getErrorCount() : currentStatus.getErrorCount() + 1)
                .lastCheckedTime(report.getCheckTime())
                .message(report.isAvailable() ? "可用" : report.getErrorMessage())
                .build();
        
        healthStatusMap.put(clientId, newStatus);
        
        if (report.isAvailable()) {
            log.debug("客户端健康检查通过: {}, 延迟: {}ms", clientId, report.getLatency());
        } else {
            log.warn("客户端健康检查失败: {}, 错误: {}", clientId, report.getErrorMessage());
        }
    }
    
    private ModelClient getClient(String clientId) {
        Map<String, ModelClient> clients = modelRouter.getAllClients();
        return clients.get(clientId);
    }
} 
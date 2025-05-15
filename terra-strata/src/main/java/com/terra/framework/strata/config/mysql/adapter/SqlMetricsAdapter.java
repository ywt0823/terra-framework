package com.terra.framework.strata.config.mysql.adapter;

import com.terra.framework.geyser.options.CacheOperation;
import com.terra.framework.strata.config.mysql.manager.AutoCacheManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * SQL指标收集器
 * 负责收集SQL执行情况，探测热点SQL
 */
@Slf4j
public class SqlMetricsAdapter {

    // 热点SQL检测阈值
    @Value("${terra.cache.sql.hot-threshold:100}")
    private int hotSqlThreshold;

    @Value("${terra.cache.sql.hot-time-threshold:200}")
    private long hotTimeThreshold;

    @Value("${terra.cache.table.hot-threshold:500}")
    private int hotTableThreshold;

    // SQL执行统计信息
    private final Map<String, SqlMetric> sqlMetrics = new ConcurrentHashMap<>();
    
    // 表访问统计信息
    private final Map<String, TableMetric> tableMetrics = new ConcurrentHashMap<>();
    
    // 热点SQL和表缓存注册表
    private final Map<String, HotSqlInfo> hotSqlRegistry = new ConcurrentHashMap<>();
    private final Map<String, HotTableInfo> hotTableRegistry = new ConcurrentHashMap<>();
    
    // 自动缓存管理器
    private final AutoCacheManager autoCacheManager;

    public SqlMetricsAdapter(AutoCacheManager autoCacheManager) {
        this.autoCacheManager = autoCacheManager;
    }

    /**
     * 记录SQL执行情况
     * 
     * @param sqlId SQL标识符
     * @param sql SQL语句
     * @param executionTime 执行时间(ms)
     */
    public void recordSqlExecution(String sqlId, String sql, long executionTime) {
        SqlMetric metric = sqlMetrics.computeIfAbsent(sqlId, id -> new SqlMetric(sql));
        metric.incrementCount();
        metric.addExecutionTime(executionTime);
        
        // 如果该SQL还未被标记为热点，检查是否达到热点阈值
        if (!hotSqlRegistry.containsKey(sqlId)) {
            if (metric.getCount() > hotSqlThreshold && metric.getAvgExecutionTime() > hotTimeThreshold) {
                registerHotSql(sqlId, sql, metric);
            }
        }
    }

    /**
     * 记录表访问情况
     * 
     * @param tableName 表名
     * @param executionTime 执行时间(ms)
     */
    public void recordTableAccess(String tableName, long executionTime) {
        TableMetric metric = tableMetrics.computeIfAbsent(tableName, name -> new TableMetric());
        metric.incrementCount();
        metric.addExecutionTime(executionTime);
        
        // 检查是否达到热点表阈值
        if (!hotTableRegistry.containsKey(tableName)) {
            if (metric.getCount() > hotTableThreshold) {
                registerHotTable(tableName, metric);
            }
        }
    }

    /**
     * 记录SQL执行错误
     */
    public void recordErrorSql(String sqlId, String sql) {
        SqlMetric metric = sqlMetrics.computeIfAbsent(sqlId, id -> new SqlMetric(sql));
        metric.incrementErrorCount();
    }

    /**
     * 注册热点SQL，创建相应的缓存
     */
    private void registerHotSql(String sqlId, String sql, SqlMetric metric) {
        log.info("检测到热点SQL - ID: {}, 执行次数: {}, 平均耗时: {}ms, SQL: {}", 
                sqlId, metric.getCount(), metric.getAvgExecutionTime(), sql);
        
        // 创建该SQL的专用缓存
        String cacheName = "hot_sql_" + sqlId.replace(".", "_");
        CacheOperation<String, Object> localCache = autoCacheManager.createLocalCache(cacheName, 1000, 30, TimeUnit.MINUTES);
        CacheOperation<String, Object> redisCache = autoCacheManager.createRedisCache(cacheName, 0, 60, TimeUnit.MINUTES);
        
        // 注册到热点SQL注册表
        HotSqlInfo hotSqlInfo = new HotSqlInfo(sqlId, sql, localCache, redisCache);
        hotSqlRegistry.put(sqlId, hotSqlInfo);
    }

    /**
     * 注册热点表，创建相应的缓存
     */
    private void registerHotTable(String tableName, TableMetric metric) {
        log.info("检测到热点表: {}, 访问次数: {}, 平均耗时: {}ms", 
                tableName, metric.getCount(), metric.getAvgExecutionTime());
        
        // 为热点表创建专用缓存
        String cacheName = "hot_table_" + tableName;
        CacheOperation<String, Object> localCache = autoCacheManager.createLocalCache(cacheName, 2000, 10, TimeUnit.MINUTES);
        CacheOperation<String, Object> redisCache = autoCacheManager.createRedisCache(cacheName, 0, 20, TimeUnit.MINUTES);
        
        // 注册到热点表注册表
        HotTableInfo hotTableInfo = new HotTableInfo(tableName, localCache, redisCache);
        hotTableRegistry.put(tableName, hotTableInfo);
    }

    /**
     * 周期性清理指标数据
     */
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void cleanupMetrics() {
        // 清理不再热门的SQL
        Set<String> toRemoveSql = hotSqlRegistry.entrySet().stream()
                .filter(entry -> {
                    SqlMetric metric = sqlMetrics.get(entry.getKey());
                    return metric == null || metric.getCount() < hotSqlThreshold / 2;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        
        toRemoveSql.forEach(id -> {
            hotSqlRegistry.remove(id);
            log.info("移除不再热门的SQL: {}", id);
        });
        
        // 清理不再热门的表
        Set<String> toRemoveTable = hotTableRegistry.entrySet().stream()
                .filter(entry -> {
                    TableMetric metric = tableMetrics.get(entry.getKey());
                    return metric == null || metric.getCount() < hotTableThreshold / 2;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        
        toRemoveTable.forEach(name -> {
            hotTableRegistry.remove(name);
            log.info("移除不再热门的表: {}", name);
        });
        
        // 重置统计数据
        sqlMetrics.clear();
        tableMetrics.clear();
    }

    /**
     * 获取热点SQL对应的缓存
     */
    public HotSqlInfo getHotSqlInfo(String sqlId) {
        return hotSqlRegistry.get(sqlId);
    }

    /**
     * 获取热点表对应的缓存
     */
    public HotTableInfo getHotTableInfo(String tableName) {
        return hotTableRegistry.get(tableName);
    }

    /**
     * 判断SQL是否为热点SQL
     */
    public boolean isHotSql(String sqlId) {
        return hotSqlRegistry.containsKey(sqlId);
    }

    /**
     * 判断表是否为热点表
     */
    public boolean isHotTable(String tableName) {
        return hotTableRegistry.containsKey(tableName);
    }
    
    /**
     * 获取所有热点SQL的ID
     */
    public Set<String> getHotSqlIds() {
        return hotSqlRegistry.keySet();
    }
    
    /**
     * 获取所有热点表的名称
     */
    public Set<String> getHotTableNames() {
        return hotTableRegistry.keySet();
    }

    /**
     * SQL指标信息
     */
    @Data
    public static class SqlMetric {
        private final String sql;
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);

        public SqlMetric(String sql) {
            this.sql = sql;
        }

        public long incrementCount() {
            return count.incrementAndGet();
        }

        public long incrementErrorCount() {
            return errorCount.incrementAndGet();
        }

        public void addExecutionTime(long time) {
            totalExecutionTime.addAndGet(time);
        }

        public long getCount() {
            return count.get();
        }

        public long getAvgExecutionTime() {
            long c = count.get();
            return c > 0 ? totalExecutionTime.get() / c : 0;
        }
    }

    /**
     * 表访问指标
     */
    @Data
    public static class TableMetric {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);

        public long incrementCount() {
            return count.incrementAndGet();
        }

        public void addExecutionTime(long time) {
            totalExecutionTime.addAndGet(time);
        }

        public long getCount() {
            return count.get();
        }

        public long getAvgExecutionTime() {
            long c = count.get();
            return c > 0 ? totalExecutionTime.get() / c : 0;
        }
    }

    /**
     * 热点SQL信息
     */
    @Data
    public static class HotSqlInfo {
        private final String sqlId;
        private final String sql;
        private final CacheOperation<String, Object> localCache;
        private final CacheOperation<String, Object> redisCache;
    }

    /**
     * 热点表信息
     */
    @Data
    public static class HotTableInfo {
        private final String tableName;
        private final CacheOperation<String, Object> localCache;
        private final CacheOperation<String, Object> redisCache;
    }
} 
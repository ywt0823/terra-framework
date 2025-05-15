package com.terra.framework.strata.helper;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

/**
 * 缓存失效切面
 * 用于在数据变更时自动清除相关缓存
 */
@Aspect
@Component
@Slf4j
public class CacheInvalidationAspect {

    @Value("${terra.cache.invalidation.enabled:true}")
    private boolean invalidationEnabled;

    private final SqlMetricsCollector metricsCollector;
    private final AutoCacheManager cacheManager;
    
    // 存储表与缓存的映射关系，用于在表数据变更时快速查找需要清除的缓存
    private final Map<String, Set<String>> tableCacheMapping = new ConcurrentHashMap<>();
    
    // 插入/更新/删除方法名模式
    private static final Pattern WRITE_METHOD_PATTERN = Pattern.compile(
            "^(insert|update|delete|remove|add|edit|modify|save|create).*", Pattern.CASE_INSENSITIVE);

    public CacheInvalidationAspect(SqlMetricsCollector metricsCollector, AutoCacheManager cacheManager) {
        this.metricsCollector = metricsCollector;
        this.cacheManager = cacheManager;
    }

    /**
     * 注册表与缓存的映射关系
     */
    public void registerTableCacheMapping(String tableName, String cacheName) {
        tableCacheMapping.computeIfAbsent(tableName, k -> new CopyOnWriteArraySet<>())
                .add(cacheName);
    }
    
    /**
     * 拦截所有Mapper接口的写操作方法
     * 在数据变更时清除相关缓存
     */
    @Around("execution(* com.terra..*.mapper.*Mapper.*(..))")
    public Object aroundMapperMethod(ProceedingJoinPoint point) throws Throwable {
        if (!invalidationEnabled) {
            return point.proceed();
        }

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        
        // 判断是否为写操作
        boolean isWriteOperation = WRITE_METHOD_PATTERN.matcher(methodName).matches();
        if (!isWriteOperation) {
            return point.proceed();
        }

        // 执行原始方法
        Object result = point.proceed();

        // 从Mapper类名中推断表名
        String className = method.getDeclaringClass().getSimpleName();
        String tableName = null;
        if (className.endsWith("Mapper")) {
            tableName = className.substring(0, className.length() - "Mapper".length()).toLowerCase();
        }

        // 如果能够推断出表名，清除相关缓存
        if (tableName != null) {
            invalidateTableRelatedCaches(tableName);
        }

        return result;
    }

    /**
     * 清除表相关的所有缓存
     */
    private void invalidateTableRelatedCaches(String tableName) {
        // 1. 清除表关联的自定义缓存
        Set<String> cachesToInvalidate = tableCacheMapping.get(tableName);
        if (cachesToInvalidate != null) {
            for (String cacheName : cachesToInvalidate) {
                AutoCacheManager.MultiLevelCache<?, ?> cache = cacheManager.getMultiLevelCache(cacheName);
                if (cache != null) {
                    cache.invalidateAll();
                    log.debug("清除表[{}]关联的缓存[{}]", tableName, cacheName);
                }
            }
        }
        
        // 2. 清除热点表缓存
        if (metricsCollector.isHotTable(tableName)) {
            SqlMetricsCollector.HotTableInfo hotTableInfo = metricsCollector.getHotTableInfo(tableName);
            if (hotTableInfo != null) {
                hotTableInfo.getLocalCache().invalidateAll();
                hotTableInfo.getRedisCache().invalidateAll();
                log.debug("清除热点表[{}]的缓存", tableName);
            }
        }
        
        // 3. 创建新的多级缓存，替换旧缓存
        AutoCacheManager.MultiLevelCache<?, ?> tableCache = cacheManager.getMultiLevelCache(tableName);
        if (tableCache != null) {
            tableCache.invalidateAll();
            log.debug("清除表[{}]的多级缓存", tableName);
        }
    }
} 
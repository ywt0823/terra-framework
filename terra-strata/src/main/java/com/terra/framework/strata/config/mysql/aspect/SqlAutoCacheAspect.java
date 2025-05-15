package com.terra.framework.strata.config.mysql.aspect;

import com.terra.framework.strata.config.mysql.manager.AutoCacheManager;
import com.terra.framework.strata.config.mysql.manager.AutoCacheManager.MultiLevelCache;
import com.terra.framework.strata.config.mysql.adapter.SqlMetricsAdapter;
import com.terra.framework.strata.config.mysql.adapter.SqlMetricsAdapter.HotSqlInfo;
import com.terra.framework.strata.config.mysql.adapter.SqlMetricsAdapter.HotTableInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * SQL自动缓存切面
 * 自动拦截Mapper查询，应用缓存策略
 */
@Aspect
@Slf4j
public class SqlAutoCacheAspect {

    private final SqlMetricsAdapter metricsCollector;
    private final AutoCacheManager cacheManager;

    @Value("${terra.cache.auto-enable:true}")
    private boolean autoEnableCache;

    @Value("${terra.cache.key-prefix:autosql_}")
    private String cacheKeyPrefix;

    public SqlAutoCacheAspect(SqlMetricsAdapter metricsCollector, AutoCacheManager cacheManager) {
        this.metricsCollector = metricsCollector;
        this.cacheManager = cacheManager;
    }

    /**
     * 拦截所有Mapper接口的查询方法
     * 对于热点SQL或热点表的查询，使用缓存处理
     */
    @Around("execution(* com.terra..*.mapper.*Mapper.*(..))")
    public Object aroundMapperMethod(ProceedingJoinPoint point) throws Throwable {
        if (!autoEnableCache) {
            return point.proceed();
        }

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        // 只处理查询方法，非查询方法直接执行
        if (!isQueryMethod(methodName)) {
            return point.proceed();
        }

        // 获取方法全限定名
        String sqlId = method.getDeclaringClass().getName() + "." + methodName;

        // 1. 检查是否为热点SQL
        if (metricsCollector.isHotSql(sqlId)) {
            HotSqlInfo hotSqlInfo = metricsCollector.getHotSqlInfo(sqlId);
            if (hotSqlInfo != null) {
                log.debug("使用热点SQL缓存: {}", sqlId);
                return handleHotSqlCache(point, hotSqlInfo, generateCacheKey(sqlId, point.getArgs()));
            }
        }

        // 2. 尝试从Mapper类名中推断表名
        String className = method.getDeclaringClass().getSimpleName();
        String tableName = null;
        if (className.endsWith("Mapper")) {
            tableName = className.substring(0, className.length() - "Mapper".length()).toLowerCase();
        }

        // 3. 检查是否为热点表
        if (tableName != null && metricsCollector.isHotTable(tableName)) {
            HotTableInfo hotTableInfo = metricsCollector.getHotTableInfo(tableName);
            if (hotTableInfo != null) {
                log.debug("使用热点表缓存: {}.{}", tableName, methodName);
                return handleHotTableCache(point, hotTableInfo, tableName, methodName, point.getArgs());
            }
        }

        // 不是热点SQL或热点表，直接执行
        return point.proceed();
    }

    /**
     * 处理热点SQL的缓存
     */
    private Object handleHotSqlCache(ProceedingJoinPoint point, HotSqlInfo hotSqlInfo, String cacheKey) throws Throwable {
        // 先查本地缓存
        Object result = hotSqlInfo.getLocalCache().getIfPresent(cacheKey);
        if (result != null) {
            return result;
        }

        // 再查Redis缓存
        result = hotSqlInfo.getRedisCache().getIfPresent(cacheKey);
        if (result != null) {
            // 将Redis缓存结果放入本地缓存
            hotSqlInfo.getLocalCache().put(cacheKey, result);
            return result;
        }

        // 执行原始方法
        result = point.proceed();
        if (result != null) {
            // 将结果放入缓存
            hotSqlInfo.getLocalCache().put(cacheKey, result);
            hotSqlInfo.getRedisCache().put(cacheKey, result);
        }
        return result;
    }

    /**
     * 处理热点表的缓存
     */
    private Object handleHotTableCache(ProceedingJoinPoint point, HotTableInfo hotTableInfo,
                                       String tableName, String methodName, Object[] args) throws Throwable {
        String cacheKey = generateCacheKey(tableName + "." + methodName, args);

        // 使用多级缓存策略
        MultiLevelCache<String, Object> multiCache = cacheManager.getMultiLevelCache(tableName);
        if (multiCache == null) {
            // 如果不存在，创建新的多级缓存
            multiCache = cacheManager.createMultiLevelCache(
                    tableName, 1000, 5, 20, TimeUnit.MINUTES);
        }

        // 从多级缓存获取结果
        return multiCache.get(cacheKey, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    return point.proceed();
                } catch (Throwable e) {
                    if (e instanceof Exception) {
                        throw (Exception) e;
                    }
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * 判断是否为查询方法
     */
    private boolean isQueryMethod(String methodName) {
        return methodName.startsWith("get") ||
                methodName.startsWith("find") ||
                methodName.startsWith("select") ||
                methodName.startsWith("query") ||
                methodName.startsWith("list") ||
                methodName.startsWith("count") ||
                methodName.startsWith("search");
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String prefix, Object[] args) {
        if (args == null || args.length == 0) {
            return cacheKeyPrefix + prefix;
        }

        StringBuilder sb = new StringBuilder(cacheKeyPrefix);
        sb.append(prefix).append("_");

        for (Object arg : args) {
            if (arg == null) {
                sb.append("null");
            } else if (arg.getClass().isArray()) {
                sb.append(Arrays.deepToString((Object[]) arg));
            } else {
                sb.append(arg.toString());
            }
            sb.append("_");
        }

        // 防止key过长
        String key = sb.toString();
        if (key.length() > 200) {
            return key.substring(0, 200) + "_" + key.hashCode();
        }
        return key;
    }
} 
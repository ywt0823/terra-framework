package com.terra.framework.strata.config.mysql.aspect;

import com.terra.framework.strata.config.mysql.annonation.AutoCache;
import com.terra.framework.strata.config.mysql.manager.AutoCacheManager;
import com.terra.framework.strata.config.mysql.manager.AutoCacheManager.MultiLevelCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 自动缓存切面
 * 处理使用AutoCache注解的方法
 */
@Aspect
@Slf4j
public class AutoCacheAspect {

    private final AutoCacheManager cacheManager;
    private final CacheInvalidationAspect invalidationAspect;

    public AutoCacheAspect(AutoCacheManager cacheManager, CacheInvalidationAspect invalidationAspect) {
        this.cacheManager = cacheManager;
        this.invalidationAspect = invalidationAspect;
    }

    /**
     * 拦截使用AutoCache注解的方法
     */
    @Around("@annotation(com.terra.framework.strata.config.mysql.annonation.AutoCache)")
    public Object aroundCachedMethod(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        AutoCache annotation = method.getAnnotation(AutoCache.class);

        if (annotation == null) {
            return point.proceed();
        }

        // 获取缓存名称
        String cacheName = annotation.name();
        if (cacheName.isEmpty()) {
            cacheName = method.getDeclaringClass().getName() + "." + method.getName();
        }

        // 生成缓存键
        String cacheKey = generateCacheKey(
                annotation.keyPrefix().isEmpty() ? cacheName : annotation.keyPrefix(),
                point.getArgs()
        );

        // 注册表与缓存的映射关系，用于缓存失效
        if (annotation.syncInvalidate() && annotation.tables().length > 0) {
            for (String table : annotation.tables()) {
                invalidationAspect.registerTableCacheMapping(table, cacheName);
            }
        }

        // 获取或创建多级缓存
        MultiLevelCache<String, Object> cache = cacheManager.getMultiLevelCache(cacheName);
        if (cache == null) {
            cache = cacheManager.createMultiLevelCache(
                    cacheName,
                    annotation.localMaxSize(),
                    annotation.localExpireTime(),
                    annotation.redisExpireTime(),
                    annotation.timeUnit()
            );
        }

        // 从缓存获取结果
        return cache.get(cacheKey, () -> {
            try {
                return point.proceed();
            } catch (Throwable e) {
                if (e instanceof Exception) {
                    throw (Exception) e;
                }
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String prefix, Object[] args) {
        if (args == null || args.length == 0) {
            return prefix;
        }

        StringBuilder sb = new StringBuilder(prefix).append(":");

        for (Object arg : args) {
            if (arg == null) {
                sb.append("null");
            } else if (arg.getClass().isArray()) {
                sb.append(Arrays.deepToString((Object[]) arg));
            } else {
                sb.append(arg);
            }
            sb.append(":");
        }

        // 防止key过长
        String key = sb.toString();
        if (key.length() > 200) {
            return key.substring(0, 200) + ":" + key.hashCode();
        }
        return key;
    }
} 
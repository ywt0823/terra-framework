package com.terra.framework.strata.config.mysql.interceptor;

import com.terra.framework.strata.config.mysql.adapter.SqlMetricsAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL监控拦截器
 * 收集SQL执行次数、耗时等信息，用于热点SQL探测
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@Slf4j
public class SqlMonitorInterceptor implements Interceptor {

    private static final Pattern TABLE_PATTERN = Pattern.compile("\\s+FROM\\s+`?(\\w+)`?", Pattern.CASE_INSENSITIVE);
    private final SqlMetricsAdapter metricsCollector;

    public SqlMonitorInterceptor(SqlMetricsAdapter metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql;
        if (invocation.getArgs().length > 5) {
            boundSql = (BoundSql) invocation.getArgs()[5];
        } else {
            boundSql = mappedStatement.getBoundSql(parameter);
        }

        String sqlId = mappedStatement.getId();
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = getSql(configuration, boundSql);

        // 执行原始方法
        Object result;
        try {
            result = invocation.proceed();
        } catch (Exception e) {
            // 记录异常SQL
            metricsCollector.recordErrorSql(sqlId, sql);
            throw e;
        }

        // 收集SQL指标
        long executionTime = System.currentTimeMillis() - startTime;
        metricsCollector.recordSqlExecution(sqlId, sql, executionTime);

        // 提取表名并记录
        String tableName = extractTableName(sql);
        if (tableName != null) {
            metricsCollector.recordTableAccess(tableName, executionTime);
        }

        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * 获取完整的SQL语句
     */
    private String getSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");

        if (CollectionUtils.isEmpty(parameterMappings) || parameterObject == null) {
            return sql;
        }

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
        } else {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            for (ParameterMapping parameterMapping : parameterMappings) {
                String propertyName = parameterMapping.getProperty();
                if (metaObject.hasGetter(propertyName)) {
                    Object obj = metaObject.getValue(propertyName);
                    sql = sql.replaceFirst("\\?", getParameterValue(obj));
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    Object obj = boundSql.getAdditionalParameter(propertyName);
                    sql = sql.replaceFirst("\\?", getParameterValue(obj));
                }
            }
        }
        return sql;
    }

    /**
     * 获取参数值
     */
    private String getParameterValue(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "'" + obj + "'";
        }
        if (obj instanceof Date) {
            return "'" + DateFormat.getDateTimeInstance().format(obj) + "'";
        }
        return obj.toString();
    }

    /**
     * 从SQL语句中提取表名
     */
    private String extractTableName(String sql) {
        Matcher matcher = TABLE_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
} 
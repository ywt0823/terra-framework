package com.terra.framework.autoconfigure.crust.trace;

import com.terra.framework.autoconfigure.crust.properties.TerraTraceProperties;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.common.util.concurrent.AbstractBatchProcess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 链路追踪数据收集器，用于收集和处理追踪数据
 */
@Slf4j
public class TraceDataCollector extends AbstractBatchProcess<TraceDataCollector.TraceData> implements InitializingBean, DisposableBean {

    private final LogPattern logPattern;
    private final TerraTraceProperties traceProperties;
    private final Map<String, TraceData> activeTraces = new ConcurrentHashMap<>();

    public TraceDataCollector(LogPattern logPattern, TerraTraceProperties traceProperties) {
        super(traceProperties.getCollector().getMaxTraceCapacity(), Duration.ofSeconds(30));
        this.logPattern = logPattern;
        this.traceProperties = traceProperties;
    }

    /**
     * 记录请求开始
     */
    public void recordRequestStart(String traceId, String spanId, String parentSpanId, String uri, Map<String, String> headers) {
        if (!shouldSample()) {
            return;
        }

        TraceData traceData = new TraceData();
        traceData.setTraceId(traceId);
        traceData.setSpanId(spanId);
        traceData.setParentSpanId(parentSpanId);
        traceData.setUri(uri);
        traceData.setStartTime(System.currentTimeMillis());
        traceData.setHeaders(headers);

        activeTraces.put(traceId + ":" + spanId, traceData);
    }

    /**
     * 记录请求结束
     */
    public void recordRequestEnd(String traceId, String spanId, int statusCode, long duration) {
        String key = traceId + ":" + spanId;
        TraceData traceData = activeTraces.remove(key);
        if (traceData != null) {
            traceData.setStatusCode(statusCode);
            traceData.setDuration(duration);
            traceData.setEndTime(System.currentTimeMillis());

            // 处理收集到的数据
            process(traceData);
        }
    }

    /**
     * 根据采样率决定是否采样
     */
    private boolean shouldSample() {
        return ThreadLocalRandom.current().nextDouble() < traceProperties.getCollector().getSampleRate();
    }

    @Override
    protected Boolean support(TraceData message) {
        return message != null && message.getTraceId() != null;
    }

    @Override
    protected Boolean batchInsert(List<TraceData> messages) {
        // 这里可以实现将跟踪数据写入日志、数据库或发送到跟踪系统
        for (TraceData data : messages) {
            log.info(logPattern.formalize("链路追踪数据",
                    "traceId", "spanId", "parentSpanId", "uri", "statusCode", "duration"),
                    data.getTraceId(), data.getSpanId(), data.getParentSpanId(),
                    data.getUri(), data.getStatusCode(), data.getDuration() + "ms");
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (traceProperties.isEnabled() && traceProperties.getCollector().isEnabled()) {
            log.info("初始化链路追踪数据收集器");
            this.schedule();
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("关闭链路追踪数据收集器");
        super.destroy();
    }

    @Data
    @AllArgsConstructor
    public static class TraceData {
        private String traceId;
        private String spanId;
        private String parentSpanId;
        private String uri;
        private long startTime;
        private long endTime;
        private int statusCode;
        private long duration;
        private Map<String, String> headers;

        public TraceData() {
        }
    }
}

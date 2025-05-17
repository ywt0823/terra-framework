package com.terra.framework.nova.core.monitoring;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

import com.terra.framework.nova.core.model.ModelInfo;

/**
 * 计时上下文，用于测量操作耗时
 *
 * @author terra-nova
 */
@Data
@Builder
public class TimingContext {

    /**
     * 开始时间
     */
    private final Instant startTime;

    /**
     * 请求ID
     */
    private final String requestId;

    /**
     * 操作类型
     */
    private final String operationType;

    /**
     * 模型信息
     */
    private final ModelInfo modelInfo;

    /**
     * 创建计时上下文
     *
     * @param modelInfo 模型信息
     * @param requestId 请求ID
     * @param operationType 操作类型
     * @return 计时上下文
     */
    public static TimingContext create(ModelInfo modelInfo, String requestId, String operationType) {
        return TimingContext.builder()
                .startTime(Instant.now())
                .requestId(requestId)
                .operationType(operationType)
                .modelInfo(modelInfo)
                .build();
    }

    /**
     * 计算从开始到现在的耗时（毫秒）
     *
     * @return 耗时
     */
    public long getElapsedTimeMs() {
        return Instant.now().toEpochMilli() - startTime.toEpochMilli();
    }
}

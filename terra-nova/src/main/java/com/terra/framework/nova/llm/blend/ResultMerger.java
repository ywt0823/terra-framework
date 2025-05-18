package com.terra.framework.nova.llm.blend;

import java.util.List;
import java.util.Map;

import com.terra.framework.nova.llm.model.ModelResponse;

/**
 * 结果合并器接口
 *
 * @author terra-nova
 */
public interface ResultMerger {

    /**
     * 合并多个模型的响应
     *
     * @param responses 响应列表
     * @param weights 权重映射（模型ID -> 权重）
     * @return 合并后的响应
     */
    ModelResponse merge(List<ModelResponse> responses, Map<String, Integer> weights);

    /**
     * 获取合并策略
     *
     * @return 合并策略
     */
    MergeStrategy getStrategy();
}

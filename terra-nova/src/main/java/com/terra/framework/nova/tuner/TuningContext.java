package com.terra.framework.nova.tuner;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 调优上下文
 * 包含调优过程中的各种上下文信息
 *
 * @author terra-nova
 */
@Data
@Builder
public class TuningContext {
    
    /**
     * 上下文ID
     */
    @Builder.Default
    private String contextId = UUID.randomUUID().toString();
    
    /**
     * 任务类型
     */
    private TaskType taskType;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
    /**
     * 目标模型
     */
    private String targetModel;
    
    /**
     * 目标提供商
     */
    private String targetProvider;
    
    /**
     * 输入文本
     */
    private String inputText;
    
    /**
     * 优化目标
     */
    @Builder.Default
    private OptimizationGoal optimizationGoal = OptimizationGoal.QUALITY;
    
    /**
     * 当前迭代次数
     */
    @Builder.Default
    private int iteration = 0;
    
    /**
     * 最大迭代次数
     */
    @Builder.Default
    private int maxIterations = 10;
    
    /**
     * 额外属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
    
    /**
     * 获取额外属性
     *
     * @param key 属性键
     * @param <T> 属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
    
    /**
     * 设置额外属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 递增迭代次数
     */
    public void incrementIteration() {
        this.iteration++;
    }
    
    /**
     * 判断是否达到最大迭代次数
     *
     * @return 是否达到最大迭代次数
     */
    public boolean isMaxIterationsReached() {
        return iteration >= maxIterations;
    }
    
    /**
     * 创建上下文副本
     *
     * @return 上下文副本
     */
    public TuningContext copy() {
        return TuningContext.builder()
                .taskType(this.taskType)
                .taskDescription(this.taskDescription)
                .targetModel(this.targetModel)
                .targetProvider(this.targetProvider)
                .inputText(this.inputText)
                .optimizationGoal(this.optimizationGoal)
                .iteration(this.iteration)
                .maxIterations(this.maxIterations)
                .attributes(new HashMap<>(this.attributes))
                .build();
    }
    
    /**
     * 优化目标枚举
     */
    public enum OptimizationGoal {
        /**
         * 质量优先
         */
        QUALITY,
        
        /**
         * 成本优先
         */
        COST,
        
        /**
         * 速度优先
         */
        SPEED,
        
        /**
         * 平衡
         */
        BALANCED
    }
    
    /**
     * 任务类型枚举
     */
    public enum TaskType {
        /**
         * 聊天对话
         */
        CHAT,
        
        /**
         * 内容生成
         */
        GENERATION,
        
        /**
         * 文本摘要
         */
        SUMMARIZATION,
        
        /**
         * 代码生成
         */
        CODE,
        
        /**
         * 翻译
         */
        TRANSLATION,
        
        /**
         * 其他
         */
        OTHER
    }
} 
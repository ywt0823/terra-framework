package com.terra.framework.nova.tuner.impl;

import com.terra.framework.nova.tuner.ParameterTuner;
import com.terra.framework.nova.tuner.TuningContext;
import com.terra.framework.nova.tuner.TuningMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于启发式规则的参数调优器
 * 根据任务类型和优化目标提供预设的参数组合
 *
 * @author terra-nova
 */
@Slf4j
public class HeuristicParameterTuner implements ParameterTuner {
    
    private Map<String, Map<String, Object>> bestParameters = new HashMap<>();
    
    @Override
    public Map<String, Object> tuneParameters(Map<String, Object> parameters, TuningContext context) {
        Map<String, Object> result = new HashMap<>(parameters);
        String key = getContextKey(context);
        
        // 如果有缓存的最佳参数，直接返回
        if (bestParameters.containsKey(key)) {
            log.debug("使用缓存的最佳参数: {}", bestParameters.get(key));
            return new HashMap<>(bestParameters.get(key));
        }
        
        // 根据任务类型和优化目标应用启发式规则
        applyTaskTypeRules(result, context.getTaskType());
        applyOptimizationGoalRules(result, context.getOptimizationGoal());
        applyModelSpecificRules(result, context.getTargetModel(), context.getTargetProvider());
        
        log.debug("应用启发式规则后参数: {}", result);
        return result;
    }
    
    @Override
    public void updateWithResult(Map<String, Object> parameters, TuningContext context, 
                                String result, TuningMetrics metrics) {
        String key = getContextKey(context);
        double score = metrics.getCompositeScore(context.getOptimizationGoal());
        
        // 如果没有最佳参数或者当前参数更好，则更新
        if (!bestParameters.containsKey(key) || 
            getBestScore(key) < score) {
            log.debug("更新最佳参数: {}，得分: {}", parameters, score);
            bestParameters.put(key, new HashMap<>(parameters));
            // 存储得分
            context.setAttribute("best_score", score);
        }
    }
    
    @Override
    public String getName() {
        return "heuristic";
    }
    
    @Override
    public void reset() {
        bestParameters.clear();
    }
    
    /**
     * 获取上下文的缓存键
     *
     * @param context 调优上下文
     * @return 缓存键
     */
    private String getContextKey(TuningContext context) {
        return context.getTaskType() + "_" + 
               context.getOptimizationGoal() + "_" + 
               context.getTargetModel() + "_" + 
               context.getTargetProvider();
    }
    
    /**
     * 获取最佳得分
     *
     * @param key 上下文键
     * @return 最佳得分
     */
    private double getBestScore(String key) {
        return bestParameters.containsKey(key) ? 
               Double.parseDouble(bestParameters.get(key).getOrDefault("_score", "0.0").toString()) : 0.0;
    }
    
    /**
     * 应用任务类型相关的规则
     *
     * @param parameters 参数集
     * @param taskType 任务类型
     */
    private void applyTaskTypeRules(Map<String, Object> parameters, TuningContext.TaskType taskType) {
        switch (taskType) {
            case CHAT:
                // 聊天对话通常需要更高的温度，使响应更有活力
                parameters.putIfAbsent("temperature", 0.8);
                parameters.putIfAbsent("top_p", 0.9);
                break;
                
            case GENERATION:
                // 内容生成需要平衡创造性和连贯性
                parameters.putIfAbsent("temperature", 0.7);
                parameters.putIfAbsent("top_p", 0.85);
                break;
                
            case SUMMARIZATION:
                // 摘要需要更加精确和连贯
                parameters.putIfAbsent("temperature", 0.3);
                parameters.putIfAbsent("top_p", 0.95);
                break;
                
            case CODE:
                // 代码生成要求精确和确定性
                parameters.putIfAbsent("temperature", 0.2);
                parameters.putIfAbsent("top_p", 0.98);
                break;
                
            case TRANSLATION:
                // 翻译要求高精度
                parameters.putIfAbsent("temperature", 0.3);
                parameters.putIfAbsent("top_p", 0.95);
                break;
                
            case OTHER:
            default:
                // 默认参数
                parameters.putIfAbsent("temperature", 0.5);
                parameters.putIfAbsent("top_p", 0.9);
                break;
        }
    }
    
    /**
     * 应用优化目标相关的规则
     *
     * @param parameters 参数集
     * @param goal 优化目标
     */
    private void applyOptimizationGoalRules(Map<String, Object> parameters, TuningContext.OptimizationGoal goal) {
        switch (goal) {
            case QUALITY:
                // 质量优先，降低温度，提高精确度
                parameters.put("temperature", Math.min(0.4, getDoubleValue(parameters, "temperature")));
                parameters.put("top_p", Math.max(0.95, getDoubleValue(parameters, "top_p")));
                break;
                
            case SPEED:
                // 速度优先，控制生成长度，加快响应速度
                parameters.putIfAbsent("max_tokens", 150);
                parameters.put("frequency_penalty", 0.5);
                parameters.put("presence_penalty", 0.5);
                break;
                
            case COST:
                // 成本优先，限制令牌数量
                parameters.putIfAbsent("max_tokens", 100);
                parameters.put("frequency_penalty", 0.7);
                parameters.put("presence_penalty", 0.7);
                break;
                
            case BALANCED:
            default:
                // 平衡各项指标
                parameters.putIfAbsent("max_tokens", 200);
                parameters.putIfAbsent("frequency_penalty", 0.0);
                parameters.putIfAbsent("presence_penalty", 0.0);
                break;
        }
    }
    
    /**
     * 应用模型特定的规则
     *
     * @param parameters 参数集
     * @param model 模型名称
     * @param provider 提供商
     */
    private void applyModelSpecificRules(Map<String, Object> parameters, String model, String provider) {
        if (model == null || provider == null) {
            return;
        }
        
        // OpenAI模型规则
        if ("openai".equalsIgnoreCase(provider)) {
            if (model.contains("gpt-4")) {
                // GPT-4对低温度更敏感
                parameters.put("temperature", Math.min(0.7, getDoubleValue(parameters, "temperature")));
            } else if (model.contains("gpt-3.5")) {
                // GPT-3.5可能需要更高的temperature以获得多样性
                parameters.put("temperature", Math.max(0.3, getDoubleValue(parameters, "temperature")));
            }
        }
        
        // Anthropic模型规则
        else if ("anthropic".equalsIgnoreCase(provider) && model.contains("claude")) {
            // Claude模型通常对温度参数更敏感
            parameters.put("temperature", Math.min(0.8, getDoubleValue(parameters, "temperature")));
            // Claude使用top_k而不是top_p
            if (parameters.containsKey("top_p")) {
                double topP = getDoubleValue(parameters, "top_p");
                parameters.remove("top_p");
                parameters.put("top_k", (int)(topP * 100));
            }
        }
        
        // Ollama模型规则
        else if ("ollama".equalsIgnoreCase(provider)) {
            // 本地模型通常需要更高的温度
            parameters.put("temperature", Math.max(0.5, getDoubleValue(parameters, "temperature")));
        }
    }
    
    /**
     * 获取参数中的Double值
     *
     * @param parameters 参数集
     * @param key 参数键
     * @return Double值，如果不存在返回0.0
     */
    private double getDoubleValue(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) {
            return 0.0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 
package com.terra.framework.nova.tuner.impl;

import com.terra.framework.nova.tuner.ParameterTuner;
import com.terra.framework.nova.tuner.TuningContext;
import com.terra.framework.nova.tuner.TuningMetrics;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 基于贝叶斯优化的参数调优器
 * 使用简化的贝叶斯优化算法来自动调整参数
 *
 * @author terra-nova
 */
@Slf4j
public class BayesianParameterTuner implements ParameterTuner {
    
    // 优化参数范围
    private static final Map<String, ParameterRange> PARAMETER_RANGES = new HashMap<>();
    
    static {
        PARAMETER_RANGES.put("temperature", new ParameterRange(0.1, 1.0));
        PARAMETER_RANGES.put("top_p", new ParameterRange(0.5, 1.0));
        PARAMETER_RANGES.put("frequency_penalty", new ParameterRange(0.0, 2.0));
        PARAMETER_RANGES.put("presence_penalty", new ParameterRange(0.0, 2.0));
        PARAMETER_RANGES.put("max_tokens", new ParameterRange(50.0, 500.0));
    }
    
    // 可调整参数列表
    private final List<String> tunableParameters = List.of(
            "temperature", "top_p", "frequency_penalty", "presence_penalty"
    );
    
    // 历史观测点
    private final Map<String, List<Observation>> observations = new HashMap<>();
    
    // 随机数生成器
    private final Random random = new Random();
    
    // 最优参数和得分
    private final Map<String, Map<String, Object>> bestParameters = new HashMap<>();
    private final Map<String, Double> bestScores = new HashMap<>();
    
    // 探索-利用平衡参数
    private double explorationRate = 0.3;
    
    @Override
    public Map<String, Object> tuneParameters(Map<String, Object> parameters, TuningContext context) {
        String contextKey = getContextKey(context);
        Map<String, Object> result = new HashMap<>(parameters);
        
        // 如果上下文有迭代次数，更新探索率
        if (context.getIteration() > 0) {
            // 随着迭代次数增加，减少探索率
            explorationRate = Math.max(0.1, 0.3 - 0.02 * context.getIteration());
        }
        
        // 如果已经找到最优参数且探索率较低，有较大概率直接返回最优参数
        if (bestParameters.containsKey(contextKey) && random.nextDouble() > explorationRate) {
            log.debug("利用已知最优参数: {}", bestParameters.get(contextKey));
            return new HashMap<>(bestParameters.get(contextKey));
        }
        
        // 获取历史观测数据
        List<Observation> contextObservations = observations.computeIfAbsent(contextKey, k -> new ArrayList<>());
        
        if (contextObservations.isEmpty()) {
            // 首次运行，使用初始参数，可能添加些微随机变化
            for (String param : tunableParameters) {
                if (result.containsKey(param) && PARAMETER_RANGES.containsKey(param)) {
                    ParameterRange range = PARAMETER_RANGES.get(param);
                    double currentValue = getDoubleValue(result, param);
                    // 添加些微随机变化
                    double newValue = currentValue + (random.nextDouble() - 0.5) * 0.1 * (range.getMax() - range.getMin());
                    // 确保在范围内
                    newValue = Math.max(range.getMin(), Math.min(range.getMax(), newValue));
                    result.put(param, newValue);
                }
            }
        } else {
            // 后续运行，使用贝叶斯优化
            applyBayesianOptimization(result, contextObservations, context);
        }
        
        log.debug("贝叶斯优化后参数: {}", result);
        return result;
    }
    
    @Override
    public void updateWithResult(Map<String, Object> parameters, TuningContext context, 
                                String result, TuningMetrics metrics) {
        String contextKey = getContextKey(context);
        double score = metrics.getCompositeScore(context.getOptimizationGoal());
        
        // 添加观测数据
        Observation observation = new Observation(new HashMap<>(parameters), score);
        observations.computeIfAbsent(contextKey, k -> new ArrayList<>()).add(observation);
        
        // 更新最佳参数和得分
        if (!bestScores.containsKey(contextKey) || bestScores.get(contextKey) < score) {
            bestParameters.put(contextKey, new HashMap<>(parameters));
            bestScores.put(contextKey, score);
            log.debug("更新最佳参数: {}，得分: {}", parameters, score);
        }
        
        log.debug("更新观测数据: 参数={}，得分={}", parameters, score);
    }
    
    @Override
    public String getName() {
        return "bayesian";
    }
    
    @Override
    public void reset() {
        observations.clear();
        bestParameters.clear();
        bestScores.clear();
        explorationRate = 0.3;
    }
    
    /**
     * 应用贝叶斯优化
     *
     * @param parameters 当前参数
     * @param observations 历史观测数据
     * @param context 调优上下文
     */
    private void applyBayesianOptimization(Map<String, Object> parameters, 
                                          List<Observation> observations,
                                          TuningContext context) {
        // 如果观测数据太少，采用随机探索
        if (observations.size() < 3) {
            applyRandomExploration(parameters);
            return;
        }
        
        // 找出最佳观测
        Observation best = observations.stream()
                .max((o1, o2) -> Double.compare(o1.getScore(), o2.getScore()))
                .orElse(null);
        
        if (best == null) {
            applyRandomExploration(parameters);
            return;
        }
        
        // 根据探索率决定是探索还是利用
        if (random.nextDouble() < explorationRate) {
            // 探索：随机选择一个参数进行调整
            String paramToExplore = tunableParameters.get(random.nextInt(tunableParameters.size()));
            if (parameters.containsKey(paramToExplore) && PARAMETER_RANGES.containsKey(paramToExplore)) {
                ParameterRange range = PARAMETER_RANGES.get(paramToExplore);
                // 在范围内随机选择一个值
                double value = range.getMin() + random.nextDouble() * (range.getMax() - range.getMin());
                parameters.put(paramToExplore, value);
                log.debug("探索参数 {}: {}", paramToExplore, value);
            }
        } else {
            // 利用：在最佳参数附近搜索
            for (String param : tunableParameters) {
                if (best.getParameters().containsKey(param) && PARAMETER_RANGES.containsKey(param)) {
                    double bestValue = getDoubleValue(best.getParameters(), param);
                    ParameterRange range = PARAMETER_RANGES.get(param);
                    
                    // 计算参数调整步长，随着迭代次数增加而减小
                    double stepSize = (range.getMax() - range.getMin()) * 0.1 / (1 + context.getIteration() * 0.2);
                    
                    // 在最佳值附近进行小幅调整
                    double adjustment = (random.nextDouble() - 0.5) * stepSize;
                    double newValue = bestValue + adjustment;
                    
                    // 确保在范围内
                    newValue = Math.max(range.getMin(), Math.min(range.getMax(), newValue));
                    parameters.put(param, newValue);
                    
                    log.debug("在最佳值附近调整参数 {}: {} -> {}", param, bestValue, newValue);
                }
            }
        }
    }
    
    /**
     * 应用随机探索
     *
     * @param parameters 当前参数
     */
    private void applyRandomExploration(Map<String, Object> parameters) {
        // 随机选择一个参数进行调整
        String paramToExplore = tunableParameters.get(random.nextInt(tunableParameters.size()));
        if (parameters.containsKey(paramToExplore) && PARAMETER_RANGES.containsKey(paramToExplore)) {
            ParameterRange range = PARAMETER_RANGES.get(paramToExplore);
            double currentValue = getDoubleValue(parameters, paramToExplore);
            
            // 在当前值附近随机选择一个值
            double delta = (random.nextDouble() - 0.5) * (range.getMax() - range.getMin()) * 0.2;
            double newValue = currentValue + delta;
            
            // 确保在范围内
            newValue = Math.max(range.getMin(), Math.min(range.getMax(), newValue));
            parameters.put(paramToExplore, newValue);
            
            log.debug("随机探索参数 {}: {} -> {}", paramToExplore, currentValue, newValue);
        }
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
    
    /**
     * 参数范围
     */
    @Data
    private static class ParameterRange {
        private final double min;
        private final double max;
        
        public ParameterRange(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
    
    /**
     * 观测数据
     */
    @Data
    private static class Observation {
        private final Map<String, Object> parameters;
        private final double score;
        
        public Observation(Map<String, Object> parameters, double score) {
            this.parameters = parameters;
            this.score = score;
        }
    }
} 
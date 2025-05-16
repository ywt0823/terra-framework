package com.terra.framework.nova.tuner.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调优配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.tuner")
public class TunerProperties {

    /**
     * 是否启用调优功能
     */
    private boolean enabled = true;
    
    /**
     * 默认调优器
     */
    private String defaultTuner = "heuristic";
    
    /**
     * 默认最大迭代次数
     */
    private int maxIterations = 10;
    
    /**
     * 启发式调优器配置
     */
    private HeuristicTunerConfig heuristic = new HeuristicTunerConfig();
    
    /**
     * 贝叶斯调优器配置
     */
    private BayesianTunerConfig bayesian = new BayesianTunerConfig();
    
    /**
     * 工作线程配置
     */
    private ThreadConfig thread = new ThreadConfig();
    
    /**
     * Actuator端点配置
     */
    private ActuatorConfig actuator = new ActuatorConfig();
    
    /**
     * 启发式调优器配置
     */
    @Data
    public static class HeuristicTunerConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
    
    /**
     * 贝叶斯调优器配置
     */
    @Data
    public static class BayesianTunerConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 初始探索率
         */
        private double initialExplorationRate = 0.3;
        
        /**
         * 最小探索率
         */
        private double minExplorationRate = 0.1;
    }
    
    /**
     * 工作线程配置
     */
    @Data
    public static class ThreadConfig {
        /**
         * 核心线程数
         */
        private int corePoolSize = 2;
        
        /**
         * 最大线程数
         */
        private int maxPoolSize = 5;
        
        /**
         * 队列容量
         */
        private int queueCapacity = 100;
        
        /**
         * 线程名称前缀
         */
        private String threadNamePrefix = "tuner-";
    }
    
    /**
     * Actuator端点配置
     */
    @Data
    public static class ActuatorConfig {
        /**
         * 是否启用端点
         */
        private boolean enabled = true;
        
        /**
         * 端点ID
         */
        private String id = "tuner";
        
        /**
         * 是否在管理端点中暴露
         */
        private boolean exposedOverManagement = true;
        
        /**
         * 是否在Web端点中暴露
         */
        private boolean exposedOverWeb = true;
    }
} 
package com.terra.framework.nova.tuner.config;

import com.terra.framework.nova.tuner.ParameterTuner;
import com.terra.framework.nova.tuner.impl.BayesianParameterTuner;
import com.terra.framework.nova.tuner.impl.HeuristicParameterTuner;
import com.terra.framework.nova.tuner.properties.TunerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 调优自动配置类
 *
 * @author terra-nova
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(TunerProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.tuner", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TunerAutoConfiguration {

    /**
     * 配置启发式参数调优器
     */
    @Bean
    @ConditionalOnProperty(prefix = "terra.nova.tuner.heuristic", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ParameterTuner heuristicParameterTuner() {
        log.info("正在配置启发式参数调优器");
        return new HeuristicParameterTuner();
    }
    
    /**
     * 配置贝叶斯参数调优器
     */
    @Bean
    @ConditionalOnProperty(prefix = "terra.nova.tuner.bayesian", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ParameterTuner bayesianParameterTuner() {
        log.info("正在配置贝叶斯参数调优器");
        return new BayesianParameterTuner();
    }
    
    /**
     * 配置调优线程池
     */
    @Bean("tunerTaskExecutor")
    @ConditionalOnMissingBean(name = "tunerTaskExecutor")
    public TaskExecutor tunerTaskExecutor(TunerProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        TunerProperties.ThreadConfig config = properties.getThread();
        
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        log.info("配置调优线程池: coreSize={}, maxSize={}, queueCapacity={}, namePrefix={}",
                config.getCorePoolSize(), config.getMaxPoolSize(),
                config.getQueueCapacity(), config.getThreadNamePrefix());
        
        return executor;
    }
} 
package com.terra.framework.nova.tool.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.tool.Tool;
import com.terra.framework.nova.tool.manager.DefaultToolManager;
import com.terra.framework.nova.tool.manager.ToolManager;
import com.terra.framework.nova.tool.properties.ToolProperties;
import com.terra.framework.nova.tool.tools.CalculatorTool;
import com.terra.framework.nova.tool.tools.HttpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 工具自动配置类
 *
 * @author terra-nova
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ToolProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.tool", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ToolAutoConfiguration {

    /**
     * 配置工具管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public ToolManager toolManager() {
        log.info("初始化工具管理器");
        return new DefaultToolManager();
    }

    /**
     * 配置计算器工具
     */
    @Bean
    @ConditionalOnMissingBean(name = "calculatorTool")
    public Tool calculatorTool(ToolManager toolManager) {
        CalculatorTool calculatorTool = new CalculatorTool();
        toolManager.registerTool(calculatorTool);
        log.info("注册计算器工具");
        return calculatorTool;
    }

    /**
     * 配置HTTP工具
     */
    @Bean
    @ConditionalOnMissingBean(name = "httpTool")
    @ConditionalOnBean(HttpClientUtils.class)
    @ConditionalOnProperty(prefix = "terra.nova.tool.http", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Tool httpTool(HttpClientUtils httpClientUtils, ToolManager toolManager, ToolProperties toolProperties) {
        HttpTool httpTool = new HttpTool(httpClientUtils);
        toolManager.registerTool(httpTool);
        log.info("注册HTTP工具");
        return httpTool;
    }
}

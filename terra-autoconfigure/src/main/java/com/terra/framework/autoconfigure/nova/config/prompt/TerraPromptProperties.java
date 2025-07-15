package com.terra.framework.autoconfigure.nova.config.prompt;

import com.terra.framework.nova.prompt.config.PromptConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Terra Prompt Mapper.
 *
 * @author DeavyJones
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "terra.nova.prompt")
public class TerraPromptProperties {

    /**
     * Locations to scan for prompt mapper XML files.
     * Defaults to scanning "prompts/" directory in the classpath.
     */
    private String[] mapperLocations = {"classpath*:/prompts/**/*.xml"};

    /**
     * 是否启用自动扫描（向后兼容）
     */
    private boolean autoScan = true;

    /**
     * 自动扫描的基础包
     */
    private String[] autoScanBasePackages = {};

    /**
     * 默认的 ChatModel Bean 名称
     */
    private String defaultChatModel = "deepSeekChatModel";

    /**
     * 默认配置
     */
    private PromptConfig defaultConfig = new PromptConfig("deepSeekChatModel", 0.7d, 10000, 5.0d);

}

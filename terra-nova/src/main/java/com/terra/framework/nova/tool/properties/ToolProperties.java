package com.terra.framework.nova.tool.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.tool")
public class ToolProperties {

    /**
     * 是否启用工具
     */
    private boolean enabled = true;

    /**
     * 工具扫描包
     */
    private List<String> scanPackages = new ArrayList<>();

    /**
     * 启用的工具
     */
    private List<String> enabledTools = new ArrayList<>();

    /**
     * 禁用的工具
     */
    private List<String> disabledTools = new ArrayList<>();

    /**
     * 工具参数配置
     */
    private Map<String, Map<String, String>> toolParameters = new HashMap<>();

    /**
     * HTTP工具配置
     */
    private HttpToolProperties http = new HttpToolProperties();

    /**
     * 获取工具的参数配置
     *
     * @param toolName 工具名称
     * @return 参数配置
     */
    public Map<String, String> getToolParameters(String toolName) {
        return toolParameters.getOrDefault(toolName, new HashMap<>());
    }

    /**
     * HTTP工具配置属性
     */
    @Data
    public static class HttpToolProperties {

        /**
         * 是否启用HTTP工具
         */
        private boolean enabled = true;

        /**
         * 允许的域名列表
         */
        private List<String> allowedDomains = new ArrayList<>();

        /**
         * 禁用的域名列表
         */
        private List<String> blockedDomains = new ArrayList<>();

        /**
         * 最大超时时间（毫秒）
         */
        private int maxTimeout = 10000;

        /**
         * 默认请求头
         */
        private Map<String, String> defaultHeaders = new HashMap<>();
    }
}

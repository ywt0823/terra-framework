package com.terra.framework.nova.agent;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认代理配置实现
 *
 * @author terra-nova
 */
@Data
public class DefaultAgentConfig implements AgentConfig {
    
    /**
     * 最大执行步骤数
     */
    private int maxSteps = 10;
    
    /**
     * 超时时间(毫秒)
     */
    private long timeoutMs = 60000;
    
    /**
     * LLM模型ID
     */
    private String modelId = "gpt-4";
    
    /**
     * 代理类型
     */
    private AgentType type = AgentType.REACT;
    
    /**
     * 可用工具列表
     */
    private List<String> tools = new ArrayList<>();
    
    /**
     * 代理名称
     */
    private String name = "DefaultAgent";
    
    /**
     * LLM参数
     */
    private Map<String, Object> llmParameters = new HashMap<>();
    
    /**
     * 创建Builder对象
     * 
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 构建器
     */
    public static class Builder {
        private final DefaultAgentConfig config = new DefaultAgentConfig();
        
        /**
         * 设置最大步骤数
         * 
         * @param maxSteps 最大步骤数
         * @return 构建器
         */
        public Builder maxSteps(int maxSteps) {
            config.setMaxSteps(maxSteps);
            return this;
        }
        
        /**
         * 设置超时时间
         * 
         * @param timeoutMs 超时时间(毫秒)
         * @return 构建器
         */
        public Builder timeoutMs(long timeoutMs) {
            config.setTimeoutMs(timeoutMs);
            return this;
        }
        
        /**
         * 设置模型ID
         * 
         * @param modelId 模型ID
         * @return 构建器
         */
        public Builder modelId(String modelId) {
            config.setModelId(modelId);
            return this;
        }
        
        /**
         * 设置代理类型
         * 
         * @param type 代理类型
         * @return 构建器
         */
        public Builder type(AgentType type) {
            config.setType(type);
            return this;
        }
        
        /**
         * 设置工具列表
         * 
         * @param tools 工具列表
         * @return 构建器
         */
        public Builder tools(List<String> tools) {
            config.setTools(tools);
            return this;
        }
        
        /**
         * 添加工具
         * 
         * @param tool 工具ID
         * @return 构建器
         */
        public Builder addTool(String tool) {
            config.getTools().add(tool);
            return this;
        }
        
        /**
         * 设置代理名称
         * 
         * @param name 代理名称
         * @return 构建器
         */
        public Builder name(String name) {
            config.setName(name);
            return this;
        }
        
        /**
         * 设置LLM参数
         * 
         * @param parameters LLM参数
         * @return 构建器
         */
        public Builder llmParameters(Map<String, Object> parameters) {
            config.setLlmParameters(parameters);
            return this;
        }
        
        /**
         * 添加LLM参数
         * 
         * @param key 参数键
         * @param value 参数值
         * @return 构建器
         */
        public Builder addLlmParameter(String key, Object value) {
            config.getLlmParameters().put(key, value);
            return this;
        }
        
        /**
         * 构建配置
         * 
         * @return 代理配置
         */
        public DefaultAgentConfig build() {
            return config;
        }
    }
} 
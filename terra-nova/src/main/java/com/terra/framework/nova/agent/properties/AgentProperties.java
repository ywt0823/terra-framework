package com.terra.framework.nova.agent.properties;

import com.terra.framework.nova.agent.AgentType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent代理系统配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.agent")
public class AgentProperties {

    /**
     * 是否启用代理系统
     */
    private boolean enabled = true;

    /**
     * 默认代理类型
     */
    private AgentType defaultType = AgentType.REACT;

    /**
     * 默认模型ID
     */
    private String defaultModelId = "gpt-4";

    /**
     * 最大执行步骤数
     */
    private int maxSteps = 10;

    /**
     * 执行超时时间(毫秒)
     */
    private long timeoutMs = 60000;

    /**
     * 线程池大小
     */
    private int threadPoolSize = 10;

    /**
     * 默认LLM参数
     */
    private Map<String, Object> defaultLlmParameters = new HashMap<>();

    /**
     * 代理扫描基础包路径
     */
    private String[] basePackages = {};


    /**
     * 对话相关配置
     */
    private final Conversation conversation = new Conversation();

    /**
     * ReAct代理配置
     */
    private final React react = new React();

    /**
     * 规划-执行代理配置
     */
    private final PlanAndExecute planAndExecute = new PlanAndExecute();

    /**
     * 对话配置
     */
    @Data
    public static class Conversation {

        /**
         * 是否与会话集成
         */
        private boolean enabled = true;

        /**
         * 是否记录中间步骤到会话
         */
        private boolean recordSteps = false;
    }

    /**
     * ReAct代理配置
     */
    @Data
    public static class React {

        /**
         * 提示词模板ID
         */
        private String promptTemplateId = "agent.react";

        /**
         * 是否启用回溯功能
         */
        private boolean backtrackingEnabled = true;

        /**
         * 回溯深度
         */
        private int backtrackingDepth = 3;
    }

    /**
     * 规划-执行代理配置
     */
    @Data
    public static class PlanAndExecute {

        /**
         * 规划提示词模板ID
         */
        private String plannerPromptTemplateId = "agent.planner";

        /**
         * 执行提示词模板ID
         */
        private String executorPromptTemplateId = "agent.executor";

        /**
         * 最大计划步骤数
         */
        private int maxPlanSteps = 5;
    }
}

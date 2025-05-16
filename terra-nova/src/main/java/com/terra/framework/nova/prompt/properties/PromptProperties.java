package com.terra.framework.nova.prompt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示引擎配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.prompt")
public class PromptProperties {

    /**
     * 是否启用提示引擎
     */
    private boolean enabled = true;

    /**
     * 默认提示策略
     */
    private String defaultStrategy = "simple";

    /**
     * 默认系统提示
     */
    private String defaultSystemPrompt = "你是一个有帮助的AI助手。请提供有用、准确且详细的回答。";

    /**
     * 最大历史消息数
     */
    private int maxHistoryMessages = 10;

    /**
     * 预定义提示模板
     */
    private List<TemplateProperties> templates = new ArrayList<>();

    /**
     * 提示策略配置
     */
    private Map<String, StrategyProperties> strategies = new HashMap<>();

    /**
     * 模板配置属性
     */
    @Data
    public static class TemplateProperties {
        /**
         * 模板名称
         */
        private String name;

        /**
         * 模板描述
         */
        private String description;

        /**
         * 模板内容
         */
        private String template;

        /**
         * 模板类型
         */
        private String type = "completion";
    }

    /**
     * 策略配置属性
     */
    @Data
    public static class StrategyProperties {
        /**
         * 策略名称
         */
        private String name;

        /**
         * 提示模板
         */
        private String promptTemplate;

        /**
         * 系统提示
         */
        private String systemPrompt;

        /**
         * 最大历史消息数
         */
        private int maxHistoryMessages = 5;
    }

    /**
     * 获取默认的模板配置
     */
    public List<TemplateProperties> getDefaultTemplates() {
        List<TemplateProperties> defaultTemplates = new ArrayList<>();

        // 通用问答模板
        TemplateProperties qaTemplate = new TemplateProperties();
        qaTemplate.setName("general-qa");
        qaTemplate.setDescription("通用问答模板");
        qaTemplate.setTemplate("{{query}}");
        qaTemplate.setType("completion");
        defaultTemplates.add(qaTemplate);

        // 摘要生成模板
        TemplateProperties summaryTemplate = new TemplateProperties();
        summaryTemplate.setName("text-summary");
        summaryTemplate.setDescription("文本摘要模板");
        summaryTemplate.setTemplate("请对以下内容生成一个简洁的摘要：\n\n{{text}}");
        summaryTemplate.setType("completion");
        defaultTemplates.add(summaryTemplate);

        // 代码生成模板
        TemplateProperties codeTemplate = new TemplateProperties();
        codeTemplate.setName("code-generation");
        codeTemplate.setDescription("代码生成模板");
        codeTemplate.setTemplate("请使用{{language}}编写代码，实现以下功能：\n\n{{requirement}}");
        codeTemplate.setType("completion");
        defaultTemplates.add(codeTemplate);

        return defaultTemplates;
    }

    /**
     * 获取默认的策略配置
     */
    public Map<String, StrategyProperties> getDefaultStrategies() {
        Map<String, StrategyProperties> defaultStrategies = new HashMap<>();

        // 简单策略
        StrategyProperties simpleStrategy = new StrategyProperties();
        simpleStrategy.setName("simple");
        simpleStrategy.setPromptTemplate("你是一个有帮助的AI助手。请回答以下问题：\n{{query}}");
        simpleStrategy.setSystemPrompt("请提供有用、准确且详细的回答。");
        simpleStrategy.setMaxHistoryMessages(5);
        defaultStrategies.put("simple", simpleStrategy);

        // 详细策略
        StrategyProperties detailedStrategy = new StrategyProperties();
        detailedStrategy.setName("detailed");
        detailedStrategy.setPromptTemplate("你是一个专业的AI助手。请详细分析并回答以下问题：\n{{query}}");
        detailedStrategy.setSystemPrompt("请提供详尽的分析和全面的回答，考虑多个角度，并给出具体的例子。");
        detailedStrategy.setMaxHistoryMessages(10);
        defaultStrategies.put("detailed", detailedStrategy);

        return defaultStrategies;
    }
}

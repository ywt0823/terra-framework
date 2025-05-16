package com.terra.framework.nova.prompt.engine;

import com.terra.framework.nova.prompt.builder.PromptBuilder;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 提示引擎服务默认实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultPromptEngineService implements PromptEngineService {
    
    /**
     * 提示模板存储（按ID索引）
     */
    private final Map<String, PromptTemplate> templateStore;
    
    /**
     * 提示模板名称索引
     */
    private final Map<String, String> templateNameIndex;
    
    /**
     * 构造函数
     */
    public DefaultPromptEngineService() {
        this.templateStore = new ConcurrentHashMap<>();
        this.templateNameIndex = new ConcurrentHashMap<>();
    }
    
    @Override
    public boolean registerTemplate(PromptTemplate template) {
        if (template == null || template.getId() == null) {
            return false;
        }
        
        templateStore.put(template.getId(), template);
        
        if (template.getName() != null && !template.getName().isEmpty()) {
            templateNameIndex.put(template.getName(), template.getId());
        }
        
        log.debug("注册提示模板: {}, {}", template.getId(), template.getName());
        return true;
    }
    
    @Override
    public PromptTemplate getTemplate(String templateId) {
        if (templateId == null) {
            return null;
        }
        return templateStore.get(templateId);
    }
    
    @Override
    public PromptTemplate getTemplateByName(String templateName) {
        if (templateName == null) {
            return null;
        }
        
        String templateId = templateNameIndex.get(templateName);
        if (templateId == null) {
            return null;
        }
        
        return getTemplate(templateId);
    }
    
    @Override
    public String render(String templateId, Map<String, Object> variables) {
        PromptTemplate template = getTemplate(templateId);
        if (template == null) {
            log.warn("模板不存在: {}", templateId);
            return null;
        }
        
        return template.format(variables);
    }
    
    @Override
    public String renderByName(String templateName, Map<String, Object> variables) {
        PromptTemplate template = getTemplateByName(templateName);
        if (template == null) {
            log.warn("模板不存在: {}", templateName);
            return null;
        }
        
        return template.format(variables);
    }
    
    @Override
    public List<PromptTemplate> listTemplates() {
        return new ArrayList<>(templateStore.values());
    }
    
    @Override
    public boolean deleteTemplate(String templateId) {
        if (templateId == null) {
            return false;
        }
        
        PromptTemplate template = templateStore.remove(templateId);
        if (template != null && template.getName() != null) {
            templateNameIndex.remove(template.getName());
            log.debug("删除提示模板: {}, {}", template.getId(), template.getName());
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean updateTemplate(PromptTemplate template) {
        if (template == null || template.getId() == null) {
            return false;
        }
        
        // 检查模板是否存在
        PromptTemplate existingTemplate = templateStore.get(template.getId());
        if (existingTemplate == null) {
            return false;
        }
        
        // 如果名称变更，更新名称索引
        if (existingTemplate.getName() != null && !existingTemplate.getName().equals(template.getName())) {
            templateNameIndex.remove(existingTemplate.getName());
        }
        
        // 更新模板
        templateStore.put(template.getId(), template);
        
        // 添加新名称索引
        if (template.getName() != null && !template.getName().isEmpty()) {
            templateNameIndex.put(template.getName(), template.getId());
        }
        
        log.debug("更新提示模板: {}, {}", template.getId(), template.getName());
        return true;
    }
    
    @Override
    public int registerTemplates(List<PromptTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (PromptTemplate template : templates) {
            if (registerTemplate(template)) {
                successCount++;
            }
        }
        
        log.debug("批量注册提示模板: {}/{}", successCount, templates.size());
        return successCount;
    }
    
    @Override
    public List<Map<String, String>> renderChatMessages(
            String templateId,
            Map<String, Object> variables,
            String systemMessage,
            List<String> userMessages,
            List<String> assistantMessages) {
        
        PromptTemplate template = getTemplate(templateId);
        if (template == null) {
            log.warn("模板不存在: {}", templateId);
            return Collections.emptyList();
        }
        
        PromptBuilder builder = PromptBuilder.from(template)
                .variables(variables)
                .systemMessage(systemMessage);
        
        // 添加历史消息
        if (userMessages != null && assistantMessages != null) {
            int messageCount = Math.min(userMessages.size(), assistantMessages.size());
            for (int i = 0; i < messageCount; i++) {
                builder.addUserMessage(userMessages.get(i))
                       .addAssistantMessage(assistantMessages.get(i));
            }
            
            // 如果用户消息比助手消息多一条（最后一条未回答的消息）
            if (userMessages.size() > messageCount) {
                builder.addUserMessage(userMessages.get(messageCount));
            }
        }
        
        return builder.buildChatMessages();
    }
} 
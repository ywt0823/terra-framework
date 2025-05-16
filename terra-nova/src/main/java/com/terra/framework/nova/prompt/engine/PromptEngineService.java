package com.terra.framework.nova.prompt.engine;

import com.terra.framework.nova.prompt.template.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * 提示引擎服务接口，负责管理和使用提示模板
 *
 * @author terra-nova
 */
public interface PromptEngineService {
    
    /**
     * 注册提示模板
     *
     * @param template 提示模板
     * @return 是否注册成功
     */
    boolean registerTemplate(PromptTemplate template);
    
    /**
     * 根据ID获取提示模板
     *
     * @param templateId 模板ID
     * @return 提示模板
     */
    PromptTemplate getTemplate(String templateId);
    
    /**
     * 根据名称获取提示模板
     *
     * @param templateName 模板名称
     * @return 提示模板
     */
    PromptTemplate getTemplateByName(String templateName);
    
    /**
     * 使用提示模板生成提示
     *
     * @param templateId 模板ID
     * @param variables 变量映射
     * @return 生成的提示字符串
     */
    String render(String templateId, Map<String, Object> variables);
    
    /**
     * 使用提示模板名称生成提示
     *
     * @param templateName 模板名称
     * @param variables 变量映射
     * @return 生成的提示字符串
     */
    String renderByName(String templateName, Map<String, Object> variables);
    
    /**
     * 列出所有提示模板
     *
     * @return 模板列表
     */
    List<PromptTemplate> listTemplates();
    
    /**
     * 删除提示模板
     *
     * @param templateId 模板ID
     * @return 是否删除成功
     */
    boolean deleteTemplate(String templateId);
    
    /**
     * 更新提示模板
     *
     * @param template 提示模板
     * @return 是否更新成功
     */
    boolean updateTemplate(PromptTemplate template);
    
    /**
     * 批量注册提示模板
     *
     * @param templates 提示模板列表
     * @return 成功注册的模板数量
     */
    int registerTemplates(List<PromptTemplate> templates);
    
    /**
     * 渲染聊天消息列表
     *
     * @param templateId 模板ID
     * @param variables 变量映射
     * @param systemMessage 系统消息
     * @param userMessages 用户消息列表
     * @param assistantMessages 助手消息列表
     * @return 聊天消息列表
     */
    List<Map<String, String>> renderChatMessages(
            String templateId,
            Map<String, Object> variables,
            String systemMessage,
            List<String> userMessages,
            List<String> assistantMessages);
} 
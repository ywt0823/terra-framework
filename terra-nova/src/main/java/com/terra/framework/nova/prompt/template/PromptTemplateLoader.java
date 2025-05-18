package com.terra.framework.nova.prompt.template;

/**
 * 提示词模板加载器接口，用于加载模板
 *
 * @author terra-nova
 */
public interface PromptTemplateLoader {

    /**
     * 根据模板ID加载模板
     *
     * @param templateId 模板ID
     * @return 加载的模板
     */
    PromptTemplate load(String templateId);
}

package com.terra.framework.nova.prompt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 提示词配置属性
 *
 * @author terra-nova
 */
@ConfigurationProperties(prefix = "terra.nova.prompt")
public class PromptProperties {

    /**
     * 模板文件路径
     */
    private String templatePath = "classpath:/prompts";

    /**
     * 模板文件扩展名
     */
    private String templateExtension = ".prompt";

    /**
     * 获取模板文件路径
     *
     * @return 模板文件路径
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * 设置模板文件路径
     *
     * @param templatePath 模板文件路径
     */
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * 获取模板文件扩展名
     *
     * @return 模板文件扩展名
     */
    public String getTemplateExtension() {
        return templateExtension;
    }

    /**
     * 设置模板文件扩展名
     *
     * @param templateExtension 模板文件扩展名
     */
    public void setTemplateExtension(String templateExtension) {
        this.templateExtension = templateExtension;
    }
}

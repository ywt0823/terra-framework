package com.terra.framework.nova.model;

/**
 * 模型客户端工厂接口
 *
 * @author terra-nova
 */
public interface ModelClientFactory {

    /**
     * 创建模型客户端
     *
     * @param config 模型配置
     * @return 模型客户端实例
     */
    AIModel createClient(ModelConfig config);
}

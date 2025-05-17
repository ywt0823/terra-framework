package com.terra.framework.nova.model;

import java.util.Map;

/**
 * 请求参数映射策略
 *
 * @author terra-nova
 */
public interface RequestMappingStrategy {

    /**
     * 映射通用参数到特定厂商参数
     *
     * @param genericParams 通用参数
     * @return 厂商特定参数
     */
    Map<String, Object> mapParameters(Map<String, Object> genericParams);
}

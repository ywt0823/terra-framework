package com.terra.framework.nova.function;

import java.util.Map;

/**
 * 函数处理器接口
 *
 * @author terra-nova
 */
public interface FunctionHandler {
    
    /**
     * 处理函数调用
     *
     * @param parameters 函数参数
     * @return 函数执行结果
     */
    Object handle(Map<String, Object> parameters);
} 
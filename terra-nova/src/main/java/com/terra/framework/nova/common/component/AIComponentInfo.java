package com.terra.framework.nova.common.component;

import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.common.annotation.ComponentType;
import com.terra.framework.nova.function.Function;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * AI组件信息
 *
 * @author terra-nova
 */
@Data
@Builder
public class AIComponentInfo {
    
    /**
     * 组件名称
     */
    private String name;
    
    /**
     * 组件描述
     */
    private String description;
    
    /**
     * 组件类型
     */
    private ComponentType[] types;
    
    /**
     * 组件分类
     */
    private String category;
    
    /**
     * 目标Bean
     */
    private Object targetBean;
    
    /**
     * 目标方法
     */
    private Method targetMethod;
    
    /**
     * Tool实现
     */
    private Tool tool;
    
    /**
     * Function实现
     */
    private Function function;
} 
package com.terra.framework.nova.common.component;

import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.common.annotation.ComponentType;
import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI组件管理器，统一管理Tool和Function
 *
 * @author terra-nova
 */
@Slf4j
public class AIComponentManager {

    private final ToolRegistry toolRegistry;
    private final FunctionRegistry functionRegistry;
    private final Map<String, AIComponentInfo> componentInfoMap = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param toolRegistry     工具注册表
     * @param functionRegistry 函数注册表
     */
    public AIComponentManager(ToolRegistry toolRegistry, FunctionRegistry functionRegistry) {
        this.toolRegistry = toolRegistry;
        this.functionRegistry = functionRegistry;
    }

    /**
     * 注册组件
     *
     * @param componentInfo 组件信息
     */
    public void registerComponent(AIComponentInfo componentInfo) {
        String componentName = componentInfo.getName();
        componentInfoMap.put(componentName, componentInfo);

        // 根据类型注册为Tool和/或Function
        for (ComponentType type : componentInfo.getTypes()) {
            if (type == ComponentType.TOOL) {
                Tool tool = componentInfo.getTool();
                if (tool != null) {
                    toolRegistry.registerTool(tool);
                    log.info("Registered component {} as Tool", componentName);
                }
            }
            if (type == ComponentType.FUNCTION) {
                Function function = componentInfo.getFunction();
                if (function != null) {
                    functionRegistry.registerFunction(function);
                    log.info("Registered component {} as Function", componentName);
                }
            }
        }
    }

    /**
     * 获取组件信息
     *
     * @param name 组件名称
     * @return 组件信息
     */
    public AIComponentInfo getComponentInfo(String name) {
        return componentInfoMap.get(name);
    }

    /**
     * 获取所有组件信息
     *
     * @return 所有组件信息
     */
    public Collection<AIComponentInfo> getAllComponentInfos() {
        return componentInfoMap.values();
    }

    /**
     * 移除组件
     *
     * @param name 组件名称
     */
    public void removeComponent(String name) {
        AIComponentInfo info = componentInfoMap.remove(name);
        if (info != null) {
            for (ComponentType type : info.getTypes()) {
                if (type == ComponentType.TOOL) {
                    toolRegistry.removeTool(name);
                } else if (type == ComponentType.FUNCTION) {
                    functionRegistry.removeFunction(name);
                }
            }
            log.info("Removed component: {}", name);
        }
    }

    /**
     * 清除所有组件
     */
    public void clearComponents() {
        componentInfoMap.clear();
        toolRegistry.clearTools();
        functionRegistry.clearFunctions();
        log.info("Cleared all components");
    }
}

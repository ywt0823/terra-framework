package com.terra.framework.nova.agent.tool.scanner;

import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.agent.tool.annotation.AITool;
import com.terra.framework.nova.agent.tool.converter.MethodToToolConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具扫描器
 *
 * @author terra-nova
 */
@Slf4j
public class ToolScanner implements BeanPostProcessor {

    private final ToolRegistry toolRegistry;
    private final String[] basePackages;
    private final MethodToToolConverter toolConverter;
    private final Map<String, Object> registeredMethodTools = new HashMap<>();

    /**
     * 构造函数
     *
     * @param toolRegistry 工具注册表
     * @param basePackages 基础包路径
     */
    public ToolScanner(ToolRegistry toolRegistry, String[] basePackages) {
        this.toolRegistry = toolRegistry;
        this.basePackages = basePackages != null && basePackages.length > 0 ? basePackages : new String[]{"com.terra"};
        this.toolConverter = new MethodToToolConverter();
        log.info("Initialized ToolScanner with base packages: {}", String.join(", ", this.basePackages));
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Tool) {
            processTool(bean, beanName);
        } else if (shouldScanClass(bean.getClass())) {
            scanForAnnotatedMethods(bean, beanName);
        }
        return bean;
    }

    private void processTool(Object bean, String beanName) {
        Tool tool = (Tool) bean;
        toolRegistry.registerTool(tool);
        log.info("Registered tool from bean {}: {}", beanName, tool.getName());
    }

    private void scanForAnnotatedMethods(Object bean, String beanName) {
        Class<?> targetClass = bean.getClass();

        // 获取所有方法（包括继承的公共方法）
        ReflectionUtils.doWithMethods(targetClass, method -> {
            AITool annotation = AnnotationUtils.findAnnotation(method, AITool.class);
            if (annotation != null) {
                processAnnotatedMethod(method, annotation, bean, beanName);
            }
        }, method -> !method.isSynthetic() && !method.isBridge() && Modifier.isPublic(method.getModifiers()));
    }

    private void processAnnotatedMethod(Method method, AITool annotation, Object bean, String beanName) {
        try {
            String methodKey = bean.getClass().getName() + "." + method.getName();

            // 避免重复注册
            if (registeredMethodTools.containsKey(methodKey)) {
                return;
            }

            // 转换为工具并注册
            Tool tool = toolConverter.convert(method, bean);
            toolRegistry.registerTool(tool);
            registeredMethodTools.put(methodKey, bean);

            log.info("Registered @AITool method as tool: {}.{} -> {}",
                bean.getClass().getSimpleName(), method.getName(), tool.getName());
        } catch (Exception e) {
            log.error("Failed to register @AITool method: {}.{}",
                bean.getClass().getSimpleName(), method.getName(), e);
        }
    }

    private boolean shouldScanClass(Class<?> clazz) {
        if (basePackages == null || basePackages.length == 0) {
            return true;
        }

        String className = clazz.getName();
        for (String basePackage : basePackages) {
            if (className.startsWith(basePackage)) {
                return true;
            }
        }
        return false;
    }
}

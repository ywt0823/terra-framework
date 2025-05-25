package com.terra.framework.nova.springai.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring AI 类加载器工具类
 * 
 * <p>提供安全的 Spring AI 类检查和加载功能，避免在 Spring AI 不可用时出现 ClassNotFoundException
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public final class SpringAIClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIClassLoader.class);

    /**
     * Spring AI 核心类列表
     */
    private static final String[] SPRING_AI_CORE_CLASSES = {
        "org.springframework.ai.chat.model.ChatModel",
        "org.springframework.ai.chat.client.ChatClient",
        "org.springframework.ai.vectorstore.VectorStore",
        "org.springframework.ai.embedding.EmbeddingModel"
    };

    /**
     * Spring AI 可用性缓存
     */
    private static volatile Boolean springAIAvailable;

    private SpringAIClassLoader() {
        // 工具类，禁止实例化
    }

    /**
     * 检查 Spring AI 是否在类路径中可用
     * 
     * @return true 如果 Spring AI 可用
     */
    public static boolean isSpringAIAvailable() {
        if (springAIAvailable == null) {
            synchronized (SpringAIClassLoader.class) {
                if (springAIAvailable == null) {
                    springAIAvailable = checkSpringAIAvailability();
                }
            }
        }
        return springAIAvailable;
    }

    /**
     * 检查指定的类是否可用
     * 
     * @param className 类名
     * @return true 如果类可用
     */
    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            logger.debug("Class not found: {}", className);
            return false;
        } catch (Exception e) {
            logger.warn("Error checking class availability: {}", className, e);
            return false;
        }
    }

    /**
     * 安全地加载指定的类
     * 
     * @param className 类名
     * @return 类对象，如果加载失败则返回 null
     */
    public static Class<?> safeLoadClass(String className) {
        try {
            return Class.forName(className, false, getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.debug("Class not found: {}", className);
            return null;
        } catch (Exception e) {
            logger.warn("Error loading class: {}", className, e);
            return null;
        }
    }

    /**
     * 安全地加载指定的类并转换为指定类型
     * 
     * @param className 类名
     * @param expectedType 期望的类型
     * @param <T> 类型参数
     * @return 类对象，如果加载失败或类型不匹配则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> safeLoadClass(String className, Class<T> expectedType) {
        try {
            Class<?> clazz = Class.forName(className, false, getClassLoader());
            if (expectedType.isAssignableFrom(clazz)) {
                return (Class<T>) clazz;
            } else {
                logger.warn("Class {} is not assignable to {}", className, expectedType.getName());
                return null;
            }
        } catch (ClassNotFoundException e) {
            logger.debug("Class not found: {}", className);
            return null;
        } catch (Exception e) {
            logger.warn("Error loading class: {}", className, e);
            return null;
        }
    }

    /**
     * 检查 Spring AI 的具体组件是否可用
     * 
     * @param component 组件名称（如 "openai", "anthropic", "ollama"）
     * @return true 如果组件可用
     */
    public static boolean isSpringAIComponentAvailable(String component) {
        // Spring AI 1.1.0 中的包结构
        String[] componentClasses = {
            "org.springframework.ai.openai.OpenAiChatModel",
            "org.springframework.ai.anthropic.AnthropicChatModel", 
            "org.springframework.ai.ollama.OllamaChatModel",
            "org.springframework.ai." + component.toLowerCase() + "." + capitalize(component) + "ChatModel"
        };
        
        for (String className : componentClasses) {
            if (isClassAvailable(className)) {
                logger.debug("Spring AI component {} is available", component);
                return true;
            }
        }
        
        logger.debug("Spring AI component {} is not available", component);
        return false;
    }

    /**
     * 获取 Spring AI 版本信息
     * 
     * @return 版本字符串，如果无法获取则返回 "unknown"
     */
    public static String getSpringAIVersion() {
        try {
            // 尝试从 Spring AI 的包信息中获取版本
            Package springAIPackage = Package.getPackage("org.springframework.ai");
            if (springAIPackage != null) {
                String version = springAIPackage.getImplementationVersion();
                if (version != null) {
                    return version;
                }
            }
            
            // 如果无法从包信息获取，尝试其他方式
            Class<?> versionClass = safeLoadClass("org.springframework.ai.core.Version");
            if (versionClass != null) {
                // 这里可以添加通过反射获取版本的逻辑
            }
            
            return "unknown";
        } catch (Exception e) {
            logger.debug("Error getting Spring AI version", e);
            return "unknown";
        }
    }

    /**
     * 重置 Spring AI 可用性缓存
     * 
     * <p>主要用于测试场景
     */
    public static void resetAvailabilityCache() {
        springAIAvailable = null;
        logger.debug("Spring AI availability cache reset");
    }

    /**
     * 获取 Spring AI 可用性详细信息
     * 
     * @return 详细信息字符串
     */
    public static String getAvailabilityDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Spring AI Availability Details:\n");
        details.append("Overall Available: ").append(isSpringAIAvailable()).append("\n");
        details.append("Version: ").append(getSpringAIVersion()).append("\n");
        details.append("Core Classes:\n");
        
        for (String className : SPRING_AI_CORE_CLASSES) {
            details.append("  - ").append(className).append(": ")
                   .append(isClassAvailable(className) ? "✓" : "✗").append("\n");
        }
        
        return details.toString();
    }

    /**
     * 检查 Spring AI 的实际可用性
     * 
     * @return true 如果 Spring AI 可用
     */
    private static boolean checkSpringAIAvailability() {
        try {
            // 检查核心类是否都可用
            for (String className : SPRING_AI_CORE_CLASSES) {
                if (!isClassAvailable(className)) {
                    logger.debug("Spring AI core class not available: {}", className);
                    return false;
                }
            }
            
            logger.info("Spring AI is available in classpath");
            return true;
        } catch (Exception e) {
            logger.warn("Error checking Spring AI availability", e);
            return false;
        }
    }

    /**
     * 获取类加载器
     * 
     * @return 类加载器
     */
    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SpringAIClassLoader.class.getClassLoader();
        }
        return classLoader;
    }

    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
} 
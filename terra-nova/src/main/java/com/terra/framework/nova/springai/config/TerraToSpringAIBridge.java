package com.terra.framework.nova.springai.config;

import com.terra.framework.nova.manager.TerraModelManager;
import com.terra.framework.nova.springai.util.SpringAIClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Terra Framework 到 Spring AI 的桥接器
 * 
 * <p>负责将 Terra 的模型抽象转换为 Spring AI 的具体实现，提供：
 * <ul>
 *   <li>模型适配和转换</li>
 *   <li>Spring AI 组件的动态获取</li>
 *   <li>配置的自动同步</li>
 *   <li>运行时的兼容性检查</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public class TerraToSpringAIBridge {

    private static final Logger logger = LoggerFactory.getLogger(TerraToSpringAIBridge.class);

    private final TerraModelManager modelManager;
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    public TerraToSpringAIBridge(TerraModelManager modelManager) {
        this.modelManager = modelManager;
        logger.info("Terra to Spring AI Bridge initialized");
    }

    /**
     * 获取默认的 ChatModel
     * 
     * @return ChatModel 实例，如果不可用则返回 null
     */
    public ChatModel getDefaultChatModel() {
        if (!SpringAIClassLoader.isSpringAIAvailable()) {
            logger.warn("Spring AI is not available in classpath");
            return null;
        }

        try {
            if (applicationContext != null) {
                Map<String, ChatModel> chatModels = applicationContext.getBeansOfType(ChatModel.class);
                if (!chatModels.isEmpty()) {
                    ChatModel defaultModel = chatModels.values().iterator().next();
                    logger.debug("Found default ChatModel: {}", defaultModel.getClass().getSimpleName());
                    return defaultModel;
                }
            }
            logger.warn("No ChatModel beans found in application context");
            return null;
        } catch (Exception e) {
            logger.error("Error getting default ChatModel", e);
            return null;
        }
    }

    /**
     * 根据名称获取 ChatModel
     * 
     * @param modelName 模型名称
     * @return ChatModel 实例
     */
    public Optional<ChatModel> getChatModel(String modelName) {
        if (!SpringAIClassLoader.isSpringAIAvailable()) {
            return Optional.empty();
        }

        return Optional.ofNullable(chatModelCache.computeIfAbsent(modelName, this::loadChatModel));
    }

    /**
     * 获取默认的 ChatClient
     * 
     * @return ChatClient 实例
     */
    public ChatClient getDefaultChatClient() {
        if (!SpringAIClassLoader.isSpringAIAvailable()) {
            logger.warn("Spring AI is not available in classpath");
            return null;
        }

        try {
            ChatModel defaultChatModel = getDefaultChatModel();
            if (defaultChatModel != null) {
                return ChatClient.builder(defaultChatModel).build();
            }
            logger.warn("Cannot create ChatClient: no default ChatModel available");
            return null;
        } catch (Exception e) {
            logger.error("Error creating default ChatClient", e);
            return null;
        }
    }

    /**
     * 根据模型名称获取 ChatClient
     * 
     * @param modelName 模型名称
     * @return ChatClient 实例
     */
    public Optional<ChatClient> getChatClient(String modelName) {
        if (!SpringAIClassLoader.isSpringAIAvailable()) {
            return Optional.empty();
        }

        return Optional.ofNullable(chatClientCache.computeIfAbsent(modelName, this::loadChatClient));
    }

    /**
     * 检查 Spring AI 集成是否可用
     * 
     * @return true 如果可用
     */
    public boolean isSpringAIAvailable() {
        return SpringAIClassLoader.isSpringAIAvailable();
    }

    /**
     * 获取集成状态信息
     * 
     * @return 状态信息映射
     */
    public Map<String, Object> getIntegrationStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("enabled", true);
        status.put("available", isSpringAIAvailable());
        status.put("chatModelCount", chatModelCache.size());
        status.put("chatClientCount", chatClientCache.size());
        
        if (applicationContext != null) {
            Map<String, ChatModel> chatModels = applicationContext.getBeansOfType(ChatModel.class);
            status.put("availableChatModels", chatModels.keySet());
        }
        
        return status;
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        chatModelCache.clear();
        chatClientCache.clear();
        logger.info("Terra to Spring AI Bridge cache cleared");
    }

    /**
     * 刷新模型配置
     */
    public void refreshModels() {
        clearCache();
        // 重新加载模型配置
        if (modelManager != null) {
            // 这里可以添加模型重新加载的逻辑
            logger.info("Model configuration refreshed");
        }
    }

    /**
     * 加载指定名称的 ChatModel
     * 
     * @param modelName 模型名称
     * @return ChatModel 实例，如果加载失败则返回 null
     */
    private ChatModel loadChatModel(String modelName) {
        try {
            if (applicationContext != null) {
                Map<String, ChatModel> chatModels = applicationContext.getBeansOfType(ChatModel.class);
                
                // 首先尝试按 bean 名称查找
                ChatModel model = chatModels.get(modelName);
                if (model != null) {
                    logger.debug("Found ChatModel by bean name: {}", modelName);
                    return model;
                }
                
                // 如果没找到，尝试按模型名称匹配
                for (ChatModel chatModel : chatModels.values()) {
                    // 这里可以添加更复杂的模型名称匹配逻辑
                    if (isModelNameMatch(chatModel, modelName)) {
                        logger.debug("Found ChatModel by name matching: {}", modelName);
                        return chatModel;
                    }
                }
            }
            
            logger.warn("ChatModel not found: {}", modelName);
            return null;
        } catch (Exception e) {
            logger.error("Error loading ChatModel: {}", modelName, e);
            return null;
        }
    }

    /**
     * 加载指定名称的 ChatClient
     * 
     * @param modelName 模型名称
     * @return ChatClient 实例，如果加载失败则返回 null
     */
    private ChatClient loadChatClient(String modelName) {
        try {
            Optional<ChatModel> chatModel = getChatModel(modelName);
            if (chatModel.isPresent()) {
                ChatClient client = ChatClient.builder(chatModel.get()).build();
                logger.debug("Created ChatClient for model: {}", modelName);
                return client;
            }
            
            logger.warn("Cannot create ChatClient: ChatModel not found for {}", modelName);
            return null;
        } catch (Exception e) {
            logger.error("Error creating ChatClient for model: {}", modelName, e);
            return null;
        }
    }

    /**
     * 检查模型名称是否匹配
     * 
     * @param chatModel ChatModel 实例
     * @param modelName 要匹配的模型名称
     * @return true 如果匹配
     */
    private boolean isModelNameMatch(ChatModel chatModel, String modelName) {
        // 这里可以实现更复杂的模型名称匹配逻辑
        // 例如检查模型的配置信息、元数据等
        
        // 简单的类名匹配
        String className = chatModel.getClass().getSimpleName().toLowerCase();
        String targetName = modelName.toLowerCase();
        
        return className.contains(targetName) || targetName.contains(className);
    }

    /**
     * 设置应用上下文（用于测试）
     * 
     * @param applicationContext Spring 应用上下文
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
} 
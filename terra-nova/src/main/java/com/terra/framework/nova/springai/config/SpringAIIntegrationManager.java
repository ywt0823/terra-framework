package com.terra.framework.nova.springai.config;

import com.terra.framework.nova.properties.TerraNovaProperties;
import com.terra.framework.nova.springai.util.SpringAIClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spring AI 集成管理器
 * 
 * <p>负责管理 Terra Framework 与 Spring AI 的集成状态，提供：
 * <ul>
 *   <li>集成状态监控</li>
 *   <li>配置验证</li>
 *   <li>健康检查</li>
 *   <li>诊断信息</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public class SpringAIIntegrationManager implements InitializingBean, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIIntegrationManager.class);

    private final TerraNovaProperties properties;
    private final TerraToSpringAIBridge bridge;
    private ApplicationContext applicationContext;

    private volatile boolean integrationInitialized = false;
    private volatile boolean integrationHealthy = false;
    private final AtomicBoolean initializing = new AtomicBoolean(false);

    public SpringAIIntegrationManager(TerraNovaProperties properties, TerraToSpringAIBridge bridge) {
        this.properties = properties;
        this.bridge = bridge;
        logger.info("SpringAI Integration Manager created");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.bridge.setApplicationContext(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (properties.getSpringAi().isEnabled()) {
            initializeIntegration();
        } else {
            logger.info("Spring AI integration is disabled");
        }
    }

    /**
     * 初始化集成
     */
    public void initializeIntegration() {
        if (!initializing.compareAndSet(false, true)) {
            logger.debug("Integration initialization already in progress");
            return;
        }

        try {
            logger.info("Initializing Spring AI integration...");

            // 1. 检查 Spring AI 可用性
            if (!SpringAIClassLoader.isSpringAIAvailable()) {
                logger.warn("Spring AI is not available in classpath");
                integrationHealthy = false;
                return;
            }

            // 2. 验证配置
            validateConfiguration();

            // 3. 检查集成健康状态
            checkIntegrationHealth();

            // 4. 标记初始化完成
            integrationInitialized = true;
            logger.info("Spring AI integration initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize Spring AI integration", e);
            integrationHealthy = false;
            throw new RuntimeException("Spring AI integration initialization failed", e);
        } finally {
            initializing.set(false);
        }
    }

    /**
     * 检查集成是否可用
     * 
     * @return true 如果集成可用
     */
    public boolean isIntegrationAvailable() {
        return integrationInitialized && 
               SpringAIClassLoader.isSpringAIAvailable() && 
               properties.getSpringAi().isEnabled();
    }

    /**
     * 检查集成是否健康
     * 
     * @return true 如果集成健康
     */
    public boolean isIntegrationHealthy() {
        return isIntegrationAvailable() && integrationHealthy;
    }

    /**
     * 获取集成状态信息
     * 
     * @return 状态信息映射
     */
    public Map<String, Object> getIntegrationStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 基本状态
        status.put("enabled", properties.getSpringAi().isEnabled());
        status.put("initialized", integrationInitialized);
        status.put("healthy", integrationHealthy);
        status.put("available", isIntegrationAvailable());
        
        // Spring AI 信息
        status.put("springAI", Map.of(
            "available", SpringAIClassLoader.isSpringAIAvailable(),
            "version", SpringAIClassLoader.getSpringAIVersion()
        ));
        
        // 桥接器状态
        if (bridge != null) {
            status.put("bridge", bridge.getIntegrationStatus());
        }
        
        // 配置信息
        status.put("configuration", Map.of(
            "autoRegisterModels", properties.getSpringAi().isAutoRegisterModels(),
            "defaultProvider", properties.getModels().getDefaultProvider(),
            "providersCount", properties.getModels().getProviders().size()
        ));
        
        // 应用上下文信息
        if (applicationContext != null) {
            status.put("applicationContext", Map.of(
                "id", applicationContext.getId(),
                "displayName", applicationContext.getDisplayName(),
                "startupDate", applicationContext.getStartupDate()
            ));
        }
        
        return status;
    }

    /**
     * 刷新集成状态
     */
    public void refreshIntegration() {
        logger.info("Refreshing Spring AI integration...");
        
        try {
            // 重置状态
            integrationInitialized = false;
            integrationHealthy = false;
            
            // 清除桥接器缓存
            if (bridge != null) {
                bridge.clearCache();
            }
            
            // 重新初始化
            if (properties.getSpringAi().isEnabled()) {
                initializeIntegration();
            }
            
            logger.info("Spring AI integration refreshed successfully");
        } catch (Exception e) {
            logger.error("Failed to refresh Spring AI integration", e);
            throw new RuntimeException("Integration refresh failed", e);
        }
    }

    /**
     * 获取诊断信息
     * 
     * @return 诊断信息字符串
     */
    public String getDiagnosticInfo() {
        StringBuilder diagnostic = new StringBuilder();
        diagnostic.append("=== Terra-Nova Spring AI Integration Diagnostic ===\n");
        
        // 基本信息
        diagnostic.append("Basic Information:\n");
        diagnostic.append("  - Enabled: ").append(properties.getSpringAi().isEnabled()).append("\n");
        diagnostic.append("  - Initialized: ").append(integrationInitialized).append("\n");
        diagnostic.append("  - Healthy: ").append(integrationHealthy).append("\n");
        diagnostic.append("  - Available: ").append(isIntegrationAvailable()).append("\n");
        
        // Spring AI 详细信息
        diagnostic.append("\nSpring AI Details:\n");
        diagnostic.append(SpringAIClassLoader.getAvailabilityDetails());
        
        // 配置信息
        diagnostic.append("\nConfiguration:\n");
        diagnostic.append("  - Auto Register Models: ").append(properties.getSpringAi().isAutoRegisterModels()).append("\n");
        diagnostic.append("  - Default Provider: ").append(properties.getModels().getDefaultProvider()).append("\n");
        diagnostic.append("  - Configured Providers: ").append(properties.getModels().getProviders().keySet()).append("\n");
        
        // 桥接器信息
        if (bridge != null) {
            diagnostic.append("\nBridge Status:\n");
            Map<String, Object> bridgeStatus = bridge.getIntegrationStatus();
            bridgeStatus.forEach((key, value) -> 
                diagnostic.append("  - ").append(key).append(": ").append(value).append("\n"));
        }
        
        // 应用上下文信息
        if (applicationContext != null) {
            diagnostic.append("\nApplication Context:\n");
            diagnostic.append("  - ID: ").append(applicationContext.getId()).append("\n");
            diagnostic.append("  - Display Name: ").append(applicationContext.getDisplayName()).append("\n");
            diagnostic.append("  - Startup Date: ").append(applicationContext.getStartupDate()).append("\n");
        }
        
        return diagnostic.toString();
    }

    /**
     * 验证配置
     */
    private void validateConfiguration() {
        logger.debug("Validating Spring AI integration configuration...");
        
        // 检查基本配置
        if (properties == null) {
            throw new IllegalStateException("TerraNovaProperties is null");
        }
        
        if (properties.getSpringAi() == null) {
            throw new IllegalStateException("Spring AI configuration is null");
        }
        
        // 检查模型配置
        if (properties.getModels() == null) {
            throw new IllegalStateException("Models configuration is null");
        }
        
        String defaultProvider = properties.getModels().getDefaultProvider();
        if (defaultProvider == null || defaultProvider.trim().isEmpty()) {
            throw new IllegalStateException("Default provider is not configured");
        }
        
        // 检查默认提供商是否已配置
        if (!properties.getModels().getProviders().containsKey(defaultProvider)) {
            logger.warn("Default provider '{}' is not configured in providers", defaultProvider);
        }
        
        logger.debug("Configuration validation completed successfully");
    }

    /**
     * 检查集成健康状态
     */
    private void checkIntegrationHealth() {
        logger.debug("Checking Spring AI integration health...");
        
        try {
            // 检查桥接器
            if (bridge == null) {
                logger.warn("TerraToSpringAIBridge is null");
                integrationHealthy = false;
                return;
            }
            
            // 检查 Spring AI 可用性
            if (!bridge.isSpringAIAvailable()) {
                logger.warn("Spring AI is not available through bridge");
                integrationHealthy = false;
                return;
            }
            
            // 尝试获取默认 ChatModel
            try {
                bridge.getDefaultChatModel();
                logger.debug("Default ChatModel is accessible");
            } catch (Exception e) {
                logger.warn("Failed to access default ChatModel: {}", e.getMessage());
                // 这不是致命错误，可能只是没有配置模型
            }
            
            // 如果所有检查都通过，标记为健康
            integrationHealthy = true;
            logger.debug("Integration health check completed successfully");
            
        } catch (Exception e) {
            logger.error("Integration health check failed", e);
            integrationHealthy = false;
        }
    }

    // Getters
    public TerraNovaProperties getProperties() {
        return properties;
    }

    public TerraToSpringAIBridge getBridge() {
        return bridge;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
} 
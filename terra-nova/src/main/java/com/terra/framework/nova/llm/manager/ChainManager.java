package com.terra.framework.nova.llm.manager;

import com.terra.framework.nova.llm.chain.Chain;
import com.terra.framework.nova.llm.chain.ConversationChain;
import com.terra.framework.nova.llm.chain.SQLChain;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain管理器
 */
@Slf4j
public class ChainManager {

    private final ModelManager modelManager;
    private final Map<String, Chain<?, ?>> chainCache = new ConcurrentHashMap<>();
    private final Map<String, ChainConfig> configCache = new ConcurrentHashMap<>();

    public ChainManager(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    /**
     * 获取Chain实例
     *
     * @param chainId Chain ID
     * @return Chain实例
     */
    @SuppressWarnings("unchecked")
    public <I, O> Chain<I, O> getChain(String chainId) {
        return (Chain<I, O>) chainCache.computeIfAbsent(chainId, this::createChain);
    }

    /**
     * 注册Chain配置
     *
     * @param chainId Chain ID
     * @param config Chain配置
     */
    public void registerConfig(String chainId, ChainConfig config) {
        configCache.put(chainId, config);
    }

    /**
     * 创建Chain实例
     *
     * @param chainId Chain ID
     * @return Chain实例
     */
    private Chain<?, ?> createChain(String chainId) {
        ChainConfig config = loadConfig(chainId);
        Chain<?, ?> chain = createChainInstance(config);
        chain.init(modelManager.getModel(config.getModelId()));
        return chain;
    }

    /**
     * 根据配置创建Chain实例
     *
     * @param config Chain配置
     * @return Chain实例
     */
    private Chain<?, ?> createChainInstance(ChainConfig config) {
        return switch (config.getType()) {
            case SQL -> new SQLChain(
                    config.getParam("databaseUrl"),
                    config.getParam("username"),
                    config.getParam("password")
            );
            case CONVERSATION -> new ConversationChain(
                    Integer.parseInt(config.getParam("maxHistory", "10"))
            );
            default -> throw new IllegalStateException("Unsupported chain type: " + config.getType());
        };
    }

    /**
     * 加载Chain配置
     *
     * @param chainId Chain ID
     * @return Chain配置
     */
    private ChainConfig loadConfig(String chainId) {
        ChainConfig config = configCache.get(chainId);
        if (config == null) {
            throw new IllegalStateException("Chain config not found for chainId: " + chainId);
        }
        return config;
    }

    /**
     * 关闭所有Chain
     */
    public void shutdown() {
        log.info("Shutting down all chains");
        chainCache.values().forEach(Chain::close);
        chainCache.clear();
    }
} 
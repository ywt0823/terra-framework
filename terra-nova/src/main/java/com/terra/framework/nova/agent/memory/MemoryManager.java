package com.terra.framework.nova.agent.memory;

import java.util.Map;
import java.util.Optional;

/**
 * 记忆管理器接口
 *
 * @author terra-nova
 */
public interface MemoryManager {
    
    /**
     * 添加记忆
     *
     * @param key 记忆键
     * @param value 记忆值
     */
    void add(String key, Object value);
    
    /**
     * 获取记忆
     *
     * @param key 记忆键
     * @return 记忆值
     */
    Optional<Object> get(String key);
    
    /**
     * 清除记忆
     */
    void clear();
    
    /**
     * 获取所有记忆
     *
     * @return 所有记忆
     */
    Map<String, Object> getAll();
} 
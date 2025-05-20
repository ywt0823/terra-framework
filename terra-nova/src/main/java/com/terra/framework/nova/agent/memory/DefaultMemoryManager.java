package com.terra.framework.nova.agent.memory;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认记忆管理器实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultMemoryManager implements MemoryManager {
    
    private final Map<String, Object> memory = new ConcurrentHashMap<>();
    
    @Override
    public void add(String key, Object value) {
        memory.put(key, value);
        log.debug("Added memory: {} -> {}", key, value);
    }
    
    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(memory.get(key));
    }
    
    @Override
    public void clear() {
        memory.clear();
        log.debug("Cleared all memories");
    }
    
    @Override
    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(memory);
    }
} 
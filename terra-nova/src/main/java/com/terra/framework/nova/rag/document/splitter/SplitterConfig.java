package com.terra.framework.nova.rag.document.splitter;

/**
 * 分割器配置接口
 * 为不同的分割器提供统一的配置方式
 *
 * @author Terra Framework Team
 * @date 2025年6月15日
 */
public interface SplitterConfig {
    
    /**
     * 获取块大小
     *
     * @return 块大小
     */
    int getChunkSize();
    
    /**
     * 获取重叠大小
     *
     * @return 重叠大小
     */
    int getOverlap();
} 
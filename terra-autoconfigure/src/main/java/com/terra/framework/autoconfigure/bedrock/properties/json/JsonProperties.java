package com.terra.framework.autoconfigure.bedrock.properties.json;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JSON工具配置属性类
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
@ConfigurationProperties(prefix = "terra.json")
public class JsonProperties {

    /**
     * 日期格式
     */
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 是否忽略未知属性
     */
    private Boolean ignoreUnknownProperties = true;

    /**
     * 序列化时是否包含null值
     */
    private Boolean includeNullValues = false;

    /**
     * 是否启用缓存
     */
    private Boolean enableCache = true;

    /**
     * 缓存大小
     */
    private Integer cacheSize = 16;
}

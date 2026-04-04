package com.terra.framework.autoconfigure.bedrock.properties.snowflake;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yangwt
 * @date 2023/9/2 11:26
 **/
@Data
@ConfigurationProperties(prefix = "terra.snowflake")
public class SnowflakeProperties {

    /**
     * 是否开启 Terra 雪花算法（预留字段，当前自动配置未读取）
     */
    private Boolean enabled = true;

    /**
     * 最大并发生成序数
     */
    private Long maxSequence = 10000L;

}

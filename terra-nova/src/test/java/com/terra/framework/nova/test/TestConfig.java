package com.terra.framework.nova.test;

import com.terra.framework.bedrock.annoation.TerraBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@TerraBootApplication
@PropertySource(value = "classpath:application.yml", ignoreResourceNotFound = true)
public class TestConfig {

    public static void main(String[] args) {
        SpringApplication.run(TestConfig.class, args);
    }
    // 测试配置类
}

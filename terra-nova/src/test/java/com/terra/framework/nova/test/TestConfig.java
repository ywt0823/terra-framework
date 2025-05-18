package com.terra.framework.nova.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.terra.framework.nova")
@SpringBootApplication
public class TestConfig {

    public static void main(String[] args) {
        SpringApplication.run(TestConfig.class, args);
    }
    // 测试配置类
}

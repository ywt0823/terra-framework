package com.terra.framework.nova.function.config;

import com.terra.framework.nova.function.FunctionRegistry;
import com.terra.framework.nova.function.converter.MethodToFunctionConverter;
import com.terra.framework.nova.function.properties.AIFunctionProperties;
import com.terra.framework.nova.function.scanner.FunctionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for AI function support.
 *
 * @author terra
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AIFunctionProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.function", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AIFunctionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MethodToFunctionConverter methodToFunctionConverter(AIFunctionProperties properties) {
        return new MethodToFunctionConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionScanner functionScanner(FunctionRegistry functionRegistry, 
                                         MethodToFunctionConverter converter,
                                         AIFunctionProperties properties) {
        FunctionScanner scanner = new FunctionScanner(functionRegistry, converter);
        if (properties.getBasePackages().length > 0) {
            log.info("Configuring base packages for AI function scanning: {}", 
                String.join(", ", properties.getBasePackages()));
        }
        return scanner;
    }
} 
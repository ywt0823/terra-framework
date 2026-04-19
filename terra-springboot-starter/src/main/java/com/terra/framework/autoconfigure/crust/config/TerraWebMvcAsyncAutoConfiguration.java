package com.terra.framework.autoconfigure.crust.config;

import com.terra.framework.autoconfigure.crust.properties.TerraWebMvcAsyncProperties;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * MVC 异步请求默认 {@link org.springframework.core.task.SimpleAsyncTaskExecutor} 高负载告警时，
 * 改用虚拟线程执行器并设置超时。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureAfter(TerraWebAutoConfiguration.class)
@EnableConfigurationProperties(TerraWebMvcAsyncProperties.class)
@ConditionalOnProperty(prefix = "terra.web.mvc-async", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TerraWebMvcAsyncAutoConfiguration {

    @Bean(name = "mvcAsyncTaskExecutor")
    @ConditionalOnMissingBean(name = "mvcAsyncTaskExecutor")
    public AsyncTaskExecutor mvcAsyncTaskExecutor(TerraWebMvcAsyncProperties props) {
        return new VirtualThreadTaskExecutor(props.getThreadNamePrefix());
    }

    @Bean
    @ConditionalOnMissingBean(name = "terraMvcAsyncSupportConfigurer")
    public WebMvcConfigurer terraMvcAsyncSupportConfigurer(
            @Qualifier("mvcAsyncTaskExecutor") AsyncTaskExecutor mvcAsyncTaskExecutor,
            TerraWebMvcAsyncProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setTaskExecutor(mvcAsyncTaskExecutor);
                configurer.setDefaultTimeout(props.getTimeoutMs());
            }
        };
    }
}

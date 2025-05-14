package com.terra.framework.crust.config;

import com.terra.framework.bedrock.config.httpclient.HttpClientAutoConfiguration;
import com.terra.framework.bedrock.config.log.ValhallaLogAutoConfiguration;
import com.terra.framework.bedrock.properties.httpclient.HttpclientConnectProperties;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.crust.customizer.HeaderCustomizer;
import com.terra.framework.crust.filter.TerraLoggingFilter;
import com.terra.framework.crust.filter.TerraTraceFilter;
import com.terra.framework.crust.interceptor.RequestHandlerInterceptor;
import com.terra.framework.crust.interceptor.TraceIdRequestInterceptor;
import com.terra.framework.crust.interceptor.TraceIdRestTemplateCustomizer;
import com.terra.framework.crust.properties.ValhallaLoggingProperties;
import com.terra.framework.crust.properties.ValhallaWebContextExcludeProperties;
import jakarta.servlet.Servlet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, CloseableHttpClient.class})
@AutoConfigureAfter({ValhallaLogAutoConfiguration.class, HttpClientAutoConfiguration.class, JacksonAutoConfiguration.class
})
@EnableConfigurationProperties({ValhallaLoggingProperties.class, HttpclientConnectProperties.class, ValhallaWebContextExcludeProperties.class})
public class TerraWebAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public ClientHttpRequestFactory clientHttpRequestFactory(CloseableHttpClient httpClient, HttpclientConnectProperties httpClientPoolConfig) {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        clientHttpRequestFactory.setConnectTimeout(httpClientPoolConfig.getConnectTimeout());
        clientHttpRequestFactory.setConnectionRequestTimeout(httpClientPoolConfig.getConnectionRequestTimeout());
        return clientHttpRequestFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = restTemplateBuilder
                .requestFactory(() -> clientHttpRequestFactory)
                .build();
        restTemplate.getMessageConverters().forEach(v -> {
            if (v instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) v).setDefaultCharset(StandardCharsets.UTF_8);
            }
            if (v instanceof AbstractJackson2HttpMessageConverter converter) {
                List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
                mediaTypes.add(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
                converter.setSupportedMediaTypes(mediaTypes);
            }
        });
        reorderXmlConvertersToEnd(restTemplate.getMessageConverters());
        return restTemplate;
    }

    @Bean
    public WebMvcConfigurer ariesWebMvcConfigurer(ObjectProvider<HeaderCustomizer> headerCustomizers, ValhallaWebContextExcludeProperties valhallaWebContextExcludeProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                List<HttpMessageConverter<?>> defaultMessageConverters = (new WebMvcConfigurationSupport() {
                    public List<HttpMessageConverter<?>> defaultMessageConverters() {
                        return super.getMessageConverters();
                    }
                }).defaultMessageConverters();
                reorderXmlConvertersToEnd(defaultMessageConverters);
                converters.addAll(defaultMessageConverters);
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                //添加映射路径
                registry.addMapping("/**")
                        //是否发送Cookie
                        .allowCredentials(true)
                        //设置放行哪些原始域
                        .allowedOriginPatterns("*")
                        //放行哪些请求方式
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        //.allowedMethods("*")
                        //放行哪些原始请求头部信息
                        .allowedHeaders("*")
                        //暴露哪些原始请求头部信息
                        .exposedHeaders("*");
            }

            /**
             * Header相关解析拦截器
             */
            @Override
            public void addInterceptors(InterceptorRegistry registry) {

                if (!valhallaWebContextExcludeProperties.getEnabled()) {
                    return;
                }

                RequestHandlerInterceptor requestHandlerInterceptor = new RequestHandlerInterceptor();
                // 自定义的HeaderCustomizer
                requestHandlerInterceptor.setHeaderCustomizers(headerCustomizers.orderedStream().collect(
                        Collectors.toList()));
                // excludes url
                requestHandlerInterceptor.setExcludes(valhallaWebContextExcludeProperties.getExcludes().split(","));
                registry.addInterceptor(requestHandlerInterceptor).addPathPatterns("/**");
            }

        };
    }


    @Bean
    public TraceIdRequestInterceptor traceIdRequestInterceptor() {
        return new TraceIdRequestInterceptor();
    }

    /**
     * RestTemplate TraceIdRequestInterceptor
     */
    @Bean
    public TraceIdRestTemplateCustomizer traceIdRestTemplateCustomizer(
            TraceIdRequestInterceptor traceIdRequestInterceptor) {
        return new TraceIdRestTemplateCustomizer(traceIdRequestInterceptor);
    }

    /**
     * Rest请求 日志拦截器
     */
    @Bean
    @ConditionalOnProperty(value = "valhalla.web.logging.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TerraLoggingFilter> filterRegistrationBean(
            ValhallaLoggingProperties valhallaLoggingProperties,
            LogPattern logPattern) {
        FilterRegistrationBean<TerraLoggingFilter> registration = new FilterRegistrationBean<>();

        TerraLoggingFilter terraLoggingFilter = new TerraLoggingFilter();
        terraLoggingFilter.setLogPattern(logPattern);
        terraLoggingFilter.setPathMatcher(new AntPathMatcher());
        terraLoggingFilter.setExcludes(valhallaLoggingProperties.getExcludeUrls());
        registration.setFilter(terraLoggingFilter);

        registration.addUrlPatterns("/*");
        registration.setName("terraLoggingFilter");
        registration.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 999);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TerraTraceFilter> traceFilterRegistrationBean() {

        FilterRegistrationBean<TerraTraceFilter> registration = new FilterRegistrationBean<>();

        TerraTraceFilter terraTraceFilter = new TerraTraceFilter();
        registration.setFilter(terraTraceFilter);
        registration.addUrlPatterns("/*");
        registration.setName("terraTraceFilter");

        registration.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 1001);
        return registration;
    }

    private void reorderXmlConvertersToEnd(List<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> xml = new ArrayList<>();
        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();

        while (true) {
            HttpMessageConverter<?> converter;
            do {
                if (!iterator.hasNext()) {
                    converters.addAll(xml);
                    return;
                }

                converter = iterator.next();
            } while (!(converter instanceof AbstractXmlHttpMessageConverter) && !(converter instanceof MappingJackson2XmlHttpMessageConverter));

            xml.add(converter);
            iterator.remove();
        }
    }

}
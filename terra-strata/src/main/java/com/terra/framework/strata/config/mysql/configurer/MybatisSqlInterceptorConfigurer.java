package com.terra.framework.strata.config.mysql.configurer;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.terra.framework.strata.config.mysql.interceptor.SqlMonitorInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * MybatisPlus拦截器配置
 * 将SQL监控拦截器加入MybatisPlus的拦截器链中
 */
@Slf4j
@RequiredArgsConstructor
public class MybatisSqlInterceptorConfigurer implements BeanPostProcessor {

    private final SqlMonitorInterceptor sqlMonitorInterceptor;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory) {
            SqlSessionFactory factory = (SqlSessionFactory) bean;
            MybatisConfiguration configuration = (MybatisConfiguration) factory.getConfiguration();
            configuration.addInterceptor(sqlMonitorInterceptor);
            log.info("向MybatisPlus拦截器链添加SQL监控拦截器");
        } else if (bean instanceof MybatisSqlSessionFactoryBean) {
            MybatisSqlSessionFactoryBean factoryBean = (MybatisSqlSessionFactoryBean) bean;
            try {
                // 添加拦截器到工厂Bean
                factoryBean.getObject().getConfiguration().addInterceptor(sqlMonitorInterceptor);
                log.info("向MybatisSqlSessionFactoryBean添加SQL监控拦截器");
            } catch (Exception e) {
                log.error("向MybatisSqlSessionFactoryBean添加SQL监控拦截器失败", e);
            }
        }
        return bean;
    }
} 
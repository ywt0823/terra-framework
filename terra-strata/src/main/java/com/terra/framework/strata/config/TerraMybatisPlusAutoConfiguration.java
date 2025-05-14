package com.terra.framework.strata.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.google.common.collect.Lists;
import com.terra.framework.strata.exception.ValhallaDataVersionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import static com.baomidou.mybatisplus.annotation.DbType.MYSQL;

/**
 * @author ywt
 * @description
 * @date 2022年11月27日 18:30
 */
@ConditionalOnClass({MybatisPlusProperties.class})
public class TerraMybatisPlusAutoConfiguration {


    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(MYSQL);
        paginationInnerInterceptor.setMaxLimit(-1L);
        paginationInnerInterceptor.setOptimizeJoin(true);
        return paginationInnerInterceptor;
    }

    @Bean
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor = new OptimisticLockerInnerInterceptor();
        optimisticLockerInnerInterceptor.setException(new ValhallaDataVersionException());
        return optimisticLockerInnerInterceptor;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.setInterceptors(Lists.newArrayList(paginationInnerInterceptor(), optimisticLockerInnerInterceptor()));
        return mybatisPlusInterceptor;
    }
}

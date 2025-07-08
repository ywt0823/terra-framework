package com.terra.framework.autoconfigure.strata.mysql.config;

import com.terra.framework.autoconfigure.strata.mysql.registrar.DynamicDataSourceRegistrar;
import com.terra.framework.autoconfigure.strata.mysql.scanner.TerraMapperScannerConfigurer;
import com.terra.framework.autoconfigure.strata.mysql.condition.OnDynamicDataSourceCondition;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@Conditional(OnDynamicDataSourceCondition.class)
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    MybatisAutoConfiguration.class
})
@Import({DynamicDataSourceRegistrar.class, TerraMapperScannerConfigurer.class})
public class TerraDynamicMybatisAutoConfiguration {
}

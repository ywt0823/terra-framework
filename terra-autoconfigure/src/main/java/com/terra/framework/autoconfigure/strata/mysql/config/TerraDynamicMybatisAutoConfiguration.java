package com.terra.framework.autoconfigure.strata.mysql.config;

import com.terra.framework.autoconfigure.strata.mysql.condition.OnDynamicDataSourceCondition;
import com.terra.framework.autoconfigure.strata.mysql.registrar.DynamicDataSourceRegistrar;
import com.terra.framework.autoconfigure.strata.mysql.scanner.TerraMapperScannerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;


@Conditional(OnDynamicDataSourceCondition.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import({DynamicDataSourceRegistrar.class, TerraMapperScannerConfigurer.class})
public class TerraDynamicMybatisAutoConfiguration {
}

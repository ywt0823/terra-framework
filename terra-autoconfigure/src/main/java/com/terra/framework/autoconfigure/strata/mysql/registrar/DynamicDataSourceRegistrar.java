package com.terra.framework.autoconfigure.strata.mysql.registrar;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.terra.framework.autoconfigure.strata.mysql.provider.DynamicDataSourceProvider;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.Objects;
import java.util.Set;

public class DynamicDataSourceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;
    private Binder binder;
    private static final String DATASOURCE_PREFIX = "spring.datasource";
    private static final String MYBATIS_PLUS_PREFIX_FORMAT = "spring.datasource.%s.mybatis";

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.binder = Binder.get(environment);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DynamicDataSourceProvider provider = new DynamicDataSourceProvider(environment);
        Set<String> dataSourceNames = provider.getDataSourceNames();
        if (dataSourceNames.isEmpty()) {
            return;
        }
        String primaryDataSourceName = environment.getProperty(DATASOURCE_PREFIX + ".primary", dataSourceNames.iterator().next());

        for (String dataSourceName : dataSourceNames) {
            boolean isPrimary = Objects.equals(dataSourceName, primaryDataSourceName);

            // 1. Register DataSource
            BeanDefinition dsDefinition = buildDataSourceDefinition(dataSourceName);
            dsDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "DataSource", dsDefinition);

            // 2. Register SqlSessionFactory (via FactoryBean)
            // We register the FactoryBean itself, and Spring will handle creating the product (SqlSessionFactory)
            // by calling getObject(). The bean is named '...SqlSessionFactory' so that the SqlSessionTemplate can resolve it.
            BeanDefinition ssfBeanDefinition = createSqlSessionFactoryBeanDefinition(dataSourceName);
            ssfBeanDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "SqlSessionFactory", ssfBeanDefinition);

            // 3. Register SqlSessionTemplate
            BeanDefinition sstDefinition = buildSqlSessionTemplateDefinition(dataSourceName);
            sstDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "SqlSessionTemplate", sstDefinition);

            // 4. Register TransactionManager
            BeanDefinition tmDefinition = buildTransactionManagerDefinition(dataSourceName);
            tmDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "TransactionManager", tmDefinition);

            // 5. Register JdbcTemplate
            BeanDefinition jdbcTemplateDefinition = buildJdbcTemplateDefinition(dataSourceName);
            jdbcTemplateDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "JdbcTemplate", jdbcTemplateDefinition);
        }
    }

    private BeanDefinition buildDataSourceDefinition(String dataSourceName) {
        DataSourceProperties dsProperties = binder.bind(DATASOURCE_PREFIX + "." + dataSourceName, DataSourceProperties.class).get();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(dsProperties.getType());
        builder.addPropertyValue("driverClassName", dsProperties.getDriverClassName());
        builder.addPropertyValue("jdbcUrl", dsProperties.getUrl()); // For HikariCP and others
        builder.addPropertyValue("username", dsProperties.getUsername());
        builder.addPropertyValue("password", dsProperties.getPassword());

        // Bind Hikari-specific properties
        String hikariPrefix = String.format("%s.%s.hikari", DATASOURCE_PREFIX, dataSourceName);
        binder.bind(hikariPrefix, Bindable.ofInstance(builder.getBeanDefinition()));

        return builder.getBeanDefinition();
    }

    private BeanDefinition createSqlSessionFactoryBeanDefinition(String dataSourceName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MybatisSqlSessionFactoryBean.class);
        builder.addPropertyReference("dataSource", dataSourceName + "DataSource");

        String mybatisPlusPrefix = String.format(MYBATIS_PLUS_PREFIX_FORMAT, dataSourceName);
        MybatisPlusProperties mybatisPlusProperties = binder.bind(mybatisPlusPrefix, Bindable.of(MybatisPlusProperties.class)).orElseGet(MybatisPlusProperties::new);

        if (mybatisPlusProperties.getMapperLocations() != null && mybatisPlusProperties.getMapperLocations().length > 0) {
            builder.addPropertyValue("mapperLocations", mybatisPlusProperties.getMapperLocations());
        }
        if (mybatisPlusProperties.getConfiguration() != null) {
            builder.addPropertyValue("configuration", mybatisPlusProperties.getConfiguration());
        }
        builder.addPropertyValue("globalConfig", mybatisPlusProperties.getGlobalConfig());

        return builder.getBeanDefinition();
    }

    private BeanDefinition buildSqlSessionTemplateDefinition(String dataSourceName) {
        return BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
            .addConstructorArgReference(dataSourceName + "SqlSessionFactory")
            .getBeanDefinition();
    }

    private BeanDefinition buildJdbcTemplateDefinition(String dataSourceName) {
        return BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class)
            .addConstructorArgReference(dataSourceName + "DataSource")
            .getBeanDefinition();
    }

    private BeanDefinition buildTransactionManagerDefinition(String dataSourceName) {
        return BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class)
            .addConstructorArgReference(dataSourceName + "DataSource")
            .getBeanDefinition();
    }
}

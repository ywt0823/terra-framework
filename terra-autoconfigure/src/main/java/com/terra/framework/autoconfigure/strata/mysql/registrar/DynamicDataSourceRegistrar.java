package com.terra.framework.autoconfigure.strata.mysql.registrar;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.terra.framework.autoconfigure.strata.mysql.provider.DynamicDataSourceProvider;
import org.apache.ibatis.session.SqlSessionFactory;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.Objects;
import java.util.Set;

public class DynamicDataSourceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;
    private Binder binder;
    private static final String DATASOURCE_PREFIX = "spring.datasource";
    private static final String MYBATIS_PLUS_PREFIX_FORMAT = "spring.datasource.%s.mybatis-plus";

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

            // 2. Register MybatisPlusProperties
            BeanDefinition mybatisPlusPropertiesDefinition = buildMybatisPlusPropertiesDefinition(dataSourceName);
            mybatisPlusPropertiesDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "MybatisPlusProperties", mybatisPlusPropertiesDefinition);

            // 3. Register MybatisSqlSessionFactoryBean (The Factory)
            BeanDefinition ssfBeanDefinition = createSqlSessionFactoryBeanDefinition(dataSourceName);
            ssfBeanDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "SqlSessionFactoryBean", ssfBeanDefinition);

            // 4. Register SqlSessionFactory (The Product)
            BeanDefinition ssfDefinition = buildSqlSessionFactoryDefinition(dataSourceName);
            ssfDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "SqlSessionFactory", ssfDefinition);

            // 5. Register SqlSessionTemplate
            BeanDefinition sstDefinition = buildSqlSessionTemplateDefinition(dataSourceName);
            sstDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "SqlSessionTemplate", sstDefinition);

            // 6. Register TransactionManager
            BeanDefinition tmDefinition = buildTransactionManagerDefinition(dataSourceName);
            tmDefinition.setPrimary(isPrimary);
            registry.registerBeanDefinition(dataSourceName + "TransactionManager", tmDefinition);
        }
    }

    private BeanDefinition buildDataSourceDefinition(String dataSourceName) {
        DataSourceProperties dsProperties = binder.bind(DATASOURCE_PREFIX + "." + dataSourceName, DataSourceProperties.class).get();
        return BeanDefinitionBuilder.genericBeanDefinition(dsProperties.getType())
                .addPropertyValue("driverClassName", dsProperties.getDriverClassName())
                .addPropertyValue("url", dsProperties.getUrl())
                .addPropertyValue("username", dsProperties.getUsername())
                .addPropertyValue("password", dsProperties.getPassword())
                .getBeanDefinition();
    }

    private BeanDefinition buildMybatisPlusPropertiesDefinition(String dataSourceName) {
        String prefix = String.format(MYBATIS_PLUS_PREFIX_FORMAT, dataSourceName);
        MybatisPlusProperties properties = binder.bind(prefix, Bindable.of(MybatisPlusProperties.class)).orElseGet(MybatisPlusProperties::new);
        if (properties.getConfiguration() == null) {
            properties.setConfiguration(new MybatisPlusProperties.CoreConfiguration());
        }
        return BeanDefinitionBuilder.genericBeanDefinition(MybatisPlusProperties.class, () -> properties)
                .getBeanDefinition();
    }

    private BeanDefinition createSqlSessionFactoryBeanDefinition(String dataSourceName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MybatisSqlSessionFactoryBean.class);
        builder.addPropertyReference("dataSource", dataSourceName + "DataSource");

        // Bind MybatisPlusProperties for this datasource
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

    private BeanDefinition buildSqlSessionFactoryDefinition(String dataSourceName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactory.class);
        builder.getRawBeanDefinition().setFactoryBeanName(dataSourceName + "SqlSessionFactoryBean");
        builder.getRawBeanDefinition().setFactoryMethodName("getObject");
        return builder.getBeanDefinition();
    }

    private BeanDefinition buildSqlSessionTemplateDefinition(String dataSourceName) {
        return BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
                .addConstructorArgReference(dataSourceName + "SqlSessionFactory")
                .getBeanDefinition();
    }

    private BeanDefinition buildTransactionManagerDefinition(String dataSourceName) {
        return BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class)
                .addConstructorArgReference(dataSourceName + "DataSource")
                .getBeanDefinition();
    }

}

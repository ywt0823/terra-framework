package com.terra.framework.autoconfigure.strata.mysql.condition;

import com.terra.framework.autoconfigure.strata.mysql.provider.DynamicDataSourceProvider;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Set;

public class OnDynamicDataSourceCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        DynamicDataSourceProvider provider = new DynamicDataSourceProvider(environment);
        Set<String> dataSourceNames = provider.getDataSourceNames();
        if (dataSourceNames.isEmpty()) {
            return ConditionOutcome.noMatch("No dynamic datasources were found in the configuration under 'spring.datasource'.");
        }
        return ConditionOutcome.match("Found dynamic datasources: " + dataSourceNames);
    }
}

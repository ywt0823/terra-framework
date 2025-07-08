package com.terra.framework.autoconfigure.strata.mysql.provider;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicDataSourceProvider {

    private final Environment environment;
    private final Binder binder;

    private static final String DATASOURCE_PREFIX = "spring.datasource";

    public DynamicDataSourceProvider(Environment environment) {
        this.environment = environment;
        this.binder = Binder.get(environment);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getDataSourceNames() {
        BindResult<Map<String, Object>> result = binder.bind(DATASOURCE_PREFIX, Bindable.mapOf(String.class, Object.class));
        if (!result.isBound()) {
            return Collections.emptySet();
        }

        Map<String, Object> properties = result.get();
        return properties.keySet().stream()
                .filter(key -> properties.get(key) instanceof Map)
                .filter(this::isDataSourceName)
                .collect(Collectors.toSet());
    }

    private boolean isDataSourceName(String name) {
        String urlProperty = String.format("%s.%s.url", DATASOURCE_PREFIX, name);
        String jdbcUrlProperty = String.format("%s.%s.jdbc-url", DATASOURCE_PREFIX, name);
        return environment.containsProperty(urlProperty) || environment.containsProperty(jdbcUrlProperty);
    }
}

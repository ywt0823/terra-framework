package com.terra.framework.autoconfigure.nova.config.condition;

import com.terra.framework.autoconfigure.nova.annoation.ConditionalOnModelEnabled;
import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;
import java.util.Objects;

public class TerraModelEnabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 从注解中获取我们需要检查的模型名称 (e.g., "openai")
        String modelName = (String) Objects.requireNonNull(metadata.getAnnotationAttributes(ConditionalOnModelEnabled.class.getName())).get("value");

        // 从环境中绑定并获取配置属性
        TerraAiProperties properties = Binder.get(context.getEnvironment())
            .bind("terra.ai", TerraAiProperties.class)
            .orElse(new TerraAiProperties());

        List<String> enabledModels = properties.getEnabledModels();

        // 检查模型名称是否存在于启用列表中
        if (enabledModels.contains(modelName)) {
            return ConditionOutcome.match("模型 '" + modelName + "' 在 terra.ai.enabled-models 列表中，已启用。");
        }

        return ConditionOutcome.noMatch("模型 '" + modelName + "' 不在 terra.ai.enabled-models 列表中，已禁用。");
    }
}

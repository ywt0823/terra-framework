package com.terra.framework.autoconfigure.nova.properties;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static org.springframework.ai.model.SpringAIModels.DEEPSEEK;

/**
 * AI a Terra a enabled.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "spring.ai")
public class TerraAiProperties {

    private List<String> enabledModels = Lists.newArrayList(DEEPSEEK);

    private TerraAiDynamicClientProperties dynamicClient;

    private TerraAiDynamicModelProperties aiDynamicModel;


}

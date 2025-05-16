package com.terra.framework.nova.llm.model.dify;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.core.ModelConfig;
import com.terra.framework.nova.llm.core.ModelType;
import com.terra.framework.nova.llm.manager.ModelManager;
import com.terra.framework.nova.llm.model.base.LLMModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Dify模型单元测试
 */
@ExtendWith(MockitoExtension.class)
public class DifyModelTest {

    @Mock
    private HttpClientUtils httpClientUtils;

    private LLMModel difyModel;
    private ModelConfig modelConfig;

    private static final String API_KEY = "test-api-key";
    private static final String APP_ID = "test-app-id";

    @BeforeEach
    void setUp() {
        // 创建模型配置
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("appId", APP_ID);
        extraParams.put("useKnowledgeBase", false);

        modelConfig = ModelConfig.builder()
            .type(ModelType.DIFY)
            .apiKey(API_KEY)
            .apiEndpoint("https://api.dify.ai/v1")
            .temperature(0.7)
            .maxTokens(1000)
            .timeoutMs(30000L)
            .maxRetries(3)
            .extraParams(extraParams)
            .build();

        // 创建模型管理器
        ModelManager modelManager = new ModelManager(httpClientUtils);
        modelManager.registerConfig("dify", modelConfig);

        // 获取模型实例
        difyModel = modelManager.getModel("dify");
        assertNotNull(difyModel, "模型实例不应为空");
    }


}

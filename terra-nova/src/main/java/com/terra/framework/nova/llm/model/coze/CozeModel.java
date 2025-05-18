package com.terra.framework.nova.llm.model.coze;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractAIModel;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.DefaultAuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelAdapter;
import com.terra.framework.nova.llm.model.ModelConfig;
import com.terra.framework.nova.llm.model.ModelInfo;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.ModelStatus;
import com.terra.framework.nova.llm.model.ModelType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

/**
 * Coze模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class CozeModel extends AbstractAIModel {

    /**
     * 模型适配器
     */
    private final ModelAdapter adapter;

    /**
     * 认证提供者
     */
    private final AuthProvider authProvider;

    /**
     * 模型信息
     */
    private ModelInfo modelInfo;

    /**
     * 构造函数
     *
     * @param config 模型配置
     * @param httpClient HTTP客户端工具
     */
    public CozeModel(ModelConfig config, HttpClientUtils httpClient) {
        super(config, httpClient);

        // 创建认证提供者
        this.authProvider = new DefaultAuthProvider(config.getAuthConfig());

        // 创建参数映射策略
        CozeRequestMappingStrategy mappingStrategy = new CozeRequestMappingStrategy();

        // 创建模型适配器
        this.adapter = new CozeAdapter(mappingStrategy, authProvider);
    }

    @Override
    public ModelResponse generate(String prompt, Map<String, Object> parameters) {
        try {
            status = ModelStatus.BUSY;

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withPrompt(prompt)
                    .withParameters(buildParameters(parameters))
                    .withStream(false)
                    .build();

            // 转换为Coze请求
            JSONObject cozeRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildEndpointUrl("/chat/completions"),
                    cozeRequest.toJSONString(),
                    StandardCharsets.UTF_8,
                    headers
            );

            // 转换响应
            ModelResponse modelResponse = adapter.convertResponse(response);
            status = ModelStatus.READY;
            return modelResponse;
        } catch (Exception e) {
            ModelException modelException = adapter.handleException(e);
            throw modelException;
        }
    }

    @Override
    public Publisher<String> generateStream(String prompt, Map<String, Object> parameters) {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        final AtomicBoolean completed = new AtomicBoolean(false);

        try {
            status = ModelStatus.BUSY;

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withPrompt(prompt)
                    .withParameters(buildParameters(parameters))
                    .withStream(true)
                    .build();

            // 转换为Coze请求
            JSONObject cozeRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 定义流式回调
            HttpClientUtils.StreamCallback callback = new HttpClientUtils.StreamCallback() {
                @Override
                public void onData(String data) {
                    try {
                        if (data.startsWith("data: ")) {
                            data = data.substring(6);
                        }
                        if ("[DONE]".equals(data)) {
                            if (completed.compareAndSet(false, true)) {
                                publisher.close();
                                status = ModelStatus.READY;
                            }
                            return;
                        }

                        JSONObject jsonResponse = JSON.parseObject(data);
                        ModelResponse response = adapter.convertResponse(jsonResponse);
                        if (response.getContent() != null) {
                            publisher.submit(response.getContent());
                        }
                    } catch (Exception e) {
                        log.error("处理流式响应失败", e);
                        if (completed.compareAndSet(false, true)) {
                            publisher.closeExceptionally(e);
                            status = ModelStatus.ERROR;
                        }
                    }
                }

                @Override
                public void onComplete() {
                    if (completed.compareAndSet(false, true)) {
                        publisher.close();
                        status = ModelStatus.READY;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (completed.compareAndSet(false, true)) {
                        publisher.closeExceptionally(adapter.handleException(throwable instanceof Exception ? (Exception) throwable : new Exception(throwable)));
                        status = ModelStatus.ERROR;
                    }
                }
            };

            // 发送流式请求
            httpClientUtils.sendPostJsonStream(
                    buildEndpointUrl("/chat/completions"),
                    cozeRequest.toJSONString(),
                    StandardCharsets.UTF_8,
                    headers,
                    callback
            );

        } catch (Exception e) {
            if (completed.compareAndSet(false, true)) {
                publisher.closeExceptionally(adapter.handleException(e));
                status = ModelStatus.ERROR;
            }
        }

        return publisher;
    }

    @Override
    public ModelResponse chat(List<Message> messages, Map<String, Object> parameters) {
        try {
            status = ModelStatus.BUSY;

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withParameters(buildParameters(parameters))
                    .withStream(false)
                    .build();

            // 添加消息
            for (Message message : messages) {
                request.getMessages().add(message);
            }

            // 转换为Coze请求
            JSONObject cozeRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildEndpointUrl("/chat/completions"),
                    cozeRequest.toJSONString(),
                    StandardCharsets.UTF_8,
                    headers
            );

            // 转换响应
            ModelResponse modelResponse = adapter.convertResponse(response);
            status = ModelStatus.READY;
            return modelResponse;
        } catch (Exception e) {
            ModelException modelException = adapter.handleException(e);
            throw modelException;
        }
    }

    @Override
    public Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        final AtomicBoolean completed = new AtomicBoolean(false);

        try {
            status = ModelStatus.BUSY;

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withParameters(buildParameters(parameters))
                    .withStream(true)
                    .build();

            // 添加消息
            for (Message message : messages) {
                request.getMessages().add(message);
            }

            // 转换为Coze请求
            JSONObject cozeRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 定义流式回调
            HttpClientUtils.StreamCallback callback = new HttpClientUtils.StreamCallback() {
                @Override
                public void onData(String data) {
                    try {
                        if (data.startsWith("data: ")) {
                            data = data.substring(6);
                        }
                        if ("[DONE]".equals(data)) {
                            if (completed.compareAndSet(false, true)) {
                                publisher.close();
                                status = ModelStatus.READY;
                            }
                            return;
                        }

                        JSONObject jsonResponse = JSON.parseObject(data);
                        ModelResponse response = adapter.convertResponse(jsonResponse);
                        if (response.getContent() != null) {
                            publisher.submit(response.getContent());
                        }
                    } catch (Exception e) {
                        log.error("处理流式响应失败", e);
                        if (completed.compareAndSet(false, true)) {
                            publisher.closeExceptionally(e);
                            status = ModelStatus.ERROR;
                        }
                    }
                }

                @Override
                public void onComplete() {
                    if (completed.compareAndSet(false, true)) {
                        publisher.close();
                        status = ModelStatus.READY;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (completed.compareAndSet(false, true)) {
                        publisher.closeExceptionally(adapter.handleException(throwable instanceof Exception ? (Exception) throwable : new Exception(throwable)));
                        status = ModelStatus.ERROR;
                    }
                }
            };

            // 发送流式请求
            httpClientUtils.sendPostJsonStream(
                    buildEndpointUrl("/chat/completions"),
                    cozeRequest.toJSONString(),
                    StandardCharsets.UTF_8,
                    headers,
                    callback
            );

        } catch (Exception e) {
            if (completed.compareAndSet(false, true)) {
                publisher.closeExceptionally(adapter.handleException(e));
                status = ModelStatus.ERROR;
            }
        }

        return publisher;
    }

    @Override
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    @Override
    public void init() {
        try {
            log.info("初始化Coze模型: {}", config.getModelId());

            // 创建模型信息
            String modelName = getModelName();
            modelInfo = ModelInfo.builder()
                    .modelId(config.getModelId())
                    .modelType(ModelType.COZE)
                    .name(modelName)
                    .vendor("Coze")
                    .streamSupported(true)
                    .chatSupported(true)
                    .build();

            status = ModelStatus.READY;
        } catch (Exception e) {
            log.error("初始化Coze模型失败", e);
            status = ModelStatus.ERROR;
            throw new ModelException("初始化Coze模型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("关闭Coze模型: {}", config.getModelId());
        status = ModelStatus.OFFLINE;
    }

    /**
     * 创建请求头
     *
     * @return 请求头数组
     */
    private Header[] createHeaders() {
        Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");

        // 获取认证头
        if (authProvider instanceof DefaultAuthProvider) {
            Header[] authHeaders = ((DefaultAuthProvider) authProvider).createAuthHeaders();

            // 合并内容类型头和认证头
            Header[] headers = new Header[authHeaders.length + 1];
            headers[0] = contentTypeHeader;
            System.arraycopy(authHeaders, 0, headers, 1, authHeaders.length);

            return headers;
        } else {
            // 仅返回内容类型头
            return new Header[]{contentTypeHeader};
        }
    }

    /**
     * 构建Coze API端点URL
     *
     * @param path API路径
     * @return 完整URL
     */
    private String buildEndpointUrl(String path) {
        String endpoint = config.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return endpoint + path;
    }

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    private String getModelName() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如coze:gpt-3.5-turbo），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从默认参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型
        return "gpt-3.5-turbo";
    }
} 
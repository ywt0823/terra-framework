package com.terra.framework.nova.core.model.tongyi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.core.exception.ModelException;
import com.terra.framework.nova.core.model.AbstractAIModel;
import com.terra.framework.nova.core.model.AuthProvider;
import com.terra.framework.nova.core.model.DefaultAuthProvider;
import com.terra.framework.nova.core.model.Message;
import com.terra.framework.nova.core.model.ModelAdapter;
import com.terra.framework.nova.core.model.ModelConfig;
import com.terra.framework.nova.core.model.ModelInfo;
import com.terra.framework.nova.core.model.ModelRequest;
import com.terra.framework.nova.core.model.ModelResponse;
import com.terra.framework.nova.core.model.ModelStatus;
import com.terra.framework.nova.core.model.ModelType;
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
 * 通义千问模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class TongyiModel extends AbstractAIModel {

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
    public TongyiModel(ModelConfig config, HttpClientUtils httpClient) {
        super(config, httpClient);

        // 创建认证提供者
        this.authProvider = new DefaultAuthProvider(config.getAuthConfig());

        // 创建参数映射策略
        TongyiRequestMappingStrategy mappingStrategy = new TongyiRequestMappingStrategy();

        // 创建模型适配器
        this.adapter = new TongyiAdapter(mappingStrategy, authProvider);
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

            // 转换为通义千问请求
            JSONObject tongyiRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildEndpointUrl("/v1/chat/completions"),
                    tongyiRequest.toJSONString(),
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

            // 转换为通义千问请求
            JSONObject tongyiRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 定义流式回调
            HttpClientUtils.StreamCallback callback = new HttpClientUtils.StreamCallback() {
                @Override
                public void onData(String chunk) {
                    try {
                        if (chunk.startsWith("data: ")) {
                            String data = chunk.substring(6).trim();

                            // 处理特殊情况
                            if ("[DONE]".equals(data)) {
                                return;
                            }

                            // 解析JSON
                            JSONObject jsonResponse = JSON.parseObject(data);

                            // 从JSON中提取内容
                            if (jsonResponse.containsKey("choices")) {
                                JSONObject firstChoice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                                if (firstChoice.containsKey("delta") &&
                                    firstChoice.getJSONObject("delta").containsKey("content")) {
                                    String content = firstChoice.getJSONObject("delta").getString("content");
                                    if (content != null && !content.isEmpty()) {
                                        publisher.submit(content);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        onError(e);
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
                        publisher.closeExceptionally(
                                adapter.handleException(new Exception(throwable))
                        );
                        status = ModelStatus.ERROR;
                    }
                }
            };

            // 发送流式请求
            httpClientUtils.sendPostJsonStream(
                    buildEndpointUrl("/v1/chat/completions"),
                    tongyiRequest.toJSONString(),
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

            // 转换为通义千问请求
            JSONObject tongyiRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildEndpointUrl("/v1/chat/completions"),
                    tongyiRequest.toJSONString(),
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

            // 转换为通义千问请求
            JSONObject tongyiRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 定义流式回调
            HttpClientUtils.StreamCallback callback = new HttpClientUtils.StreamCallback() {
                @Override
                public void onData(String chunk) {
                    try {
                        if (chunk.startsWith("data: ")) {
                            String data = chunk.substring(6).trim();

                            // 处理特殊情况
                            if ("[DONE]".equals(data)) {
                                return;
                            }

                            // 解析JSON
                            JSONObject jsonResponse = JSON.parseObject(data);

                            // 从JSON中提取内容
                            if (jsonResponse.containsKey("choices")) {
                                JSONObject firstChoice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                                if (firstChoice.containsKey("delta") &&
                                    firstChoice.getJSONObject("delta").containsKey("content")) {
                                    String content = firstChoice.getJSONObject("delta").getString("content");
                                    if (content != null && !content.isEmpty()) {
                                        publisher.submit(content);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        onError(e);
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
                        publisher.closeExceptionally(
                                adapter.handleException(new Exception(throwable))
                        );
                        status = ModelStatus.ERROR;
                    }
                }
            };

            // 发送流式请求
            httpClientUtils.sendPostJsonStream(
                    buildEndpointUrl("/v1/chat/completions"),
                    tongyiRequest.toJSONString(),
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
            log.info("初始化通义千问模型: {}", config.getModelId());

            // 创建模型信息
            String modelName = getModelName();
            modelInfo = ModelInfo.builder()
                    .modelId(config.getModelId())
                    .modelType(ModelType.TONGYI)
                    .name(modelName)
                    .vendor("阿里")
                    .streamSupported(true)
                    .chatSupported(true)
                    .build();

            status = ModelStatus.READY;
        } catch (Exception e) {
            log.error("初始化通义千问模型失败", e);
            status = ModelStatus.ERROR;
            throw new ModelException("初始化通义千问模型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("关闭通义千问模型: {}", config.getModelId());
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
     * 构建通义千问API端点URL
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

        // 如果模型ID包含冒号（如tongyi:qwen-turbo），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型
        return "qwen-turbo";
    }
}

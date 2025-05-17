package com.terra.framework.nova.core.model.dify;

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
 * Dify模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class DifyModel extends AbstractAIModel {

    /**
     * 模型适配器
     */
    private final ModelAdapter adapter;

    /**
     * 参数映射策略
     */
    private final DifyRequestMappingStrategy mappingStrategy;

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
    public DifyModel(ModelConfig config, HttpClientUtils httpClient) {
        super(config, httpClient);

        // 创建认证提供者
        this.authProvider = new DefaultAuthProvider(config.getAuthConfig());

        // 创建参数映射策略
        this.mappingStrategy = new DifyRequestMappingStrategy();

        // 创建模型适配器
        this.adapter = new DifyAdapter(mappingStrategy, authProvider);
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

            // 转换为Dify请求
            JSONObject difyRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildEndpointUrl("/completion-messages", parameters),
                    difyRequest.toJSONString(),
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

            // 转换为Dify请求
            JSONObject difyRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 定义流式回调
            HttpClientUtils.StreamCallback callback = new HttpClientUtils.StreamCallback() {
                private final StringBuilder buffer = new StringBuilder();

                @Override
                public void onData(String chunk) {
                    try {
                        if (chunk.trim().isEmpty()) {
                            return;
                        }

                        // Dify的流式输出不是标准的SSE格式，需要特殊处理
                        if (chunk.startsWith("data: ")) {
                            String data = chunk.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                return;
                            }

                            JSONObject jsonResponse = JSON.parseObject(data);
                            if (jsonResponse.containsKey("event") &&
                                "message".equals(jsonResponse.getString("event"))) {
                                if (jsonResponse.containsKey("answer")) {
                                    String content = jsonResponse.getString("answer");
                                    if (content != null && !content.isEmpty()) {
                                        publisher.submit(content);
                                    }
                                }
                            }
                        } else {
                            // 尝试作为JSON解析
                            try {
                                JSONObject jsonResponse = JSON.parseObject(chunk);
                                if (jsonResponse.containsKey("answer")) {
                                    String content = jsonResponse.getString("answer");
                                    if (content != null && !content.isEmpty()) {
                                        publisher.submit(content);
                                    }
                                }
                            } catch (Exception e) {
                                // 如果不是JSON，则直接发送
                                if (!chunk.trim().isEmpty()) {
                                    publisher.submit(chunk.trim());
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
                    buildEndpointUrl("/completion-messages", parameters),
                    difyRequest.toJSONString(),
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

            // 转换为Dify请求
            JSONObject difyRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildEndpointUrl("/chat-messages", parameters),
                    difyRequest.toJSONString(),
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

            // 转换为Dify请求
            JSONObject difyRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 定义流式回调
            HttpClientUtils.StreamCallback callback = new HttpClientUtils.StreamCallback() {
                @Override
                public void onData(String chunk) {
                    try {
                        if (chunk.trim().isEmpty()) {
                            return;
                        }

                        // Dify的流式输出不是标准的SSE格式，需要特殊处理
                        if (chunk.startsWith("data: ")) {
                            String data = chunk.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                return;
                            }

                            JSONObject jsonResponse = JSON.parseObject(data);
                            if (jsonResponse.containsKey("event") &&
                                "message".equals(jsonResponse.getString("event"))) {
                                if (jsonResponse.containsKey("answer")) {
                                    String content = jsonResponse.getString("answer");
                                    if (content != null && !content.isEmpty()) {
                                        publisher.submit(content);
                                    }
                                }
                            }
                        } else {
                            // 尝试作为JSON解析
                            try {
                                JSONObject jsonResponse = JSON.parseObject(chunk);
                                if (jsonResponse.containsKey("answer")) {
                                    String content = jsonResponse.getString("answer");
                                    if (content != null && !content.isEmpty()) {
                                        publisher.submit(content);
                                    }
                                }
                            } catch (Exception e) {
                                // 如果不是JSON，则直接发送
                                if (!chunk.trim().isEmpty()) {
                                    publisher.submit(chunk.trim());
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
                    buildEndpointUrl("/chat-messages", parameters),
                    difyRequest.toJSONString(),
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
            log.info("初始化Dify模型: {}", config.getModelId());

            // 创建模型信息
            String appId = getAppId();
            modelInfo = ModelInfo.builder()
                    .modelId(config.getModelId())
                    .modelType(ModelType.DIFY)
                    .name("Dify App: " + appId)
                    .vendor("Dify")
                    .streamSupported(true)
                    .chatSupported(true)
                    .build();

            status = ModelStatus.READY;
        } catch (Exception e) {
            log.error("初始化Dify模型失败", e);
            status = ModelStatus.ERROR;
            throw new ModelException("初始化Dify模型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("关闭Dify模型: {}", config.getModelId());
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
     * 构建Dify API端点URL
     *
     * @param path API路径
     * @param parameters 请求参数
     * @return 完整URL
     */
    private String buildEndpointUrl(String path, Map<String, Object> parameters) {
        String endpoint = config.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // 获取应用程序ID
        String appId = mappingStrategy.getAppId(parameters);
        if (appId == null) {
            appId = getAppId();
        }

        // Dify API格式: /api/v1/apps/{app_id}/completion-messages 或 /api/v1/apps/{app_id}/chat-messages
        return endpoint + "/api/v1/apps/" + appId + path;
    }

    /**
     * 获取应用程序ID
     *
     * @return 应用程序ID
     */
    private String getAppId() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如dify:app_id），则提取应用程序ID
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            String modelStr = modelObj.toString();
            if (modelStr.contains(":")) {
                return modelStr.substring(modelStr.indexOf(':') + 1);
            }
            return modelStr;
        }

        throw new ModelException("无法确定Dify应用程序ID，请在模型ID中使用格式：dify:your_app_id");
    }
}

package com.terra.framework.nova.model.wenxin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.exception.ModelException;
import com.terra.framework.nova.model.AbstractAIModel;
import com.terra.framework.nova.model.AuthConfig;
import com.terra.framework.nova.model.AuthProvider;
import com.terra.framework.nova.model.AuthType;
import com.terra.framework.nova.model.DefaultAuthProvider;
import com.terra.framework.nova.model.Message;
import com.terra.framework.nova.model.ModelAdapter;
import com.terra.framework.nova.model.ModelConfig;
import com.terra.framework.nova.model.ModelInfo;
import com.terra.framework.nova.model.ModelRequest;
import com.terra.framework.nova.model.ModelResponse;
import com.terra.framework.nova.model.ModelStatus;
import com.terra.framework.nova.model.ModelType;
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
 * 文心一言模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class WenxinModel extends AbstractAIModel {

    /**
     * 模型适配器
     */
    private final ModelAdapter adapter;

    /**
     * 参数映射策略
     */
    private final WenxinRequestMappingStrategy mappingStrategy;

    /**
     * 认证提供者
     */
    private final AuthProvider authProvider;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌过期时间
     */
    private long tokenExpiresAt;

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
    public WenxinModel(ModelConfig config, HttpClientUtils httpClient) {
        super(config, httpClient);

        // 创建认证提供者
        this.authProvider = new DefaultAuthProvider(config.getAuthConfig());

        // 创建参数映射策略
        this.mappingStrategy = new WenxinRequestMappingStrategy();

        // 创建模型适配器
        this.adapter = new WenxinAdapter(mappingStrategy, authProvider);
    }

    @Override
    public ModelResponse generate(String prompt, Map<String, Object> parameters) {
        try {
            status = ModelStatus.BUSY;

            // 确保访问令牌有效
            ensureValidAccessToken();

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withPrompt(prompt)
                    .withParameters(buildParameters(parameters))
                    .withStream(false)
                    .build();

            // 转换为文心一言请求
            JSONObject wenxinRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildModelEndpointUrl(parameters),
                    wenxinRequest.toJSONString(),
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

            // 确保访问令牌有效
            ensureValidAccessToken();

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withPrompt(prompt)
                    .withParameters(buildParameters(parameters))
                    .withStream(true)
                    .build();

            // 转换为文心一言请求
            JSONObject wenxinRequest = adapter.convertRequest(request, JSONObject.class);

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
                            if (jsonResponse.containsKey("delta")) {
                                String content = jsonResponse.getString("delta");
                                if (content != null && !content.isEmpty()) {
                                    publisher.submit(content);
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
                    buildModelEndpointUrl(parameters),
                    wenxinRequest.toJSONString(),
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

            // 确保访问令牌有效
            ensureValidAccessToken();

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withParameters(buildParameters(parameters))
                    .withStream(false)
                    .build();

            // 添加消息
            for (Message message : messages) {
                request.getMessages().add(message);
            }

            // 转换为文心一言请求
            JSONObject wenxinRequest = adapter.convertRequest(request, JSONObject.class);

            // 准备请求头
            Header[] headers = createHeaders();

            // 发送请求
            String response = httpClientUtils.sendPostJson(
                    buildModelEndpointUrl(parameters),
                    wenxinRequest.toJSONString(),
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

            // 确保访问令牌有效
            ensureValidAccessToken();

            // 构建请求
            ModelRequest request = ModelRequest.builder()
                    .withParameters(buildParameters(parameters))
                    .withStream(true)
                    .build();

            // 添加消息
            for (Message message : messages) {
                request.getMessages().add(message);
            }

            // 转换为文心一言请求
            JSONObject wenxinRequest = adapter.convertRequest(request, JSONObject.class);

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
                            if (jsonResponse.containsKey("delta")) {
                                String content = jsonResponse.getString("delta");
                                if (content != null && !content.isEmpty()) {
                                    publisher.submit(content);
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
                    buildModelEndpointUrl(parameters),
                    wenxinRequest.toJSONString(),
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
            log.info("初始化文心一言模型: {}", config.getModelId());

            // 获取访问令牌
            refreshAccessToken();

            // 创建模型信息
            String modelName = getModelName();
            modelInfo = ModelInfo.builder()
                    .modelId(config.getModelId())
                    .modelType(ModelType.WENXIN)
                    .name(modelName)
                    .vendor("百度")
                    .streamSupported(true)
                    .chatSupported(true)
                    .build();

            status = ModelStatus.READY;
        } catch (Exception e) {
            log.error("初始化文心一言模型失败", e);
            status = ModelStatus.ERROR;
            throw new ModelException("初始化文心一言模型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("关闭文心一言模型: {}", config.getModelId());
        status = ModelStatus.OFFLINE;
    }

    /**
     * 创建请求头
     *
     * @return 请求头数组
     */
    private Header[] createHeaders() {
        Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");

        // 文心一言不使用普通的API Key认证，而是在URL中附加access_token
        return new Header[]{contentTypeHeader};
    }

    /**
     * 确保访问令牌有效，如果过期则刷新
     */
    private void ensureValidAccessToken() {
        long now = System.currentTimeMillis();

        // 如果令牌为空或者即将过期（10分钟内），则刷新令牌
        if (accessToken == null || now >= tokenExpiresAt - 600000) {
            refreshAccessToken();
        }
    }

    /**
     * 刷新访问令牌
     */
    private void refreshAccessToken() {
        try {
            AuthConfig authConfig = config.getAuthConfig();

            if (authConfig.getAuthType() != AuthType.API_KEY) {
                throw new ModelException("文心一言模型仅支持API_KEY认证模式");
            }

            // 获取API Key和Secret Key
            String apiKey = authConfig.getApiKey();
            String secretKey = authConfig.getApiKeySecret();

            if (apiKey == null || secretKey == null) {
                throw new ModelException("文心一言API认证需要API Key和Secret Key");
            }

            // 构建OAuth请求URL
            String oauthUrl = String.format(
                    "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                    apiKey, secretKey
            );

            // 发送OAuth请求
            String response = httpClientUtils.sendPostJson(
                    oauthUrl,
                    "",
                    StandardCharsets.UTF_8,
                    new Header[]{new BasicHeader("Content-Type", "application/json")}
            );

            // 解析响应获取访问令牌
            JSONObject json = JSON.parseObject(response);
            accessToken = json.getString("access_token");
            int expiresIn = json.getIntValue("expires_in");

            // 计算过期时间（提前一些过期以防止边界问题）
            tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L) - 60000L;

            log.debug("成功获取文心一言访问令牌，有效期: {} 秒", expiresIn);
        } catch (Exception e) {
            throw new ModelException("获取文心一言访问令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建模型API端点URL，包括访问令牌
     *
     * @param parameters 参数
     * @return 完整URL
     */
    private String buildModelEndpointUrl(Map<String, Object> parameters) {
        String modelName = mappingStrategy.getModelName(parameters);
        String endpoint = config.getEndpoint();

        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        return String.format("%s/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/%s?access_token=%s",
                endpoint, modelName, accessToken);
    }

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    private String getModelName() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如wenxin:ernie-4.0），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型
        return "ernie-4.0";
    }
}

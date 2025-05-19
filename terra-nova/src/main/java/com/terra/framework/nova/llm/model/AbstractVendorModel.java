package com.terra.framework.nova.llm.model;

import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.exception.ModelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 供应商模型抽象类，使用模板方法模式减少重复代码
 *
 * @author terra-nova
 */
@Slf4j
public abstract class AbstractVendorModel extends AbstractAIModel {

    /**
     * 模型适配器
     */
    protected final ModelAdapter adapter;

    /**
     * 认证提供者
     */
    protected final AuthProvider authProvider;

    /**
     * 模型信息
     */
    protected ModelInfo modelInfo;

    /**
     * 构造函数
     *
     * @param config          模型配置
     * @param httpClientUtils HTTP客户端工具
     * @param adapter         模型适配器
     * @param authProvider    认证提供者
     */
    protected AbstractVendorModel(ModelConfig config, HttpClientUtils httpClientUtils,
                                  ModelAdapter adapter, AuthProvider authProvider) {
        super(config, httpClientUtils);
        this.adapter = adapter;
        this.authProvider = authProvider;
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

            // 执行请求并获取响应
            String response = executeRequest(request, getCompletionsEndpoint());

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

            // 执行流式请求
            executeStreamRequest(request, publisher, completed, getCompletionsEndpoint());

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

            // 构建请求，先添加消息再构建
            ModelRequest.ModelRequestBuilder requestBuilder = ModelRequest.builder()
                .withParameters(buildParameters(parameters))
                .withStream(false);

            // 添加消息
            for (Message message : messages) {
                requestBuilder.addMessage(message);
            }

            // 构建完整请求
            ModelRequest request = requestBuilder.build();

            // 执行请求并获取响应
            String response = executeRequest(request, getChatEndpoint());

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

            // 构建请求，先添加消息再构建
            ModelRequest.ModelRequestBuilder requestBuilder = ModelRequest.builder()
                .withParameters(buildParameters(parameters))
                .withStream(true);

            // 添加消息
            for (Message message : messages) {
                requestBuilder.addMessage(message);
            }

            // 构建完整请求
            ModelRequest request = requestBuilder.build();

            // 执行流式请求
            executeStreamRequest(request, publisher, completed, getChatEndpoint());

        } catch (Exception e) {
            if (completed.compareAndSet(false, true)) {
                publisher.closeExceptionally(adapter.handleException(e));
                status = ModelStatus.ERROR;
            }
        }

        return publisher;
    }

    /**
     * 执行请求并返回响应
     *
     * @param request  模型请求
     * @param endpoint 请求端点
     * @return 响应字符串
     * @throws Exception 如果请求失败
     */
    protected String executeRequest(ModelRequest request, String endpoint) throws Exception {
        // 转换为供应商请求
        JSONObject vendorRequest = adapter.convertRequest(request, JSONObject.class);

        // 准备请求头
        Header[] headers = createHeaders();

        // 准备完整URL
        String url = buildFullEndpointUrl(endpoint, request.getParameters());

        // 发送请求
        return httpClientUtils.sendPostJson(
            url,
            vendorRequest.toJSONString(),
            StandardCharsets.UTF_8,
            headers
        );
    }

    /**
     * 执行流式请求
     *
     * @param request   模型请求
     * @param publisher 发布者
     * @param completed 完成标志
     * @param endpoint  请求端点
     * @throws Exception 如果请求失败
     */
    protected void executeStreamRequest(
        ModelRequest request,
        SubmissionPublisher<String> publisher,
        AtomicBoolean completed,
        String endpoint) throws Exception {

        // 转换为供应商请求
        JSONObject vendorRequest = adapter.convertRequest(request, JSONObject.class);

        // 准备请求头
        Header[] headers = createHeaders();

        // 准备完整URL
        String url = buildFullEndpointUrl(endpoint, request.getParameters());

        // 定义流式回调
        HttpClientUtils.StreamCallback callback = createStreamCallback(publisher, completed);

        // 发送流式请求
        httpClientUtils.sendPostJsonStream(
            url,
            vendorRequest.toJSONString(),
            StandardCharsets.UTF_8,
            headers,
            callback
        );
    }

    /**
     * 创建流式回调
     *
     * @param publisher 发布者
     * @param completed 完成标志
     * @return 流式回调
     */
    protected HttpClientUtils.StreamCallback createStreamCallback(
        SubmissionPublisher<String> publisher,
        AtomicBoolean completed) {
        return new HttpClientUtils.StreamCallback() {
            @Override
            public void onData(String chunk) {
                try {
                    String content = processStreamData(chunk);
                    if (content != null && !content.isEmpty()) {
                        publisher.submit(content);
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
    }

    @Override
    public void init() {
        try {
            log.info("初始化{}模型: {}", getVendorName(), config.getModelId());

            // 创建模型信息
            modelInfo = ModelInfo.builder()
                .modelId(config.getModelId())
                .modelType(getModelType())
                .name(getModelName())
                .vendor(getVendorName())
                .streamSupported(true)
                .chatSupported(true)
                .build();

            initVendorSpecific();
            status = ModelStatus.READY;
        } catch (Exception e) {
            log.error("初始化{}模型失败", getVendorName(), e);
            status = ModelStatus.ERROR;
            throw new ModelException("初始化" + getVendorName() + "模型失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("关闭{}模型: {}", getVendorName(), config.getModelId());
        closeVendorSpecific();
        status = ModelStatus.OFFLINE;
    }

    /**
     * 创建请求头
     *
     * @return 请求头数组
     */
    protected Header[] createHeaders() {
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
     * 获取供应商名称
     *
     * @return 供应商名称
     */
    protected abstract String getVendorName();

    /**
     * 获取模型类型
     *
     * @return 模型类型
     */
    protected abstract ModelType getModelType();

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    protected abstract String getModelName();

    /**
     * 获取聊天端点
     *
     * @return 聊天端点
     */
    protected abstract String getChatEndpoint();

    /**
     * 获取文本生成端点
     *
     * @return 文本生成端点
     */
    protected abstract String getCompletionsEndpoint();

    /**
     * 构建完整的端点URL
     *
     * @param endpoint   端点路径
     * @param parameters 请求参数
     * @return 完整URL
     */
    protected abstract String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters);

    /**
     * 处理流式数据
     *
     * @param chunk 数据块
     * @return 处理后的内容
     */
    protected abstract String processStreamData(String chunk);

    /**
     * 初始化供应商特定的内容
     */
    protected void initVendorSpecific() {

    }

    /**
     * 关闭供应商特定的资源
     */
    protected void closeVendorSpecific() {
        // 默认实现为空，子类可以覆盖
    }

    @Override
    public ModelInfo getModelInfo() {
        return modelInfo;
    }
}

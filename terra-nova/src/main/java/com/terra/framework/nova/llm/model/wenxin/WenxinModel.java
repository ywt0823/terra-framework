package com.terra.framework.nova.llm.model.wenxin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 文心一言模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class WenxinModel extends AbstractVendorModel {

    /**
     * 参数映射策略
     */
    private final WenxinRequestMappingStrategy mappingStrategy;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌过期时间
     */
    private long tokenExpiresAt;

    /**
     * 构造函数
     *
     * @param config     模型配置
     * @param httpClient HTTP客户端工具
     */
    public WenxinModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config,
            httpClient,
            new WenxinAdapter(new WenxinRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
        this.mappingStrategy = new WenxinRequestMappingStrategy();
    }

    @Override
    protected String getVendorName() {
        return "百度";
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.WENXIN;
    }

    @Override
    protected String getModelName() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如wenxin:ernie-bot），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型
        return "ernie-bot";
    }

    @Override
    protected String getChatEndpoint() {
        return ""; // 文心一言使用buildModelEndpointUrl动态构建端点
    }

    @Override
    protected String getCompletionsEndpoint() {
        return ""; // 文心一言使用buildModelEndpointUrl动态构建端点
    }

    @Override
    protected String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters) {
        // 文心一言需要特殊处理端点构建
        return buildModelEndpointUrl(parameters);
    }

    /**
     * 重写执行请求方法，确保访问令牌有效
     */
    @Override
    protected String executeRequest(ModelRequest request, String endpoint) throws Exception {
        // 确保访问令牌有效
        ensureValidAccessToken();

        // 调用父类方法
        return super.executeRequest(request, endpoint);
    }

    /**
     * 重写执行流式请求方法，确保访问令牌有效
     */
    @Override
    protected void executeStreamRequest(
        ModelRequest request,
        SubmissionPublisher<String> publisher,
        AtomicBoolean completed,
        String endpoint) throws Exception {

        // 确保访问令牌有效
        ensureValidAccessToken();

        // 调用父类方法
        super.executeStreamRequest(request, publisher, completed, endpoint);
    }

    @Override
    protected String processStreamData(String chunk) {
        if (chunk.startsWith("data: ")) {
            String data = chunk.substring(6).trim();

            // 处理特殊情况
            if ("[DONE]".equals(data)) {
                return null;
            }

            try {
                // 解析JSON
                JSONObject jsonResponse = JSON.parseObject(data);

                // 从JSON中提取内容
                if (jsonResponse.containsKey("delta")) {
                    String content = jsonResponse.getString("delta");
                    if (content != null && !content.isEmpty()) {
                        return content;
                    }
                }
            } catch (Exception e) {
                log.debug("解析文心一言流式数据失败: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * 初始化供应商特定内容
     */
    @Override
    protected void initVendorSpecific() {
        try {
            refreshAccessToken();
        } catch (Exception e) {
            log.error("初始化文心一言访问令牌失败", e);
            throw new ModelException("初始化文心一言访问令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 确保访问令牌有效
     */
    private void ensureValidAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken == null || now >= tokenExpiresAt - 60000) { // 提前1分钟刷新
            refreshAccessToken();
        }
    }

    /**
     * 刷新访问令牌
     */
    private void refreshAccessToken() {
        try {
            // 获取API密钥和密钥
            AuthConfig authConfig = config.getAuthConfig();
            if (authConfig == null || authConfig.getAuthType() != AuthType.AK_SK) {
                throw new ModelException("文心一言需要AK/SK认证");
            }

            String apiKey = authConfig.getApiKeyId();
            String secretKey = authConfig.getApiKeySecret();

            if (apiKey == null || secretKey == null) {
                throw new ModelException("未配置文心一言API密钥和密钥");
            }

            // 构建访问令牌请求URL
            String tokenEndpoint = "https://aip.baidubce.com/oauth/2.0/token";
            String tokenUrl = String.format(
                "%s?grant_type=client_credentials&client_id=%s&client_secret=%s",
                tokenEndpoint, apiKey, secretKey
            );

            // 发送请求
            JSONObject jsonResponse = httpClientUtils.sendPostJson(
                tokenUrl,
                "",
                StandardCharsets.UTF_8,
                new Header[]{new BasicHeader("Content-Type", "application/json")}
            );

            // 解析响应
            if (jsonResponse.containsKey("access_token")) {
                accessToken = jsonResponse.getString("access_token");
                int expiresIn = jsonResponse.getIntValue("expires_in");
                tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
                log.info("文心一言访问令牌已刷新，有效期: {} 秒", expiresIn);
            } else {
                throw new ModelException("获取文心一言访问令牌失败: " + jsonResponse.toJSONString());
            }
        } catch (Exception e) {
            log.error("刷新文心一言访问令牌失败", e);
            throw new ModelException("刷新文心一言访问令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建文心一言模型端点URL
     *
     * @param parameters 请求参数
     * @return 完整URL
     */
    private String buildModelEndpointUrl(Map<String, Object> parameters) {
        // 获取模型名称
        String modelName = mappingStrategy.getModelName(parameters);
        if (modelName == null) {
            modelName = getModelName();
        }

        // 根据模型名称构建端点
        String baseUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/";

        return baseUrl + modelName + "?access_token=" + accessToken;
    }
}

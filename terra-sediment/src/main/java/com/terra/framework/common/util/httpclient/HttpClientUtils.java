package com.terra.framework.common.util.httpclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.exception.HttpClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * HTTP客户端工具类
 * 提供基于Apache HttpClient的HTTP请求封装
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class HttpClientUtils implements AutoCloseable {

    private final CloseableHttpClient httpClient;
    private final HttpClientConfig config;
    private final ExecutorService executorService;
    private final boolean ownsHttpClient;


    /**
     * 使用自定义配置创建HttpClientUtils实例
     *
     * @param config 自定义配置
     */
    public HttpClientUtils(HttpClientConfig config) {
        this.config = config;
        this.httpClient = createHttpClient();
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize(), r -> {
            Thread thread = new Thread(r, "terra-http-client-");
            thread.setDaemon(true);
            return thread;
        });
        this.ownsHttpClient = true;
    }

    /**
     * 使用外部提供的HttpClient创建HttpClientUtils实例
     *
     * @param httpClient 外部HttpClient
     * @param config 客户端配置
     */
    public HttpClientUtils(CloseableHttpClient httpClient, HttpClientConfig config) {
        this.httpClient = httpClient;
        this.config = config;
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize(), r -> {
            Thread thread = new Thread(r, "terra-http-client-");
            thread.setDaemon(true);
            return thread;
        });
        this.ownsHttpClient = false;
    }

    /**
     * 创建HttpClient实例
     */
    private CloseableHttpClient createHttpClient() {
        try {
            // 创建连接管理器
            PoolingHttpClientConnectionManager connectionManager = createConnectionManager();

            // 创建HttpClient构建器
            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setConnectionManagerShared(false)
                    .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());

            // 配置重试策略
            if (config.isRetryEnabled()) {
                clientBuilder.setRetryStrategy(new DefaultHttpRequestRetryStrategy(
                        config.getMaxRetryCount(),
                        Timeout.ofSeconds(1)
                ));
            }

            // 配置默认请求参数
            clientBuilder.setDefaultRequestConfig(createRequestConfig());

            return clientBuilder.build();
        } catch (Exception e) {
            log.error("创建HttpClient实例失败", e);
            throw new RuntimeException("创建HttpClient实例失败", e);
        }
    }

    /**
     * 创建连接管理器
     */
    private PoolingHttpClientConnectionManager createConnectionManager() throws Exception {
        PoolingHttpClientConnectionManager connectionManager;

        // 如果需要验证SSL证书，使用默认的SSL上下文
        if (config.isValidateSSLCertificate()) {
            connectionManager = new PoolingHttpClientConnectionManager();
        } else {
            // 否则创建信任所有证书的SSL上下文
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new NoopHostnameVerifier()
            );

            connectionManager = new PoolingHttpClientConnectionManager();
        }

        // 配置连接参数
        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                .setSocketTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                .build();

        connectionManager.setDefaultConnectionConfig(connConfig);
        connectionManager.setMaxTotal(config.getMaxTotalConnections());
        connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerRoute());

        return connectionManager;
    }

    /**
     * 创建请求配置
     */
    private RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                .build();
    }

    /**
     * 发送表单POST请求
     *
     * @param url     请求URL
     * @param params  表单参数
     * @param charset 编码
     * @param token   认证令牌
     * @return JSON响应
     */
    public JSONObject sendPostForm(String url, Map<String, String> params, Charset charset, String token) {
        try {
            return executeRequest(url, req -> {
                HttpPost httpPost = createHttpPost(url);

                // 设置表单参数
                if (params != null && !params.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
                    params.forEach((key, value) -> nameValuePairs.add(new BasicNameValuePair(key, value)));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, charset));
                }

                // 设置请求头
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpPost.setHeader("User-Agent", "Terra-Http-Client/1.0");

                if (StringUtils.isNotEmpty(token)) {
                httpPost.setHeader("Authorization", "Bearer " + token);
            }

                return httpClient.execute(httpPost);
            }, charset);
        } catch (Exception e) {
            handleException(url, e);
            return null; // 不会执行到这里，handleException会抛出异常
        }
    }

    /**
     * 发送JSON POST请求
     *
     * @param url     请求URL
     * @param json    JSON请求体
     * @param charset 编码
     * @param token   认证令牌
     * @return JSON响应
     */
    public JSONObject sendPostJson(String url, String json, Charset charset, String token) {
        try {
            return executeRequest(url, req -> {
                HttpPost httpPost = createHttpPost(url);

                // 设置JSON请求体
                if (StringUtils.isNotEmpty(json)) {
                    StringEntity entity = new StringEntity(json, charset);
                    httpPost.setEntity(entity);
                }

                // 设置请求头
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("User-Agent", "Terra-Http-Client/1.0");

                if (StringUtils.isNotEmpty(token)) {
                httpPost.setHeader("Authorization", "Bearer " + token);
            }

                return httpClient.execute(httpPost);
            }, charset);
        } catch (Exception e) {
            handleException(url, e);
            return null; // 不会执行到这里，handleException会抛出异常
        }
    }

    /**
     * 发送JSON POST请求（自定义请求头）
     *
     * @param url     请求URL
     * @param json    JSON请求体
     * @param charset 编码
     * @param headers 自定义请求头
     * @return JSON响应
     */
    public JSONObject sendPostJson(String url, String json, Charset charset, Header... headers) {
        try {
            return executeRequest(url, req -> {
                HttpPost httpPost = createHttpPost(url);

                // 设置JSON请求体
                if (StringUtils.isNotEmpty(json)) {
                    StringEntity entity = new StringEntity(json, charset);
                    httpPost.setEntity(entity);
                }

                // 设置内容类型（如果没有在headers中指定）
                boolean hasContentType = false;
                if (headers != null) {
                    for (Header header : headers) {
                        if ("Content-Type".equalsIgnoreCase(header.getName())) {
                            hasContentType = true;
                            break;
                        }
                    }
                }

                if (!hasContentType) {
                    httpPost.setHeader("Content-Type", "application/json");
                }

                // 设置用户代理（如果没有在headers中指定）
                boolean hasUserAgent = false;
                if (headers != null) {
                    for (Header header : headers) {
                        if ("User-Agent".equalsIgnoreCase(header.getName())) {
                            hasUserAgent = true;
                            break;
                        }
                    }
                }

                if (!hasUserAgent) {
                    httpPost.setHeader("User-Agent", "Terra-Http-Client/1.0");
                }

                // 设置自定义请求头
                if (headers != null && headers.length > 0) {
            httpPost.setHeaders(headers);
                }

                return httpClient.execute(httpPost);
            }, charset);
        } catch (Exception e) {
            handleException(url, e);
            return null; // 不会执行到这里，handleException会抛出异常
        }
    }

    /**
     * 发送GET请求
     *
     * @param url     请求URL
     * @param charset 编码
     * @param token   认证令牌
     * @return JSON响应
     */
    public JSONObject sendGet(String url, Charset charset, String token) {
        try {
            return executeRequest(url, req -> {
                HttpGet httpGet = createHttpGet(url);

                // 设置请求头
                httpGet.setHeader("User-Agent", "Terra-Http-Client/1.0");

                if (StringUtils.isNotEmpty(token)) {
                httpGet.setHeader("Authorization", "Bearer " + token);
            }

                return httpClient.execute(httpGet);
            }, charset);
        } catch (Exception e) {
            handleException(url, e);
            return null; // 不会执行到这里，handleException会抛出异常
        }
    }

    /**
     * 发送GET请求（自定义请求头）
     *
     * @param url     请求URL
     * @param charset 编码
     * @param headers 自定义请求头
     * @return JSON响应
     */
    public JSONObject sendGet(String url, Charset charset, Header... headers) {
        try {
            return executeRequest(url, req -> {
                HttpGet httpGet = createHttpGet(url);

                // 设置用户代理（如果没有在headers中指定）
                boolean hasUserAgent = false;
                if (headers != null) {
                    for (Header header : headers) {
                        if ("User-Agent".equalsIgnoreCase(header.getName())) {
                            hasUserAgent = true;
                            break;
                        }
                    }
                }

                if (!hasUserAgent) {
                    httpGet.setHeader("User-Agent", "Terra-Http-Client/1.0");
                }

                // 设置自定义请求头
                if (headers != null && headers.length > 0) {
            httpGet.setHeaders(headers);
                }

                return httpClient.execute(httpGet);
            }, charset);
        } catch (Exception e) {
            handleException(url, e);
            return null; // 不会执行到这里，handleException会抛出异常
        }
    }

    /**
     * 发送POST请求并返回字符串结果
     *
     * @param url     请求URL
     * @param json    JSON请求体
     * @param charset 编码
     * @param headers 请求头
     * @return 响应字符串
     */
    public String sendPostJsonForString(String url, String json, Charset charset, Header... headers) {
        try {
            return executeRequestForString(url, req -> {
                HttpPost httpPost = createHttpPost(url);

                // 设置JSON请求体
                if (StringUtils.isNotEmpty(json)) {
                    StringEntity entity = new StringEntity(json, charset);
                    httpPost.setEntity(entity);
                }

                // 设置内容类型（如果没有在headers中指定）
            boolean hasContentType = false;
            if (headers != null) {
                for (Header header : headers) {
                    if ("Content-Type".equalsIgnoreCase(header.getName())) {
                        hasContentType = true;
                        break;
                    }
                }
            }

            if (!hasContentType) {
                httpPost.setHeader("Content-Type", "application/json");
            }

                // 设置自定义请求头
            if (headers != null && headers.length > 0) {
                httpPost.setHeaders(headers);
            }

                return httpClient.execute(httpPost);
            }, charset);
        } catch (Exception e) {
            handleException(url, e);
            return null; // 不会执行到这里，handleException会抛出异常
        }
    }

    /**
     * 流式处理回调接口
     */
    public interface StreamCallback {
        /**
         * 接收数据块
         *
         * @param chunk 数据块内容
         */
        void onData(String chunk);

        /**
         * 完成回调
         */
        void onComplete();

        /**
         * 错误回调
         *
         * @param throwable 异常
         */
        void onError(Throwable throwable);
    }

    /**
     * 发送POST请求并以流式方式处理响应
     *
     * @param url      请求URL
     * @param json     JSON请求体
     * @param charset  字符集
     * @param headers  请求头
     * @param callback 流式回调
     */
    public void sendPostJsonStream(String url, String json, Charset charset, Header[] headers, StreamCallback callback) {
        CompletableFuture.runAsync(() -> {
            HttpPost httpPost = null;
            CloseableHttpResponse response = null;

            try {
                httpPost = createHttpPost(url);

                // 设置JSON请求体
                if (StringUtils.isNotEmpty(json)) {
                    StringEntity entity = new StringEntity(json, charset);
                    httpPost.setEntity(entity);
                }

                // 设置内容类型（如果没有在headers中指定）
                boolean hasContentType = false;
                if (headers != null) {
                    for (Header header : headers) {
                        if ("Content-Type".equalsIgnoreCase(header.getName())) {
                            hasContentType = true;
                            break;
                        }
                    }
                }

                if (!hasContentType) {
                    httpPost.setHeader("Content-Type", "application/json");
                }

                // 设置自定义请求头
                if (headers != null && headers.length > 0) {
                    httpPost.setHeaders(headers);
                }

                // 执行请求
                response = httpClient.execute(httpPost);

                // 检查响应状态
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    try (InputStream inputStream = response.getEntity().getContent();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.isEmpty()) {
                                callback.onData(line);
                            }
                        }
                        callback.onComplete();
                    }
                } else {
                    // 处理错误响应
                    String errorBody = EntityUtils.toString(response.getEntity(), charset);
                    HttpClientException exception;

                    if (statusCode >= 400 && statusCode < 500) {
                        exception = HttpClientException.clientError(url, statusCode, errorBody);
                    } else if (statusCode >= 500) {
                        exception = HttpClientException.serverError(url, statusCode, errorBody);
                    } else {
                        exception = new HttpClientException(
                                HttpClientException.HttpErrorType.UNKNOWN_ERROR,
                                "未知HTTP错误 [" + statusCode + "]: " + errorBody,
                                url,
                                statusCode
                        );
                    }

                    callback.onError(exception);
                }
            } catch (Exception e) {
                HttpClientException exception;

                if (e instanceof SocketTimeoutException) {
                    if (e.getMessage().contains("connect")) {
                        exception = HttpClientException.connectionTimeout(url, e);
                    } else {
                        exception = HttpClientException.readTimeout(url, e);
                    }
                } else {
                    exception = new HttpClientException(
                            HttpClientException.HttpErrorType.UNKNOWN_ERROR,
                            "HTTP请求异常: " + e.getMessage(),
                            url,
                            0,
                            e
                    );
                }

                callback.onError(exception);
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (IOException e) {
                    log.error("关闭HTTP响应失败", e);
                }
            }
        }, executorService);
    }

    /**
     * 创建HttpPost实例
     *
     * @param url 请求URL
     * @return HttpPost实例
     */
    private HttpPost createHttpPost(String url) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(createRequestConfig());
        return httpPost;
    }

    /**
     * 创建HttpGet实例
     *
     * @param url 请求URL
     * @return HttpGet实例
     */
    private HttpGet createHttpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(createRequestConfig());
        return httpGet;
    }

    /**
     * 执行HTTP请求并解析JSON响应
     *
     * @param url      请求URL
     * @param executor HTTP请求执行器
     * @param charset  响应编码
     * @return JSON响应
     * @throws Exception 执行异常
     */
    private JSONObject executeRequest(String url, HttpRequestExecutor executor, Charset charset) throws Exception {
        CloseableHttpResponse response = null;

        try {
            // 执行请求
            response = executor.execute(url);

            // 处理响应
            int statusCode = response.getCode();
            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = EntityUtils.toString(response.getEntity(), charset);
                if (StringUtils.isNotEmpty(responseBody)) {
                    try {
                        return JSON.parseObject(responseBody);
        } catch (Exception e) {
                        log.warn("解析JSON响应失败: {}", responseBody, e);
                        JSONObject errorResult = new JSONObject();
                        errorResult.put("rawResponse", responseBody);
                        errorResult.put("parseError", e.getMessage());
                        return errorResult;
                    }
                } else {
                    return new JSONObject();
                }
            } else {
                // 处理错误响应
                String errorBody = EntityUtils.toString(response.getEntity(), charset);
                if (statusCode >= 400 && statusCode < 500) {
                    throw HttpClientException.clientError(url, statusCode, errorBody);
                } else if (statusCode >= 500) {
                    throw HttpClientException.serverError(url, statusCode, errorBody);
                } else {
                    throw new HttpClientException(
                            HttpClientException.HttpErrorType.UNKNOWN_ERROR,
                            "未知HTTP错误 [" + statusCode + "]: " + errorBody,
                            url,
                            statusCode
                    );
                }
            }
        } finally {
            if (response != null && config.isCloseResponseAfterExecution()) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("关闭HTTP响应失败", e);
                }
            }
        }
    }

    /**
     * 执行HTTP请求并返回字符串响应
     *
     * @param url      请求URL
     * @param executor HTTP请求执行器
     * @param charset  响应编码
     * @return 字符串响应
     * @throws Exception 执行异常
     */
    private String executeRequestForString(String url, HttpRequestExecutor executor, Charset charset) throws Exception {
        CloseableHttpResponse response = null;

        try {
            // 执行请求
            response = executor.execute(url);

            // 处理响应
            int statusCode = response.getCode();
            if (statusCode >= 200 && statusCode < 300) {
                return EntityUtils.toString(response.getEntity(), charset);
            } else {
                // 处理错误响应
                String errorBody = EntityUtils.toString(response.getEntity(), charset);
                if (statusCode >= 400 && statusCode < 500) {
                    throw HttpClientException.clientError(url, statusCode, errorBody);
                } else if (statusCode >= 500) {
                    throw HttpClientException.serverError(url, statusCode, errorBody);
                } else {
                    throw new HttpClientException(
                            HttpClientException.HttpErrorType.UNKNOWN_ERROR,
                            "未知HTTP错误 [" + statusCode + "]: " + errorBody,
                            url,
                            statusCode
                    );
                }
            }
        } finally {
            if (response != null && config.isCloseResponseAfterExecution()) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("关闭HTTP响应失败", e);
                }
            }
        }
    }

    /**
     * 处理异常
     *
     * @param url 请求URL
     * @param e   异常
     */
    private void handleException(String url, Exception e) {
        if (e instanceof HttpClientException) {
            throw (HttpClientException) e;
        } else if (e instanceof SocketTimeoutException) {
            if (e.getMessage().contains("connect")) {
                throw HttpClientException.connectionTimeout(url, e);
            } else {
                throw HttpClientException.readTimeout(url, e);
            }
        } else {
            throw new HttpClientException(
                    HttpClientException.HttpErrorType.UNKNOWN_ERROR,
                    "HTTP请求异常: " + e.getMessage(),
                    url,
                    0,
                    e
            );
        }
    }

    /**
     * HTTP请求执行器接口
     */
    @FunctionalInterface
    private interface HttpRequestExecutor {
        /**
         * 执行HTTP请求
         *
         * @param url 请求URL
         * @return HTTP响应
         * @throws Exception 执行异常
         */
        CloseableHttpResponse execute(String url) throws Exception;
    }

    /**
     * 关闭HTTP客户端和线程池
     */
    @Override
    public void close() {
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 如果HttpClient是由此类创建的，则关闭它
        if (ownsHttpClient && httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("关闭HttpClient失败", e);
            }
        }
    }

    /**
     * 构建HttpClientUtils实例的Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * HttpClientUtils构建器
     */
    public static class Builder {
        private Integer connectTimeout;
        private Integer readTimeout;
        private Integer maxTotalConnections;
        private Integer maxConnectionsPerRoute;
        private Boolean retryEnabled;
        private Integer maxRetryCount;
        private Boolean validateSSLCertificate;
        private Boolean closeResponseAfterExecution;
        private Integer threadPoolSize;

        private Builder() {
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder maxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
            return this;
        }

        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }

        public Builder retryEnabled(boolean retryEnabled) {
            this.retryEnabled = retryEnabled;
            return this;
        }

        public Builder maxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public Builder validateSSLCertificate(boolean validateSSLCertificate) {
            this.validateSSLCertificate = validateSSLCertificate;
            return this;
        }

        public Builder closeResponseAfterExecution(boolean closeResponseAfterExecution) {
            this.closeResponseAfterExecution = closeResponseAfterExecution;
            return this;
        }

        public Builder threadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        public HttpClientUtils build() {
            HttpClientConfig.Builder configBuilder = HttpClientConfig.builder();

            if (connectTimeout != null) configBuilder.connectTimeout(connectTimeout);
            if (readTimeout != null) configBuilder.readTimeout(readTimeout);
            if (maxTotalConnections != null) configBuilder.maxTotalConnections(maxTotalConnections);
            if (maxConnectionsPerRoute != null) configBuilder.maxConnectionsPerRoute(maxConnectionsPerRoute);
            if (retryEnabled != null) configBuilder.retryEnabled(retryEnabled);
            if (maxRetryCount != null) configBuilder.maxRetryCount(maxRetryCount);
            if (validateSSLCertificate != null) configBuilder.validateSSLCertificate(validateSSLCertificate);
            if (closeResponseAfterExecution != null) configBuilder.closeResponseAfterExecution(closeResponseAfterExecution);
            if (threadPoolSize != null) configBuilder.threadPoolSize(threadPoolSize);

            return new HttpClientUtils(configBuilder.build());
        }
    }

    /**
     * 创建连接管理器
     *
     * @param config HTTP客户端配置
     * @return 连接管理器
     */
    public static PoolingHttpClientConnectionManager createConnectionManager(HttpClientConfig config) {
        try {
            PoolingHttpClientConnectionManager connectionManager;

            // 如果需要验证SSL证书，使用默认的SSL上下文
            if (config.isValidateSSLCertificate()) {
                connectionManager = new PoolingHttpClientConnectionManager();
            } else {
                // 否则创建信任所有证书的SSL上下文
                SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                        .build();

                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                        sslContext,
                        new NoopHostnameVerifier()
                );

                connectionManager = new PoolingHttpClientConnectionManager();
            }

            // 配置连接参数
            ConnectionConfig connConfig = ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                    .setSocketTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                    .build();

            connectionManager.setDefaultConnectionConfig(connConfig);
            connectionManager.setMaxTotal(config.getMaxTotalConnections());
            connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerRoute());

            return connectionManager;
        } catch (Exception e) {
            log.error("创建HttpClient连接管理器失败", e);
            throw new RuntimeException("创建HttpClient连接管理器失败", e);
        }
    }

    /**
     * 创建请求配置
     *
     * @param config HTTP客户端配置
     * @return 请求配置
     */
    public static RequestConfig createRequestConfig(HttpClientConfig config) {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                .build();
    }

    /**
     * 创建重试策略
     *
     * @param maxRetryCount 最大重试次数
     * @return 重试策略
     */
    public static DefaultHttpRequestRetryStrategy createRetryStrategy(int maxRetryCount) {
        return new DefaultHttpRequestRetryStrategy(
                maxRetryCount,
                Timeout.ofSeconds(1)
        );
    }
}

package com.terra.framework.common.util.httpclient;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Httpclient工具类，在引用时记得把log等级调成debug或者更低，否则会导致日志打印很多信息 (log4j.logger.org.apache.commons.httpclient=DEBUG)
 *
 * @author ywt
 * @date 2019年2月9日 09:00:02
 * @since 1.3.9
 **/
@Slf4j
public class HttpClientUtils {

    private final CloseableHttpClient closeableHttpClient;
    private final RequestConfig requestConfig;
    private final ExecutorService executorService;

    /**
     * HTTP请求结果接口
     */
    @FunctionalInterface
    private interface HttpResult {
        /**
         * 执行HTTP请求并返回响应
         *
         * @param apiUrl      请求URL
         * @param body        请求体
         * @param contentType 内容类型
         * @return HTTP响应
         * @throws Exception 请求异常
         */
        CloseableHttpResponse apply(URL apiUrl, Object body, ContentType contentType) throws Exception;
    }

    public HttpClientUtils(CloseableHttpClient closeableHttpClient, RequestConfig requestConfig) {
        this.closeableHttpClient = closeableHttpClient;
        this.requestConfig = requestConfig;
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * 表单提交
     *
     * @param url     请求路径
     * @param map     参数K-V
     * @param charset 编码
     * @param token   Token
     * @return 返回结果
     */
    public JSONObject sendPostByFormData(final String url, final Map<String, String> map, final Charset charset, final String token) throws MalformedURLException {
        return getResult(Objects.requireNonNull(sendData(new URL(url), map, ContentType.APPLICATION_FORM_URLENCODED, (apiUrl, body, contentType) -> {
            // 创建post方式请求对象
            HttpPost httpPost = getHttpPost(url);
            // 装填参数
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            // 设置参数到请求对象中
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, charset));
            // 设置header信息
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            httpPost.setHeader("Content-type", contentType);
            if (!StringUtils.isEmpty(token)) {
                httpPost.setHeader("Authorization", "Bearer " + token);
            }
            return HttpClients.createDefault().execute(httpPost);
        })), charset);
    }


    /**
     * body 提交
     *
     * @param url     请求路径
     * @param json    参数
     * @param charset 编码
     * @param token   Token
     * @return 返回结果
     * @throws MalformedURLException
     */
    public JSONObject sendPostDataByJson(final String url, final String json, final Charset charset, final String token) throws MalformedURLException {
        return getResult(Objects.requireNonNull(sendData(new URL(url), json, ContentType.APPLICATION_FORM_URLENCODED, (apiUrl, body, contentType) -> {
            // 创建post方式请求对象
            HttpPost httpPost = getHttpPost(url);
            // 设置参数到请求对象中
            StringEntity stringEntity = new StringEntity(json, charset);
            httpPost.setEntity(stringEntity);
            if (!StringUtils.isEmpty(token)) {
                httpPost.setHeader("Authorization", "Bearer " + token);
            }
            // 设置header信息
            return closeableHttpClient.execute(httpPost);
        })), charset);
    }

    /**
     * body 提交
     *
     * @param url     请求路径
     * @param json    参数
     * @param charset 编码
     * @param headers 请求头
     * @return 返回结果
     * @throws MalformedURLException
     */
    public JSONObject sendPostDataByJson(final String url, final String json, final Charset charset, Header... headers) throws MalformedURLException {
        return getResult(Objects.requireNonNull(sendData(new URL(url), json, ContentType.APPLICATION_FORM_URLENCODED, (apiUrl, body, contentType) -> {
            // 创建post方式请求对象
            HttpPost httpPost = getHttpPost(url);
            // 设置参数到请求对象中
            StringEntity stringEntity = new StringEntity(json, charset);
            httpPost.setEntity(stringEntity);
            httpPost.setHeaders(headers);
            // 设置header信息
            return closeableHttpClient.execute(httpPost);
        })), charset);
    }


    /**
     * GET提交
     *
     * @param url     请求路径
     * @param charset 编码
     * @param token   token
     * @return 返回结果
     * @throws MalformedURLException
     */
    public JSONObject sendGetData(final String url, final Charset charset, final String token) throws MalformedURLException {
        // 通过请求对象获取响应对象
        return getResult(Objects.requireNonNull(sendData(new URL(url), null, null, (apiUrl, body, contentType) -> {
            HttpGet httpGet = getHttpGet(url);
            if (!StringUtils.isEmpty(token)) {
                httpGet.setHeader("Authorization", "Bearer " + token);
            }
            return closeableHttpClient.execute(httpGet);
        })), charset);
    }


    /**
     * GET提交
     *
     * @param url     请求路径
     * @param charset 编码
     * @param headers headers
     * @return 返回结果
     * @throws MalformedURLException
     */
    public JSONObject sendGetData(final String url, final Charset charset, final Header... headers) throws MalformedURLException {
        // 通过请求对象获取响应对象
        return getResult(Objects.requireNonNull(sendData(new URL(url), null, null, (apiUrl, body, contentType) -> {
            HttpGet httpGet = getHttpGet(url);
            httpGet.setHeaders(headers);
            return closeableHttpClient.execute(httpGet);
        })), charset);
    }

    // 新增方法 - 开始

    /**
     * 发送POST请求，返回JSON字符串
     *
     * @param url     请求URL
     * @param json    JSON请求体
     * @param charset 字符集
     * @param headers 请求头
     * @return JSON响应字符串
     */
    public String sendPostJson(final String url, final String json, final Charset charset, final Header... headers) {
        try {
            // 创建post方式请求对象
            HttpPost httpPost = getHttpPost(url);

            // 设置Content-Type
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

            // 设置请求体
            if (json != null && !json.isEmpty()) {
                StringEntity stringEntity = new StringEntity(json, charset);
                httpPost.setEntity(stringEntity);
            }

            // 设置请求头
            if (headers != null && headers.length > 0) {
                httpPost.setHeaders(headers);
            }

            // 执行请求
            try (CloseableHttpResponse response = closeableHttpClient.execute(httpPost)) {
                if (response.getCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity(), charset);
                } else {
                    String errorResponse;
                    try {
                        errorResponse = EntityUtils.toString(response.getEntity(), charset);
                    } catch (ParseException e) {
                        errorResponse = "无法解析响应内容: " + e.getMessage();
                    }
                    log.error("HTTP POST请求失败: URL={}, 状态码={}, 响应={}", url, response.getCode(), errorResponse);
                    throw new IOException("HTTP请求失败: " + errorResponse);
                }
            }
        } catch (IOException e) {
            log.error("发送POST请求异常: URL={}", url, e);
            throw new RuntimeException("发送POST请求异常", e);
        } catch (ParseException e) {
            log.error("解析HTTP响应异常: URL={}", url, e);
            throw new RuntimeException("解析HTTP响应异常", e);
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
    public void sendPostJsonStream(final String url, final String json, final Charset charset,
                                   final Header[] headers, final StreamCallback callback) {
        executorService.execute(() -> {
            HttpPost httpPost = null;
            CloseableHttpResponse response = null;
            try {
                // 创建post方式请求对象
                httpPost = getHttpPost(url);

                // 设置Content-Type
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

                // 设置请求体
                if (json != null && !json.isEmpty()) {
                    StringEntity stringEntity = new StringEntity(json, charset);
                    httpPost.setEntity(stringEntity);
                }

                // 设置请求头
                if (headers != null && headers.length > 0) {
                    httpPost.setHeaders(headers);
                }

                // 执行请求
                response = closeableHttpClient.execute(httpPost);

                if (response.getCode() == HttpStatus.SC_OK) {
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
                    String errorResponse;
                    try {
                        errorResponse = EntityUtils.toString(response.getEntity(), charset);
                    } catch (ParseException e) {
                        errorResponse = "无法解析响应内容: " + e.getMessage();
                    }
                    log.error("HTTP POST流式请求失败: URL={}, 状态码={}, 响应={}", url, response.getCode(), errorResponse);
                    callback.onError(new IOException("HTTP请求失败: " + errorResponse));
                }
            } catch (IOException e) {
                log.error("发送POST流式请求异常: URL={}", url, e);
                callback.onError(e);
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (IOException e) {
                    log.error("关闭HTTP响应异常", e);
                }
            }
        });
    }

    /**
     * 关闭HTTP客户端和线程池
     */
    public void close() {
        try {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (IOException e) {
            log.error("关闭HTTP客户端异常", e);
        }
    }

    // 新增方法 - 结束

    /**
     * 获取HttpPost实体类
     *
     * @param url 路径
     * @return
     */
    private HttpPost getHttpPost(String url) {
        // 创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        return httpPost;
    }


    /**
     * 获取HttpGet实体类
     *
     * @param url 路径
     * @return
     */
    private HttpGet getHttpGet(String url) {
        // 创建post方式请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        return httpGet;
    }

    private CloseableHttpResponse sendData(URL apiUrl, Object body, ContentType contentType, HttpResult httpResult) {
        try {
            return httpResult.apply(apiUrl, body, contentType);
        } catch (Exception e) {
            log.error("http请求错误-处理数据异常", e);
            return null;
        }
    }


    private JSONObject getResult(CloseableHttpResponse response, Charset charset) {
        if (response == null) {
            return null;
        }
        String result = "";
        if (response.getCode() == HttpStatus.SC_OK) {
            try {
                result = EntityUtils.toString(response.getEntity(), charset);
            } catch (IOException | ParseException e) {
                log.error("http请求错误-IO异常", e);
            }
        } else {
            log.error("http请求错误 {}", JSON.toJSONString(response));
        }
        try {
            response.close();
        } catch (IOException e) {
            log.error("http请求错误-释放链接异常", e);
        }
        return JSON.parseObject(result);
    }

}

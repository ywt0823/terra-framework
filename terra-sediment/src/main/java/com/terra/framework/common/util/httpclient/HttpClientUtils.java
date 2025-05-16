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
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public HttpClientUtils(CloseableHttpClient closeableHttpClient, RequestConfig requestConfig) {
        this.closeableHttpClient = closeableHttpClient;
        this.requestConfig = requestConfig;
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
            } catch (Exception e) {
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

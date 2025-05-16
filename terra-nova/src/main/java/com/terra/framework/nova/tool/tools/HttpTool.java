package com.terra.framework.nova.tool.tools;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.tool.AbstractTool;
import com.terra.framework.nova.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP工具，用于发送HTTP请求
 *
 * @author terra-nova
 */
@Slf4j
public class HttpTool extends AbstractTool {
    
    private final HttpClientUtils httpClientUtils;
    
    /**
     * 构造函数
     *
     * @param httpClientUtils HTTP客户端工具
     */
    public HttpTool(HttpClientUtils httpClientUtils) {
        super("http", "发送HTTP请求并获取响应", "network", false, false);
        this.httpClientUtils = httpClientUtils;
    }
    
    @Override
    protected Map<String, ParameterDescription> initializeParameterDescriptions() {
        Map<String, ParameterDescription> params = new HashMap<>();
        
        params.put("url", new ParameterDescription(
                "url",
                "请求URL",
                "string",
                true
        ));
        
        params.put("method", new ParameterDescription(
                "method",
                "HTTP方法，支持GET、POST、PUT、DELETE",
                "string",
                false,
                "GET"
        ));
        
        params.put("headers", new ParameterDescription(
                "headers",
                "HTTP头，格式为JSON字符串",
                "string",
                false
        ));
        
        params.put("body", new ParameterDescription(
                "body",
                "请求体，用于POST和PUT请求",
                "string",
                false
        ));
        
        params.put("timeout", new ParameterDescription(
                "timeout",
                "请求超时时间（毫秒）",
                "integer",
                false,
                "5000"
        ));
        
        return params;
    }
    
    @Override
    protected String doExecute(Map<String, String> parameters) throws ToolExecutionException {
        try {
            String url = parameters.get("url");
            String method = getParameterOrDefault(parameters, "method");
            method = method != null ? method.toUpperCase() : "GET";
            
            String headersJson = getParameterOrDefault(parameters, "headers");
            String body = getParameterOrDefault(parameters, "body");
            
            int timeout;
            try {
                String timeoutStr = getParameterOrDefault(parameters, "timeout");
                timeout = timeoutStr != null ? Integer.parseInt(timeoutStr) : 5000;
            } catch (NumberFormatException e) {
                timeout = 5000;
            }
            
            // 解析头部
            List<Header> headers = parseHeaders(headersJson);
            
            // 执行请求
            switch (method) {
                case "GET":
                    return httpClientUtils.sendGetDataByJson(url, StandardCharsets.UTF_8, headers.toArray(new Header[0])).toJSONString();
                case "POST":
                    return httpClientUtils.sendPostDataByJson(url, body, StandardCharsets.UTF_8, headers.toArray(new Header[0])).toJSONString();
                case "PUT":
                    return httpClientUtils.sendPutDataByJson(url, body, StandardCharsets.UTF_8, headers.toArray(new Header[0])).toJSONString();
                case "DELETE":
                    return httpClientUtils.sendDeleteDataByJson(url, StandardCharsets.UTF_8, headers.toArray(new Header[0])).toJSONString();
                default:
                    throw new ToolExecutionException(getName(), "UNSUPPORTED_METHOD", "不支持的HTTP方法: " + method);
            }
        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolExecutionException(
                    getName(),
                    "HTTP_REQUEST_ERROR",
                    "HTTP请求失败: " + e.getMessage(),
                    e
            );
        }
    }
    
    /**
     * 解析HTTP头
     *
     * @param headersJson 头部JSON字符串
     * @return HTTP头列表
     */
    private List<Header> parseHeaders(String headersJson) {
        List<Header> headers = new ArrayList<>();
        
        // 默认添加一个Content-Type头
        headers.add(new BasicHeader("Content-Type", "application/json"));
        
        // 如果没有提供头部JSON，返回默认头部
        if (headersJson == null || headersJson.isEmpty()) {
            return headers;
        }
        
        try {
            Map<String, Object> headersMap = com.alibaba.fastjson.JSON.parseObject(headersJson);
            for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                String name = entry.getKey();
                String value = String.valueOf(entry.getValue());
                headers.add(new BasicHeader(name, value));
            }
        } catch (Exception e) {
            log.warn("解析HTTP头部失败: {}", e.getMessage());
            // 出错时返回默认头部
        }
        
        return headers;
    }
} 
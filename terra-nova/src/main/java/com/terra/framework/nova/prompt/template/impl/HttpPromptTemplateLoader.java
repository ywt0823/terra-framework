package com.terra.framework.nova.prompt.template.impl;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import com.terra.framework.nova.prompt.template.PromptTemplateLoader;
import com.terra.framework.nova.prompt.template.TemplateEngine;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于HTTP远程加载的提示词模板加载器
 * 可以从远程API服务器加载模板内容
 *
 * @author terra-nova
 */
public class HttpPromptTemplateLoader implements PromptTemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(HttpPromptTemplateLoader.class);

    /**
     * HTTP客户端工具
     */
    private final HttpClientUtils httpClient;

    /**
     * 模板引擎
     */
    private final TemplateEngine engine;

    /**
     * 模板服务基础URL
     */
    private final String baseUrl;

    /**
     * 模板路径模式，{id}会被替换为模板ID
     */
    private final String templatePathPattern;

    /**
     * 认证信息，如API密钥等
     */
    private final Map<String, String> authHeaders;

    /**
     * 模板缓存
     */
    private final Map<String, CacheEntry> templateCache = new ConcurrentHashMap<>();

    /**
     * 缓存过期时间（毫秒）
     */
    private final long cacheTtlMillis;

    /**
     * 缓存条目，包含内容和过期时间
     */
    private static class CacheEntry {
        final PromptTemplate template;
        final long expiryTimeMillis;

        CacheEntry(PromptTemplate template, long expiryTimeMillis) {
            this.template = template;
            this.expiryTimeMillis = expiryTimeMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTimeMillis;
        }
    }

    /**
     * 构造函数
     *
     * @param httpClient HTTP客户端工具
     * @param engine     模板引擎
     * @param baseUrl    模板服务基础URL
     */
    public HttpPromptTemplateLoader(HttpClientUtils httpClient, TemplateEngine engine, String baseUrl) {
        this(httpClient, engine, baseUrl, "/templates/{id}", null, TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * 构造函数
     *
     * @param httpClient          HTTP客户端工具
     * @param engine              模板引擎
     * @param baseUrl             模板服务基础URL
     * @param templatePathPattern 模板路径模式
     * @param authHeaders         认证头信息
     * @param cacheTtlMillis      缓存过期时间（毫秒）
     */
    public HttpPromptTemplateLoader(
        HttpClientUtils httpClient,
        TemplateEngine engine,
        String baseUrl,
        String templatePathPattern,
        Map<String, String> authHeaders,
        long cacheTtlMillis) {
        this.httpClient = Objects.requireNonNull(httpClient, "HttpClientUtils cannot be null");
        this.engine = Objects.requireNonNull(engine, "TemplateEngine cannot be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        this.templatePathPattern = templatePathPattern != null ? templatePathPattern : "/templates/{id}";
        this.authHeaders = authHeaders;
        this.cacheTtlMillis = cacheTtlMillis > 0 ? cacheTtlMillis : TimeUnit.MINUTES.toMillis(5);
    }

    @Override
    public PromptTemplate load(String templateId) {
        // 检查缓存
        CacheEntry cacheEntry = templateCache.get(templateId);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            return cacheEntry.template;
        }

        // 缓存不存在或已过期，从远程加载
        PromptTemplate template = loadFromRemote(templateId);

        // 更新缓存
        long expiryTime = System.currentTimeMillis() + cacheTtlMillis;
        templateCache.put(templateId, new CacheEntry(template, expiryTime));

        return template;
    }

    /**
     * 从远程服务器加载模板
     *
     * @param templateId 模板ID
     * @return 加载的模板
     * @throws IllegalArgumentException 如果模板不存在
     * @throws RuntimeException         如果加载失败
     */
    private PromptTemplate loadFromRemote(String templateId) {
        String url = buildTemplateUrl(templateId);

        try {
            log.debug("Loading template from remote: {}", url);

            List<Header> headers = new ArrayList<>();
            if (authHeaders != null) {
                authHeaders.forEach((key, value) -> headers.add(new BasicHeader(key, value)));
            }

            String response = httpClient.sendPostJson(
                url,
                null,
                StandardCharsets.UTF_8,
                headers.toArray(new Header[0])
            ).toJSONString();

            if (StringUtils.hasText(response)) {
                return new StringPromptTemplate(response, engine);
            }

            log.error("Failed to load template: {}, response was empty", templateId);
            throw new IllegalArgumentException("Template not found or empty: " + templateId);

        } catch (Exception e) {
            log.error("Error loading template from remote: {}", templateId, e);
            throw new RuntimeException("Failed to load template: " + templateId, e);
        }
    }

    /**
     * 构建模板URL
     *
     * @param templateId 模板ID
     * @return 完整的模板URL
     */
    private String buildTemplateUrl(String templateId) {
        return baseUrl + templatePathPattern.replace("{id}", templateId);
    }

    /**
     * 清除模板缓存
     */
    public void clearCache() {
        templateCache.clear();
        log.info("Template cache cleared");
    }

    /**
     * 移除指定模板的缓存
     *
     * @param templateId 模板ID
     */
    public void invalidateCache(String templateId) {
        templateCache.remove(templateId);
        log.debug("Cache invalidated for template: {}", templateId);
    }

    /**
     * 设置新的缓存过期时间
     *
     * @param ttlMillis 新的过期时间（毫秒）
     */
    public void setCacheTtl(long ttlMillis) {
        // 这会影响新缓存的添加，不会影响已有缓存
        // 要更新现有缓存，需要调用clearCache()
    }
}

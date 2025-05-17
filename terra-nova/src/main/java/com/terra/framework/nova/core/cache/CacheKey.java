package com.terra.framework.nova.core.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.terra.framework.nova.core.model.Message;

/**
 * 缓存键生成器
 *
 * @author terra-nova
 */
public class CacheKey {

    private static final String MODEL_FIELD = "model";
    private static final String TEMPERATURE_FIELD = "temperature";
    private static final String TOP_P_FIELD = "top_p";

    /**
     * 参数中需要包含在缓存键中的字段
     */
    private static final List<String> CACHE_KEY_PARAM_FIELDS = List.of(
            MODEL_FIELD,
            TEMPERATURE_FIELD,
            TOP_P_FIELD,
            "max_tokens",
            "presence_penalty",
            "frequency_penalty",
            "seed"
    );

    /**
     * 为提示词生成缓存键
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 缓存键
     */
    public static String forPrompt(String prompt, Map<String, Object> parameters) {
        StringBuilder builder = new StringBuilder();

        // 添加提示词
        builder.append("prompt:").append(prompt).append("|");

        // 添加重要参数
        builder.append(extractRelevantParams(parameters));

        return hash(builder.toString());
    }

    /**
     * 为消息列表生成缓存键
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 缓存键
     */
    public static String forMessages(List<Message> messages, Map<String, Object> parameters) {
        StringBuilder builder = new StringBuilder();

        // 添加消息列表
        builder.append("messages:");
        for (Message message : messages) {
            builder.append(message.getRole().name())
                   .append(":")
                   .append(message.getContent())
                   .append("|");
        }

        // 添加重要参数
        builder.append(extractRelevantParams(parameters));

        return hash(builder.toString());
    }

    /**
     * 提取关键参数
     *
     * @param parameters 参数
     * @return 参数字符串
     */
    private static String extractRelevantParams(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }

        // 使用TreeMap确保参数排序一致
        Map<String, Object> relevantParams = new TreeMap<>();

        for (String field : CACHE_KEY_PARAM_FIELDS) {
            if (parameters.containsKey(field)) {
                relevantParams.put(field, parameters.get(field));
            }
        }

        return relevantParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
    }

    /**
     * 计算字符串的SHA-256哈希值
     *
     * @param input 输入字符串
     * @return Base64编码的哈希值
     */
    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // 回退到简单的字符串表示
            return String.valueOf(input.hashCode());
        }
    }
}

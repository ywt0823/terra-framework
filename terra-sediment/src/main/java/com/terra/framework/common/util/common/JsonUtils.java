package com.terra.framework.common.util.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * JSON与对象互转帮助类.
 * <p>
 * 这是一个纯粹的工具类，不依赖于Spring框架.
 * 它需要通过外部调用 {@link #init(ObjectMapper)} 方法来初始化内部的ObjectMapper实例.
 * 在Spring环境中，这个初始化过程应该由一个Spring-aware的模块（如terra-bedrock）来完成.
 * </p>
 *
 * @author Terra Framework Team
 * @since 2025-06-01
 */
@Slf4j
public final class JsonUtils {

    private static ObjectMapper objectMapper;

    private JsonUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 初始化工具类，注入ObjectMapper实例.
     * 此方法应该在应用程序启动时被调用一次.
     *
     * @param objectMapper an object mapper
     */
    public static void init(ObjectMapper objectMapper) {
        if (JsonUtils.objectMapper == null) {
            JsonUtils.objectMapper = objectMapper;
            log.info("JsonUtils initialized with custom ObjectMapper.");
        }
    }

    private static void ensureInitialized() {
        Objects.requireNonNull(objectMapper, "JsonUtils has not been initialized. " +
                "Please call JsonUtils.init(objectMapper) at application startup.");
    }

    /**
     * 获取内部使用的ObjectMapper实例（主要用于框架内部需要传递mapper的场景）.
     *
     * @return {@link ObjectMapper} a {@link com.fasterxml.jackson.databind.ObjectMapper} object
     */
    public static ObjectMapper getObjectMapper() {
        ensureInitialized();
        return objectMapper;
    }

    /**
     * 创建空的JsonNode对象.
     *
     * @return a {@link com.fasterxml.jackson.databind.node.ObjectNode} object
     */
    public static ObjectNode createObjectNode() {
        ensureInitialized();
        return objectMapper.createObjectNode();
    }

    /**
     * 创建空的JsonNode数组.
     *
     * @return a {@link com.fasterxml.jackson.databind.node.ArrayNode} object
     */
    public static ArrayNode createArrayNode() {
        ensureInitialized();
        return objectMapper.createArrayNode();
    }

    /**
     * json字符串转为对象.
     *
     * @param json  json
     * @param clazz T类的class文件
     * @param <T>   泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T jsonCovertToObject(String json, Class<T> clazz) {
        ensureInitialized();
        if (json == null || clazz == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json转换失败: {}, 原因: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * json字符串转为对象.
     *
     * @param json json
     * @param type 对象在Jackson中的类型
     * @param <T>  泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T jsonCovertToObject(String json, TypeReference<T> type) {
        ensureInitialized();
        if (json == null || type == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            log.error("json转换失败: {}, 原因: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * 将流中的数据转为java对象.
     *
     * @param inputStream 输入流
     * @param clazz       类的class
     * @param <T>         泛型, 代表返回参数的类型
     * @return 返回对象 如果参数任意一个为 null则返回null
     */
    public static <T> T covertStreamToObject(InputStream inputStream, Class<T> clazz) {
        ensureInitialized();
        if (inputStream == null || clazz == null) {
            return null;
        }
        try {
            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            log.error("json转换失败, 原因: {}", e.getMessage());
        }
        return null;
    }

    /**
     * json字符串转为复杂类型List.
     *
     * @param json            json
     * @param collectionClazz 集合的class
     * @param elementsClazz   集合中泛型的class
     * @param <T>             泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T jsonCovertToObject(String json, Class<?> collectionClazz, Class<?>... elementsClazz) {
        ensureInitialized();
        if (json == null || collectionClazz == null || elementsClazz == null || elementsClazz.length == 0) {
            return null;
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClazz, elementsClazz);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            log.error("json转换失败: {}, 原因: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * 对象转为json字符串.
     *
     * @param o 将要转化的对象
     * @return 返回json字符串
     */
    public static String objectCovertToJson(Object o) {
        ensureInitialized();
        if (o == null) {
            return null;
        }
        try {
            return o instanceof String ? (String) o : objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("json转换失败, 原因: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 对象转为格式化的json字符串（用于调试）.
     *
     * @param o 将要转化的对象
     * @return 返回格式化的json字符串
     */
    public static String objectCovertToPrettyJson(Object o) {
        ensureInitialized();
        if (o == null) {
            return null;
        }
        try {
            return o instanceof String
                    ? (String) o
                    : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("json转换失败, 原因: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 将对象转为另一个对象.
     * 切记,两个对象结构要一致
     * 多用于Object转为具体的对象
     *
     * @param o               将要转化的对象
     * @param collectionClazz 集合的class
     * @param elementsClazz   集合中泛型的class
     * @param <T>             泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T objectCovertToObject(Object o, Class<?> collectionClazz, Class<?>... elementsClazz) {
        String json = objectCovertToJson(o);
        return jsonCovertToObject(json, collectionClazz, elementsClazz);
    }

    /**
     * 将对象转为Map.
     *
     * @param o 将要转化的对象
     * @return 返回Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(Object o) {
        ensureInitialized();
        if (o == null) {
            return null;
        }
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        return jsonCovertToObject(objectCovertToJson(o), new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 将Map转为指定类型的对象.
     *
     * @param map   Map对象
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 转换后的对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || clazz == null) {
            return null;
        }
        return jsonCovertToObject(objectCovertToJson(map), clazz);
    }

    /**
     * 合并两个JSON对象.
     *
     * @param source 源JSON对象
     * @param target 目标JSON对象，合并后的结果会更新到这个对象
     * @return 合并后的JSON对象
     */
    public static ObjectNode merge(ObjectNode source, ObjectNode target) {
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }

        source.fields().forEachRemaining(entry -> target.set(entry.getKey(), entry.getValue()));
        return target;
    }

    /**
     * 检查字符串是否为有效的JSON.
     *
     * @param json 要检查的JSON字符串
     * @return 是否为有效的JSON
     */
    public static boolean isValidJson(String json) {
        ensureInitialized();
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

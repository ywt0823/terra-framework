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

/**
 * <p>
 * JSON与对象互转帮助类
 * </p>
 *
 * @param objectMapper -- GETTER --
 *                     获取内部使用的ObjectMapper
 * @author Terra Framework Team
 * @since 2025-06-01
 */
@Slf4j
public record JsonUtils(ObjectMapper objectMapper) {

    /**
     * 默认ObjectMapper实例
     */
    private static ObjectMapper DEFAULT_MAPPER;

    /**
     * 自定义ObjectMapper缓存
     * key: 配置标识，value: 对应的ObjectMapper实例
     */
    private static final Map<String, ObjectMapper> MAPPER_CACHE = new ConcurrentHashMap<>();

    /**
     * 单例实例
     */
    private static JsonUtils INSTANCE;

    /**
     * 为支持Spring自动注入提供的构造方法
     *
     * @param objectMapper 通过Spring注入的ObjectMapper
     */
    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        DEFAULT_MAPPER = objectMapper;
        INSTANCE = this;
    }

    /**
     * 获取JsonUtils实例（兼容旧代码）
     */
    public static JsonUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (JsonUtils.class) {
                if (INSTANCE == null) {
                    DEFAULT_MAPPER = createDefaultMapper();
                    INSTANCE = new JsonUtils(DEFAULT_MAPPER);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 创建默认配置的ObjectMapper
     */
    public static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 序列化配置
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 反序列化配置
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 添加Java8序列化支持和新版时间对象序列化支持
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    /**
     * 获取配置了特定序列化包含策略的ObjectMapper
     *
     * @param inclusion 序列化包含策略
     * @return ObjectMapper实例
     */
    public static ObjectMapper getMapperWithInclusion(JsonInclude.Include inclusion) {
        String key = "inclusion:" + inclusion.name();
        return MAPPER_CACHE.computeIfAbsent(key, k -> {
            ObjectMapper mapper = createDefaultMapper();
            mapper.setSerializationInclusion(inclusion);
            return mapper;
        });
    }

    /**
     * 获取忽略未知属性的ObjectMapper
     *
     * @param ignoreUnknown 是否忽略未知属性
     * @return ObjectMapper实例
     */
    public static ObjectMapper getMapperWithIgnoreUnknown(boolean ignoreUnknown) {
        String key = "ignoreUnknown:" + ignoreUnknown;
        return MAPPER_CACHE.computeIfAbsent(key, k -> {
            ObjectMapper mapper = createDefaultMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, !ignoreUnknown);
            return mapper;
        });
    }

    /**
     * 创建空的JsonNode对象
     */
    public static ObjectNode createObjectNode() {
        return DEFAULT_MAPPER.createObjectNode();
    }

    /**
     * 创建空的JsonNode数组
     */
    public static ArrayNode createArrayNode() {
        return DEFAULT_MAPPER.createArrayNode();
    }

    /**
     * json字符串转为对象
     *
     * @param json  json
     * @param clazz T类的class文件
     * @param <T>   泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T jsonCovertToObject(String json, Class<T> clazz) {
        if (json == null || clazz == null) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json转换失败: {}, 原因: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * json字符串转为对象
     *
     * @param json json
     * @param type 对象在Jackson中的类型
     * @param <T>  泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T jsonCovertToObject(String json, TypeReference<T> type) {
        if (json == null || type == null) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            log.error("json转换失败: {}, 原因: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * 将流中的数据转为java对象
     *
     * @param inputStream 输入流
     * @param clazz       类的class
     * @param <T>         泛型, 代表返回参数的类型
     * @return 返回对象 如果参数任意一个为 null则返回null
     */
    public static <T> T covertStreamToObject(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null || clazz == null) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            log.error("json转换失败, 原因: {}", e.getMessage());
        }
        return null;
    }

    /**
     * json字符串转为复杂类型List
     *
     * @param json            json
     * @param collectionClazz 集合的class
     * @param elementsClazz   集合中泛型的class
     * @param <T>             泛型, 代表返回参数的类型
     * @return 返回T的实例
     */
    public static <T> T jsonCovertToObject(String json, Class<?> collectionClazz, Class<?>... elementsClazz) {
        if (json == null || collectionClazz == null || elementsClazz == null || elementsClazz.length == 0) {
            return null;
        }
        try {
            JavaType javaType = DEFAULT_MAPPER.getTypeFactory().constructParametricType(collectionClazz, elementsClazz);
            return DEFAULT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            log.error("json转换失败: {}, 原因: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * 对象转为json字符串
     *
     * @param o 将要转化的对象
     * @return 返回json字符串
     */
    public static String objectCovertToJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return o instanceof String ? (String) o : DEFAULT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("json转换失败, 原因: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 对象转为格式化的json字符串（用于调试）
     *
     * @param o 将要转化的对象
     * @return 返回格式化的json字符串
     */
    public static String objectCovertToPrettyJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return o instanceof String
                ? (String) o
                : DEFAULT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("json转换失败, 原因: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 将对象转为另一个对象
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
     * 将对象转为Map
     *
     * @param o 将要转化的对象
     * @return 返回Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(Object o) {
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
     * 将Map转为指定类型的对象
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
     * 合并两个JSON对象
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
     * 检查字符串是否为有效的JSON
     *
     * @param json 要检查的JSON字符串
     * @return 是否为有效的JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            DEFAULT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

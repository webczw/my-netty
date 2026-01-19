package com.webczw.my.netty.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 工具类
 * <p>
 * 基于 Jackson 实现 POJO 与 JSON 字符串之间的转换
 *
 * @author webczw
 */
@Slf4j
public final class JsonUtil {

    /**
     * Jackson ObjectMapper 实例
     */
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // 美化输出，格式化 JSON
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 忽略未知属性
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtil() {
        throw new AssertionError("JsonUtil 不支持实例化");
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 要转换的对象
     * @return JSON 字符串，转换失败返回 null
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转换为 JSON 字符串失败", e);
            return null;
        }
    }

    /**
     * 将对象转换为 JSON 字符串（美化格式）
     *
     * @param obj 要转换的对象
     * @return 美化格式的 JSON 字符串，转换失败返回 null
     */
    public static String toJsonPretty(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转换为美化的 JSON 字符串失败", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的对象
     *
     * @param json     JSON 字符串
     * @param clazz    目标类型
     * @param <T>      泛型类型
     * @return 转换后的对象，转换失败返回 null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 字符串转换为对象失败", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为复杂类型的对象
     *
     * @param json           JSON 字符串
     * @param typeReference  类型引用（用于处理泛型）
     * @param <T>            泛型类型
     * @return 转换后的对象，转换失败返回 null
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON 字符串转换为复杂类型对象失败", e);
            return null;
        }
    }

    /**
     * 检查字符串是否为有效的 JSON
     *
     * @param json 要检查的字符串
     * @return 如果是有效的 JSON 格式返回 true，否则返回 false
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
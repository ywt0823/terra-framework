package com.terra.framework.nova.model;

/**
 * 消息角色枚举
 *
 * @author terra-nova
 */
public enum MessageRole {
    /**
     * 系统角色
     */
    SYSTEM("system"),

    /**
     * 用户角色
     */
    USER("user"),

    /**
     * 助手角色
     */
    ASSISTANT("assistant"),

    /**
     * 人类角色（Claude特有）
     */
    HUMAN("human"),

    /**
     * 工具角色
     */
    TOOL("tool"),

    /**
     * 函数角色
     */
    FUNCTION("function");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值获取角色枚举
     *
     * @param value 角色值字符串
     * @return 对应的角色枚举，如果不存在则返回USER
     */
    public static MessageRole fromValue(String value) {
        for (MessageRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }
}

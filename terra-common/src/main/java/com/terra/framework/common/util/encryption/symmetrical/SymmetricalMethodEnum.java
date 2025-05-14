package com.terra.framework.common.util.encryption.symmetrical;


/**
 * @author ywt
 * @description
 * @date 2021年08月19日 16:25
 */
public enum SymmetricalMethodEnum {

    /**
     * PBE加密方式
     */
    PBE(1, "PBE"),
    AES(2, "AES");

    private final Integer code;
    private final String msg;

    SymmetricalMethodEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

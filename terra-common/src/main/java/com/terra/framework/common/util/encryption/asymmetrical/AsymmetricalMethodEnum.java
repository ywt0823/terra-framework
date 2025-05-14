package com.terra.framework.common.util.encryption.asymmetrical;


/**
 * @author ywt
 * @description
 * @date 2021年08月19日 16:36
 */
public enum AsymmetricalMethodEnum  {


    /**
     * PBE加密方式
     */
    RSA(1, "RSA")
    ;

    private final Integer code;
    private final String msg;

    AsymmetricalMethodEnum(Integer code, String msg) {
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

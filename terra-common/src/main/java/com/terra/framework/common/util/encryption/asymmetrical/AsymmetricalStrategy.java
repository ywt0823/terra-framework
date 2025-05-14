package com.terra.framework.common.util.encryption.asymmetrical;

/**
 * @author ywt
 * @description 非对称性加密策略接口
 * @date 2021年08月19日 11:47
 */
public interface AsymmetricalStrategy {


    /**
     * 加密
     *
     * @param password 密码
     * @return 加密后的字符串
     */
    String encrypt(String password);

    /**
     * 解密
     *
     * @param encryptionPassword 加密密码
     * @return 解密后的密码
     */
    String decrypt(String encryptionPassword);

}

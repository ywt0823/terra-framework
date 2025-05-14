package com.terra.framework.common.util.encryption.symmetrical;


/**
 * @author ywt
 * @description 对称加密上下文
 * @date 2021年08月19日 16:22
 */
public final class SymmetricalContext {

    /**
     * 加密
     *
     * @param password 密码
     * @return 加密后的字符串
     */
    String encrypt(String password, SymmetricalMethodEnum symmetricalMethodEnum) {
        SymmetricalStrategy symmetricalStrategy;
        return switch (symmetricalMethodEnum) {
            case PBE -> {
                symmetricalStrategy = PBEUtils.getInstance();
                yield symmetricalStrategy.encrypt(password);
            }
            case AES -> {
                symmetricalStrategy = AESUtils.getInstance();
                yield symmetricalStrategy.encrypt(password);
            }
        };
    }

    /**
     * 解密
     *
     * @param encryptionPassword 加密密码
     * @return 解密后的密码
     */
    String decrypt(String encryptionPassword, SymmetricalMethodEnum symmetricalMethodEnum) {
        SymmetricalStrategy symmetricalStrategy;
        return switch (symmetricalMethodEnum) {
            case PBE -> {
                symmetricalStrategy = PBEUtils.getInstance();
                yield symmetricalStrategy.decrypt(encryptionPassword);
            }
            case AES -> {
                symmetricalStrategy = AESUtils.getInstance();
                yield symmetricalStrategy.decrypt(encryptionPassword);
            }
        };
    }
}

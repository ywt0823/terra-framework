package com.terra.framework.common.util.encryption.symmetrical;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

/**
 * @author ywt
 * @description PBE加密方式工具类
 * @date 2021年08月19日 14:10
 */
final class PBEUtils implements SymmetricalStrategy {
    /**
     * 加密方式（不可修改）
     */
    private final static String KEY_PBE = "PBEWithMD5AndTripleDES";

    /**
     * 加密盐
     */
    private static final byte[] SALT = "valhalla".getBytes(StandardCharsets.UTF_8);

    private final static int SALT_COUNT = 100;

    private static PBEUtils PASSWORD_BASED_ENCRYPTION_UTILS;

    private PBEUtils() {
    }

    public static synchronized PBEUtils getInstance() {
        if (PASSWORD_BASED_ENCRYPTION_UTILS == null) {
            PASSWORD_BASED_ENCRYPTION_UTILS = new PBEUtils();
        }
        return PASSWORD_BASED_ENCRYPTION_UTILS;
    }

    /**
     * 转换密钥
     */
    private static Key stringToKey() {
        SecretKey secretKey = null;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(KEY_PBE.toCharArray());
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_PBE);
            secretKey = factory.generateSecret(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return secretKey;
    }

    @Override
    public String encrypt(String password) {
        byte[] result = null;
        try {
            // 获取密钥
            Key k = stringToKey();
            PBEParameterSpec parameterSpec = new PBEParameterSpec(SALT, SALT_COUNT);
            Cipher cipher = Cipher.getInstance(KEY_PBE);
            cipher.init(Cipher.ENCRYPT_MODE, k, parameterSpec);
            result = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    public String decrypt(String encryptionPassword) {
        byte[] bytes = null;
        try {
            // 获取密钥
            Key k = stringToKey();
            PBEParameterSpec parameterSpec = new PBEParameterSpec(SALT, SALT_COUNT);
            Cipher cipher = Cipher.getInstance(KEY_PBE);
            cipher.init(Cipher.DECRYPT_MODE, k, parameterSpec);
            bytes = cipher.doFinal(Base64.getDecoder().decode(encryptionPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return new String(Objects.requireNonNull(bytes));
    }

}

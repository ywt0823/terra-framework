package com.terra.framework.common.util.encryption.symmetrical;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * @author ywt
 * @description
 * @date 2021年08月19日 17:00
 */
 final class AESUtils implements SymmetricalStrategy {

    private static AESUtils aesUtils;

    private AESUtils() {
    }

    public static synchronized AESUtils getInstance() {
        if (aesUtils == null) {
            aesUtils = new AESUtils();
        }
        return aesUtils;
    }

    /**
     * 密钥
     * AES加密要求key必须要128个比特位（这里需要长度为16，否则会报错）
     */
    private static final String KEY = "1234567887654321";

    private static final String ALGORITHMS = "AES/ECB/PKCS5Padding";

    @Override
    public String encrypt(String password) {
        byte[] result = null;
        try {
            KeyGenerator kGen = KeyGenerator.getInstance("AES");
            kGen.init(128);
            Cipher cipher = Cipher.getInstance(ALGORITHMS);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY.getBytes(), "AES"));
            result = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | NoSuchPaddingException | BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    public String decrypt(String encryptionPassword) {
        byte[] bytes = null;
        try {
            // 获取密钥
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            Cipher cipher = Cipher.getInstance(ALGORITHMS);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY.getBytes(), "AES"));
            bytes = cipher.doFinal(Base64.getDecoder().decode(encryptionPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return new String(Objects.requireNonNull(bytes));
    }


}

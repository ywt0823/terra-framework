package com.terra.framework.common.util.encryption.asymmetrical;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author ywt
 * @description RSA工具类
 * @date 2021年08月20日 9:14
 */
public final class RSAUtils implements AsymmetricalStrategy {

    private static RSAUtils rsaUtils;

    private RSAUtils() {
    }

    public static synchronized RSAUtils getInstance() {
        if (rsaUtils == null) {
            rsaUtils = new RSAUtils();
        }
        return rsaUtils;
    }

    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDZ9klZhE9FUBvfDQizIgPKfxYWStsqzEXrwZwmG3CDAtwf5u1KdGhwGS/d7ii+AWQhOs4bVAO2qjB4c51yEQP+Z/+DwJNeW8DsPTYME+xNr4UQrIYvzIsFUVzwFp0LQfxOHJRBlfSHEJrIIBUHQJCWm/ZDmxrTU4Fo1hNw6lAO5QIDAQAB";
    private static final String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANn2SVmET0VQG98NCLMiA8p/FhZK2yrMRevBnCYbcIMC3B/m7Up0aHAZL93uKL4BZCE6zhtUA7aqMHhznXIRA/5n/4PAk15bwOw9NgwT7E2vhRCshi/MiwVRXPAWnQtB/E4clEGV9IcQmsggFQdAkJab9kObGtNTgWjWE3DqUA7lAgMBAAECgYBSDBHkbpfggZ1cQ23I8L74lQpdbeQ+ohKYApOjDKU3y/XM1ILyigteVzzbj/EqRR/KGnbHdsLHGh2Q2gSkkZ8sjR7FDdn0IH8GP7Inh+rgQnvzZwMqWz0IZejuhTrslEMs/OZK7angEVQW0nSbBXoiGqxsHFrh9X57Jm4RPIgSaQJBAPnX7uO/ZRoTyo5DpCOaXHPHVywKNJZKK03ONlhsUQFLUNcDln04LLdaAbE7Xzq4AtuTACSatu7+fTaKRnCPW8sCQQDfVT0GgyMiI5uwOr3Wr0qwhXuKlejXKkEcGHxfLzw47pL4HmtsiCjWhbKLicJRNl5dLasu3kSxYSWkS+yqA0oPAkEAwSqwaqmaE3Vl0GR/zX8VJ/bhcQ1a5y/lFzzuS6NDZgrDL9p4Scxa1k/iqxLKZ+3JV9bp72MnONTOHpL+LxzHzQJAJHL86rssWL4HAtQTd55USkr+xreAsN0skzFL2x0SbxhA1E3iqmvuflSDt1JcbHS7Ien6LkQIYgrA4feiQH0Y8QJAEilAQIgfm7Mr3noDmidR0akpVR5GrpVdWxzd12YE4OJc2G8JbPdFAAr4hSnH4UIvvE9LcmepQ/jTLiZVGM8xig==";


    @Override
    public String encrypt(String password) {
        String outStr = "";
        try {
            //base64编码的公钥
            byte[] decoded = Base64.decodeBase64(publicKey);
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] result = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            outStr = Base64.encodeBase64String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outStr;
    }

    @Override
    public String decrypt(String encryptionPassword) {
        String outStr = "";
        try {
            //64位解码加密后的字符串
            byte[] inputByte = Base64.decodeBase64(encryptionPassword.getBytes(StandardCharsets.UTF_8));
            //base64编码的私钥
            byte[] decoded = Base64.decodeBase64(privateKey);
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
            //RSA解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            outStr = new String(cipher.doFinal(inputByte));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outStr;
    }


}

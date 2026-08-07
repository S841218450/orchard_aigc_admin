package com.example.orchardauth.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * RSA 非对称加密工具类
 * <p>
 * 负责管理密钥对、提供公钥给前端、解密前端加密的密码。
 * 密钥对在首次访问时自动生成，生命周期跟随应用进程。
 */
public class RsaUtil {

    private static final int KEY_SIZE = 2048;

    private static final KeyPair KEY_PAIR;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            KEY_PAIR = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA 密钥对生成失败", e);
        }
    }

    /**
     * 获取 Base64 编码的公钥字符串（PEM 格式，供前端 jsencrypt 使用）
     */
    public static String getPublicKeyBase64() {
        String encoded = Base64.getEncoder().encodeToString(KEY_PAIR.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }

    /**
     * 使用 RSA 私钥解密前端传来的密文
     *
     * @param encryptedText Base64 编码的密文（jsencrypt 输出）
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, KEY_PAIR.getPrivate());
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA 解密失败，请确认密码加密方式正确", e);
        }
    }
}

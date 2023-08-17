package cn.webank.wedpr.pir.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
// import java.math.BigInteger;

@Service
public class AESEncService {

    public static String generateRandomKey() {
        // 随机生成 16 位字符串格式的密钥
        byte[] keyBytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public static String encryptAES(String plainText, String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        Key key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decryptAES(String encryptedText, String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        Key key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static long convertKeyToNumber(String key) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        long keyNumber = 0;
        for (byte b : keyBytes) {
            keyNumber = (keyNumber << 8) | (b & 0xFF);
        }
        return keyNumber;
    }

    public static String convertNumberToKey(long keyNumber) {
        byte[] keyBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            keyBytes[i] = (byte) (keyNumber & 0xFF);
            keyNumber >>= 8;
        }
        return Base64.getEncoder().encodeToString(keyBytes);
    }

}

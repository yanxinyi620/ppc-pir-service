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
    public static void main(String[] args) throws Exception {
        String plainText = "Hello, World!";
        String keyString = generateRandomKey();  // 随机生成 16 位字符串格式的密钥
        System.out.println("KeyString Text: " + keyString);

        // // 将密钥字符串转换为整数(无法保证正整数)
        // BigInteger keyInt = new BigInteger(Base64.getDecoder().decode(keyString));
        // // 将整数转换为字符串
        // String convertedKeyString = Base64.getEncoder().encodeToString(keyInt.toByteArray());
        // System.out.println("Original Key String: " + keyString);
        // System.out.println("Converted Key Integer: " + keyInt);
        // System.out.println("Converted Key String: " + convertedKeyString);

        // // 将密钥转换为正整数（无法保证正整数）
        // long keyNumber = convertKeyToNumber(keyString);
        // System.out.println("Key as number: " + keyNumber);
        // // 将正整数转换为字符串密钥
        // String convertedKeyString = convertNumberToKey(keyNumber);
        // System.out.println("Key as string: " + convertedKeyString);

        // 加密
        String encryptedText = encryptAES(plainText, keyString);
        System.out.println("Encrypted Text: " + encryptedText);

        // 解密
        String decryptedText = decryptAES(encryptedText, keyString);
        System.out.println("Decrypted Text: " + decryptedText);
    }

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

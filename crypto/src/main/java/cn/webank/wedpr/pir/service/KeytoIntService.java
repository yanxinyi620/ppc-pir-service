package cn.webank.wedpr.pir.service;

import org.springframework.stereotype.Service;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.math.BigInteger;

@Service
public class KeytoIntService {
    public static void main(String[] args) {

        // String str = "Hello, World!";
        String str = "DSidpEoaZgMs7wkv4pCzMg==";

        // 字符串转字节数组
        byte[] bytes = str.getBytes();
        // byte[] bytes = {0x00, 0x01, 0x02, 0x03};
        System.out.println("Bytes: " + bytesToHexString(bytes));

        // // 将字节序列转换为整数(int 太短会丢失信息)
        // int number = bytesToInt(bytes, ByteOrder.BIG_ENDIAN);
        // System.out.println("Number: " + number);

        // // 将整数转换为字节序列
        // byte[] convertedBytes = intToBytes(number, ByteOrder.BIG_ENDIAN);
        // System.out.println("Converted Bytes: " + bytesToHexString(convertedBytes));

        // 将字节序列转换为整数
        BigInteger number = bytesToBigInteger(bytes);
        System.out.println("Number: " + number);

        // 将整数转换为字节序列
        byte[] convertedBytes = bigIntegerToBytes(number);
        System.out.println("Converted Bytes: " + bytesToHexString(convertedBytes));

        // 字节数组转字符串
        String convertedStr = new String(convertedBytes);
        System.out.println("Converted String: " + convertedStr);
    }

    public static int bytesToInt(byte[] bytes, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(byteOrder);
        return buffer.getInt();
    }

    public static byte[] intToBytes(int number, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).order(byteOrder);
        buffer.putInt(number);
        return buffer.array();
    }

    public static BigInteger bytesToBigInteger(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

    public static byte[] bigIntegerToBytes(BigInteger number) {
        byte[] bytes = number.toByteArray();
        if (bytes[0] == 0) {
            byte[] tmpBytes = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmpBytes, 0, tmpBytes.length);
            return tmpBytes;
        }
        return bytes;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}

package cn.webank.wedpr.pir.service;

import org.springframework.stereotype.Service;
import java.math.BigInteger;
// import java.util.Arrays;
import java.security.SecureRandom;

@Service
public class CryptoService {

    public static final BigInteger DEFAULT_G = new BigInteger("9020881489161854992071763483314773468341853433975756385639545080944698236944020124874820917267762049756743282301106459062535797137327360192691469027152272");
    public static final BigInteger DEFAULT_N = new BigInteger("102724610959913950919762303151320427896415051258714708724768326174083057407299433043362228762657118029566890747043004760241559786931866234640457856691885212534669604964926915306738569799518792945024759514373214412797317972739022405456550476153212687312211184540248262330559143446510677062823907392904449451177");
    public static final BigInteger DEFAULT_FI = new BigInteger ("102724610959913950919762303151320427896415051258714708724768326174083057407299433043362228762657118029566890747043004760241559786931866234640457856691885192126363163670343672910761259882348623401714459980712242233796355982147797162316532450768783823909695360736554767341443201861573989081253763975895939627220");

    // 生成随机数
    public static BigInteger getRandomInt() {
        SecureRandom random = new SecureRandom();

        BigInteger num = new BigInteger(DEFAULT_N.bitLength(), random);
        while (num.compareTo(DEFAULT_N) >= 0) {
            num = new BigInteger(DEFAULT_N.bitLength(), random);
        }
        // System.out.println(num);
        return num;
    }

    public static BigInteger getPowMod(BigInteger b) {
        return DEFAULT_G.modPow(b, DEFAULT_N);
    }

    public static BigInteger getOTPow(BigInteger a, BigInteger b) {
        return a.modPow(b, DEFAULT_N);
    }

    public static BigInteger getMulMod(BigInteger a, BigInteger b) {
        return a.multiply(b).mod(DEFAULT_FI);
    }

    public static BigInteger getOTMul(BigInteger a, BigInteger b) {
        return a.multiply(b).mod(DEFAULT_N);
    }

    // public static String getAESKey() {
    //     SecureRandom secureRandom = new SecureRandom();
    //     byte[] bytes = new byte[16];
    //     secureRandom.nextBytes(bytes);
    //     // System.out.println(Arrays.toString(bytes));
    //     return Arrays.toString(bytes);
    // }

}

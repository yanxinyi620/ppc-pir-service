package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.entity.PirTable;
import cn.webank.wedpr.pir.message.ServerOTRequest;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.pir.message.ServerResultlist;
import cn.webank.wedpr.pir.message.body.ServerResultBody;

import java.util.*;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import javax.persistence.EntityManager;
// import javax.persistence.Query;

@Service
public class ServerOTService {

    private static final Logger logger = LoggerFactory.getLogger(ServerOTService.class);

    // @Autowired private EntityManager entityManager;
    @Autowired private QueryFilterService queryFilterService;

    
    public ServerOTResponse runServerOTparam(ServerOTRequest serverOTRequest) throws Exception {

        logger.info("Server start runServerOTparam.");

        // HBasePrefixQuery.hbaseFilter();
        // logger.info("Server end HBasePrefixQuery.");

        String datasetId = serverOTRequest.getDatasetId();
        BigInteger x = serverOTRequest.getX();
        BigInteger y = serverOTRequest.getY();

        List<ServerResultlist> serverResultlistArrayList = new ArrayList<>();
        for (int i = 0; i < serverOTRequest.getList().size(); i++) {
            // 需要遍历 List, 获取 z0 and filter
            BigInteger z0 = serverOTRequest.getList().get(i).getZ0();
            String filter = serverOTRequest.getList().get(i).getFilter();
            // 根据datasetId和filter，从queryFilterService获取前缀匹配结果
            List<PirTable> pirTableList = queryFilterService.queryFilterSql(datasetId, filter);
            ServerResultlist serverResultlist = new ServerResultlist();
            List<ServerResultBody> serverResultBodyArrayList = new ArrayList<>();
            for (PirTable pirTable: pirTableList) {
                String uid = pirTable.getPirkey();
                String message = "";
                if (serverOTRequest.getJobType().equals("0")) {
                    message = "True";
                } else {
                    message = pirTable.getPirvalue();
                }

                // 字符串转字节数组
                byte[] Uidbytes = uid.getBytes();
                // System.out.println("UidBytes: " + KeytoIntService.bytesToHexString(Uidbytes));

                // 将字节序列转换为整数
                BigInteger UidNumber = KeytoIntService.bytesToBigInteger(Uidbytes);
                // System.out.println("UidNumber: " + UidNumber);

                BigInteger blinding_r = CryptoService.getRandomInt();
                BigInteger blinding_s = CryptoService.getRandomInt();
                BigInteger w = CryptoService.getOTMul(
                    CryptoService.getOTPow(x, blinding_s), CryptoService.getPowMod(blinding_r));
                BigInteger z1 = CryptoService.getOTMul(z0, CryptoService.getPowMod(UidNumber));
                BigInteger key = CryptoService.getOTMul(
                    CryptoService.getOTPow(z1, blinding_s), CryptoService.getOTPow(y, blinding_r));
                // logger.info("Server w: {}, key: {}.", w, key);

                // String aes_key_bytes = CryptoService.getAESKey();
                // logger.info("Server aes_key_bytes: {}.", aes_key_bytes);
                String keyString = AESEncService.generateRandomKey();
                // System.out.println("AES_key_String: " + keyString);

                // 字符串转字节数组
                byte[] bytes = keyString.getBytes();
                // System.out.println("Bytes: " + KeytoIntService.bytesToHexString(bytes));

                // 将字节序列转换为整数
                BigInteger number = KeytoIntService.bytesToBigInteger(bytes);
                // System.out.println("Number: " + number);

                // 对整数AES密钥OT加密
                BigInteger message_cipher = key.xor(number);
                // System.out.println("Key xor Number: " + message_cipher);

                // AES加密
                String cipher_str = AESEncService.encryptAES(message, keyString);
                // System.out.println("Encrypted Text: " + cipher_str);

                ServerResultBody serverResultBody = new ServerResultBody();
                serverResultBody.setE(message_cipher);
                serverResultBody.setW(w);
                serverResultBody.setC(cipher_str);
                serverResultBodyArrayList.add(serverResultBody);
            }
            serverResultlist.setList(serverResultBodyArrayList);
            serverResultlistArrayList.add(serverResultlist);
        }
        
        ServerOTResponse serverOTResponse = new ServerOTResponse();
        serverOTResponse.setList(serverResultlistArrayList);
        logger.info("Server runServerOTparam success.");

        return serverOTResponse;
    }
}

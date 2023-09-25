package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.common.WedprStatusEnum;
import cn.webank.wedpr.pir.entity.PirTable;
import cn.webank.wedpr.pir.mapper.QueryFilterMapper;
import cn.webank.wedpr.pir.message.ServerOTRequest;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.pir.message.ServerResultlist;
import cn.webank.wedpr.pir.message.body.ClientDataBody;
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
    // @Autowired private QueryFilterService queryFilterService;
    @Autowired private QueryFilterMapper queryFilterMapper;

    public ServerOTResponse runServerOTparam(ServerOTRequest serverOTRequest) throws WedprException {

        logger.info("Server start runServerOTparam.");
        // 数据方为每个匹配到的消息密钥m_i，选择随机数(r_i,s_i)
        // 计算w_i=x^{s_i}g^{r_i},key=z_i^{s_i}y_i^{r_i},z_{i}=z_\cdot g^{id_i} \\ 加密消息E_i=key \oplus m_i

        // HBasePrefixQuery.hbaseFilter();
        // logger.info("Server end HBasePrefixQuery.");

        String datasetId = serverOTRequest.getDatasetId();
        BigInteger x = serverOTRequest.getX();
        BigInteger y = serverOTRequest.getY();

        List<ServerResultlist> serverResultlistArrayList = new ArrayList<>();
        for (int i = 0; i < serverOTRequest.getDataBodyList().size(); i++) {
            // 需要遍历 List, 获取 z0 and filter
            BigInteger z0 = serverOTRequest.getDataBodyList().get(i).getZ0();
            String filter = serverOTRequest.getDataBodyList().get(i).getFilter();

            List<PirTable> pirTableList = new ArrayList<>();
            try {
                // 根据datasetId和filter，从queryFilterService获取前缀匹配结果
                // List<PirTable> pirTableList = queryFilterService.queryFilterSql(datasetId, filter);
                pirTableList = queryFilterMapper.idFilterTable(datasetId, filter);
            } catch (Exception e) {
                e.printStackTrace();
                throw new WedprException(WedprStatusEnum.DB_ERROR);
            }
            
            ServerResultlist serverResultlist = new ServerResultlist();
            List<ServerResultBody> serverResultBodyArrayList = new ArrayList<>();
            for (PirTable pirTable: pirTableList) {
                String uid = pirTable.getPirkey();
                // String message = "";
                // if (serverOTRequest.getJobType().equals("0")) {
                //     message = "True";
                // } else {
                //     message = pirTable.getPirvalue();
                // }
                String message = pirTable.getPirvalue();

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

                ServerResultBody serverResultBody = new ServerResultBody();
                serverResultBody.setE(message_cipher);
                serverResultBody.setW(w);

                // AES加密
                try {
                    String cipher_str = AESEncService.encryptAES(message, keyString);
                    // System.out.println("Encrypted Text: " + cipher_str);
                    serverResultBody.setC(cipher_str);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new WedprException(WedprStatusEnum.AES_ENCRYPT_ERROR);
                }

                serverResultBodyArrayList.add(serverResultBody);
            }
            serverResultlist.setResultBodyList(serverResultBodyArrayList);
            serverResultlistArrayList.add(serverResultlist);
        }
        
        ServerOTResponse serverOTResponse = new ServerOTResponse();
        serverOTResponse.setResultBodyList(serverResultlistArrayList);
        logger.info("Server runServerOTparam success.");

        return serverOTResponse;
    }

    public ServerOTResponse serverOTcipher(ServerOTRequest serverOTRequest) throws WedprException {

        logger.info("Server start serverOTcipher.");
        // 数据方为每个消息m_i，选择随机数(r_i,s_i)
        // 计算w_i=x^{s_i}g^{r_i},key=z_i^{s_i}y_i^{r_i},z_{i+1}=z_i\cdot g 
        // 加密消息E_i=key \oplus m_i

        String datasetId = serverOTRequest.getDatasetId();
        BigInteger x = serverOTRequest.getX();
        BigInteger y = serverOTRequest.getY();

        List<ServerResultlist> serverResultlistArrayList = new ArrayList<>();
        for (int i = 0; i < serverOTRequest.getDataBodyList().size(); i++) {
            // 需要遍历 List, 获取 z0 and idHashList
            BigInteger z0 = serverOTRequest.getDataBodyList().get(i).getZ0();
            List<ClientDataBody> idHashList = serverOTRequest.getDataBodyList().get(i).getIdHashList();

            ServerResultlist serverResultlist = new ServerResultlist();
            List<ServerResultBody> serverResultBodyArrayList = new ArrayList<>();
            // 根据datasetId和idHashList，从queryFilterService获取匹配结果列表
            // List<PirTable> pirTableList = new ArrayList<>();
            for (int j = 0; j < idHashList.size(); j++) {
                
                List<PirTable> pirTableListResult = new ArrayList<>();
                try {
                    // List<PirTable> pirTableListResult = queryFilterService.queryMatchSql(
                    //     datasetId, idHashList.get(j).getSearchId());
                    pirTableListResult = queryFilterMapper.idObfuscationTable(
                        datasetId, idHashList.get(j).getSearchId());
                    // pirTableList.addAll(pirTableListResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new WedprException(WedprStatusEnum.DB_ERROR);
                }

                for (PirTable pirTable: pirTableListResult) {
                    // String uid = pirTable.getPirkey();
                    // String message = "";
                    // if (serverOTRequest.getJobType().equals("0")) {
                    //     message = "True";
                    // } else {
                    //     message = pirTable.getPirvalue();
                    // }
                    String message = pirTable.getPirvalue();

                    // 字符串转字节数组
                    // byte[] Uidbytes = uid.getBytes();
                    // System.out.println("UidBytes: " + KeytoIntService.bytesToHexString(Uidbytes));

                    // 将字节序列转换为整数
                    // BigInteger UidNumber = KeytoIntService.bytesToBigInteger(Uidbytes);
                    // System.out.println("UidNumber: " + UidNumber);

                    BigInteger blinding_r = CryptoService.getRandomInt();
                    BigInteger blinding_s = CryptoService.getRandomInt();
                    BigInteger w = CryptoService.getOTMul(
                        CryptoService.getOTPow(x, blinding_s), CryptoService.getPowMod(blinding_r));
                    // BigInteger z1 = CryptoService.getOTMul(z0, CryptoService.getPowMod(UidNumber));
                    BigInteger z1 = CryptoService.getOTMul(z0, CryptoService.getPowMod(BigInteger.valueOf(j)));
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

                    ServerResultBody serverResultBody = new ServerResultBody();
                    serverResultBody.setE(message_cipher);
                    serverResultBody.setW(w);

                    // AES加密
                    try {
                        String cipher_str = AESEncService.encryptAES(message, keyString);
                        // System.out.println("Encrypted Text: " + cipher_str);
                        serverResultBody.setC(cipher_str);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new WedprException(WedprStatusEnum.AES_ENCRYPT_ERROR);
                    }

                    serverResultBodyArrayList.add(serverResultBody);
                }
            }
            serverResultlist.setResultBodyList(serverResultBodyArrayList);
            serverResultlistArrayList.add(serverResultlist);
        }
        
        ServerOTResponse serverOTResponse = new ServerOTResponse();
        serverOTResponse.setResultBodyList(serverResultlistArrayList);
        logger.info("Server serverOTcipher success.");

        return serverOTResponse;
    }
}

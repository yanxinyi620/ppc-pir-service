package cn.webank.wedpr.http.service;

// import cn.webank.wedpr.http.api.PirAppServiceApi;
// import cn.webank.wedpr.http.config.PirConfig;
import cn.webank.wedpr.shared.common.WedprException;
// import cn.webank.wedpr.http.dao.PirTableJPA;
import cn.webank.wedpr.http.entity.PirTable;
import cn.webank.wedpr.http.message.ClientOTRequest;
import cn.webank.wedpr.http.message.ClientOTResponse;
import cn.webank.wedpr.http.message.body.ServerDataBody;
import cn.webank.wedpr.http.message.ServerOTRequest;
import cn.webank.wedpr.http.message.ServerOTResponse;
import cn.webank.wedpr.http.message.ServerResultlist;
import cn.webank.wedpr.http.message.body.ServerResultBody;
// import cn.webank.wedpr.http.message.AddDataRequest;
// import cn.webank.wedpr.http.message.DeleteDataRequest;
// import cn.webank.wedpr.http.message.PostClientJobRequest;
// import cn.webank.wedpr.http.message.PostClientJobResponse;
// import cn.webank.wedpr.http.message.PostServerJobRequest;
// import cn.webank.wedpr.http.message.PostServerJobResponse;

import java.util.*;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Service
public class PirAppService {

    private static final Logger logger = LoggerFactory.getLogger(PirAppService.class);

    // @Autowired private PirConfig ppcsConfig;
    // @Autowired
    // private PirTableJPA pirTableJPA;

    @Autowired
    private EntityManager entityManager;

    public Object runClientFilter(String searchId, int filterLength) throws WedprException {
        // return searchId;
        return searchId.length() < filterLength ? searchId : searchId.substring(0, filterLength);
    }

    public ClientOTResponse runClientOTparam(ClientOTRequest clientOTRequest) throws WedprException {

        int filterLength = clientOTRequest.getFilterLength();
        BigInteger blinding_a = CryptoService.getRandomInt();
        BigInteger blinding_b = CryptoService.getRandomInt();
        // logger.info("Client blinding_a: {}, blinding_b: {}", blinding_a, blinding_b);

        BigInteger x = CryptoService.getPowMod(blinding_a);
        BigInteger y = CryptoService.getPowMod(blinding_b);
        BigInteger c_blinding = CryptoService.getMulMod(blinding_a, blinding_b);

        ClientOTResponse clientOTResponse = new ClientOTResponse();
        List<ServerDataBody> serverDataArrayList = new ArrayList<>();
        // int obfuscation_order = 1;
        for (int i = 0; i < clientOTRequest.getList().size(); i++) {
            String searchId = clientOTRequest.getList().get(i).getSearchId();
            String filter = searchId.length() < filterLength ? searchId : searchId.substring(0, filterLength);
            // Random rand = new Random();
            // int id_index = rand.nextInt(obfuscation_order+1);  // 生成 [0, 1] 范围内的随机整数

            // 字符串转字节数组
            byte[] Idbytes = searchId.getBytes();
            // System.out.println("IDBytes: " + KeytoIntService.bytesToHexString(Idbytes));

            // 将字节序列转换为整数
            BigInteger IdNumber = KeytoIntService.bytesToBigInteger(Idbytes);
            // System.out.println("IDNumber: " + IdNumber);

            // BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(BigInteger.valueOf(id_index)));
            // BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(BigInteger.valueOf(0)));
            BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(IdNumber));

            ServerDataBody serverDataBody = new ServerDataBody();
            serverDataBody.setFilter(filter);
            serverDataBody.setZ0(z0);
            serverDataArrayList.add(serverDataBody);
        }

        // logger.info("Client x: {}, y: {}, b: {}.", x, y, blinding_b);
        clientOTResponse.setB(blinding_b);
        clientOTResponse.setX(x);
        clientOTResponse.setY(y);
        clientOTResponse.setList(serverDataArrayList);
        return clientOTResponse;
    }

    public ServerOTResponse runServerOTparam(ServerOTRequest serverOTRequest) throws Exception {

        logger.info("Server start runServerOTparam.");
        
        // HBasePrefixQuery.hbaseFilter();
        // logger.info("Server end HBasePrefixQuery.");

        String datasetId = serverOTRequest.getDatasetId();
        BigInteger x = serverOTRequest.getX();
        BigInteger y = serverOTRequest.getY();

        ServerOTResponse serverOTResponse = new ServerOTResponse();
        List<ServerResultlist> serverResultlistArrayList = new ArrayList<>();
        for (int i = 0; i < serverOTRequest.getList().size(); i++) {
            // 需要遍历 List, 获取 z0 and filter
            BigInteger z0 = serverOTRequest.getList().get(i).getZ0();
            String filter = serverOTRequest.getList().get(i).getFilter();
            
            // List<PirTable> pirTableList = pirTableJPA.findByPirkeyStartingWith(filter);
            // List<PirTable> pirTableList = pirTableJPA.findByPrefix(datasetId, filter);
            String nativeQuery = "SELECT * FROM " + datasetId + " e WHERE e.pirkey LIKE CONCAT(:prefix, '%')";
            Query query = entityManager.createNativeQuery(nativeQuery, PirTable.class);
            query.setParameter("prefix", filter);
            List<PirTable> pirTableList = query.getResultList();

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
        serverOTResponse.setList(serverResultlistArrayList);

        return serverOTResponse;
    }
}

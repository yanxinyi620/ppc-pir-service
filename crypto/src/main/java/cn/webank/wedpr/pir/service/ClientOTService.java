package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.message.ClientOTRequest;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.body.ServerDataBody;

import java.util.*;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClientOTService {

    private static final Logger logger = LoggerFactory.getLogger(ClientOTService.class);

    public ClientOTResponse runClientOTparam(ClientOTRequest clientOTRequest) throws WedprException {

        logger.info("Client start runClientOTparam.");

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
        logger.info("Client runClientOTparam success.");

        return clientOTResponse;
    }
}

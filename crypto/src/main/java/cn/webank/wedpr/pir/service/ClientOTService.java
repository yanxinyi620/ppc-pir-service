package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.message.ClientOTRequest;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.body.ClientDataBody;
import cn.webank.wedpr.pir.message.body.ServerDataBody;

import java.util.*;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClientOTService {

    private static final Logger logger = LoggerFactory.getLogger(ClientOTService.class);

    public BigInteger calculateZ0(String searchId, BigInteger c_blinding) {
        // 字符串转字节数组
        byte[] Idbytes = searchId.getBytes();
        // System.out.println("IDBytes: " + KeytoIntService.bytesToHexString(Idbytes));

        // 将字节序列转换为整数
        BigInteger IdNumber = KeytoIntService.bytesToBigInteger(Idbytes);
        // System.out.println("IDNumber: " + IdNumber);

        // BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(BigInteger.valueOf(id_index)));
        // BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(BigInteger.valueOf(0)));
        BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(IdNumber));
        return z0;
    }

    public BigInteger calculateIndexZ0(Integer idIndex, BigInteger c_blinding) {
        // 将整数转长整数
        BigInteger IdNumber = BigInteger.valueOf(idIndex);
        BigInteger z0 = CryptoService.getPowMod(c_blinding.subtract(IdNumber));
        return z0;
    }

    public ClientOTResponse runClientOTparam(ClientOTRequest clientOTRequest) throws WedprException {

        logger.info("Client start runClientOTparam.");
        // hash披露, 请求方选择id，生成随机数a、b
        // 计算x=g^a,y=g^b,c_{id_{\delta}}=ab,z_{\delta}=g^{c_\delta},z_0=g^{c_{id_{\delta}}-id_{\delta}}

        int filterLength = clientOTRequest.getFilterLength();
        BigInteger blinding_a = CryptoService.getRandomInt();
        BigInteger blinding_b = CryptoService.getRandomInt();
        // logger.info("Client blinding_a: {}, blinding_b: {}", blinding_a, blinding_b);

        BigInteger x = CryptoService.getPowMod(blinding_a);
        BigInteger y = CryptoService.getPowMod(blinding_b);
        BigInteger c_blinding = CryptoService.getMulMod(blinding_a, blinding_b);

        List<ServerDataBody> serverDataArrayList = new ArrayList<>();
        // int obfuscation_order = 1;
        for (int i = 0; i < clientOTRequest.getDataBodyList().size(); i++) {
            String searchId = clientOTRequest.getDataBodyList().get(i).getSearchId();
            String filter = searchId.length() < filterLength ? searchId : searchId.substring(0, filterLength);
            // Random rand = new Random();
            // int id_index = rand.nextInt(obfuscation_order+1);  // 生成 [0, 1] 范围内的随机整数

            BigInteger z0 = calculateZ0(searchId, c_blinding);

            ServerDataBody serverDataBody = new ServerDataBody();
            serverDataBody.setFilter(filter);
            serverDataBody.setZ0(z0);
            serverDataArrayList.add(serverDataBody);
        }

        // logger.info("Client x: {}, y: {}, b: {}.", x, y, blinding_b);
        ClientOTResponse clientOTResponse = new ClientOTResponse();
        clientOTResponse.setB(blinding_b);
        clientOTResponse.setX(x);
        clientOTResponse.setY(y);
        clientOTResponse.setDataBodyList(serverDataArrayList);
        logger.info("Client runClientOTparam success.");

        return clientOTResponse;
    }

    public ClientOTResponse clientOTcipher(ClientOTRequest clientOTRequest) throws WedprException {

        logger.info("Client start clientOTcipher.");
        // hash筛选, 请求方选择顺序\delta\in \{0,1,..,m-1\}，生成随机数a、b
        // 计算x=g^a,y=g^b,c_{\delta}=ab,z_{\delta}=g^{c_\delta},z_0=g^{c_{\delta}-\delta}

        int obfuscationOrder = clientOTRequest.getObfuscationOrder();
        BigInteger blinding_a = CryptoService.getRandomInt();
        BigInteger blinding_b = CryptoService.getRandomInt();
        // logger.info("Client blinding_a: {}, blinding_b: {}", blinding_a, blinding_b);

        BigInteger x = CryptoService.getPowMod(blinding_a);
        BigInteger y = CryptoService.getPowMod(blinding_b);
        BigInteger c_blinding = CryptoService.getMulMod(blinding_a, blinding_b);

        List<ServerDataBody> serverDataArrayList = new ArrayList<>();
        // int obfuscation_order = 1;
        for (int i = 0; i < clientOTRequest.getDataBodyList().size(); i++) {
            String searchId = clientOTRequest.getDataBodyList().get(i).getSearchId();
            Random rand = new Random();
            Integer idIndex = rand.nextInt(obfuscationOrder+1);  // 生成 [0, 1] 范围内的随机整数
            BigInteger z0 = calculateIndexZ0(idIndex, c_blinding);

            // 生成obfuscationOrder+1个混淆hash, 将idIndex位置的hash替换为真实hash值
            List<ClientDataBody> clientDataBodyList = IdHashVecService.getIdHashVec(
                obfuscationOrder, idIndex, searchId);

            ServerDataBody serverDataBody = new ServerDataBody();
            serverDataBody.setZ0(z0);
            serverDataBody.setIdIndex(idIndex);
            serverDataBody.setIdHashList(clientDataBodyList);
            serverDataArrayList.add(serverDataBody);
        }

        // logger.info("Client x: {}, y: {}, b: {}.", x, y, blinding_b);
        ClientOTResponse clientOTResponse = new ClientOTResponse();
        clientOTResponse.setB(blinding_b);
        clientOTResponse.setX(x);
        clientOTResponse.setY(y);
        clientOTResponse.setDataBodyList(serverDataArrayList);
        logger.info("Client clientOTcipher success.");

        return clientOTResponse;
    }
}

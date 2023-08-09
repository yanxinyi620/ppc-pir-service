package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.message.ClientDecryptRequest;
import cn.webank.wedpr.pir.message.ClientDecryptResponse;
import cn.webank.wedpr.pir.message.body.PirResultBody;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClientDecryptService {

    private static final Logger logger = LoggerFactory.getLogger(ClientDecryptService.class);

    public ClientDecryptResponse runDecryptOTparam(ClientDecryptRequest clientDecryptRequest) throws Exception {

        logger.info("Client start runDecryptOTparam.");

        ClientDecryptResponse clientDecryptResponse = new ClientDecryptResponse();

        List<PirResultBody> pirReseltBodyArrayList = new ArrayList<>();
        for (int i = 0; i < clientDecryptRequest.getServerResult().getList().size(); i++) {
            PirResultBody pirReseltBody = new PirResultBody();
            pirReseltBody.setSearchId(clientDecryptRequest.getList().get(i).getSearchId());

            for (int j = 0; j < clientDecryptRequest.getServerResult().getList().get(i).getList().size(); j++) {
                BigInteger e = clientDecryptRequest.getServerResult().getList().get(i).getList().get(j).getE();
                BigInteger w = clientDecryptRequest.getServerResult().getList().get(i).getList().get(j).getW();
                String cipher_str = clientDecryptRequest.getServerResult().getList().get(i).getList().get(j).getC();
                BigInteger w1 = CryptoService.getOTPow(w, clientDecryptRequest.getB());

                try{
                    // 对整数AES密钥OT解密
                    BigInteger message_recover = w1.xor(e);
                    // System.out.println("w xor b (Number): " + message_recover);

                    // 将整数转换为字节序列
                    byte[] convertedBytes = KeytoIntService.bigIntegerToBytes(message_recover);
                    // System.out.println("Converted Bytes (Bytes): " + KeytoIntService.bytesToHexString(convertedBytes));

                    // 字节数组转字符串
                    String convertedStr = new String(convertedBytes);
                    // System.out.println("Converted String (AES_key_String): " + convertedStr);

                    // AES解密
                    String decryptedText = AESEncService.decryptAES(cipher_str, convertedStr);
                    // System.out.println("Decrypted Text: " + decryptedText);

                    pirReseltBody.setSearchDetail(decryptedText);
                    pirReseltBodyArrayList.add(pirReseltBody);
                } catch (Exception err) {
                    // logger.info("Client pir ERROR: {}.", err);
                }
            }

            if (clientDecryptRequest.getServerResult().getList().get(i).getList().size() == 0) {
                pirReseltBody.setSearchDetail("message not found");
                pirReseltBodyArrayList.add(pirReseltBody);
            } else {
            }
        }

        clientDecryptResponse.setDetail(pirReseltBodyArrayList);
        logger.info("Client runDecryptOTparam success.");

        return clientDecryptResponse;
    }
}

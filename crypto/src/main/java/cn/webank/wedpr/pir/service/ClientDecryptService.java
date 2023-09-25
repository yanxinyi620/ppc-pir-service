package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.message.ClientDecryptRequest;
import cn.webank.wedpr.pir.message.ClientDecryptResponse;
import cn.webank.wedpr.pir.message.body.PirResultBody;
import cn.webank.wedpr.pir.message.body.ServerResultBody;

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
        // 请求方遍历所有id, w_{\delta}^b=x^{s_{\delta}b}g^{r_{\delta}b}=w_i^b, m_i=w_{\delta}^b\oplus E_i
        // 使用M_i解密C_i，得到明文S_i

        ClientDecryptResponse clientDecryptResponse = new ClientDecryptResponse();

        List<PirResultBody> pirReseltBodyArrayList = new ArrayList<>();
        for (int i = 0; i < clientDecryptRequest.getServerResult().getResultBodyList().size(); i++) {
            PirResultBody pirResultBody = new PirResultBody();
            pirResultBody.setSearchId(clientDecryptRequest.getDataBodyList().get(i).getSearchId());
            List<ServerResultBody> serverResultlist = clientDecryptRequest.getServerResult().getResultBodyList().get(i).getResultBodyList();

            for (int j = 0; j < serverResultlist.size(); j++) {
                BigInteger e = serverResultlist.get(j).getE();
                BigInteger w = serverResultlist.get(j).getW();
                String cipher_str = serverResultlist.get(j).getC();
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

                    pirResultBody.setSearchExist(true);
                    pirResultBody.setSearchValue(decryptedText);
                    pirReseltBodyArrayList.add(pirResultBody);
                } catch (Exception err) {
                    // logger.info("Client pir ERROR: {}.", err);
                }
            }

            // if (serverResultlist.size() == 0) {
            if (pirResultBody.getSearchValue() == null) {
                pirResultBody.setSearchExist(false);
                pirReseltBodyArrayList.add(pirResultBody);
            } else {
            }
        }

        clientDecryptResponse.setDetail(pirReseltBodyArrayList);
        logger.info("Client runDecryptOTparam success.");

        return clientDecryptResponse;
    }

    public ClientDecryptResponse decryptOTcipher(ClientDecryptRequest clientDecryptRequest) throws Exception {

        logger.info("Client start decryptOTcipher.");
        // 请求方计算第\delta个结果, w_{\delta}^b=x^{s_{\delta}b}g^{r_{\delta}b}, m_i=w_{\delta}^b\oplus E_i

        ClientDecryptResponse clientDecryptResponse = new ClientDecryptResponse();

        List<PirResultBody> pirReseltBodyArrayList = new ArrayList<>();
        for (int i = 0; i < clientDecryptRequest.getServerResult().getResultBodyList().size(); i++) {
            PirResultBody pirResultBody = new PirResultBody();
            pirResultBody.setSearchId(clientDecryptRequest.getDataBodyList().get(i).getSearchId());
            List<ServerResultBody> serverResultlist = clientDecryptRequest.getServerResult().getResultBodyList().get(i).getResultBodyList();

            for (int j = 0; j < serverResultlist.size(); j++) {
                BigInteger e = serverResultlist.get(j).getE();
                BigInteger w = serverResultlist.get(j).getW();
                String cipher_str = serverResultlist.get(j).getC();
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

                    pirResultBody.setSearchExist(true);
                    pirResultBody.setSearchValue(decryptedText);
                    pirReseltBodyArrayList.add(pirResultBody);
                } catch (Exception err) {
                    // logger.info("Client pir ERROR: {}.", err);
                }
            }

            // if (serverResultlist.size() == 0) {
            if (pirResultBody.getSearchValue() == null) {
                pirResultBody.setSearchExist(false);
                pirReseltBodyArrayList.add(pirResultBody);
            } else {
            }
        }

        clientDecryptResponse.setDetail(pirReseltBodyArrayList);
        logger.info("Client decryptOTcipher success.");

        return clientDecryptResponse;
    }
}

package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.message.body.ClientDataBody;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IdHashVecService {

    private static final Logger logger = LoggerFactory.getLogger(IdHashVecService.class);

    public static List<ClientDataBody> getIdHashVec(
        int obfuscationOrder, Integer idIndex, String searchId) throws WedprException {

        List<ClientDataBody> clientDataBodyList = new ArrayList<>();
        for (int i = 0; i < obfuscationOrder+1; i++) {
            ClientDataBody clientDataBody = new ClientDataBody();

            if (idIndex == i) {
                clientDataBody.setSearchId(searchId);
            } else {
                String uuid = UUID.randomUUID().toString();
                String hash = makeHash(uuid.getBytes());
                clientDataBody.setSearchId(hash);
            }

            clientDataBodyList.add(clientDataBody);
        }

        return clientDataBodyList;
    }

    public static String makeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

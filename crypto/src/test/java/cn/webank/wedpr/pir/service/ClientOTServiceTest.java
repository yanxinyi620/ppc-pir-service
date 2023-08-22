package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.message.ClientOTRequest;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.body.ClientDataBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ClientOTServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ClientOTServiceTest.class);

    // @Autowired private ObjectMapper objectMapper;
    // @Autowired private ClientOTService clientOTService;

    @Test
    public void testClientOTparam() throws WedprException {
        
        Integer otlength = 4;
        List<String> searchIdList = new ArrayList<>();
        // 添加元素到列表
        searchIdList.add("abc123456789");
        searchIdList.add("jdf987654321");
        searchIdList.add("jdfregbgfhgg");

        List<ClientDataBody> clientDataArrayList = new ArrayList<>();
        for (int i = 0; i < searchIdList.size(); i++) {
            String searchId = searchIdList.get(i);

            ClientDataBody clientDataBody = new ClientDataBody();
            clientDataBody.setSearchId(searchId);
            clientDataArrayList.add(clientDataBody);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        ClientOTRequest clientOTRequest = new ClientOTRequest();
        clientOTRequest.setFilterLength(otlength);
        clientOTRequest.setDataBodyList(clientDataArrayList);

        try {
            logger.info("Singleton test clientOTRequest: {}.", objectMapper.writeValueAsString(clientOTRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        ClientOTService clientOTService = new ClientOTService();
        ClientOTResponse otParamResponse = clientOTService.runClientOTparam(clientOTRequest);
        
        try {
            logger.info("Singleton test otParamResponse: {}", objectMapper.writeValueAsString(otParamResponse));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        assert true;

    }
}

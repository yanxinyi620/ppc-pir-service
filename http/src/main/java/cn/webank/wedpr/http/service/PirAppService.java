package cn.webank.wedpr.http.service;

// import java.util.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.webank.wedpr.http.config.PirControllerConfig;
import cn.webank.wedpr.http.message.ClientJobRequest;
import cn.webank.wedpr.http.message.PirResultResponse;
import cn.webank.wedpr.http.message.ServerJobRequest;
import cn.webank.wedpr.http.message.SimpleEntity;
import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.message.ClientDecryptRequest;
import cn.webank.wedpr.pir.message.ClientDecryptResponse;
import cn.webank.wedpr.pir.message.ClientOTRequest;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.ServerOTRequest;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.pir.service.ClientDecryptService;
import cn.webank.wedpr.pir.service.ClientOTService;
import cn.webank.wedpr.pir.service.ServerOTService;

@Service
public class PirAppService {
    
    // private static final Logger logger = LoggerFactory.getLogger(PirAppService.class);

    @Autowired private PirControllerConfig pirConfig;
    @Autowired private ClientOTService clientOTService;
    @Autowired private ServerOTService serverOTService;
    @Autowired private ClientDecryptService clientDecryptService;

    public ClientOTResponse requesterOtCipher(ClientJobRequest clientJobRequest) throws WedprException {

        // 1. hash披露，获取bashOT参数
        ClientOTRequest clientOTRequest = new ClientOTRequest();
        ClientOTResponse otParamResponse = new ClientOTResponse();

        clientOTRequest.setList(clientJobRequest.getList());
        if (clientJobRequest.getJobAlgorithmType().equals("0")) {
            clientOTRequest.setFilterLength(pirConfig.getOtlength());
            otParamResponse = clientOTService.runClientOTparam(clientOTRequest);
        } else {
            clientOTRequest.setObfuscationOrder(clientJobRequest.getObfuscationOrder());
            otParamResponse = clientOTService.clientOTcipher(clientOTRequest);
        }

        return otParamResponse;
    }

    public PirResultResponse requesterOtRecover(
        ClientOTResponse otParamResponse, ClientJobRequest clientJobRequest, SimpleEntity otResult) throws Exception {

        // 3. 根据筛选结果，获取最终匿踪结果
        ClientDecryptRequest clientDecryptRequest = new ClientDecryptRequest();
        clientDecryptRequest.setB(otParamResponse.getB());
        clientDecryptRequest.setList(clientJobRequest.getList());
        clientDecryptRequest.setServerResult(otResult.getData());

        ClientDecryptResponse clientDecryptResponse = clientDecryptService.runDecryptOTparam(clientDecryptRequest);
        // ClientDecryptResponse clientDecryptResponse = new ClientDecryptResponse();
        // if (clientJobRequest.getJobAlgorithmType().equals("0")) {
        //     clientDecryptResponse = clientDecryptService.runDecryptOTparam(clientDecryptRequest);
        // } else {
        //     clientDecryptResponse = clientDecryptService.decryptOTcipher(clientDecryptRequest);
        // }

        PirResultResponse pirResultResponse = new PirResultResponse();
        pirResultResponse.setJobId(clientJobRequest.getJobId());
        pirResultResponse.setDetail(clientDecryptResponse);

        return pirResultResponse;
    }

    public ServerOTResponse providerOtCipher(ServerJobRequest serverJobRequest) throws Exception {

        // 1. 根据请求，筛选数据，加密密钥，返回筛选结果及AES消息密文
        ServerOTRequest serverOTRequest = new ServerOTRequest();
        serverOTRequest.setJobType(serverJobRequest.getJobType());
        serverOTRequest.setDatasetId(serverJobRequest.getDatasetId());
        serverOTRequest.setX(serverJobRequest.getX());
        serverOTRequest.setY(serverJobRequest.getY());
        serverOTRequest.setList(serverJobRequest.getList());

        ServerOTResponse otResultResponse = new ServerOTResponse();
        if (serverJobRequest.getJobAlgorithmType().equals("0")) {
            otResultResponse = serverOTService.runServerOTparam(serverOTRequest);
        } else {
            otResultResponse = serverOTService.serverOTcipher(serverOTRequest);
        }
        
        return otResultResponse;
    }
}

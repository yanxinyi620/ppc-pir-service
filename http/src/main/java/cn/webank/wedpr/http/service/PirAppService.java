package cn.webank.wedpr.http.service;

import cn.webank.wedpr.http.config.PirControllerConfig;
import cn.webank.wedpr.http.utils.ParamUtils.*;
import cn.webank.wedpr.http.message.ClientJobRequest;
import cn.webank.wedpr.http.message.PirResultResponse;
import cn.webank.wedpr.http.message.ServerJobRequest;
import cn.webank.wedpr.http.message.SimpleEntity;
import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.common.WedprStatusEnum;
import cn.webank.wedpr.pir.message.ClientDecryptRequest;
import cn.webank.wedpr.pir.message.ClientDecryptResponse;
import cn.webank.wedpr.pir.message.ClientOTRequest;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.ServerOTRequest;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.pir.service.ClientDecryptService;
import cn.webank.wedpr.pir.service.ClientOTService;
import cn.webank.wedpr.pir.service.ServerOTService;

// import java.util.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        clientOTRequest.setDataBodyList(clientJobRequest.getSearchIdList());
        if (clientJobRequest.getJobAlgorithmType().equals(AlgorithmType.idFilter.getValue())) {
            clientOTRequest.setFilterLength(pirConfig.getFilterLength());
            otParamResponse = clientOTService.runClientOTparam(clientOTRequest);
        } else if (clientJobRequest.getJobAlgorithmType().equals(AlgorithmType.idObfuscation.getValue())) {
            clientOTRequest.setObfuscationOrder(clientJobRequest.getObfuscationOrder());
            otParamResponse = clientOTService.clientOTcipher(clientOTRequest);
        } else {
            throw new WedprException(WedprStatusEnum.INVALID_INPUT_VALUE);
        }

        return otParamResponse;
    }

    public PirResultResponse requesterOtRecover(
        ClientOTResponse otParamResponse, ClientJobRequest clientJobRequest, SimpleEntity otResult) throws Exception {

        // 3. 根据筛选结果，获取最终匿踪结果
        ClientDecryptRequest clientDecryptRequest = new ClientDecryptRequest();
        clientDecryptRequest.setB(otParamResponse.getB());
        clientDecryptRequest.setDataBodyList(clientJobRequest.getSearchIdList());
        clientDecryptRequest.setServerResult(otResult.getData());

        ClientDecryptResponse clientDecryptResponse = clientDecryptService.runDecryptOTparam(clientDecryptRequest);
        // ClientDecryptResponse clientDecryptResponse = new ClientDecryptResponse();
        // if (clientJobRequest.getJobAlgorithmType().equals(AlgorithmType.idFilter.getValue())) {
        //     clientDecryptResponse = clientDecryptService.runDecryptOTparam(clientDecryptRequest);
        // } else if (clientJobRequest.getJobAlgorithmType().equals(AlgorithmType.idObfuscation.getValue())) {
        //     clientDecryptResponse = clientDecryptService.decryptOTcipher(clientDecryptRequest);
        // } else {
        //     throw new WedprException(WedprStatusEnum.INVALID_INPUT_VALUE);
        // }

        PirResultResponse pirResultResponse = new PirResultResponse();
        pirResultResponse.setJobId(clientJobRequest.getJobId());
        pirResultResponse.setJobType(clientJobRequest.getJobType());
        pirResultResponse.setDetail(clientDecryptResponse);

        return pirResultResponse;
    }

    public ServerOTResponse providerOtCipher(ServerJobRequest serverJobRequest) throws Exception {

        // 1. 根据请求，筛选数据，加密密钥，返回筛选结果及AES消息密文
        ServerOTRequest serverOTRequest = new ServerOTRequest();
        serverOTRequest.setJobType(serverJobRequest.getJobType());
        // serverOTRequest.setDatasetId(serverJobRequest.getDatasetId());
        String datasetId = serverJobRequest.getDatasetId();
        if (datasetId.length() > pirConfig.getDatasetIdSubstr()) {
            serverOTRequest.setDatasetId(
                pirConfig.getDatasetIdPrefix() + datasetId.substring(
                    datasetId.length() - pirConfig.getDatasetIdSubstr()));
        } else {
            serverOTRequest.setDatasetId(
                pirConfig.getDatasetIdPrefix() + datasetId);
        }
        serverOTRequest.setX(serverJobRequest.getX());
        serverOTRequest.setY(serverJobRequest.getY());
        serverOTRequest.setDataBodyList(serverJobRequest.getDataBodyList());

        ServerOTResponse otResultResponse = new ServerOTResponse();
        if (serverJobRequest.getJobAlgorithmType().equals(AlgorithmType.idFilter.getValue())) {
            otResultResponse = serverOTService.runServerOTparam(serverOTRequest);
        } else if (serverJobRequest.getJobAlgorithmType().equals(AlgorithmType.idObfuscation.getValue())) {
            otResultResponse = serverOTService.serverOTcipher(serverOTRequest);
        } else {
            throw new WedprException(WedprStatusEnum.INVALID_INPUT_VALUE);
        }
        
        return otResultResponse;
    }
}

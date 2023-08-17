package cn.webank.wedpr.http.controller;

import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.http.config.PirControllerConfig;
import cn.webank.wedpr.http.service.PirAppService;
import cn.webank.wedpr.http.message.ClientJobRequest;
import cn.webank.wedpr.http.message.ClientJobResponse;
import cn.webank.wedpr.http.message.ClientPirResponse;
import cn.webank.wedpr.http.message.ClientPirfailResponse;
import cn.webank.wedpr.http.message.PirResultResponse;
import cn.webank.wedpr.http.message.ServerJobRequest;
import cn.webank.wedpr.http.message.SimpleEntity;
import cn.webank.wedpr.http.message.JobRequest;
import cn.webank.wedpr.http.message.JobEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class PirController {

    private static final Logger logger = LoggerFactory.getLogger(PirController.class);

    @Autowired private PirControllerConfig pirConfig;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PirAppService pirAppService;
    @Autowired private RestTemplate restTemplate;

    @RequestMapping("/test")
    private Object test() {
        // return "OK!";
        ClientJobResponse clientResponse = ClientJobResponse.successResponse();
        clientResponse.setData("testing OK!");
        return clientResponse;
    }

    // 请求方
    @PostMapping("/client")
    public Object client_service(@RequestBody ClientJobRequest clientJobRequest) throws Exception {

        try {
            // logger.info("Clientjob: clientJobRequest: {}.", objectMapper.writeValueAsString(clientJobRequest));
            clientJobRequest.setJobType((clientJobRequest.getJobType() != null) ? clientJobRequest.getJobType() : "0");
            clientJobRequest.setJobAlgorithmType(
                (clientJobRequest.getJobAlgorithmType() != null) ? clientJobRequest.getJobAlgorithmType() : "0");
            clientJobRequest.setObfuscationOrder(
                (clientJobRequest.getObfuscationOrder() != null) ? clientJobRequest.getObfuscationOrder() : 1);
            logger.info("Clientjob: clientJobRequest: {}.", objectMapper.writeValueAsString(clientJobRequest));

            // 1. hash披露，获取bashOT参数
            ClientOTResponse otParamResponse = pirAppService.requesterOtCipher(clientJobRequest);
            logger.info("Clientjob: otParamResponse: {}.", objectMapper.writeValueAsString(otParamResponse));

            // 2. 发送hash披露，bashOT参数给数据方，并获取筛选结果
            ServerJobRequest serverJobRequest = new ServerJobRequest();
            serverJobRequest.setJobId(clientJobRequest.getJobId());
            serverJobRequest.setJobType(clientJobRequest.getJobType());
            serverJobRequest.setJobCreatorAgencyId(clientJobRequest.getJobCreatorAgencyId());
            serverJobRequest.setParticipateAgencyId(clientJobRequest.getParticipateAgencyId());
            serverJobRequest.setDatasetId(clientJobRequest.getDatasetId());
            serverJobRequest.setJobCreator(clientJobRequest.getJobCreator());
            serverJobRequest.setJobAlgorithmType(clientJobRequest.getJobAlgorithmType());
            serverJobRequest.setX(otParamResponse.getX());
            serverJobRequest.setY(otParamResponse.getY());
            // clear getList 中的 idIndex 信息
            serverJobRequest.setList(otParamResponse.getList());
            for (int i = 0; i < serverJobRequest.getList().size(); i++) {
                serverJobRequest.getList().get(i).setIdIndex(0);
            }
            logger.info("Client post request: data: {}.", objectMapper.writeValueAsString(serverJobRequest));

            // 从配置获取searchendpoint
            String searchendpoint = null;
            logger.info("Client post request: agencyip: {}.", pirConfig.getAgencyip());
            for (int i = 0; i < pirConfig.getAgencyip().size(); i++) {
                String agency = pirConfig.getAgencyip().get(i);
                if (clientJobRequest.getSearchIp().equals(agency)) {
                    searchendpoint = pirConfig.getAgencyendpoint().get(i);
                } else {
                }
            }
            logger.info("Client post request: searchendpoint: {}.", searchendpoint);

            // 设置请求头
            String pirUrl = "http://"+ searchendpoint + "/api/server";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 将JobRequest对象转换为HttpEntity，并设置请求头
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(serverJobRequest);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(pirUrl, requestEntity, String.class);

            // 创建SimpleEntity对象，并将ResponseEntity的内容设置到SimpleEntity中
            String responseBody = responseEntity.getBody();
            SimpleEntity otResult = new SimpleEntity();
            if (responseBody != null) {
                otResult = objectMapper.readValue(responseBody, SimpleEntity.class);
            }

            if (otResult.getCode() != 0) {
                ClientJobResponse clientResponse = ClientJobResponse.failureResponse(
                    otResult.getCode(), otResult.getMessage());
                ClientPirfailResponse pirResponse = new ClientPirfailResponse();
                pirResponse.setError(clientResponse);
                return pirResponse;
            }

            // 3. 根据筛选结果，获取最终匿踪结果
            PirResultResponse pirResultResponse = pirAppService.requesterOtRecover(
                otParamResponse, clientJobRequest, otResult);
            logger.info("Client pir result: message: {}.", objectMapper.writeValueAsString(pirResultResponse));

            ClientJobResponse clientResponse = ClientJobResponse.successResponse();
            // clientResponse.setData("client run successfully!");
            clientResponse.setData(pirResultResponse);
            // return clientResponse;

            ClientPirResponse pirResponse = new ClientPirResponse();
            pirResponse.setResult(clientResponse);
            return pirResponse;

        } catch (Exception e)
        
        {
            ClientJobResponse clientResponse = ClientJobResponse.failureResponse(500, "pir job failed");
            ClientPirfailResponse pirResponse = new ClientPirfailResponse();
            pirResponse.setError(clientResponse);
            return pirResponse;
        }
    }

    // 数据方
    @PostMapping("/server")
    public Object server_service(@RequestBody ServerJobRequest serverJobRequest) throws Exception {
        logger.info("Serverjob: serverJobRequest: {}.", objectMapper.writeValueAsString(serverJobRequest));

        // 调用web服务，保存匿踪参与任务
        if (pirConfig.getDeploymode() != 2) {
            JobRequest jobRequest = new JobRequest();
            jobRequest.setJobId(serverJobRequest.getJobId());
            jobRequest.setJobTitle("PPC-AYS-Title");
            jobRequest.setToken("pws_api_key");
            jobRequest.setJobCreatorAgencyId(serverJobRequest.getJobCreatorAgencyId());
            jobRequest.setParticipateAgencyId(serverJobRequest.getParticipateAgencyId());
            jobRequest.setDatasetId(serverJobRequest.getDatasetId());
            jobRequest.setJobCreator(serverJobRequest.getJobCreator());

            // 设置请求头
            String pmsUrl = "http://" + pirConfig.getWebServiceEndpoint() + "/api/v3/ppc-management/pms/jobs-ays";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 将JobRequest对象转换为HttpEntity，并设置请求头
            HttpEntity<JobRequest> requestEntity = new HttpEntity<>(jobRequest, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(pmsUrl, requestEntity, String.class);

            // 创建JobEntity对象，并将ResponseEntity的内容设置到JobEntity中
            String responseBody = responseEntity.getBody();
            JobEntity jobResult = new JobEntity();
            // jobResult.setErrorCode(responseEntity.getStatusCodeValue());
            // jobResult.setMessage(responseEntity.getStatusCode().getReasonPhrase());
            // jobResult.setData(responseEntity.getBody());
            if (responseBody != null) {
                jobResult = objectMapper.readValue(responseBody, JobEntity.class);
            }
            logger.info("Serverjob: patchResponse: {}.", jobResult.getErrorCode());

            if (jobResult.getErrorCode() != 0) {
                ClientJobResponse clientResponse = ClientJobResponse.failureResponse(
                    jobResult.getErrorCode(), jobResult.getMessage());
                return clientResponse;
            } else {
            }
        } else {
        }

        // 1. 根据请求，筛选数据，加密密钥，返回筛选结果及AES消息密文
        ServerOTResponse otResultResponse = pirAppService.providerOtCipher(serverJobRequest);
        logger.info("Serverjob: otResultResponse: {}.", objectMapper.writeValueAsString(otResultResponse));

        ClientJobResponse clientResponse = ClientJobResponse.successResponse();
        // clientResponse.setData("server run successfully!");
        // clientResponse.setData(otResultResponse.getList());
        clientResponse.setData(otResultResponse);
        return clientResponse;
    }
}

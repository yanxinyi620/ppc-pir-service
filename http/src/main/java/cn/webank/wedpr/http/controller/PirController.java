package cn.webank.wedpr.http.controller;

import cn.webank.wedpr.pir.service.ClientOTService;
import cn.webank.wedpr.pir.service.ServerOTService;
import cn.webank.wedpr.pir.service.ClientDecryptService;
import cn.webank.wedpr.pir.message.ClientOTRequest;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.ServerOTRequest;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.pir.message.ClientDecryptRequest;
import cn.webank.wedpr.pir.message.ClientDecryptResponse;

import cn.webank.wedpr.http.config.PirControllerConfig;
import cn.webank.wedpr.http.message.ClientJobRequest;
import cn.webank.wedpr.http.message.ClientJobResponse;
import cn.webank.wedpr.http.message.ClientPirResponse;
import cn.webank.wedpr.http.message.ClientPirfailResponse;
import cn.webank.wedpr.http.message.PirResultResponse;
import cn.webank.wedpr.http.message.ServerJobRequest;
import cn.webank.wedpr.http.message.SimpleEntity;
import cn.webank.wedpr.http.message.JobRequest;
import cn.webank.wedpr.http.message.JobEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
// import java.math.BigInteger;
// import java.util.List;
// import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;

@RestController
@RequestMapping("/api")
public class PirController {

    private static final Logger logger = LoggerFactory.getLogger(PirController.class);

    @Autowired private PirControllerConfig pirConfig;
    @Autowired private ObjectMapper objectMapper;
    // @Autowired private PirAppService pirAppService;

    @Autowired private ClientOTService clientOTService;
    @Autowired private ServerOTService serverOTService;
    @Autowired private ClientDecryptService clientDecryptService;

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

        // ClientOTService clientOTService = new ClientOTService();
        // ClientDecryptService clientDecryptService = new ClientDecryptService();

        try {

            logger.info("Clientjob: clientJobRequest: {}.", objectMapper.writeValueAsString(clientJobRequest));

            // 1. hash披露，获取bashOT参数
            ClientOTRequest clientOTRequest = new ClientOTRequest();
            clientOTRequest.setFilterLength(pirConfig.getOtlength());
            clientOTRequest.setList(clientJobRequest.getList());
            // ClientOTResponse otParamResponse = pirAppService.runClientOTparam(clientOTRequest);
            ClientOTResponse otParamResponse = clientOTService.runClientOTparam(clientOTRequest);
            logger.info("Clientjob: otParamResponse: {}.", objectMapper.writeValueAsString(otParamResponse));

            // 2. 发送hash披露，bashOT参数给数据方，并获取筛选结果
            ServerJobRequest serverJobRequest = new ServerJobRequest();
            serverJobRequest.setJobId(clientJobRequest.getJobId());
            // serverJobRequest.setJobType(clientJobRequest.getJobType());
            serverJobRequest.setJobType((clientJobRequest.getJobType() != null) ? clientJobRequest.getJobType() : "0");
            serverJobRequest.setJobCreatorAgencyId(clientJobRequest.getJobCreatorAgencyId());
            serverJobRequest.setParticipateAgencyId(clientJobRequest.getParticipateAgencyId());
            serverJobRequest.setDatasetId(clientJobRequest.getDatasetId());
            serverJobRequest.setJobCreator(clientJobRequest.getJobCreator());
            serverJobRequest.setX(otParamResponse.getX());
            serverJobRequest.setY(otParamResponse.getY());
            serverJobRequest.setList(otParamResponse.getList());
            logger.info("Client post request: data: {}.", objectMapper.writeValueAsString(serverJobRequest));

            // ------ OkHttp post 请求 ------
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                objectMapper.writeValueAsString(serverJobRequest), mediaType);

            // String searchIp = clientJobRequest.getSearchIp();
            // Integer searchPort = pirConfig.getPort();
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

            Request request = new Request.Builder().url("http://"+ searchendpoint + "/api/server").post(body).build();
            okhttp3.ResponseBody responseBody = client.newCall(request).execute().body();

            SimpleEntity otResult = new SimpleEntity();
            if (responseBody != null) {
                String responseBodyString = responseBody.string();
                // ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBodyString);
                otResult = objectMapper.treeToValue(jsonNode, SimpleEntity.class);
            }

            if (otResult.getCode() != 0) {
                ClientJobResponse clientResponse = ClientJobResponse.failureResponse(
                    otResult.getCode(), otResult.getMessage());
                ClientPirfailResponse pirResponse = new ClientPirfailResponse();
                pirResponse.setError(clientResponse);
                return pirResponse;
            } else {
            }

            // 3. 根据筛选结果，获取最终匿踪结果
            ClientDecryptRequest clientDecryptRequest = new ClientDecryptRequest();
            clientDecryptRequest.setB(otParamResponse.getB());
            clientDecryptRequest.setList(clientJobRequest.getList());
            clientDecryptRequest.setServerResult(otResult.getData());
            ClientDecryptResponse clientDecryptResponse = clientDecryptService.runDecryptOTparam(clientDecryptRequest);

            PirResultResponse pirResultResponse = new PirResultResponse();
            pirResultResponse.setJobId(clientJobRequest.getJobId());
            pirResultResponse.setDetail(clientDecryptResponse);
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

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(objectMapper.writeValueAsString(jobRequest), mediaType);
            Request request = new Request.Builder().url(
                "http://" + pirConfig.getWebServiceEndpoint() + "/api/v3/ppc-management/pms/jobs-ays").patch(body).build();
            okhttp3.ResponseBody responseBody = client.newCall(request).execute().body();

            JobEntity jobResult = new JobEntity();
            if (responseBody != null) {
                String responseBodyString = responseBody.string();
                JsonNode jsonNode = objectMapper.readTree(responseBodyString);
                jobResult = objectMapper.treeToValue(jsonNode, JobEntity.class);
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

        // ServerOTService serverOTService = new ServerOTService();

        // 1. 根据请求，筛选数据，加密密钥，返回筛选结果及AES消息密文
        ServerOTRequest serverOTRequest = new ServerOTRequest();
        serverOTRequest.setJobType(serverJobRequest.getJobType());
        serverOTRequest.setDatasetId(serverJobRequest.getDatasetId());
        serverOTRequest.setX(serverJobRequest.getX());
        serverOTRequest.setY(serverJobRequest.getY());
        serverOTRequest.setList(serverJobRequest.getList());
        // ServerOTResponse otResultResponse = pirAppService.runServerOTparam(serverOTRequest);
        ServerOTResponse otResultResponse = serverOTService.runServerOTparam(serverOTRequest);
        logger.info("Serverjob: otResultResponse: {}.", objectMapper.writeValueAsString(otResultResponse));

        ClientJobResponse clientResponse = ClientJobResponse.successResponse();
        // clientResponse.setData("server run successfully!");
        // clientResponse.setData(otResultResponse.getList());
        clientResponse.setData(otResultResponse);
        return clientResponse;
    }
}

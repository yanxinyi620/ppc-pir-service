package cn.webank.wedpr.http.controller;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.common.WedprStatusEnum;
import cn.webank.wedpr.pir.message.ClientOTResponse;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.http.config.PirControllerConfig;
import cn.webank.wedpr.http.utils.HttpUtils;
import cn.webank.wedpr.http.utils.ParamUtils.*;
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
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.ResponseEntity;
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
    @Autowired private RestTemplate okHttpTemplate;
    @Autowired private RestTemplate trustOkHttpTemp;

    @RequestMapping("/test")
    private Object test() {
        // return "OK!";
        ClientJobResponse clientResponse = ClientJobResponse.successResponse();
        clientResponse.setData("testing OK!");
        return clientResponse;
    }

    // 请求方
    @PostMapping("/client")
    public Object clientService(@RequestBody ClientJobRequest clientJobRequest) {

        logger.info("Client job Starting, JobId: {}.", clientJobRequest.getJobId());
        // 初始化匿踪结果
        PirResultResponse pirResultResponse = new PirResultResponse();

        try {
            // logger.info("Clientjob: clientJobRequest: {}.", objectMapper.writeValueAsString(clientJobRequest));
            // 对未传参变量赋默认值
            clientJobRequest.setJobType((clientJobRequest.getJobType() != null) ? clientJobRequest.getJobType() : "0");
            clientJobRequest.setJobAlgorithmType(
                (clientJobRequest.getJobAlgorithmType() != null) ? clientJobRequest.getJobAlgorithmType() : "0");
            clientJobRequest.setObfuscationOrder(
                (clientJobRequest.getObfuscationOrder() != null) 
                ? clientJobRequest.getObfuscationOrder() : pirConfig.getObfuscationNumber());
            logger.info("Clientjob: clientJobRequest: {}.", objectMapper.writeValueAsString(clientJobRequest));
            
            // 1. hash披露/hash混淆，获取bashOT参数
            ClientOTResponse otParamResponse = pirAppService.requesterOtCipher(clientJobRequest);
            logger.info("Clientjob: otParamResponse: {}.", objectMapper.writeValueAsString(otParamResponse));

            // 2. 发送bashOT参数给数据方，并获取筛选结果
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
            serverJobRequest.setDataBodyList(otParamResponse.getDataBodyList());
            for (int i = 0; i < serverJobRequest.getDataBodyList().size(); i++) {
                serverJobRequest.getDataBodyList().get(i).setIdIndex(0);
            }
            logger.info("Client post request: data: {}.", objectMapper.writeValueAsString(serverJobRequest));

            // 从配置获取searchendpoint
            String searchendpoint = null;
            logger.info("Client post request: agencyip: {}.", pirConfig.getAgencyIp());
            for (int i = 0; i < pirConfig.getAgencyIp().size(); i++) {
                String agency = pirConfig.getAgencyIp().get(i);
                if (clientJobRequest.getSearchIp().equals(agency)) {
                    searchendpoint = pirConfig.getAgencyEndpoint().get(i);
                } else {
                }
            }
            logger.info("Client post request: searchendpoint: {}.", searchendpoint);

            // 设置请求头
            String pirUrl = HttpUtils.formatHttpUrl(pirConfig.getSslOn(), searchendpoint, pirConfig.getPirUri());
            String requestBody = objectMapper.writeValueAsString(serverJobRequest);

            SimpleEntity otResult = new SimpleEntity();
            if (pirConfig.getSslOn()) {
                otResult = HttpUtils.sendPostRequestWithRetry(
                    trustOkHttpTemp, pirUrl, requestBody, SimpleEntity.class, pirConfig.getRetryTimes());
            } else {
                otResult = HttpUtils.sendPostRequestWithRetry(
                    restTemplate, pirUrl, requestBody, SimpleEntity.class, pirConfig.getRetryTimes());
            }
            logger.info("Client post response: message: {}.", objectMapper.writeValueAsString(otResult));

            if (Objects.isNull(otResult.getData()) || otResult.getCode() != 0) {
                logger.warn(
                    "从数据方查询pir任务失败, taskID: {}, response: {}", clientJobRequest.getJobId(), otResult);
                ClientJobResponse clientResponse = ClientJobResponse.failureResponse(
                    otResult.getCode(), otResult.getMessage());
                ClientPirfailResponse pirResponse = new ClientPirfailResponse();
                pirResponse.setError(clientResponse);
                return pirResponse;
            }

            // 3. 根据筛选结果，获取最终匿踪结果
            pirResultResponse = pirAppService.requesterOtRecover(
                otParamResponse, clientJobRequest, otResult);
            logger.info("Client pir result: message: {}.", objectMapper.writeValueAsString(pirResultResponse));

        } catch (WedprException e) {
            // 处理 WedprException 错误
            e.printStackTrace();
            ClientPirfailResponse pirResponse = new ClientPirfailResponse();
            ClientJobResponse clientResponse = ClientJobResponse.failureResponse(
                Integer.parseInt(e.getStatus().getCode()), e.getStatus().getMessage());
            pirResponse.setError(clientResponse);
            logger.warn(
                "匿踪任务失败, taskID: {}, response: {}", 
                clientJobRequest.getJobId(), e.getStatus().getMessage());
            return pirResponse;
        } catch (Exception e) {
            // 处理其他可检查异常
            e.printStackTrace();
            ClientPirfailResponse pirResponse = new ClientPirfailResponse();
            ClientJobResponse clientResponse = ClientJobResponse.failureResponse(
                Integer.parseInt(WedprStatusEnum.SYSTEM_EXCEPTION.getCode()), 
                WedprStatusEnum.SYSTEM_EXCEPTION.getMessage());
            pirResponse.setError(clientResponse);
            logger.warn(
                "匿踪任务失败, taskID: {}, response: {}", 
                clientJobRequest.getJobId(), WedprStatusEnum.SYSTEM_EXCEPTION.getMessage());
            return pirResponse;
        }

        ClientJobResponse clientResponse = ClientJobResponse.successResponse();
        clientResponse.setData(pirResultResponse);
        // return clientResponse;
        logger.info("Client job Success, JobId: {}.", clientJobRequest.getJobId());

        ClientPirResponse pirResponse = new ClientPirResponse();
        pirResponse.setResult(clientResponse);
        return pirResponse;
    }

    // 数据方
    @PostMapping("/server")
    public Object serverService(@RequestBody ServerJobRequest serverJobRequest) throws Exception {

        logger.info("Server job Starting, JobId: {}.", serverJobRequest.getJobId());

        // 1. 根据请求，筛选数据，加密密钥，返回筛选结果及AES消息密文
        ClientJobResponse clientResponse = ClientJobResponse.successResponse();
        try {
            logger.info("Serverjob: serverJobRequest: {}.", objectMapper.writeValueAsString(serverJobRequest));
            ServerOTResponse otResultResponse = pirAppService.providerOtCipher(serverJobRequest);
            logger.info("Serverjob: otResultResponse: {}.", objectMapper.writeValueAsString(otResultResponse));
            // clientResponse.setData(otResultResponse.getList());
            clientResponse.setData(otResultResponse);

            // 调用web服务，保存匿踪参与任务
            if (pirConfig.getDeployMode() == DeployMode.PrivateMode.getValue()) {
                JobRequest jobRequest = new JobRequest();
                jobRequest.setJobId(serverJobRequest.getJobId());
                jobRequest.setJobTitle("PPC-AYS-Title");
                jobRequest.setToken("pws_api_key");
                jobRequest.setJobCreatorAgencyId(serverJobRequest.getJobCreatorAgencyId());
                jobRequest.setParticipateAgencyId(serverJobRequest.getParticipateAgencyId());
                jobRequest.setDatasetId(serverJobRequest.getDatasetId());
                jobRequest.setJobCreator(serverJobRequest.getJobCreator());

                // 设置请求头
                String pmsUrl = HttpUtils.formatHttpUrl(
                    pirConfig.getSslOn(), pirConfig.getWebServiceEndpoint(), pirConfig.getPmsUri());
                String requestBody = objectMapper.writeValueAsString(jobRequest);
                // JobEntity jobResult =
                //         HttpUtils.sendPostRequestWithRetry(
                //                 restTemplate, pmsUrl, requestBody, JobEntity.class, pirConfig.getRetryTimes());
                JobEntity jobResult =
                        HttpUtils.sendPatchRequest(
                                okHttpTemplate, pmsUrl, requestBody, JobEntity.class);

                if (Objects.isNull(jobResult.getData()) || jobResult.getErrorCode() != 0) {
                    logger.warn(
                        "从web服务查询调用patch,保存匿踪参与任务失败, taskID: {}, response: {}", 
                        jobRequest.getJobId(), jobResult);
                    ClientJobResponse clientpatchResponse = ClientJobResponse.failureResponse(
                        jobResult.getErrorCode(), jobResult.getMessage());
                    return clientpatchResponse;
                }
                logger.info("Serverjob: patchResponse: {}.", jobResult.getErrorCode());
            }

            logger.info("Server job Success, JobId: {}.", serverJobRequest.getJobId());

        } catch (WedprException e) {
            // 处理 WedprException 错误
            e.printStackTrace();
            clientResponse.setCode(Integer.parseInt(e.getStatus().getCode()));
            clientResponse.setMessage(e.getStatus().getMessage());
            logger.warn(
                "服务方匿踪任务失败, taskID: {}, response: {}", 
                serverJobRequest.getJobId(), e.getStatus().getMessage());
            // clientResponse.setData(e);
        } catch (Exception e) {
            // 处理其他可检查异常
            e.printStackTrace();
            clientResponse.setCode(Integer.parseInt(WedprStatusEnum.SYSTEM_EXCEPTION.getCode()));
            clientResponse.setMessage(WedprStatusEnum.SYSTEM_EXCEPTION.getMessage());
            logger.warn(
                "服务方匿踪任务失败, taskID: {}, response: {}", 
                serverJobRequest.getJobId(), WedprStatusEnum.SYSTEM_EXCEPTION.getMessage());
        }
        
        return clientResponse;
    }
}

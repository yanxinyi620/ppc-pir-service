package cn.webank.wedpr.http.utils;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.common.WedprStatusEnum;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static String formatHttpUrl(boolean sslOn, String endpoint, String uri) {
        String protocol = "http";
        if (sslOn) {

            protocol = "https";
        }

        return String.format("%s://%s%s", protocol, endpoint, uri);
    }

    public static <T> T sendGetRequest(RestTemplate restTemplate, String url, Class<T> responseType)
            throws WedprException {
        T response = null;
        try {
            ResponseEntity<T> httpResponse = restTemplate.getForEntity(url, responseType);
            HttpStatus statusCode = httpResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                response = Objects.requireNonNull(httpResponse.getBody());
            } else {
                logger.error(
                        "发送GET请求失败, url: {}, status: {}, message: {}",
                        url,
                        statusCode.value(),
                        statusCode.getReasonPhrase());
                throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
            }
        } catch (HttpClientErrorException e) {
            logger.error("发送GET请求失败, url: {}", url, e);
            throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
        }

        return response;
    }

    public static <T, R> T sendPostRequest(
            RestTemplate restTemplate, String url, R body, Class<T> responseType)
            throws WedprException {
        T response = null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<R> entity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<T> httpResponse = restTemplate.postForEntity(url, entity, responseType);
            HttpStatus statusCode = httpResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                response = Objects.requireNonNull(httpResponse.getBody());
            } else {
                logger.error(
                        "发送POST请求失败, url: {}. status: {}, message: {}",
                        url,
                        statusCode.value(),
                        statusCode.getReasonPhrase());
                throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
            }
        } catch (HttpClientErrorException e) {
            logger.error("发送POST请求失败, url: {}", url, e);
            throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
        }

        return response;
    }

    public static <T, R> T sendPostRequestWithRetry(
            RestTemplate restTemplate, String url, R body, Class<T> responseType, int maxRetries)
            throws WedprException {
        T response = null;
        int retries = 0;
        while (retries <= maxRetries) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                // 关闭长连接，keep-alive
                httpHeaders.setConnection("close");

                HttpEntity<R> entity = new HttpEntity<>(body, httpHeaders);
                ResponseEntity<T> httpResponse =
                        restTemplate.postForEntity(url, entity, responseType);
                HttpStatus statusCode = httpResponse.getStatusCode();
                if (statusCode == HttpStatus.OK) {
                    response = Objects.requireNonNull(httpResponse.getBody());
                    break; // 请求成功，跳出重试循环
                } else {
                    logger.error(
                            "发送POST请求失败, url: {}, status: {}, message: {}",
                            url,
                            statusCode.value(),
                            statusCode.getReasonPhrase());
                    throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
                }
            } catch (WedprException e) {
                throw e;
            } catch (Exception e) {
                logger.info("发送POST请求失败, url: {}", url, e);
                retries++;
                if (retries > maxRetries) {
                    logger.error("发送POST请求失败, url: {}", url, e);
                    throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
                }
                logger.info("进行第{}次POST请求重试...", retries);
            }
        }

        return response;
    }

    public static <T, R> T sendPatchRequest(
            RestTemplate restTemplate, String url, R body, Class<T> responseType)
            throws WedprException {
        T response = null;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<R> entity = new HttpEntity<>(body, httpHeaders);
            // ResponseEntity<T> httpResponse = restTemplate.postForEntity(url, entity, responseType);
            ResponseEntity<T> httpResponse = restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
            HttpStatus statusCode = httpResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                response = Objects.requireNonNull(httpResponse.getBody());
            } else {
                logger.error(
                        "发送PATCH请求失败, url: {}. status: {}, message: {}",
                        url,
                        statusCode.value(),
                        statusCode.getReasonPhrase());
                throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
            }
        } catch (HttpClientErrorException e) {
            logger.error("发送PATCH请求失败, url: {}", url, e);
            throw new WedprException(WedprStatusEnum.HTTP_CALL_ERROR);
        }

        return response;
    }
}

package cn.webank.wedpr.http.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.TrustManagerFactory;


@Configuration
public class TrustOkHttpResRemp {

    private static final Logger logger = LoggerFactory.getLogger(TrustOkHttpResRemp.class);

    @Value("${ssl.on}")
    private Boolean sslOn;

    @Value("${client.ssl.key-store-password}")
    private String jksPassword;

    @Value("${client.ssl.key-store}")
    private String keystoreRelativePath;

    @Value("${client.ssl.trust-store}")
    private String truststoreRelativePath;

    // String keystoreRelativePath = "http/src/main/resources/keystore.jks";
    // String truststoreRelativePath = "truststore.jks";
    // String jksPassword = "Wedpr2023";

    @Bean
    public RestTemplate trustOkHttpTemp(@Qualifier("clientHttpRequestFactory") ClientHttpRequestFactory factory) {
        if (sslOn) {
            return new RestTemplate(factory);
        }
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }

    @Bean(name = "clientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory() throws Exception {
        if (sslOn) {
            return new OkHttp3ClientHttpRequestFactory(okHttpClient());
        }
        return new OkHttp3ClientHttpRequestFactory();
    }

    @Bean
    public OkHttpClient okHttpClient() throws Exception {

        if (sslOn) {
            // 加载 JKS 证书文件
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream keystoreStream = new FileInputStream(keystoreRelativePath)) {
                keyStore.load(keystoreStream, jksPassword.toCharArray());
            }

            // 创建 TrustManager，使用上述的 KeyStore
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // 创建 SSLContext，使用上述的 TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext().getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
                .connectionPool(new ConnectionPool())
                .build();
        }
        return null;
    }

    @Bean
    public SSLContext sslContext() throws Exception {

        // 初始化信任管理器工厂
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        logger.info("trustStream start load trustStore.jks.");
        try (InputStream trustStream = getClass().getClassLoader().getResourceAsStream(truststoreRelativePath)) {
            // trustStore.load(trustStream, jksPassword.toCharArray());
            if (trustStream == null) {
                System.out.println("trustStream is null");
            } else {
                System.out.println("trustStream is not null");
            }
            trustStore.load(trustStream, jksPassword.toCharArray());
            System.out.println("trustStore loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("trustStream finish load trustStore.jks.");
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

        // SSLContext sslContext = SSLContext.getInstance("TLS");
        // sslContext.init(null, trustSpecificCertificate(), new java.security.SecureRandom());

        return sslContext;
    }
}

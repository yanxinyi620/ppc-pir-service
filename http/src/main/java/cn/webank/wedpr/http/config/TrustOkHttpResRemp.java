package cn.webank.wedpr.http.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

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
    String keystoreRelativePath = "http/src/main/resources/keystore.jks";
    // String keystoreRelativePath = "keystore.jks";
    // String truststoreRelativePath = "http/src/main/resources/truststore.jks";
    String truststoreRelativePath = "truststore.jks";
    String jksPassword = "Wedpr2023";

    @Bean
    public RestTemplate trustOkHttpTemp(@Qualifier("clientHttpRequestFactory") ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean(name = "clientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory() throws Exception {
        return new OkHttp3ClientHttpRequestFactory(okHttpClient());
    }

    // @Bean
    // public OkHttpClient okHttpClient() throws Exception {
    //     return new OkHttpClient.Builder()
    //         .sslSocketFactory(sslContext().getSocketFactory(), (X509TrustManager) trustSpecificCertificate()[0])
    //         .connectionPool(new ConnectionPool())
    //         .build();
    // }

    @Bean
    public OkHttpClient okHttpClient() throws Exception {

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

    // private TrustManager[] trustAllCertificates() {
    //     return new TrustManager[]{
    //         new X509TrustManager() {
    //             public X509Certificate[] getAcceptedIssuers() {
    //                 return new X509Certificate[0];
    //             }

    //             public void checkClientTrusted(X509Certificate[] certs, String authType) {
    //             }

    //             public void checkServerTrusted(X509Certificate[] certs, String authType) {
    //             }
    //         }
    //     };
    // }


    private TrustManager[] trustSpecificCertificate() throws Exception {
        try {
            // 1. 加载 keystore.p12 证书文件
            KeyStore keyStore = KeyStore.getInstance("JKS");

            // 注意：这里使用 ClassLoader 加载资源，路径应该是相对于类路径的
            // logger.info("inputStream start load keystore.p12.");
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(keystoreRelativePath)) {
                // keyStore.load(inputStream, jksPassword.toCharArray());
                if (inputStream == null) {
                    System.out.println("inputStream is null");
                } else {
                    System.out.println("inputStream is not null");
                }
                keyStore.load(inputStream, jksPassword.toCharArray());
                System.out.println("inputStream loaded successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // logger.info("inputStream finish load keystore.p12.");

            // 2. 创建 TrustManager，仅信任指定 keystore.p12 证书
            TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                        // 客户端证书验证逻辑，根据实际需求实现
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                        try {
                            // 检查服务端证书是否在 keystore 中
                            for (X509Certificate cert : x509Certificates) {
                                if (keyStore.getCertificateAlias(cert) != null) {
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            // throw new java.security.cert.CertificateException("Error checking server certificate", e);
                            logger.error("Error checking server certificate.");
                        }
                        // throw new java.security.cert.CertificateException("Untrusted server certificate");
                        logger.error("Untrusted server certificate.");
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        // return new X509Certificate[0];  // 返回空数组而不是 null
                        return getTrustedCertificates();
                    }
                }
            };

            return trustManagers;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing trust manager", e);
        }
    }


    // 获取可信的自签名证书数组
    private X509Certificate[] getTrustedCertificates() {
        try {
            // 加载 keystore.p12 证书文件
            KeyStore keyStore = KeyStore.getInstance("JKS");

            // 注意：这里使用 ClassLoader 加载资源，路径应该是相对于类路径的
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(keystoreRelativePath)) {
                keyStore.load(inputStream, jksPassword.toCharArray());
            }

            // 获取 keystore 中的证书
            Enumeration<String> aliases = keyStore.aliases();
            List<X509Certificate> trustedCerts = new ArrayList<>();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                trustedCerts.add(cert);
            }

            return trustedCerts.toArray(new X509Certificate[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error loading trusted certificates from keystore.p12", e);
        }
    }

}

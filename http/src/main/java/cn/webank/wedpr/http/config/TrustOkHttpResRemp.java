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

@Configuration
public class TrustOkHttpResRemp {

    @Bean
    public RestTemplate trustOkHttpTemp(@Qualifier("clientHttpRequestFactory") ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean(name = "clientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory() throws Exception {
        return new OkHttp3ClientHttpRequestFactory(okHttpClient());
    }

    @Bean
    public OkHttpClient okHttpClient() throws Exception {
        return new OkHttpClient.Builder()
            .sslSocketFactory(sslContext().getSocketFactory(), (X509TrustManager) trustAllCertificates()[0])
            .connectionPool(new ConnectionPool())
            .build();
    }

    @Bean
    public SSLContext sslContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates(), new java.security.SecureRandom());
        return sslContext;
    }

    private TrustManager[] trustAllCertificates() {
        return new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
    }
}

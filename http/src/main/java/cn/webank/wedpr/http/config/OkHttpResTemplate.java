package cn.webank.wedpr.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OkHttpResTemplate {
    @Bean
    public RestTemplate okHttpTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}

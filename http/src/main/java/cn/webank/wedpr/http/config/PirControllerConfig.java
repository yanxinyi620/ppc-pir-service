package cn.webank.wedpr.http.config;

import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PirControllerConfig {

    @Value("${server.port}")
    private Integer port;

    @Value("${web.service.endpoint}")
    private String webserviceendpoint;

    @Value("${pir.length}")
    private Integer otlength;

    @Value("${server.deploymode}")
    private Integer deploymode;

    @Value("#{'${server.agencyip}'.split(';')}")
    private List<String> agencyip;

    @Value("#{'${server.agencyendpoint}'.split(';')}")
    private List<String> agencyendpoint;

    public Integer getPort() {
        return port;
    }

    public String getWebServiceEndpoint() {
        return webserviceendpoint;
    }

    public Integer getOtlength() {
        return otlength;
    }

    public Integer getDeploymode() {
        return deploymode;
    }

    public List<String> getAgencyip() {
        return agencyip;
    }

    public List<String> getAgencyendpoint() {
        return agencyendpoint;
    }
}

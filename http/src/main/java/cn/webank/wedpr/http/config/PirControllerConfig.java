package cn.webank.wedpr.http.config;

import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PirControllerConfig {

    @Value("${server.port}")
    private Integer pirPort;

    @Value("#{'${server.agencyip}'.split(';')}")
    private List<String> agencyIp;

    @Value("#{'${server.agencyendpoint}'.split(';')}")
    private List<String> agencyEndpoint;

    @Value("${pir.deploymode}")
    private Integer deployMode;

    @Value("${pir.filter.length}")
    private Integer filterLength;

    @Value("${pir.obfuscation.number}")
    private Integer obfuscationNumber;

    @Value("${http.retrytimes}")
    private Integer retryTimes;

    @Value("${web.service.endpoint}")
    private String webServiceEndpoint;

    @Value("${pir.server.uri}")
    private String pirUri;

    @Value("${pms.patch.uri}")
    private String pmsUri;

    @Value("${dataset.id.prefix}")
    private String datasetIdPrefix;

    @Value("${dataset.id.substring}")
    private Integer datasetIdSubstr;

    @Value("${ssl.on}")
    private Boolean sslOn;

    public Integer getPirPort() {
        return pirPort;
    }

    public List<String> getAgencyIp() {
        return agencyIp;
    }

    public List<String> getAgencyEndpoint() {
        return agencyEndpoint;
    }

    public Integer getDeployMode() {
        return deployMode;
    }

    public Integer getFilterLength() {
        return filterLength;
    }

    public Integer getObfuscationNumber() {
        return obfuscationNumber;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public String getWebServiceEndpoint() {
        return webServiceEndpoint;
    }

    public String getPirUri() {
        return pirUri;
    }

    public String getPmsUri() {
        return pmsUri;
    }

    public String getDatasetIdPrefix() {
        return datasetIdPrefix;
    }

    public Integer getDatasetIdSubstr() {
        return datasetIdSubstr;
    }

    public Boolean getSslOn() {
        return sslOn;
    }
}

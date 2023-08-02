package cn.webank.wedpr.http.message;

import lombok.Data;

@Data
public class ClientPirResponse {
    
    ClientJobResponse result;
    
    // PirResponseData result;
    // @Data
    // public class PirResponseData {
    //     ClientJobResponse clientJobResponse;
    // }
}

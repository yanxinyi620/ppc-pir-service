package cn.webank.wedpr.http.message;

import lombok.Data;

@Data
public class ClientPirfailResponse {
    
    ClientJobResponse error;
    
    // PirResponseData result;
    // @Data
    // public class PirResponseData {
    //     ClientJobResponse clientJobResponse;
    // }
}

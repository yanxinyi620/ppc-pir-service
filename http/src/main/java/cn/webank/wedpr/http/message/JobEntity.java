package cn.webank.wedpr.http.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JobEntity extends BaseResponse {
    int errorCode;
    String message;
    String data;
}

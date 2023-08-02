package cn.webank.wedpr.http.message;

import cn.webank.wedpr.http.message.body.PirResultBody;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PirResultResponse extends BaseResponse {
    String jobId;
    List<PirResultBody> detail;
}

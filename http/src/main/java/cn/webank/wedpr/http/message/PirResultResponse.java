package cn.webank.wedpr.http.message;

// import cn.webank.wedpr.pir.message.body.PirResultBody;
import cn.webank.wedpr.pir.message.ClientDecryptResponse;
// import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PirResultResponse extends BaseResponse {
    String jobId;
    String jobType;
    // List<PirResultBody> detail;
    ClientDecryptResponse detail;

    // Detail detail;
    // @Data
    // public class Detail {
    //     ClientDecryptResponse clientDecryptResponse;
    // }
}

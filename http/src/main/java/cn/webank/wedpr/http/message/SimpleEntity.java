package cn.webank.wedpr.http.message;

// import cn.webank.wedpr.http.message.body.ServerResultBody;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SimpleEntity extends BaseResponse {
    int code;
    String message;
    ResponseData data;

    @Data
    public class ResponseData {
        List<ServerResultlist> list;
    }
}

package cn.webank.wedpr.http.message;

// import cn.webank.wedpr.http.message.body.ServerResultBody;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServerOTResponse extends BaseResponse {
    List<ServerResultlist> list;
}

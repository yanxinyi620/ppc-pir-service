package cn.webank.wedpr.http.message;

import cn.webank.wedpr.http.message.body.ServerDataBody;
import java.util.List;
import java.math.BigInteger;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServerJobRequest extends BaseRequest {
    // ServerDataBody serverData;
    BigInteger x;
    BigInteger y;
    List<ServerDataBody> list;
}

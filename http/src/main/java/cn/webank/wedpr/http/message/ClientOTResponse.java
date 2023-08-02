package cn.webank.wedpr.http.message;

import cn.webank.wedpr.http.message.body.ServerDataBody;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientOTResponse extends BaseResponse {
    BigInteger b;
    BigInteger x;
    BigInteger y;
    List<ServerDataBody> list;
}

package cn.webank.wedpr.pir.message;

import cn.webank.wedpr.pir.message.body.ServerDataBody;
import java.util.List;
import lombok.Data;
// import lombok.EqualsAndHashCode;
import java.math.BigInteger;

// @EqualsAndHashCode(callSuper = true)
@Data
public class ClientOTResponse {
    BigInteger b;
    BigInteger x;
    BigInteger y;
    List<ServerDataBody> dataBodyList;
}

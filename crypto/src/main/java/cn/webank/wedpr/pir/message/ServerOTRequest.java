package cn.webank.wedpr.pir.message;

import cn.webank.wedpr.pir.message.body.ServerDataBody;
import java.util.List;
import java.math.BigInteger;
import lombok.Data;
// import lombok.EqualsAndHashCode;

// @EqualsAndHashCode(callSuper = true)
@Data
public class ServerOTRequest {
    String jobType;
    String datasetId;
    BigInteger x;
    BigInteger y;
    List<ServerDataBody> list;
}

package cn.webank.wedpr.pir.message;

// import cn.webank.wedpr.pir.message.body.ServerResultBody;
import cn.webank.wedpr.pir.message.body.ClientDataBody;
import java.util.List;
import lombok.Data;
// import lombok.EqualsAndHashCode;
import java.math.BigInteger;

// @EqualsAndHashCode(callSuper = true)
@Data
public class ClientDecryptRequest {
    BigInteger b;
    List<ClientDataBody> list;
    // ServerResult serverResult;
    // @Data
    // public class ServerResult {
    //     List<ServerResultlist> list;
    // }
    ServerOTResponse serverResult;
}

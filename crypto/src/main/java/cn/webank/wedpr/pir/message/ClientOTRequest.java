package cn.webank.wedpr.pir.message;

import cn.webank.wedpr.pir.message.body.ClientDataBody;
import java.util.List;
import lombok.Data;
// import lombok.EqualsAndHashCode;

// @EqualsAndHashCode(callSuper = true)
@Data
public class ClientOTRequest {
    int filterLength;
    Integer obfuscationOrder;
    // ClientDataBody clientData;
    List<ClientDataBody> dataBodyList;
}

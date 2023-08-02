package cn.webank.wedpr.http.message;

import cn.webank.wedpr.http.message.body.ClientDataBody;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientJobRequest extends BaseRequest {
    String searchIp;
    // ClientDataBody clientData;
    List<ClientDataBody> list;
}

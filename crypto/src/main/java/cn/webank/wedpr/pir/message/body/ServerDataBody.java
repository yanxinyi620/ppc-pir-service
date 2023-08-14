package cn.webank.wedpr.pir.message.body;

import lombok.Data;
import java.math.BigInteger;
import java.util.List;

@Data
public class ServerDataBody {
    BigInteger z0;
    String filter;
    int idIndex;
    List<ClientDataBody> idHashList;
}

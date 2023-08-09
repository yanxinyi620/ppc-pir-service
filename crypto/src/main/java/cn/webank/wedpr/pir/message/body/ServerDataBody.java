package cn.webank.wedpr.pir.message.body;

import lombok.Data;
import java.math.BigInteger;

@Data
public class ServerDataBody {
    BigInteger z0;
    String filter;
}

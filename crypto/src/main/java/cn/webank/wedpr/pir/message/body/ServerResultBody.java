package cn.webank.wedpr.pir.message.body;

import lombok.Data;
import java.math.BigInteger;

@Data
public class ServerResultBody {
    BigInteger e;
    BigInteger w;
    String c;
}

package cn.webank.wedpr.pir.message;

import cn.webank.wedpr.pir.message.body.ServerResultBody;
import java.util.List;
import lombok.Data;
// import lombok.EqualsAndHashCode;

// @EqualsAndHashCode(callSuper = true)
@Data
public class ServerResultlist {
    List<ServerResultBody> list;
}

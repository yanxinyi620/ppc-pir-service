package cn.webank.wedpr.pir.message.body;

import lombok.Data;

@Data
public class PirResultBody {
    String searchId;
    Boolean searchExist;
    String searchValue;
}

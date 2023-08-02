package cn.webank.wedpr.http.message.body;

import lombok.Data;

@Data
public class SetDataBody {
    String usename;
    String datasetId;
    Integer idx;
}

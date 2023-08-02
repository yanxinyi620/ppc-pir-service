package cn.webank.wedpr.http.message;

import lombok.Data;

@Data
public class BaseRequest {
    // 任务ID
    String jobId;
    // 任务类型
    String jobType;
    // 发起方机构id
    String jobCreatorAgencyId;
    // 数据方机构id
    String participateAgencyId;
    // 数据集id
    String datasetId;
    // 发起方用户名
    String jobCreator;
}

package cn.webank.wedpr.http.message;

import lombok.Data;

@Data
public class JobRequest {
    // 任务ID
    String jobId;

    // 任务类型
    String jobTitle;

    String token;
    String jobCreatorAgencyId;
    String participateAgencyId;
    String datasetId;
    String jobCreator;
}

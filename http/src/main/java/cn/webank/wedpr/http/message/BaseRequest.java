package cn.webank.wedpr.http.message;

import lombok.Data;

@Data
public class BaseRequest {
    // 任务ID
    String jobId;
    // 任务类型(0: 查询存在性, 1: 查询对应值)
    String jobType;
    // 发起方机构id
    String jobCreatorAgencyId;
    // 数据方机构id
    String participateAgencyId;
    // 数据集id
    String datasetId;
    // 发起方用户名
    String jobCreator;

    // 匿踪算法类型(0: hash披露算法, 1: hash混淆算法)
    String jobAlgorithmType;
    // 查询范围
    // Integer searchFiled;
    // id obfuscation order(混淆数量，填入>=1的整数，默认为1)
    Integer obfuscationOrder;
}

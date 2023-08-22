package cn.webank.wedpr.http.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ParamUtils {

    // 定义 JobType 枚举类
    @AllArgsConstructor
    @Getter
    public enum JobType {
        idExist("0"), idValue("1");

        private String value;

        // public int getValue() {
        //     return value;
        // }
    }

    // 定义 AlgorithmType 枚举类
    @AllArgsConstructor
    @Getter
    public enum AlgorithmType {
        idFilter("0"), idObfuscation("1");

        private String value;

        // public int getValue() {
        //     return value;
        // }
    }
}

package cn.webank.wedpr.http.service;

import cn.webank.wedpr.http.utils.ParamUtils.AlgorithmType;
import cn.webank.wedpr.http.utils.ParamUtils.JobType;
import cn.webank.wedpr.pir.common.WedprException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ParamUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(ParamUtilsTest.class);

    @Test
    public void testParamUtils() throws WedprException {

        String selectedJobType = "3";
        String selectedAlgorithm = "1";

        if (selectedJobType == JobType.idExist.getValue()) {
            System.out.println("Selected JobType: " + JobType.idExist.getValue());
            logger.info("Selected JobType: " + JobType.idExist.getValue());
        } else if (selectedJobType == JobType.idValue.getValue()) {
            System.out.println("Selected JobType: " + JobType.idValue.getValue());
            logger.info("Selected JobType: " + JobType.idValue.getValue());
        } else {
            System.out.println("Invalid JobType option");
            logger.info("Invalid JobType option");
        }

        if (selectedAlgorithm == AlgorithmType.idFilter.getValue()) {
            System.out.println("Selected AlgorithmType: " + AlgorithmType.idFilter.getValue());
            logger.info("Selected AlgorithmType: " + AlgorithmType.idFilter.getValue());
        } else if (selectedAlgorithm == AlgorithmType.idObfuscation.getValue()) {
            System.out.println("Selected AlgorithmType: " + AlgorithmType.idObfuscation.getValue());
            logger.info("Selected AlgorithmType: " + AlgorithmType.idObfuscation.getValue());
        } else {
            System.out.println("Invalid AlgorithmType option");
            logger.info("Invalid AlgorithmType option");
        }

    }
}

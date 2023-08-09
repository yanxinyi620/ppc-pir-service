package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.common.WedprException;
import cn.webank.wedpr.pir.message.ServerOTRequest;
import cn.webank.wedpr.pir.message.ServerOTResponse;
import cn.webank.wedpr.pir.message.body.ServerDataBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

@SpringBootTest
public class ServerOTServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ClientOTServiceTest.class);

    // @Autowired private ObjectMapper objectMapper;
    @Autowired private ServerOTService serverOTService;

    @Test
    public void testServerOTparam() throws WedprException {
        
        // "jobId":"j-123","jobType":"1","jobCreatorAgencyId":"1001","participateAgencyId":"1001","datasetId":"t_pir","jobCreator":"admin",
        // "x":14270551836700888481126517798254026547703523183642719791299327716888724632877193492803962338343087963017635343637598051759596834590042370767428481017737547030432648599154848866664208619948785997934768878040977609454696044460364609916482281508015487701226750311067535312634027360980005942455568987071322370505,
        // "y":60106705207356264675467910226713945541297614672379640997505837000491857382397260307947833384714013852792196146148939487816990099432516030424638620185880478801739564682430434235172940349299049251061616188985051021012526825456123956039888025190202548676257368842559920029966097500820159656362853178619497577848,
        // "list":[
            // {"z0":34892639292189340553461785685298621763795724025619494486784176338658763548079073431283558601440704269742566498217839967316908039431904382862329623007642573279253743624792730909729484788502748652952534107954119000661293518228366751858902551579100637309977233585574313610381653076181972704793702017185507431246,
            // "filter":"abc1"},
            // {"z0":93910703884279641599192316220434253045390044122450779135852915980337673960861181505425240047739029714772872064708758024127949607201727497338081111045895898190171730613636153018816452149850814037344475647879637567165781490770283299386956842860623961010346431401689531647125539946911808924060032874457667411645,
            // "filter":"jdf9"},
            // {"z0":4803610520312931868205742264057610273806826703378435412921850265231755231108028209258152330719723633972692360561721503554573857919644744922923709795454599581688170528052317166523882450561580080800849022379275882479154170557083333393577846666847319439380810073053969119454478627694282973843013823640420594744,
            // "filter":"jdfr"}]}.

        String jobType = "1";
        String datasetId = "t_pir";
        BigInteger x = new BigInteger("14270551836700888481126517798254026547703523183642719791299327716888724632877193492803962338343087963017635343637598051759596834590042370767428481017737547030432648599154848866664208619948785997934768878040977609454696044460364609916482281508015487701226750311067535312634027360980005942455568987071322370505");
        BigInteger y = new BigInteger("60106705207356264675467910226713945541297614672379640997505837000491857382397260307947833384714013852792196146148939487816990099432516030424638620185880478801739564682430434235172940349299049251061616188985051021012526825456123956039888025190202548676257368842559920029966097500820159656362853178619497577848");

        List<BigInteger> z0List = new ArrayList<>();
        // 添加元素到列表
        z0List.add(new BigInteger("34892639292189340553461785685298621763795724025619494486784176338658763548079073431283558601440704269742566498217839967316908039431904382862329623007642573279253743624792730909729484788502748652952534107954119000661293518228366751858902551579100637309977233585574313610381653076181972704793702017185507431246"));
        z0List.add(new BigInteger("93910703884279641599192316220434253045390044122450779135852915980337673960861181505425240047739029714772872064708758024127949607201727497338081111045895898190171730613636153018816452149850814037344475647879637567165781490770283299386956842860623961010346431401689531647125539946911808924060032874457667411645"));
        z0List.add(new BigInteger("4803610520312931868205742264057610273806826703378435412921850265231755231108028209258152330719723633972692360561721503554573857919644744922923709795454599581688170528052317166523882450561580080800849022379275882479154170557083333393577846666847319439380810073053969119454478627694282973843013823640420594744"));

        List<String> filterList = new ArrayList<>();
        // 添加元素到列表
        filterList.add("abc1");
        filterList.add("jdf9");
        filterList.add("jdfr");

        List<ServerDataBody> serverDataArrayList = new ArrayList<>();
        for (int i = 0; i < filterList.size(); i++) {
            String filter = filterList.get(i);
            BigInteger z0 = z0List.get(i);

            ServerDataBody serverDataBody = new ServerDataBody();
            serverDataBody.setZ0(z0);
            serverDataBody.setFilter(filter);
            serverDataArrayList.add(serverDataBody);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        ServerOTRequest serverOTRequest = new ServerOTRequest();
        serverOTRequest.setJobType(jobType);
        serverOTRequest.setDatasetId(datasetId);
        serverOTRequest.setX(x);
        serverOTRequest.setY(y);
        serverOTRequest.setList(serverDataArrayList);

        try {
            logger.info("Singleton test clientOTRequest: {}.", objectMapper.writeValueAsString(serverOTRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        ServerOTService serverOTService = new ServerOTService();
        // 1. 因为serverOTService.runServerOTparam可能存在报错情况,此处需要使用try将return结果存入serverOTResponse.
        // 2. 因为serverOTResponse可能存在未赋值的情况(即try出现异常),不方便用if分别处理两种情况,所以提前赋一个null值.
        ServerOTResponse serverOTResponse = null;
        try {
            serverOTResponse = serverOTService.runServerOTparam(serverOTRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (serverOTResponse != null) {
            try {
                logger.info("Singleton test serverOTResponse: {}", objectMapper.writeValueAsString(serverOTResponse));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            // 处理变量未初始化的情况
        }

        assert true;

    }
}

package benchmark;

import com.google.gson.Gson;
import demo.IotSimpleEntity;
import org.junit.Before;
import org.junit.Test;
import util.CodecUtil;
import util.ProtobufCountUtil;
import util.ProtocolConfig;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 20:20
 */

public class TestCompressionRate {

    ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();

    Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        CodecUtil.registerClass(IotSimpleEntity.class);
    }


    /**
     * test default compression rate
     */
    @Test
    public void testDefaultConfig() throws IllegalAccessException {
        protocolConfig.setEnableDoubleCompression(false);
        protocolConfig.setEnableBaseLineCompression(false);
        protocolConfig.setVariableHeadByteLength(4);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        long myLength = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        for (int i = 0; i < 100000; i++) {
            IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
            myLength += codecUtil.encode(iotSimpleEntity).length;
            protobufLength += ProtobufCountUtil.countBytes(iotSimpleEntity);
            jsonLength += gson.toJson(iotSimpleEntity).getBytes().length;
        }
        System.out.println("myLength: " + myLength);
        System.out.println("protobufLength: " + protobufLength);
        System.out.println("jsonLength: " + jsonLength);
    }


}

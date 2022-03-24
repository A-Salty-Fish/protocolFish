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

    @Test
    public void testCodec2WithBaseLine() throws Exception {
        IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity(255.0);
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(4);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(IotSimpleEntity.clone(iotSimpleEntity));
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        long myLength2 = 0;
        long myLength1 = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        for (int i = 0; i < 100000; i++) {
            iotSimpleEntity = IotSimpleEntity.randomNearIotSimpleEntity(iotSimpleEntity, 10.0, 4, 1000, 20);
            myLength1 += codecUtil.encode(iotSimpleEntity).length;
            myLength2 += codecUtil.encode2(iotSimpleEntity).length;
            protobufLength += ProtobufCountUtil.countBytes(iotSimpleEntity);
            jsonLength += gson.toJson(iotSimpleEntity).getBytes().length;
        }
        System.out.println("myLength1: " + myLength1);
        System.out.println("myLength2: " + myLength2);
        System.out.println("protobufLength: " + protobufLength);
        System.out.println("jsonLength: " + jsonLength);
        System.out.println();
        System.out.println("myLength1/protobufLength: " + (double) myLength1 / protobufLength);
        System.out.println("myLength2/protobufLength: " + (double) myLength2 / protobufLength);
        System.out.println("myLength1/jsonLength: " + (double) myLength1 / jsonLength);
        System.out.println("myLength2/jsonLength: " + (double) myLength2 / jsonLength);
    }
}

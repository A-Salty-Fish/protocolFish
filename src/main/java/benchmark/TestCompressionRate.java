package benchmark;

import com.google.gson.Gson;
import demo.IotSimpleEntity;
import org.junit.Before;
import org.junit.Test;
import util.CodecUtil;
import util.ProtobufCountUtil;
import util.ProtocolConfig;
import util.XmlUtil;

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
    public void testDefaultConfig() throws Exception {
        protocolConfig.setEnableDoubleCompression(false);
        protocolConfig.setEnableBaseLineCompression(false);
        protocolConfig.setVariableHeadByteLength(4);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        long myLength1 = 0;
        long myLength2 = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        long xmlLength = 0;
        for (int i = 0; i < 1000; i++) {
            IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
            myLength1 += codecUtil.encode(iotSimpleEntity).length;
            myLength2 += codecUtil.encode2(iotSimpleEntity).length;
            protobufLength += ProtobufCountUtil.countBytes(iotSimpleEntity);
            jsonLength += gson.toJson(iotSimpleEntity).getBytes().length;
            xmlLength += XmlUtil.convertToXml(iotSimpleEntity).getBytes().length;
        }
        System.out.println("|myLength1\t|" + myLength1 + "|\t");
        System.out.println("|myLength2\t|" + myLength2 + "|\t");
        System.out.println("|protobufLength\t| " + protobufLength + "|\t");
        System.out.println("|jsonLength:\t|" + jsonLength + "|\t");
        System.out.println("|xmlLength:\t|" + xmlLength + "|\t");
    }

    @Test
    public void testCodecWithDoubleCompression() throws Exception {
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setEnableBaseLineCompression(false);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        long myLength1 = 0;
        long myLength2 = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        long xmlLength = 0;
        for (int i = 0; i < 1000; i++) {
            IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
            myLength1 += codecUtil.encode(iotSimpleEntity).length;
            myLength2 += codecUtil.encode2(iotSimpleEntity).length;
            protobufLength += ProtobufCountUtil.countBytes(iotSimpleEntity);
            jsonLength += gson.toJson(iotSimpleEntity).getBytes().length;
            xmlLength += XmlUtil.convertToXml(iotSimpleEntity).getBytes().length;
        }
        System.out.println("|myLength1\t|" + myLength1 + "|\t");
        System.out.println("|myLength2\t|" + myLength2 + "|\t");
        System.out.println("|protobufLength\t| " + protobufLength + "|\t");
        System.out.println("|jsonLength:\t|" + jsonLength + "|\t");
        System.out.println("|xmlLength:\t|" + xmlLength + "|\t");
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
        long xmlLength = 0;
        for (int i = 0; i < 2000; i++) {
            iotSimpleEntity = IotSimpleEntity.randomNearIotSimpleEntity(iotSimpleEntity, 10.0, 4, 1000, 20);
            myLength1 += codecUtil.encode(iotSimpleEntity).length;
            myLength2 += codecUtil.encode2(iotSimpleEntity).length;
            protobufLength += ProtobufCountUtil.countBytes(iotSimpleEntity);
            jsonLength += gson.toJson(iotSimpleEntity).getBytes().length;
            xmlLength += XmlUtil.convertToXml(iotSimpleEntity).getBytes().length;
        }
        System.out.println("|myLength1\t|" + myLength1 + "|\t");
        System.out.println("|myLength2\t|" + myLength2 + "|\t");
        System.out.println("|protobufLength\t| " + protobufLength + "|\t");
        System.out.println("|jsonLength:\t|" + jsonLength + "|\t");
        System.out.println("|xmlLength:\t|" + xmlLength + "|\t");
    }
}

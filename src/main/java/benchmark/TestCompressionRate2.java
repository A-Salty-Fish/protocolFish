package benchmark;

import com.google.gson.Gson;
import demo.IotSimpleEntity;
import demo.TestEntity;
import org.junit.Before;
import org.junit.Test;
import util.*;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/26 15:20
 */

public class TestCompressionRate2 {

    ProtocolConfig protocolConfig;

    CodecUtil codecUtil;

    @Before
    public void before() {
        CodecUtil.registerClass(IotSimpleEntity.class);
        protocolConfig = ProtocolConfig.defaultConfig();
    }

    @Test
    public void testClone() throws Exception {
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));
        TestEntity testEntity2 = CompressionRateTestUtil.clone(testEntity);
        System.out.println(new Gson().toJson(testEntity2));
    }

    @Test
    public void testRandom() throws Exception {
        TestEntity testEntity = CompressionRateTestUtil.random(TestEntity.class);
        System.out.println(new Gson().toJson(testEntity));
    }

    @Test
    public void testNear() throws Exception {
        TestEntity testEntity = CompressionRateTestUtil.random(TestEntity.class);
        System.out.println(new Gson().toJson(testEntity));
        System.out.println(new Gson().toJson(CompressionRateTestUtil.near(testEntity, 10, 10, 1.0)));
    }

    @Test
    public void testCompressionRateWithStringLength() throws Exception {
        IotSimpleEntity baseTestEntity = CompressionRateTestUtil.random(IotSimpleEntity.class, 128);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(baseTestEntity);
        codecUtil = new CodecUtil(protocolConfig);
        System.out.println("string length\t" + "my length\t" + "protobuf length\t" + "json length\t" + "xmlLength\t");
        for (int i = 128; i >= 0; i--) {
            long myLength = 0;
            long protobufLength = 0;
            long jsonLength = 0;
            long xmlLength = 0;
            for (int j = 0; j < 1000; j++) {
                IotSimpleEntity testEntity = CompressionRateTestUtil.near(baseTestEntity, 1, 1, 1.0);
                testEntity.stringValue1 = testEntity.stringValue1.substring(0, i);
                myLength += codecUtil.encode3(testEntity).length;
                protobufLength += ProtobufCountUtil.countBytes(testEntity);
                jsonLength += new Gson().toJson(testEntity).getBytes().length;
                xmlLength += XmlUtil.convertToXml(testEntity).getBytes().length;
            }
            System.out.println("" + i + "\t" + myLength + "\t" + protobufLength + "\t" + jsonLength + "\t" + xmlLength);
        }
    }
}

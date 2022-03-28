package benchmark;

import com.google.gson.Gson;
import demo.HomeIotEntity;
import demo.IotSimpleEntity;
import demo.TestEntity;
import org.junit.Before;
import org.junit.Test;
import util.*;

import java.util.List;

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

    @Test
    public void testCompressionRateWithHomeIotData() throws Exception {
        CodecUtil.registerClass(HomeIotEntity.class);
        List<HomeIotEntity> homeIotEntities = CsvUtil.convertCsvFileToObjects(HomeIotEntity.class, "./data/HomeC.csv");
        HomeIotEntity baseTestEntity = homeIotEntities.get(0);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(baseTestEntity);
        codecUtil = new CodecUtil(protocolConfig);

        long myLength1 = 0;
        long myLength2 = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        System.out.println("step\tmy1\tmy2\tprotobuf\tjson\t");
        for (HomeIotEntity homeIotEntity : homeIotEntities) {
            protobufLength += ProtobufCountUtil.countBytes(homeIotEntity);
            jsonLength += new Gson().toJson(homeIotEntity).getBytes().length;
        }
        for (int i = 0; i < homeIotEntities.size(); i++) {
            HomeIotEntity homeIotEntity = homeIotEntities.get(i);
            myLength1 += codecUtil.encode(homeIotEntity).length;
            myLength2 += codecUtil.encode3(homeIotEntity).length;
            protocolConfig.setBaseLine(homeIotEntity);
        }
        System.out.println("|" + 1 + "\t|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
        for (int step = 5; step <= 45; step += 5) {
            myLength1 = 0;
            myLength2 = 0;
            protocolConfig.setBaseLine(baseTestEntity);
            for (int i = 0; i < homeIotEntities.size(); i++) {
                HomeIotEntity homeIotEntity = homeIotEntities.get(i);
                myLength1 += codecUtil.encode(homeIotEntity).length;
                myLength2 += codecUtil.encode3(homeIotEntity).length;
                if (i % step == 0) {
                    protocolConfig.setBaseLine(homeIotEntity);
                }
            }
            System.out.println("|" + step + "\t|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
        }
        for (int step = 50; step <= 1000; step += 50) {
            myLength1 = 0;
            myLength2 = 0;
            protocolConfig.setBaseLine(baseTestEntity);
            for (int i = 0; i < homeIotEntities.size(); i++) {
                HomeIotEntity homeIotEntity = homeIotEntities.get(i);
                myLength1 += codecUtil.encode(homeIotEntity).length;
                myLength2 += codecUtil.encode3(homeIotEntity).length;
                if (i % step == 0) {
                    protocolConfig.setBaseLine(homeIotEntity);
                }
            }
            System.out.println("|" + step + "\t|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
        }
    }

    @Test
    public void testCompressionRateWithHomeIotDataWithDoubleCompression() throws Exception {
        CodecUtil.registerClass(HomeIotEntity.class);
        List<HomeIotEntity> homeIotEntities = CsvUtil.convertCsvFileToObjects(HomeIotEntity.class, "./data/HomeC.csv");
        HomeIotEntity baseTestEntity = homeIotEntities.get(0);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(baseTestEntity);
        protocolConfig.setDoubleCompressionAccuracy(4);
        protocolConfig.setEnableDoubleCompression(true);
        codecUtil = new CodecUtil(protocolConfig);

        long myLength1 = 0;
        long myLength2 = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        System.out.println("step\tmy1\tmy2\tprotobuf\tjson\t");
        for (HomeIotEntity homeIotEntity : homeIotEntities) {
            protobufLength += ProtobufCountUtil.countBytes(homeIotEntity);
            jsonLength += new Gson().toJson(homeIotEntity).getBytes().length;
        }
        for (int i = 0; i < homeIotEntities.size(); i++) {
            HomeIotEntity homeIotEntity = homeIotEntities.get(i);
            myLength1 += codecUtil.encode(homeIotEntity).length;
            myLength2 += codecUtil.encode3(homeIotEntity).length;
            protocolConfig.setBaseLine(homeIotEntity);
        }
        System.out.println("|" + 1 + "\t|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
        for (int step = 5; step <= 45; step += 5) {
            myLength1 = 0;
            myLength2 = 0;
            protocolConfig.setBaseLine(baseTestEntity);
            for (int i = 0; i < homeIotEntities.size(); i++) {
                HomeIotEntity homeIotEntity = homeIotEntities.get(i);
                myLength1 += codecUtil.encode(homeIotEntity).length;
                myLength2 += codecUtil.encode3(homeIotEntity).length;
                if (i % step == 0) {
                    protocolConfig.setBaseLine(homeIotEntity);
                }
            }
            System.out.println("|" + step + "\t|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
        }
        for (int step = 50; step <= 1000; step += 50) {
            myLength1 = 0;
            myLength2 = 0;
            protocolConfig.setBaseLine(baseTestEntity);
            for (int i = 0; i < homeIotEntities.size(); i++) {
                HomeIotEntity homeIotEntity = homeIotEntities.get(i);
                myLength1 += codecUtil.encode(homeIotEntity).length;
                myLength2 += codecUtil.encode3(homeIotEntity).length;
                if (i % step == 0) {
                    protocolConfig.setBaseLine(homeIotEntity);
                }
            }
            System.out.println("|" + step + "\t|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
        }
    }

    @Test
    public void testCompressionRateWithDefaultConfig() throws Exception {
        CodecUtil.registerClass(HomeIotEntity.class);
        List<HomeIotEntity> homeIotEntities = CsvUtil.convertCsvFileToObjects(HomeIotEntity.class, "./data/HomeC.csv");
        codecUtil = new CodecUtil(protocolConfig);

        long myLength1 = 0;
        long myLength2 = 0;
        long protobufLength = 0;
        long jsonLength = 0;
        System.out.println("step\tmy1\tmy2\tprotobuf\tjson\t");
        for (HomeIotEntity homeIotEntity : homeIotEntities) {
            protobufLength += ProtobufCountUtil.countBytes(homeIotEntity);
            jsonLength += new Gson().toJson(homeIotEntity).getBytes().length;
            myLength1 += codecUtil.encode(homeIotEntity).length;
            myLength2 += codecUtil.encode2(homeIotEntity).length;
        }
        System.out.println("|" + myLength1 + "\t|" + myLength2 + "\t|" + protobufLength + "\t|" + jsonLength);
    }
}

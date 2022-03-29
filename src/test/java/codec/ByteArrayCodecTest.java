package codec;

import com.google.gson.Gson;
import demo.TestEntity;
import org.junit.Before;
import org.junit.Test;
import util.ProtocolConfig;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 15:30
 */

public class ByteArrayCodecTest {

    @Before
    public void setUp() throws Exception {
        ByteArrayCodec.registerClass(TestEntity.class);
    }

    @Test
    public void testEncode() throws Exception {
        ByteArrayCodec byteArrayCodec = new ByteArrayCodec();
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        byte[] bytes = byteArrayCodec.encode(testEntity);
        System.out.println(bytes.length);
    }

    @Test
    public void testDecode() throws Exception {
        ByteArrayCodec byteArrayCodec = new ByteArrayCodec();
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));
        byte[] bytes = byteArrayCodec.encode(testEntity);
        System.out.println(bytes.length);
        TestEntity testEntity1 =  byteArrayCodec.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity1));
    }

    @Test
    public void testDecodeWithDoubleCompression() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(4);
        TestEntity testEntity = TestEntity.getRandomTestEntity(1024.0);
        System.out.println(new Gson().toJson(testEntity));
        ByteArrayCodec codec = new ByteArrayCodec(protocolConfig);
        byte[] bytes = codec.encode(testEntity);
        TestEntity testEntity2 = codec.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
    }

    @Test
    public void testDecodeWithBaseLineCompression() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        TestEntity testEntity = TestEntity.getRandomTestEntity(1024.0);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(testEntity);
        TestEntity testEntity1 = TestEntity.getNextNearRandomTestEntity(testEntity);
        System.out.println(new Gson().toJson(testEntity1));
        ByteArrayCodec codec = new ByteArrayCodec(protocolConfig);
        byte[] bytes = codec.encode(testEntity1);
        TestEntity testEntity2 = codec.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
    }

    @Test
    public void testDecodeWithBaseLineAndDoubleCompression() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(4);
        TestEntity testEntity = TestEntity.getRandomTestEntity(1024.0);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(testEntity);
        TestEntity testEntity1 = TestEntity.getNextNearRandomTestEntity(testEntity);
        System.out.println(new Gson().toJson(testEntity1));
        ByteArrayCodec codec = new ByteArrayCodec(protocolConfig);
        byte[] bytes = codec.encode(testEntity1);
        TestEntity testEntity2 = codec.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
    }
}

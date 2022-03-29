package codec;

import com.google.gson.Gson;
import demo.TestEntity;
import org.junit.Before;
import org.junit.Test;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 15:39
 */

public class ByteStreamCodecTest {

    @Before
    public void setUp() throws Exception {
        ByteStreamCodec.registerClass(TestEntity.class);
    }

    @Test
    public void testEncode() throws Exception {
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        ByteStreamCodec byteStreamCodec = new ByteStreamCodec();
        byte[] bytes = byteStreamCodec.encode(testEntity);
        System.out.println(bytes.length);
    }

    @Test
    public void testDecode() throws Exception {
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));
        ByteStreamCodec byteStreamCodec = new ByteStreamCodec();
        byte[] bytes = byteStreamCodec.encode(testEntity);
        TestEntity testEntity1 = byteStreamCodec.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity1));
    }
}

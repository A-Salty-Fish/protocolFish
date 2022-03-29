package codec;

import com.google.gson.Gson;
import demo.TestEntity;
import org.junit.Before;
import org.junit.Test;

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
}

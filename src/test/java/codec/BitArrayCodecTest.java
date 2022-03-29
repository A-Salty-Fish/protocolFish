package codec;

import com.google.gson.Gson;
import demo.TestEntity;
import org.junit.Before;
import org.junit.Test;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 15:13
 */

public class BitArrayCodecTest {

    @Before
    public void setUp() throws Exception {
        BitArrayCodec.registerClass(TestEntity.class);
    }

    @Test
    public void testEncode() throws Exception {
        BitArrayCodec codec = new BitArrayCodec();
        byte[] bytes = codec.encode(TestEntity.getRandomTestEntity());
        System.out.println(bytes.length);
    }

    @Test
    public void testDecode() throws Exception {
        BitArrayCodec codec = new BitArrayCodec();
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));
        byte[] bytes = codec.encode(testEntity);
        TestEntity testEntity2 = codec.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
    }
}

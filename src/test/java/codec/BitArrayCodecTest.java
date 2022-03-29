package codec;

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
}

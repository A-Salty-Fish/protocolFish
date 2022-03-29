package codec;

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
}

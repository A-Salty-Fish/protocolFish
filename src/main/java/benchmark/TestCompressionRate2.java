package benchmark;

import com.google.gson.Gson;
import demo.TestEntity;
import org.junit.Test;
import util.CompressionRateTestUtil;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/26 15:20
 */

public class TestCompressionRate2 {

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
        System.out.println(new Gson().toJson(CompressionRateTestUtil.near(testEntity, 10, 10, 1.0, 2)));
    }

}

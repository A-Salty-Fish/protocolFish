package util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 14:51
 */

public class FloatDoubleCompressionTest {

    @Test
    public void testDouble() {
        byte[] bytes = FloatDoubleCompressionUtil.convertDoubleToBytes(1.5);
        System.out.println(bytes.length);
        Assert.assertEquals(FloatDoubleCompressionUtil.convertBytesToDouble(bytes), 1.5, 0.00001);
    }

    @Test
    public void testFloat() {
        byte[] bytes = FloatDoubleCompressionUtil.convertFloatToBytes(1.5f);
        System.out.println(bytes.length);
        System.out.println(FloatDoubleCompressionUtil.convertBytesToFloat(bytes));
        Assert.assertEquals(FloatDoubleCompressionUtil.convertBytesToFloat(bytes), 1.5f, 0.00001);
    }

}

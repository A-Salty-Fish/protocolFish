package prediction;

import org.junit.Test;
import util.ByteUtil;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/30 16:03
 */

public class MovingWindowPredictionTest {

    @Test
    public void testPrediction() throws Exception {
        MovingWindowPrediction prediction = new MovingWindowPrediction(100, 4);
        System.out.println(ByteUtil.bytesToString(ByteUtil.convertIntegerToBytes(111111111)));
        for (int i = 0; i < 1000; i++) {
            prediction.addHistoryData(ByteUtil.convertIntegerToBytes(111111111));
        }
        System.out.println(ByteUtil.bytesToString(prediction.getPredictionData()));
    }

}

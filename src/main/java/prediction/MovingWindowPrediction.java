package prediction;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/30 15:02
 */

public class MovingWindowPrediction implements Prediction {

    int windowSize;

    int byteLength;

    Queue<byte[]> window;

    public MovingWindowPrediction(int windowSize, int byteLength) {
        this.windowSize = windowSize;
        this.byteLength = byteLength;
        this.window = new LinkedList<>();
    }

    @Override
    public void addHistoryData(byte[] historyData) {
        if (window.size() == windowSize) {
            window.poll();
        }
        window.offer(historyData);
    }

    @Override
    public byte[] getPredictionData() {
        if (window.size() < 3) {
            return window.peek();
        }
        byte[] result = new byte[byteLength];
        for (int i = 0; i < byteLength - 1; i++) {
            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;
            int sum = 0;
            for (byte[] data : window) {
                sum += data[i];
                max = Math.max(max, data[i]);
                min = Math.min(min, data[i]);
            }
            sum -= max + min;
            result[i] = (byte) (sum / (window.size() - 2));
        }
        return result;
    }
}

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
        return new byte[0];
    }
}

package prediction;

public interface Prediction {

    public void addHistoryData(byte[] historyData);

    public byte[] getPredictionData();
}

package codec;

public interface Codec {

    public byte[] encode(Object obj) throws Exception;

    public <T> T decode(byte[] bytes, Class<T> clazz) throws Exception;

}

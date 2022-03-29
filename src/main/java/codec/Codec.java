package codec;

public interface Codec {

    public byte[] encode(Object obj) throws Exception;

    public Object decode(byte[] bytes) throws Exception;

}

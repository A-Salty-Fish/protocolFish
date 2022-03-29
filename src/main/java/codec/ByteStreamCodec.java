package codec;

import handler.header.PlainBodyHeader;
import lombok.extern.slf4j.Slf4j;
import util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static util.CodecUtil.FieldType.CONSTANT_LENGTH;
import static util.CodecUtil.FieldType.NULLABLE_CONSTANT_LENGTH;
import static util.CodecUtil.getZigZag;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 15:35
 */
@Slf4j
public class ByteStreamCodec implements Codec{

    String clientKey;

    ProtocolConfig protocolConfig;

    private static final ConcurrentHashMap<Class<?>, List<Field>> constantLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, List<Field>> variableLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Integer> classToIdentity = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, Class<?>> identityToClass = new ConcurrentHashMap<>();

    public ByteStreamCodec() {
        this.clientKey = null;
        this.protocolConfig = ProtocolConfig.defaultConfig();
    }

    /**
     * for server pipeline
     *
     * @param clientKey
     */
    public ByteStreamCodec(String clientKey) {
        this.clientKey = clientKey;
        protocolConfig = ServerStoreConfig.get(clientKey);
        if (protocolConfig == null) {
            protocolConfig = ProtocolConfig.defaultConfig();
        }
    }

    /**
     * for client
     *
     * @param protocolConfig
     */
    public ByteStreamCodec(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        this.clientKey = null;
    }

    public static void registerClass(Class<?> clazz) {
        int classIdentity = clazz.getName().hashCode() & ByteUtil.getMask(PlainBodyHeader.PlainBodyLabelPosition.CLASS_IDENTITY.value());
        classToIdentity.put(clazz, classIdentity);
        identityToClass.put(classIdentity, clazz);
        constantLengthFieldMap.put(clazz, new ArrayList<>(16));
        variableLengthFieldMap.put(clazz, new ArrayList<>(16));
        for (Field filed : clazz.getDeclaredFields()) {
            CodecUtil.FieldType fieldType = CodecUtil.getFieldType(filed);
            if (fieldType == CONSTANT_LENGTH || fieldType == NULLABLE_CONSTANT_LENGTH) {
                constantLengthFieldMap.get(clazz).add(filed);
            } else {
                variableLengthFieldMap.get(clazz).add(filed);
            }
        }
        log.info("register enity class: {}", clazz.getName());
        constantLengthFieldMap.get(clazz).sort(Comparator.comparing(Field::getName));
        variableLengthFieldMap.get(clazz).sort(Comparator.comparing(Field::getName));
    }

    @Override
    public byte[] encode(Object obj) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(obj.getClass());
        List<Field> variableLengthFields = variableLengthFieldMap.get(obj.getClass());
        ByteArrayOutputStream out = new ByteArrayOutputStream(constantLengthFields.size() * 4 + variableLengthFields.size() * 32);
        if (protocolConfig.getEnableBaseLineCompression()) {
            for (Field field : constantLengthFields) {
                field.setAccessible(true);
                encodeWithBaseLine(out, field.get(obj), field.get(protocolConfig.getBaseLine()));
            }
            for (Field field : variableLengthFields) {
                field.setAccessible(true);
                encodeWithBaseLine(out, field.get(obj), field.get(protocolConfig.getBaseLine()));
            }
        } else {
            for (Field field : constantLengthFields) {
                field.setAccessible(true);
                encodeWithOutBaseLine(out, field.get(obj));
            }
            for (Field field : variableLengthFields) {
                field.setAccessible(true);
                encodeWithOutBaseLine(out, field.get(obj));
            }
        }
        return out.toByteArray();
    }

    public ByteArrayOutputStream encodeWithBaseLine(ByteArrayOutputStream out, Object obj, Object baseLineObj) throws Exception {
        Class<?> clazz = obj.getClass();
        if (clazz == Integer.class) {
            return encodeInt(out, (Integer) obj ^ (Integer) baseLineObj);
        } else if (clazz == Long.class) {
            return encodeLong(out, (Long) obj ^ (Long) baseLineObj);
        } else if (clazz == Double.class) {
            if (protocolConfig.getEnableDoubleCompression()) {
                long doubleL = compressDoubleToLong((Double) obj);
                long baseLineDoubleL = compressDoubleToLong((Double) baseLineObj);
                return encodeLong(out, doubleL ^ baseLineDoubleL);
            }
            long doubleL = Double.doubleToLongBits((Double) obj);
            long baseLineDoubleL = Double.doubleToLongBits((Double) baseLineObj);
            return encodeLong(out, doubleL ^ baseLineDoubleL);
        } else if (clazz == String.class) {
            return encodeString(out, (String) obj);
        } else {
            throw new Exception("type not support");
        }
    }

    public ByteArrayOutputStream encodeWithOutBaseLine(ByteArrayOutputStream out, Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        if (clazz == Integer.class) {
            return encodeInt(out, (Integer) obj);
        } else if (clazz == Long.class) {
            return encodeLong(out, (Long) obj);
        } else if (clazz == Double.class) {
            if (protocolConfig.getEnableDoubleCompression()) {
                long doubleL = compressDoubleToLong((Double) obj);
                return encodeLong(out, doubleL);
            }
            long doubleL = Double.doubleToLongBits((Double) obj);
            return encodeLong(out, doubleL);
        } else if (clazz == String.class) {
            return encodeString(out, (String) obj);
        } else {
            throw new Exception("type not support");
        }
    }

    public ByteArrayOutputStream encodeLong(ByteArrayOutputStream out, long n) throws Exception {
        n = getZigZag(n);
        while ((n & (~0x7f)) != 0) {
            out.write((byte) ((n & 0x7F) | 0x80));
            n >>>= 7;
        }
        out.write((byte) n);
        return out;
    }

    public ByteArrayOutputStream encodeInt(ByteArrayOutputStream out, int n) throws Exception {
        n = getZigZag(n);
        while ((n & ~0x7F) != 0) {
            out.write((byte) ((n & 0x7F) | 0x80));
            n >>>= 7;
        }
        out.write((byte) n);
        return out;
    }

    public ByteArrayOutputStream encodeString(ByteArrayOutputStream out, String str) throws Exception {
        byte[] strBytes = str.getBytes(protocolConfig.getCharset());
        int length = strBytes.length;
        encodeInt(out, length);
        out.write(strBytes);
        return out;
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(clazz);
        List<Field> variableLengthFields = variableLengthFieldMap.get(clazz);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        T obj = clazz.newInstance();
        if (protocolConfig.getEnableBaseLineCompression()) {
            for (Field field : constantLengthFields) {
                field.setAccessible(true);
                decodeWithBaseLine(in, field, obj, field.get(protocolConfig.getBaseLine()));
            }
            for (Field field : variableLengthFields) {
                field.setAccessible(true);
                decodeWithBaseLine(in, field, obj, field.get(protocolConfig.getBaseLine()));
            }
        } else {
            for (Field field : constantLengthFields) {
                field.setAccessible(true);
                decodeWithoutBaseLine(in, field, obj);
            }
            for (Field field : variableLengthFields) {
                field.setAccessible(true);
                decodeWithoutBaseLine(in, field, obj);
            }
        }
        return obj;
    }

    public ByteArrayInputStream decodeWithBaseLine(ByteArrayInputStream in, Field field, Object obj, Object baseLineObj) throws Exception {
        Class<?> fieldType = field.getType();
        if (fieldType == Integer.class || fieldType == int.class) {
            field.set(obj, decodeInt(in) ^ (Integer) baseLineObj);
        } else if (fieldType == Long.class || fieldType == long.class) {
            field.set(obj, decodeLong(in) ^ (Long) baseLineObj);
        } else if (fieldType == Double.class || fieldType == double.class) {
            long doubleL = decodeLong(in);
            double baseLineDouble = (Double) baseLineObj;
            if (protocolConfig.getEnableDoubleCompression()) {
                field.set(obj, deCompressDoubleFromLong(doubleL ^ compressDoubleToLong(baseLineDouble)));
            } else {
                field.set(obj, Double.longBitsToDouble(doubleL ^ Double.doubleToLongBits(baseLineDouble)));
            }
        } else if (fieldType == String.class) {
            field.set(obj, decodeString(in));
        } else {
            throw new Exception("type not support");
        }
        return in;
    }

    public ByteArrayInputStream decodeWithoutBaseLine(ByteArrayInputStream in, Field field, Object obj) throws Exception {
        Class<?> fieldType = field.getType();
        if (fieldType == Integer.class || fieldType == int.class) {
            field.set(obj, decodeInt(in));
        } else if (fieldType == Long.class || fieldType == long.class) {
            field.set(obj, decodeLong(in));
        } else if (fieldType == Double.class || fieldType == double.class) {
            long doubleL = decodeLong(in);
            if (protocolConfig.getEnableDoubleCompression()) {
                field.set(obj, deCompressDoubleFromLong(doubleL));
            } else {
                field.set(obj, Double.longBitsToDouble(doubleL));
            }
        } else if (fieldType == String.class) {
            field.set(obj, decodeString(in));
        } else {
            throw new Exception("type not support");
        }
        return in;
    }

    public String decodeString(ByteArrayInputStream in) throws Exception {
        int length = decodeInt(in);
        byte[] strBytes = new byte[length];
        in.read(strBytes);
        return new String(strBytes, protocolConfig.getCharset());
    }

    public int decodeInt(ByteArrayInputStream in) throws Exception {
        int b = in.read() & 0xff;
        int n = b & 0x7f;
        int len = 1;
        while (b > 0x7f) {
            b = in.read() & 0xff;
            n ^= (b & 0x7f) << (7 * len);
            len++;
        }
        return (n >>> 1) ^ -(n & 1); // back to two's-complement
    }

    public long decodeLong(ByteArrayInputStream in) throws Exception {
        long b = in.read() & 0xff;
        long n = b & 0x7f;
        int len = 1;
        while (b > 0x7f) {
            b = in.read() & 0xff;
            n ^= (b & 0x7f) << (7 * len);
            len++;
        }
        return (n >>> 1) ^ -(n & 1); // back to two's-complement
    }

    public long compressDoubleToLong(double value) {
        return FloatDoubleCompressionUtil.compressDoubleToLong(value, protocolConfig.getDoubleCompressionAccuracy());
    }

    public double deCompressDoubleFromLong(long l) {
        return FloatDoubleCompressionUtil.deCompressDoubleFromLong(l, protocolConfig.getDoubleCompressionAccuracy());
    }
}

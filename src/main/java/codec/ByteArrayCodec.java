package codec;

import handler.header.PlainBodyHeader;
import lombok.extern.slf4j.Slf4j;
import util.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static util.CodecUtil.*;
import static util.CodecUtil.FieldType.CONSTANT_LENGTH;
import static util.CodecUtil.FieldType.NULLABLE_CONSTANT_LENGTH;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 15:24
 */
@Slf4j
public class ByteArrayCodec implements Codec{
    String clientKey;

    ProtocolConfig protocolConfig;

    private static final ConcurrentHashMap<Class<?>, List<Field>> constantLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, List<Field>> variableLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Integer> classToIdentity = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, Class<?>> identityToClass = new ConcurrentHashMap<>();

    public ByteArrayCodec() {
        this.clientKey = null;
        this.protocolConfig = ProtocolConfig.defaultConfig();
    }

    /**
     * for server pipeline
     *
     * @param clientKey
     */
    public ByteArrayCodec(String clientKey) {
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
    public ByteArrayCodec(ProtocolConfig protocolConfig) {
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
        List<Byte> bytes = new ArrayList<>(constantLengthFields.size() * 4 + variableLengthFields.size() * 128);
        for (Field field : constantLengthFields) {
            field.setAccessible(true);
            Byte[] fieldBytes;
            if (protocolConfig.getEnableBaseLineCompression()) {
                fieldBytes = encodeWithBaseLine(obj, field, protocolConfig.getBaseLine());
            } else {
                fieldBytes = encodeWithoutBaseLine(obj, field);
            }
            for (Byte b : fieldBytes) {
                if (b != null) {
                    bytes.add(b);
                }
            }
        }
        for (Field field : variableLengthFields) {
            field.setAccessible(true);
            Byte[] fieldBytes = encodeWithoutBaseLine(obj, field);
            for (Byte b : fieldBytes) {
                if (b != null) {
                    bytes.add(b);
                }
            }
        }
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return bytesArray;
    }

    public Byte[] encodeWithBaseLine(Object obj, Field field, Object baseLineObj) throws Exception {
        Class<?> fieldType = field.getType();
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            int value = (Integer) field.get(obj);
            int baseLineValue = (Integer) field.get(baseLineObj);
            return encodeInt(value ^ baseLineValue);
        } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            long value = (Long) field.get(obj);
            long baseLineValue = (Long) field.get(baseLineObj);
            return encodeLong(value ^ baseLineValue);
        } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            Double value = (Double) field.get(obj);
            Double baseLineValue = (Double) field.get(baseLineObj);
            if (protocolConfig.getEnableDoubleCompression()) {
                long lValue = compressDoubleToLong(value);
                long lBaseLineValue = compressDoubleToLong(baseLineValue);
                return encodeLong(lValue ^ lBaseLineValue);
            }
            long lValue = Double.doubleToLongBits(value);
            long lBaseLineValue = Double.doubleToLongBits(baseLineValue);
            return encodeLong(lValue ^ lBaseLineValue);
        } else {
            throw new Exception("Unsupported field type: " + fieldType.getName());
        }
    }

    public Byte[] encodeWithoutBaseLine(Object obj, Field field) throws Exception {
        Class<?> fieldType = field.getType();
//        List<Byte> bytes;
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            return encodeInt((Integer) field.get(obj));
        } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            return encodeLong((Long) field.get(obj));
        } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            Double doubleValue = (Double) field.get(obj);
            if (protocolConfig.getEnableDoubleCompression()) {
                long value = compressDoubleToLong(doubleValue);
                return encodeLong(value);
            }
            long value = Double.doubleToLongBits(doubleValue);
            return encodeLong(value);
        } else if (fieldType.equals(String.class)) {
            return encodeString((String) field.get(obj));
        } else {
            throw new Exception("type not support");
        }
    }

    public Byte[] encodeString(String str) throws Exception {
        byte[] stringBytes = str.getBytes(protocolConfig.getCharset());
        int length = stringBytes.length;
        Byte[] lengthBytes = encodeInt(length);
        Byte[] bytes = new Byte[lengthBytes.length + stringBytes.length];
        System.arraycopy(lengthBytes, 0, bytes, 0, lengthBytes.length);
        for (int i = 0; i < stringBytes.length; i++) {
            bytes[i + lengthBytes.length] = stringBytes[i];
        }
        return bytes;
    }

    public long compressDoubleToLong(double value) {
        return FloatDoubleCompressionUtil.compressDoubleToLong(value, protocolConfig.getDoubleCompressionAccuracy());
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(clazz);
        List<Field> variableLengthFields = variableLengthFieldMap.get(clazz);
        int offset = 0;
        T result = clazz.newInstance();
        for (Field field : constantLengthFields) {
            offset += decodeConstantBytes(bytes, offset, field, result);
        }
        for (Field field : variableLengthFields) {
            byte[] lengthBytes = getConstantBytes(bytes, offset);
            offset += lengthBytes.length;
            int length = decodeInt(lengthBytes);
            byte[] valueBytes = new byte[length];
            System.arraycopy(bytes, offset, valueBytes, 0, length);
            offset += length;
            field.set(result, new String(valueBytes, protocolConfig.getCharset()));
        }
        return result;
    }

    public int decodeConstantBytes(byte[] bytes, int offset, Field field, Object obj) throws Exception {
        Class<?> fieldType = field.getType();
        byte[] fieldBytes = getConstantBytes(bytes, offset);
        if (protocolConfig.getEnableBaseLineCompression()) {
            field.set(obj, decodeConstantFieldBytes(fieldBytes, fieldType, field.get(protocolConfig.getBaseLine())));
        } else {
            field.set(obj, decodeConstantFieldBytes(fieldBytes, fieldType));
        }
        return fieldBytes.length;
    }

    public byte[] getConstantBytes(byte[] bytes, int offset) throws Exception {
        List<Byte> constantBytes = new ArrayList<>(10);
        while ((bytes[offset] & (1 << 7)) != 0) {
            constantBytes.add(bytes[offset++]);
        }
        constantBytes.add(bytes[offset]);
        byte[] result = new byte[constantBytes.size()];
        for (int i = 0; i < constantBytes.size(); i++) {
            result[i] = constantBytes.get(i);
        }
        return result;
    }

    public Object decodeConstantFieldBytes(byte[] bytes, Class<?> clazz, Object baseLineObj) throws Exception {
        if (clazz == int.class || clazz == Integer.class) {
            return decodeInt(bytes) ^ (Integer) baseLineObj;
        } else if (clazz == long.class || clazz == Long.class) {
            return decodeLong(bytes) ^ (Long) baseLineObj;
        } else if (clazz == double.class || clazz == Double.class) {
            double baseLineDouble = (Double) baseLineObj;
            long l = decodeLong(bytes);
            if (protocolConfig.getEnableDoubleCompression()) {
                return deCompressDoubleFromLong(l ^ compressDoubleToLong(baseLineDouble));
            }
            long baseLinel = Double.doubleToLongBits((Double) baseLineObj);
            return Double.longBitsToDouble(l ^ baseLinel);
        } else {
            throw new Exception("type not support");
        }
    }

    public Object decodeConstantFieldBytes(byte[] bytes, Class<?> clazz) throws Exception {
        if (clazz == int.class || clazz == Integer.class) {
            return decodeInt(bytes);
        } else if (clazz == long.class || clazz == Long.class) {
            return decodeLong(bytes);
        } else if (clazz == double.class || clazz == Double.class) {
            long l = decodeLong(bytes);
            if (protocolConfig.getEnableDoubleCompression()) {
                return deCompressDoubleFromLong(l);
            }
            return Double.longBitsToDouble(l);
        } else {
            throw new Exception("type not support");
        }
    }

    public double deCompressDoubleFromLong(long l) {
        return FloatDoubleCompressionUtil.deCompressDoubleFromLong(l, protocolConfig.getDoubleCompressionAccuracy());
    }
}

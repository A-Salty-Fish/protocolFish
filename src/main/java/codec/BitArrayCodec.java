package codec;

import handler.header.PlainBodyHeader;
import lombok.extern.slf4j.Slf4j;
import util.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static util.CodecUtil.FieldType.CONSTANT_LENGTH;
import static util.CodecUtil.FieldType.NULLABLE_CONSTANT_LENGTH;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 15:05
 */
@Slf4j
public class BitArrayCodec implements Codec {

    String clientKey;

    ProtocolConfig protocolConfig;

    private static final ConcurrentHashMap<Class<?>, List<Field>> constantLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, List<Field>> variableLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Integer> classToIdentity = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, Class<?>> identityToClass = new ConcurrentHashMap<>();

    public static enum FieldType {
        CONSTANT_LENGTH,
        VARIABLE_LENGTH,
        NULLABLE_CONSTANT_LENGTH,
        NULLABLE_VARIABLE_LENGTH
    }

    public BitArrayCodec() {
        this.clientKey = null;
        this.protocolConfig = ProtocolConfig.defaultConfig();
    }

    /**
     * for server pipeline
     *
     * @param clientKey
     */
    public BitArrayCodec(String clientKey) {
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
    public BitArrayCodec(ProtocolConfig protocolConfig) {
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
    public Object decode(byte[] bytes) throws Exception {
        return null;
    }

    @Override
    public byte[] encode(Object obj) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(obj.getClass());
        List<Field> variableLengthFields = variableLengthFieldMap.get(obj.getClass());
        List<Byte> bytes = new ArrayList<>(constantLengthFields.size() * 4 + variableLengthFields.size() * 4);
        int offset = 0;
        for (Field field : constantLengthFields) {
            field.setAccessible(true);
            offset += encodeConstantLengthField(obj, field, bytes, offset);
        }
        for (Field field : variableLengthFields) {
            field.setAccessible(true);
            offset += encodeVariableLengthField(obj, field, bytes, offset);
        }
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return bytesArray;
    }

    public int encodeConstantLengthField(Object obj, Field field, List<Byte> bytes, int offset) throws IllegalAccessException {
        byte[] addBytes = getBytes(obj, field);
        int constantValueBytesNum = getConstantValueBytesNum(addBytes);
        int headLength = getConstantHeadLength(field);
        return appendConstantBytes(addBytes, offset, bytes, constantValueBytesNum, headLength);
    }

    public int appendConstantBytes(byte[] addBytes, int offset, List<Byte> bytes, int constantValueBytesNum, int headLength) {
        int index = offset / 8;
        int bitOffset = offset % 8;
        if (headLength > 0) {
            int writeConstantValueBytesNum = constantValueBytesNum - 1;
            if (bitOffset == 0) {
                bytes.add((byte) (writeConstantValueBytesNum << (8 - headLength)));
            } else {
                Byte curByte = bytes.get(index);
                if (bitOffset + headLength > 8) {
                    bytes.add((byte) 0);
                    Byte nextByte = bytes.get(index + 1);
                    bytes.set(index, (byte) (curByte | (writeConstantValueBytesNum >> (bitOffset + headLength - 8))));
                    bytes.set(index + 1, (byte) (nextByte | (writeConstantValueBytesNum << (16 - (bitOffset + headLength)))));
                    index++;
                } else {
                    bytes.set(index, (byte) (curByte | (writeConstantValueBytesNum << (8 - bitOffset - headLength))));
                    if (bitOffset + headLength == 8) {
                        index++;
                    }
                }
            }
            bitOffset += headLength;
            bitOffset %= 8;
        }
        for (int i = addBytes.length - constantValueBytesNum; i < addBytes.length; i++) {
            byte addByte = addBytes[i];
            bytes.add((byte) 0);
            Byte curByte = bytes.get(index);
            curByte = (byte) (curByte | (((addByte) >> bitOffset) & ByteUtil.getMask(8 - bitOffset)));
            bytes.set(index, curByte);
            index++;
            if (bitOffset != 0) {
                Byte nextByte = bytes.get(index);
                nextByte = (byte) (nextByte | (addByte << (8 - bitOffset)));
                bytes.set(index, nextByte);
            }
        }
        return 8 * constantValueBytesNum + headLength;
    }

    public int getConstantHeadLength(Field field) {
        int length = getConstantLengthFieldByteLength(field);
        switch (length) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 8:
                return 3;
            default:
                return -1;
        }
    }

    public int getConstantLengthFieldByteLength(Field field) {
        Class<?> fieldType = field.getType();
        return getConstantClassLength(fieldType);
    }

    public int getConstantClassLength(Class<?> clazz) {
        if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
            return 1;
        }
        if (clazz.equals(short.class) || clazz.equals(Short.class) || clazz.equals(char.class) || clazz.equals(Character.class)) {
            return 2;
        }
        if (clazz.equals(int.class) || clazz.equals(Integer.class) || clazz.equals(float.class) || clazz.equals(Float.class)) {
            return 4;
        }
        if (clazz.equals(long.class) || clazz.equals(Long.class) || clazz.equals(double.class) || clazz.equals(Double.class)) {
            return 8;
        }
        if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
            return 1;
        }
        if (clazz.equals(LocalDateTime.class) || clazz.equals(LocalDate.class)) {
            return 8;
        }
        return -1;
    }

    public int getConstantValueBytesNum(byte[] addBytes) {
        if (addBytes.length == 1) {
            return 1;
        } else if (addBytes.length == 2) {
            if (addBytes[0] == 0) {
                return 1;
            } else {
                return 2;
            }
        } else if (addBytes.length == 4) {
            if (addBytes[0] == 0 && addBytes[1] == 0 && addBytes[2] == 0) {
                return 1;
            } else if (addBytes[0] == 0 && addBytes[1] == 0) {
                return 2;
            } else if (addBytes[0] == 0) {
                return 3;
            } else {
                return 4;
            }
        } else if (addBytes.length == 8) {
            for (int i = 0; i < 7; i++) {
                if (addBytes[i] != 0) {
                    return 8 - i;
                }
            }
            return 1;
        } else {
            return addBytes.length;
        }
    }

    public int encodeVariableLengthField(Object obj, Field field, List<Byte> bytes, int offset) throws IllegalAccessException {
        byte[] addBytes = getBytes(obj, field);
        int addOffset = 0;
        if (offset % 8 != 0) {
            addOffset = 8 - offset % 8;
        }
        return appendVariableBytes(addBytes, offset, bytes) + addOffset;
    }


    public int appendVariableBytes(byte[] addBytes, int offset, List<Byte> bytes) {
        int addBytesNum = addBytes.length;
//        byte[] headBytes = new byte[variableHeadByteLength];
        int variableHeadByteLength = protocolConfig.getVariableHeadByteLength();
        if (variableHeadByteLength == 1) {
            addBytesNum = Math.min(addBytesNum, 255);
        } else if (variableHeadByteLength == 2) {
            addBytesNum = Math.min(addBytesNum, 65535);
        } else if (variableHeadByteLength == 3) {
            addBytesNum = Math.min(addBytesNum, 16777215);
        }
        for (int i = 0; i < variableHeadByteLength; i++) {
            bytes.add((byte) ((addBytesNum >> (8 * (protocolConfig.getVariableHeadByteLength() - i - 1))) & 0xff));
        }
        for (int i = 0; i < addBytesNum; i++) {
            byte addByte = addBytes[i];
            bytes.add(addByte);
        }
        return addBytesNum * 8 + variableHeadByteLength * 8;
    }

    public byte[] getBytes(Object fieldValue, Class<?> fieldType) {
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            return new byte[]{(byte) fieldValue};
        }
        if (fieldType.equals(short.class) || fieldType.equals(Short.class) || fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            short value = (short) fieldValue;
            return new byte[]{(byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            int value = (int) fieldValue;
            return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            long value = (long) fieldValue;
            return new byte[]{(byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            float value = (float) fieldValue;
            int floatBits = Float.floatToIntBits(value);
            return new byte[]{(byte) (floatBits >> 24), (byte) (floatBits >> 16), (byte) (floatBits >> 8), (byte) floatBits};
        }
        if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            double value = (double) fieldValue;
            if (protocolConfig.getEnableDoubleCompression()) {
                Long longValue = compressDoubleToLong(value);
                return getBytes(longValue, Long.class);
            }
            long doubleBits = Double.doubleToLongBits(value);
            return new byte[]{(byte) (doubleBits >> 56), (byte) (doubleBits >> 48), (byte) (doubleBits >> 40), (byte) (doubleBits >> 32), (byte) (doubleBits >> 24), (byte) (doubleBits >> 16), (byte) (doubleBits >> 8), (byte) doubleBits};
        }
        if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            return new byte[]{(byte) ((boolean) fieldValue ? 1 : 0)};
        }
        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime value = (LocalDateTime) fieldValue;
            long epochMilli = value.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            return new byte[]{(byte) (epochMilli >> 56), (byte) (epochMilli >> 48), (byte) (epochMilli >> 40), (byte) (epochMilli >> 32), (byte) (epochMilli >> 24), (byte) (epochMilli >> 16), (byte) (epochMilli >> 8), (byte) epochMilli};
        }
        if (fieldType.equals(LocalDate.class)) {
            LocalDate value = (LocalDate) fieldValue;
            long epochMilli = value.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
            return new byte[]{(byte) (epochMilli >> 56), (byte) (epochMilli >> 48), (byte) (epochMilli >> 40), (byte) (epochMilli >> 32), (byte) (epochMilli >> 24), (byte) (epochMilli >> 16), (byte) (epochMilli >> 8), (byte) epochMilli};
        }
        if (fieldType.equals(String.class)) {
            String value = (String) fieldValue;
            return value.getBytes(protocolConfig.getCharset());
        }
        return new byte[0];
    }

    public byte[] getBytes(Object fieldValue, Object baseLineValue, Class<?> fieldType) {
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            byte fieldV = (byte) fieldValue;
            byte baseLineV = (byte) baseLineValue;
            fieldV ^= baseLineV;
            return new byte[]{(byte) (fieldV)};
        }
        if (fieldType.equals(short.class) || fieldType.equals(Short.class) || fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            short fieldV = (short) fieldValue;
            short baseLineV = (short) baseLineValue;
            fieldV ^= baseLineV;
            return new byte[]{(byte) (fieldV >> 8), (byte) fieldV};
        }
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            int value = (int) fieldValue;
            int baseLineV = (int) baseLineValue;
            value ^= baseLineV;
            return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            long value = (long) fieldValue;
            long baseLineV = (long) baseLineValue;
            value ^= baseLineV;
            return new byte[]{(byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            float value = (float) fieldValue;
            int floatBits = Float.floatToIntBits(value);
            int baseLineV = Float.floatToIntBits((float) baseLineValue);
            floatBits ^= baseLineV;
            return new byte[]{(byte) (floatBits >> 24), (byte) (floatBits >> 16), (byte) (floatBits >> 8), (byte) floatBits};
        }
        if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            double value = (double) fieldValue;
            double baseLineV = (double) baseLineValue;
            if (protocolConfig.getEnableDoubleCompression()) {
                Long longValue = compressDoubleToLong(value);
                Long baseLineLongValue = compressDoubleToLong(baseLineV);
                return getBytes(longValue, baseLineLongValue, Long.class);
            }
            long doubleBits = Double.doubleToLongBits(value);
            long baseLineDoubleBits = Double.doubleToLongBits(baseLineV);
            doubleBits ^= baseLineDoubleBits;
            return new byte[]{(byte) (doubleBits >> 56), (byte) (doubleBits >> 48), (byte) (doubleBits >> 40), (byte) (doubleBits >> 32), (byte) (doubleBits >> 24), (byte) (doubleBits >> 16), (byte) (doubleBits >> 8), (byte) doubleBits};
        }
        if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            return new byte[]{(byte) ((boolean) fieldValue ? 1 : 0)};
        }
        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime value = (LocalDateTime) fieldValue;
            long epochMilli = value.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            return new byte[]{(byte) (epochMilli >> 56), (byte) (epochMilli >> 48), (byte) (epochMilli >> 40), (byte) (epochMilli >> 32), (byte) (epochMilli >> 24), (byte) (epochMilli >> 16), (byte) (epochMilli >> 8), (byte) epochMilli};
        }
        if (fieldType.equals(LocalDate.class)) {
            LocalDate value = (LocalDate) fieldValue;
            long epochMilli = value.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
            return new byte[]{(byte) (epochMilli >> 56), (byte) (epochMilli >> 48), (byte) (epochMilli >> 40), (byte) (epochMilli >> 32), (byte) (epochMilli >> 24), (byte) (epochMilli >> 16), (byte) (epochMilli >> 8), (byte) epochMilli};
        }
        if (fieldType.equals(String.class)) {
            String value = (String) fieldValue;
            return value.getBytes(protocolConfig.getCharset());
        }
        return new byte[0];
    }

    public byte[] getBytes(Object obj, Field field) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        Object baseLineObj = protocolConfig.getBaseLine();
        if (!protocolConfig.getEnableBaseLineCompression() || baseLineObj == null) {
            return getBytes(field.get(obj), fieldType);
        } else {
            return getBytes(field.get(obj), field.get(baseLineObj), fieldType);
        }
    }

    public long compressDoubleToLong(double value) {
        return FloatDoubleCompressionUtil.compressDoubleToLong(value, protocolConfig.getDoubleCompressionAccuracy());
    }
}

package util;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import handler.header.PlainBodyHeader;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import static util.CodecUtil.FieldType.*;

/**
 * @author 13090
 * @version 1.0
 * @description: encode and decode util
 * @date 2022/3/14 16:28
 */
@Slf4j
public class CodecUtil {

    String clientKey;

    ProtocolConfig protocolConfig;

    public CodecUtil() {
        this.clientKey = null;
        this.protocolConfig = ProtocolConfig.defaultConfig();
    }

    /**
     * for server pipeline
     *
     * @param clientKey
     */
    public CodecUtil(String clientKey) {
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
    public CodecUtil(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        this.clientKey = null;
    }

    public byte[] encode(Object obj) throws IllegalAccessException {
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
            curByte = (byte) (curByte | (((addByte) >> bitOffset) & getMask(8 - bitOffset)));
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

    private static final ConcurrentHashMap<Class<?>, List<Field>> constantLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, List<Field>> variableLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Integer> classToIdentity = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, Class<?>> identityToClass = new ConcurrentHashMap<>();

    public static Integer getIdentityByClass(Class<?> clazz) {
        return classToIdentity.get(clazz);
    }

    public static Class<?> getClassByIdentity(int identity) {
        return identityToClass.get(identity);
    }

    public static void registerClass(Class<?> clazz) {
        int classIdentity = clazz.getName().hashCode() & ByteUtil.getMask(PlainBodyHeader.PlainBodyLabelPosition.CLASS_IDENTITY.value());
        classToIdentity.put(clazz, classIdentity);
        identityToClass.put(classIdentity, clazz);
        constantLengthFieldMap.put(clazz, new ArrayList<>(16));
        variableLengthFieldMap.put(clazz, new ArrayList<>(16));
        for (Field filed : clazz.getDeclaredFields()) {
            FieldType fieldType = getFieldType(filed);
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

    public static FieldType getFieldType(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType.equals(int.class) || fieldType.equals(long.class) ||
                fieldType.equals(byte.class) || fieldType.equals(short.class)
                || fieldType.equals(char.class) || fieldType.equals(double.class) || fieldType.equals(float.class)
                || fieldType.equals(boolean.class)) {
            return CONSTANT_LENGTH;
        }
        if (fieldType.equals(Integer.class) || fieldType.equals(Long.class) ||
                fieldType.equals(Byte.class) || fieldType.equals(Short.class) ||
                fieldType.equals(Character.class) || fieldType.equals(Double.class) || fieldType.equals(Float.class) ||
                fieldType.equals(LocalDate.class) || fieldType.equals(LocalDateTime.class)) {
            return NULLABLE_CONSTANT_LENGTH;
        }
        if (fieldType.equals(String.class)) {
            return NULLABLE_VARIABLE_LENGTH;
        }
        return VARIABLE_LENGTH;
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

    public int getConstantLengthFieldByteLength(Field field) {
        Class<?> fieldType = field.getType();
        return getConstantClassLength(fieldType);
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

    public static enum FieldType {
        CONSTANT_LENGTH,
        VARIABLE_LENGTH,
        NULLABLE_CONSTANT_LENGTH,
        NULLABLE_VARIABLE_LENGTH
    }

    public <T> T decode(byte[] bytes, Class<T> clazz) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(clazz);
        List<Field> variableLengthFields = variableLengthFieldMap.get(clazz);
        int offset = 0;
        T result = clazz.newInstance();
        for (Field field : constantLengthFields) {
            offset += decodeConstantBytes(bytes, field, offset, result);
        }
        for (Field field : variableLengthFields) {
            offset += decodeVariableBytes(bytes, field, offset, result);
        }
        return result;
    }

    public int decodeVariableBytes(byte[] bytes, Field field, int offset, Object obj) throws Exception {
        int curOffset = offset;
        int byteLength = getVariableLength(bytes, offset);
        if (offset % 8 != 0) {
            curOffset += 8 - (offset % 8);
        }
        curOffset += protocolConfig.getVariableHeadByteLength() * 8;
        int curIndex = curOffset / 8;
        byte[] stringBytes = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            stringBytes[i] = bytes[curIndex + i];
        }
        field.set(obj, new String(stringBytes, protocolConfig.getCharset()));
        curOffset += byteLength * 8;
        return curOffset - offset;
    }

    public int decodeConstantBytes(byte[] bytes, Field field, int offset, Object obj) throws Exception {
        int headLength = getConstantHeadLength(field);
        int valueLength = getLengthFromHead(bytes, headLength, offset) + 1;
        offset += headLength;
        byte[] valueBytes = getValueBytes(bytes, offset, valueLength);
        Object rowObj = convertBytesToObject(valueBytes, field);
        if (protocolConfig.getEnableBaseLineCompression() && protocolConfig.getBaseLine() != null) {
            Object baseLineObj = field.get(protocolConfig.getBaseLine());
            Object fieldValue = convertObjectWithBaseLine(rowObj, baseLineObj);
            field.set(obj, fieldValue);
        } else {
            field.set(obj, rowObj);
        }
        return headLength + valueLength * 8;
    }

    public int getLengthFromHead(byte[] bytes, int headLength, int offset) {
        int bitOffset = offset % 8;
        int index = offset / 8;
        if (bitOffset + headLength > 8) {
            byte curByte = bytes[index];
            byte nextByte = bytes[index + 1];
            int curHeadLength = 8 - bitOffset;
            int nextHeadLength = headLength - curHeadLength;
            curByte = (byte) (curByte & getMask(curHeadLength));
            nextByte = (byte) ((nextByte >> (8 - nextHeadLength)) & getMask(nextHeadLength));
            return (curByte << nextHeadLength) | nextByte;
        } else {
            byte curByte = bytes[index];
            return (((curByte << bitOffset) >> (8 - headLength)) & getMask(headLength));
        }
    }

    public byte[] getValueBytes(byte[] bytes, int offset, int valueLength) {
        byte[] result = new byte[valueLength];
        int index = offset / 8;
        int bitOffset = offset % 8;
        if (bitOffset == 0) {
            for (int i = 0; i < valueLength; i++) {
                result[i] = bytes[index + i];
            }
        } else {
            for (int i = 0; i < valueLength; i++) {
                byte curByte = bytes[index + i];
                byte nextByte = bytes[index + i + 1];
                result[i] = (byte) ((curByte << bitOffset) | ((nextByte >> (8 - bitOffset)) & getMask(bitOffset)));
            }
        }
        return result;
    }

    public Object convertObjectWithBaseLine(Object rowObj, Object baseObj) {
        Class<?> fieldType = rowObj.getClass();
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            byte value = (byte) rowObj;
            byte baseValue = (byte) baseObj;
            return value ^ baseValue;
        } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
            short value = (short) rowObj;
            short baseValue = (short) baseObj;
            return value ^ baseValue;
        } else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            char value = (char) rowObj;
            char baseValue = (char) baseObj;
            return value ^ baseValue;
        } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            int value = (int) rowObj;
            int baseValue = (int) baseObj;
            return value ^ baseValue;
        } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            long value = (long) rowObj;
            long baseValue = (long) baseObj;
            return value ^ baseValue;
        } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            float value = (float) rowObj;
            float baseValue = (float) baseObj;
            int intValue = Float.floatToIntBits(value);
            int intBaseValue = Float.floatToIntBits(baseValue);
            return Float.intBitsToFloat(intValue ^ intBaseValue);
        } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            double value = (double) rowObj;
            double baseValue = (double) baseObj;
            long longValue = Double.doubleToLongBits(value);
            long longBaseValue = Double.doubleToLongBits(baseValue);
            return Double.longBitsToDouble(longValue ^ longBaseValue);
        } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            return (boolean) rowObj;
        } else if (fieldType.equals(LocalDateTime.class)) {
            return (LocalDateTime) rowObj;
        } else if (fieldType.equals(LocalDate.class)) {
            return (LocalDate) rowObj;
        } else if (fieldType.equals(String.class)) {
            return (String) rowObj;
        }
        return null;
    }

    public Object convertBytesToObject(byte[] bytes, Class<?> fieldType) throws Exception {
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            return bytes[0];
        } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
            if (bytes.length == 1) {
                return (short) bytes[0];
            } else if (bytes.length == 2) {
                return (short) ((bytes[0] << 8) | bytes[1]);
            } else {
                return null;
            }
//            return ByteBuffer.wrap(bytes).getShort();
        } else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            if (bytes.length == 1) {
                return (char) bytes[0];
            } else if (bytes.length == 2) {
                return (char) ((bytes[0] << 8) | bytes[1]);
            } else {
                return null;
            }
//            return ByteBuffer.wrap(bytes).getChar();
        } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            byte[] wholeBytes = new byte[4];
            if (bytes.length == 1) {
                wholeBytes[0] = 0;
                wholeBytes[1] = 0;
                wholeBytes[2] = 0;
                wholeBytes[3] = bytes[0];
            } else if (bytes.length == 2) {
                wholeBytes[0] = 0;
                wholeBytes[1] = 0;
                wholeBytes[2] = bytes[0];
                wholeBytes[3] = bytes[1];
            } else if (bytes.length == 3) {
                wholeBytes[0] = 0;
                wholeBytes[1] = bytes[0];
                wholeBytes[2] = bytes[1];
                wholeBytes[3] = bytes[2];
            } else if (bytes.length == 4) {
                wholeBytes[0] = bytes[0];
                wholeBytes[1] = bytes[1];
                wholeBytes[2] = bytes[2];
                wholeBytes[3] = bytes[3];
            } else {
                return null;
            }
            return ByteBuffer.wrap(wholeBytes).getInt();
        } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            byte[] wholeBytes = new byte[8];
            int wholeBytesIndex = 7;
            int bytesIndex = bytes.length - 1;
            while (bytesIndex >= 0) {
                wholeBytes[wholeBytesIndex--] = bytes[bytesIndex--];
            }
            return ByteBuffer.wrap(wholeBytes).getLong();
        } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            byte[] wholeBytes = new byte[4];
            if (bytes.length == 4) {
                return ByteBuffer.wrap(bytes).getFloat();
            } else if (bytes.length == 3) {
                wholeBytes[0] = 0;
                wholeBytes[1] = bytes[0];
                wholeBytes[2] = bytes[1];
                wholeBytes[3] = bytes[2];
                return ByteBuffer.wrap(wholeBytes).getFloat();
            } else if (bytes.length == 2) {
                wholeBytes[0] = 0;
                wholeBytes[1] = 0;
                wholeBytes[2] = bytes[0];
                wholeBytes[3] = bytes[1];
                return ByteBuffer.wrap(wholeBytes).getFloat();
            } else if (bytes.length == 1) {
                wholeBytes[0] = 0;
                wholeBytes[1] = 0;
                wholeBytes[2] = 0;
                wholeBytes[3] = bytes[0];
                return ByteBuffer.wrap(wholeBytes).getFloat();
            }
            return null;
        } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            byte[] wholeBytes = new byte[8];
            int wholeBytesIndex = 7;
            int bytesIndex = bytes.length - 1;
            while (bytesIndex >= 0) {
                wholeBytes[wholeBytesIndex--] = bytes[bytesIndex--];
            }
            if (protocolConfig.getEnableDoubleCompression()) {
                return deCompressDoubleFromLong(ByteBuffer.wrap(wholeBytes).getLong());
            }
            return ByteBuffer.wrap(wholeBytes).getDouble();
        } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            return bytes[0] != 0;
        } else if (fieldType.equals(LocalDateTime.class)) {
            byte[] wholeBytes = new byte[8];
            int wholeBytesIndex = 7;
            int bytesIndex = bytes.length - 1;
            while (bytesIndex >= 0) {
                wholeBytes[wholeBytesIndex--] = bytes[bytesIndex--];
            }
            long time = ByteBuffer.wrap(wholeBytes).getLong();
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        } else if (fieldType.equals(LocalDate.class)) {
            byte[] wholeBytes = new byte[8];
            int wholeBytesIndex = 7;
            int bytesIndex = bytes.length - 1;
            while (bytesIndex >= 0) {
                wholeBytes[wholeBytesIndex--] = bytes[bytesIndex--];
            }
            long time = ByteBuffer.wrap(wholeBytes).getLong();
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalDate();
        } else if (fieldType.equals(String.class)) {
            return new String(bytes, protocolConfig.getCharset());
        }
        return null;
    }

    public Object convertBytesToObject(byte[] bytes, Field field) throws Exception {
        return convertBytesToObject(bytes, field.getType());
    }

    public static byte getMask(int length) {
        if (length == 1) {
            return 0x01;
        } else if (length == 2) {
            return 0x03;
        } else if (length == 3) {
            return 0x07;
        } else if (length == 4) {
            return 0x0F;
        } else if (length == 5) {
            return 0x1F;
        } else if (length == 6) {
            return 0x3F;
        } else if (length == 7) {
            return 0x7F;
        } else if (length == 8) {
            return -1;
        } else {
            return 0;
        }
    }

    public int getVariableLength(byte[] bytes, int offset) {
        if (offset % 8 != 0) {
            offset += 8 - offset % 8;
        }
        int index = offset / 8;
        int length = 0;
        for (int i = index; i < index + protocolConfig.getVariableHeadByteLength(); i++) {
            length <<= 8;
            length |= bytes[i] & 0xFF;
        }
        return length;
    }

    public Long compressDoubleToLong(double value) {
        int accuracy = protocolConfig.getDoubleCompressionAccuracy();
        int power = (int) Math.pow(10, accuracy);
        if (Double.MAX_VALUE / power < value) {
            return Long.MAX_VALUE;
        }
        return Math.round(value * power);
    }

    public Double deCompressDoubleFromLong(long value) throws Exception {
        if (value == Long.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        int accuracy = protocolConfig.getDoubleCompressionAccuracy();
        int power = (int) Math.pow(10, accuracy);
        return ((double) value) / power;
    }

    public byte[] encode2(Object obj) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(obj.getClass());
        List<Field> variableLengthFields = variableLengthFieldMap.get(obj.getClass());
        List<Byte> bytes = new ArrayList<>(constantLengthFields.size() * 4 + variableLengthFields.size() * 128);
        for (Field field : constantLengthFields) {
            field.setAccessible(true);
            Byte[] fieldBytes;
            if (protocolConfig.getEnableBaseLineCompression()) {
                fieldBytes = encode2WithBaseLine(obj, field, protocolConfig.getBaseLine());
            } else {
                fieldBytes = encode2WithoutBaseLine(obj, field);
            }
            for (Byte b : fieldBytes) {
                if (b != null) {
                    bytes.add(b);
                }
            }
        }
        for (Field field : variableLengthFields) {
            field.setAccessible(true);
            Byte[] fieldBytes = encode2WithoutBaseLine(obj, field);
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

    public Byte[] encode2WithBaseLine(Object obj, Field field, Object baseLineObj) throws Exception {
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

    public Byte[] encode2WithoutBaseLine(Object obj, Field field) throws Exception {
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

    public long getZigZag(long num) {
        return ((num >> 63) ^ (num << 1));
    }

    public int getZigZag(int num) {
        return ((num >> 31) ^ (num << 1));
    }

    public Byte[] encodeInt(int n) {
        Byte[] buf = new Byte[5];
        int pos = 0;
        n = getZigZag(n);
        while((n & ~0x7F) != 0) {
            buf[pos++] = (byte)((n & 0x7F) | 0x80);
            n >>>= 7;
        }
        buf[pos] = (byte) n;
        return buf;
    }

    public Byte[] encodeLong(long n) {
// move sign to low-order bit, and flip others if negative
        Byte[] buf = new Byte[10];
        int pos = 0;
        n = getZigZag(n);
        while ((n & (~0x7f)) != 0) {
            buf[pos++] = (byte) ((n | 0x80) & 0xFF);
            n >>>= 7;
        }
        buf[pos] = (byte) n;
        return buf;
    }

    public <T> T decode2(byte[] bytes, Class<T> clazz) throws Exception {
        List<Field> constantLengthFields = constantLengthFieldMap.get(clazz);
        List<Field> variableLengthFields = variableLengthFieldMap.get(clazz);
        int offset = 0;
        T result = clazz.newInstance();
        for (Field field : constantLengthFields) {
            offset += decodeConstantBytes2(bytes, offset, field, result);
        }
        for (Field field : variableLengthFields) {
            byte[] lengthBytes = getConstantBytes2(bytes, offset);
            offset += lengthBytes.length;
            int length = decodeInt(lengthBytes);
            byte[] valueBytes = new byte[length];
            System.arraycopy(bytes, offset, valueBytes, 0, length);
            offset += length;
            field.set(result, new String(valueBytes, protocolConfig.getCharset()));
        }
        return result;
    }

    public int decodeConstantBytes2(byte[] bytes, int offset, Field field, Object obj) throws Exception {
        Class<?> fieldType = field.getType();
        byte[] fieldBytes = getConstantBytes2(bytes, offset);
        if (protocolConfig.getEnableBaseLineCompression()) {
            field.set(obj, decodeConstantFieldBytes2(fieldBytes, fieldType, field.get(protocolConfig.getBaseLine())));
        } else {
            field.set(obj, decodeConstantFieldBytes2(fieldBytes, fieldType));
        }
        return fieldBytes.length;
    }

    public byte[] getConstantBytes2(byte[] bytes, int offset) throws Exception {
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

    public Object decodeConstantFieldBytes2(byte[] bytes, Class<?> clazz) throws Exception {
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

    public Object decodeConstantFieldBytes2(byte[] bytes, Class<?> clazz, Object baseLineObj) throws Exception {
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

    public int decodeInt(byte[] buf) throws Exception {
        int len = 1;
        int b = buf[0] & 0xff;
        int n = b & 0x7f;
        if (b > 0x7f) {
            b = buf[len++] & 0xff;
            n ^= (b & 0x7f) << 7;
            if (b > 0x7f) {
                b = buf[len++] & 0xff;
                n ^= (b & 0x7f) << 14;
                if (b > 0x7f) {
                    b = buf[len++] & 0xff;
                    n ^= (b & 0x7f) << 21;
                    if (b > 0x7f) {
                        b = buf[len++] & 0xff;
                        n ^= (b & 0x7f) << 28;
                        if (b > 0x7f) {
                            throw new Exception("Invalid int encoding");
                        }
                    }
                }
            }
        }
        return (n >>> 1) ^ -(n & 1); // back to two's-complement
    }

    public long decodeLong(byte[] buf) throws Exception {
        int len = 1;
        long b = buf[0] & 0xff;
        long n = b & 0x7f;
        while (b > 0x7f) {
            b = buf[len++] & 0xff;
            n ^= (b & 0x7f) << (7 * (len - 1));
        }
        return (n >>> 1) ^ -(n & 1); // back to two's-complement
    }

    public byte[] encode3(Object obj) throws Exception {
        return null;
    }

    public ByteArrayOutputStream encodeWithoutBaseLine3(Object obj, Field field) throws Exception {
        return null;
    }

    public ByteArrayOutputStream encodeInt3(ByteArrayOutputStream out, int n) throws Exception {
        n = getZigZag(n);
        while((n & ~0x7F) != 0) {
            out.write((byte) ((n & 0x7F) | 0x80));
            n >>>= 7;
        }
        out.write((byte) n);
        return out;
    }

    public ByteArrayOutputStream encodeLong3(ByteArrayOutputStream out, long n) throws Exception {
        n = getZigZag(n);
        while ((n & (~0x7f)) != 0) {
            out.write((byte) ((n & 0x7F) | 0x80));
            n >>>= 7;
        }
        out.write((byte) n);
        return out;
    }

    public int decodeInt3(ByteArrayInputStream in) throws Exception {
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

    public long decodeLong3(ByteArrayInputStream in) throws Exception {
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
}

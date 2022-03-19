package util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    public final static int MAGIC_NUM = 0x114514;

    public Byte[] encode(Object obj) throws IllegalAccessException {
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
        return bytes.toArray(new Byte[0]);
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
            if (bitOffset == 0) {
                bytes.add((byte) (constantValueBytesNum << (8 - headLength)));
            } else {
                if (bitOffset + headLength > 8) {
                    Byte curByte = bytes.get(index);
                    bytes.add((byte) 0);
                    Byte nextByte = bytes.get(index + 1);
                    bytes.set(index, (byte) (curByte | (constantValueBytesNum >> (bitOffset + headLength - 8))));
                    bytes.set(index + 1, (byte) (nextByte | (constantValueBytesNum << (8 - bitOffset))));
                } else {
                    Byte curByte = bytes.get(index);
                    bytes.set(index, (byte) (curByte | (constantValueBytesNum << (8 - bitOffset - headLength))));
                }
            }
            bitOffset += headLength;
            bitOffset %= 8;
        }
        for (int i = addBytes.length - constantValueBytesNum; i < addBytes.length; i++) {
            byte addByte = addBytes[i];
            bytes.add((byte) 0);
            Byte curByte = bytes.get(index);
            curByte = (byte) (curByte | (addByte >> bitOffset));
            bytes.set(index, curByte);
            bitOffset += 8;
            bitOffset %= 8;
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
        return 0;
    }

    private static final ConcurrentHashMap<Class<?>, List<Field>> constantLengthFieldMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, List<Field>> variableLengthFieldMap = new ConcurrentHashMap<>();


    public static void registerClass(Class<?> clazz) {
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

    public static int getConstantLengthFieldByteLength(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            return 1;
        }
        if (fieldType.equals(short.class) || fieldType.equals(Short.class) || fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            return 2;
        }
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class) || fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            return 4;
        }
        if (fieldType.equals(long.class) || fieldType.equals(Long.class) || fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            return 8;
        }
        if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            return 1;
        }
        if (fieldType.equals(LocalDateTime.class) || fieldType.equals(LocalDate.class)) {
            return 8;
        }
        return -1;
    }

    public static byte[] getBytes(Object obj, Field field) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            return new byte[]{(byte) field.get(obj)};
        }
        if (fieldType.equals(short.class) || fieldType.equals(Short.class) || fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            short value = (short) field.get(obj);
            return new byte[]{(byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            int value = (int) field.get(obj);
            return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            long value = (long) field.get(obj);
            return new byte[]{(byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        }
        if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            float value = (float) field.get(obj);
            int floatBits = Float.floatToIntBits(value);
            return new byte[]{(byte) (floatBits >> 24), (byte) (floatBits >> 16), (byte) (floatBits >> 8), (byte) floatBits};
        }
        if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            double value = (double) field.get(obj);
            long doubleBits = Double.doubleToLongBits(value);
            return new byte[]{(byte) (doubleBits >> 56), (byte) (doubleBits >> 48), (byte) (doubleBits >> 40), (byte) (doubleBits >> 32), (byte) (doubleBits >> 24), (byte) (doubleBits >> 16), (byte) (doubleBits >> 8), (byte) doubleBits};
        }
        if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            return new byte[]{(byte) ((boolean) field.get(obj) ? 1 : 0)};
        }
        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime value = (LocalDateTime) field.get(obj);
            long epochMilli = value.getLong(ChronoField.INSTANT_SECONDS);
            return new byte[]{(byte) (epochMilli >> 56), (byte) (epochMilli >> 48), (byte) (epochMilli >> 40), (byte) (epochMilli >> 32), (byte) (epochMilli >> 24), (byte) (epochMilli >> 16), (byte) (epochMilli >> 8), (byte) epochMilli};
        }
        if (fieldType.equals(LocalDate.class)) {
            LocalDate value = (LocalDate) field.get(obj);
            long epochMilli = value.getLong(ChronoField.INSTANT_SECONDS);
            return new byte[]{(byte) (epochMilli >> 56), (byte) (epochMilli >> 48), (byte) (epochMilli >> 40), (byte) (epochMilli >> 32), (byte) (epochMilli >> 24), (byte) (epochMilli >> 16), (byte) (epochMilli >> 8), (byte) epochMilli};
        }
        return new byte[0];
    }

    public static enum FieldType {
        CONSTANT_LENGTH,
        VARIABLE_LENGTH,
        NULLABLE_CONSTANT_LENGTH,
        NULLABLE_VARIABLE_LENGTH
    }

}

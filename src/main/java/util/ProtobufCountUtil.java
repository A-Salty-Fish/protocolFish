package util;

import java.lang.reflect.Field;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 19:40
 */

public class ProtobufCountUtil {
    public static int countBytes(Object obj) throws IllegalAccessException {
        int bytes = 0;
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            bytes += getFieldValueLength(field, obj) + getFieldHeadLength(field);
        }
        return bytes;
    }

    public static int getFieldHeadLength(Field field) {
        Class<?> clazz = field.getType();
        if (clazz == int.class || clazz == Integer.class) {
            return 1;
        } else if (clazz == long.class || clazz == Long.class) {
            return 1;
        } else if (clazz == double.class || clazz == Double.class) {
            return 1;
        } else if (clazz == float.class || clazz == Float.class) {
            return 1;
        } else if (clazz == String.class) {
            return 4;
        }
        return -1;
    }

    public static int getFieldValueLength(Field field, Object obj) throws IllegalAccessException {
        Class<?> clazz = field.getType();
        if (clazz == int.class || clazz == Integer.class) {
            return getVarNumLength((Integer) field.get(obj));
        } else if (clazz == long.class || clazz == Long.class) {
            return getVarNumLength((Long) field.get(obj));
        } else if (clazz == float.class || clazz == Float.class) {
            return 4;
        } else if (clazz == double.class || clazz == Double.class) {
            return 8;
        } else if (clazz == String.class) {
            String str = ((String) field.get(obj));
            return str.getBytes().length;
        }
        return 0;
    }

    public static int getVarNumLength(int num) {
        return encodeInt(num);
    }

    public static int getVarNumLength(long num) {
        return encodeLong(num);
    }

    public static long getZigZag(long num) {
        return ((num >> 63) ^ (num << 1));
    }

    public static int getZigZag(int num) {
        return ((num >> 31) ^ (num << 1));
    }

    public static int encodeInt(int n) {
// move sign to low-order bit, and flip others if negative
        byte[] buf = new byte[10];
        int pos = 0;
        n = (n << 1) ^ (n >> 31);
        int start = pos;
        if ((n & ~0x7F) != 0) {
            buf[pos++] = (byte) ((n | 0x80) & 0xFF);
            n >>>= 7;
            if (n > 0x7F) {
                buf[pos++] = (byte) ((n | 0x80) & 0xFF);
                n >>>= 7;
                if (n > 0x7F) {
                    buf[pos++] = (byte) ((n | 0x80) & 0xFF);
                    n >>>= 7;
                    if (n > 0x7F) {
                        buf[pos++] = (byte) ((n | 0x80) & 0xFF);
                        n >>>= 7;
                    }
                }
            }
        }
        buf[pos++] = (byte) n;
        return pos - start;
    }

    public static int encodeLong(long n) {
// move sign to low-order bit, and flip others if negative
        byte[] buf = new byte[10];
        int pos = 0;
        n = (n << 1) ^ (n >> 63);
        int start = pos;
        if ((n & ~0x7F) != 0) {
            buf[pos++] = (byte) ((n | 0x80) & 0xFF);
            n >>>= 7;
            if (n > 0x7F) {
                buf[pos++] = (byte) ((n | 0x80) & 0xFF);
                n >>>= 7;
                if (n > 0x7F) {
                    buf[pos++] = (byte) ((n | 0x80) & 0xFF);
                    n >>>= 7;
                    if (n > 0x7F) {
                        buf[pos++] = (byte) ((n | 0x80) & 0xFF);
                        n >>>= 7;
                    }
                }
            }
        }
        buf[pos++] = (byte) n;
        return pos - start;
    }
}

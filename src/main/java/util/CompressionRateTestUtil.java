package util;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/26 15:21
 */

public class CompressionRateTestUtil {

    @SuppressWarnings("unchecked")
    public static <T> T clone(T t) throws Exception {
        T obj = (T) t.getClass().newInstance();
        for (Field field : t.getClass().getDeclaredFields()) {
            Class<?> type = field.getType();
            if (type.isPrimitive()) {
                field.set(obj, field.get(t));
            } else {
                if (type.equals(Double.class)) {
                    field.set(obj, ((Double) field.get(t)).doubleValue());
                } else if (type.equals(Integer.class)) {
                    field.set(obj, ((Integer) field.get(t)).intValue());
                } else if (type.equals(Long.class)) {
                    field.set(obj, ((Long) field.get(t)).longValue());
                } else if (type.equals(String.class)) {
                    field.set(obj, new String(((String) field.get(t)).getBytes()));
                } else {
                    throw new Exception("type not support");
                }
            }
        }
        return obj;
    }

    public static <T> T random(Class<T> clazz) throws Exception {
        T obj = clazz.newInstance();
        Random random = new Random();
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            if (type.equals(int.class) || type.equals(Integer.class)) {
                field.set(obj, random.nextInt());
            } else if (type.equals(long.class) || type.equals(Long.class)) {
                field.set(obj, random.nextLong());
            } else if (type.equals(double.class) || type.equals(Double.class)) {
                field.set(obj, random.nextDouble() * random.nextLong());
            } else if (type.equals(String.class)) {
                field.set(obj, String.valueOf(random.nextLong()));
            }
        }
        return obj;
    }
}

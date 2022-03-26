package util;

import java.lang.reflect.Field;

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

}

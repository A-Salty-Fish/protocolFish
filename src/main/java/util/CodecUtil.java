package util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public static ByteBuf encode(String msg) {
        log.info("row msg:{}", msg);
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer(4 + 4 + bytes.length);
        buf.writeInt(MAGIC_NUM);
        buf.writeInt(msg.length());
        buf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        log.info("encoded msg:{}", buf);
        return buf;
    }

    public static String deCode(ByteBuf byteBuf) {
        log.info("decode msg:{}", byteBuf.toString());
        int magicNum = byteBuf.readInt();
        if (magicNum != MAGIC_NUM) {
            return null;
        }
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        String msg = new String(bytes, StandardCharsets.UTF_8);
        log.info("row msg:{}", msg);
        return msg;
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

    public static enum FieldType {
        CONSTANT_LENGTH,
        VARIABLE_LENGTH,
        NULLABLE_CONSTANT_LENGTH,
        NULLABLE_VARIABLE_LENGTH
    }

}

package util;

import com.sun.org.apache.bcel.internal.classfile.Code;
import demo.TestEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/18 19:27
 */

public class CodeCTest {

    public static void main(String[] args) {
        testGetBytes2();
    }

    public static TestEntity getEntity() {
        TestEntity entity = new TestEntity();
        entity.setDoubleNum(1.1);
        entity.setDoubleNum2(1111111.1111111111);
        entity.setIntNum(1);
        entity.setIntNum2(1111111111);
        entity.setLongNum(1L);
        entity.setLongNum2(1111111111111111111L);
        entity.setName("1");
        entity.setName2("111111111111111111111111");
        entity.setLocalDate(LocalDate.now());
        entity.setLocalDateTime(LocalDateTime.now());
        return entity;
    }

    public static byte[] testGetBytes() {
        TestEntity entity = getEntity();
        try {
            return CodecUtil.getBytes(entity, entity.getClass().getField("intNum"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void testGetBytes2() {
        TestEntity entity = getEntity();
        try {
            List<Byte> bytes = new ArrayList<>();
            int offset = (CodecUtil.encodeConstantLengthField(entity, entity.getClass().getField("intNum"), bytes,
                    0));
            offset = CodecUtil.encodeConstantLengthField(entity, entity.getClass().getField("intNum2"), bytes, offset);
            System.out.println(Arrays.toString(bytes.toArray()));
//            01000010 00111010 00110101 11000111
//            01000010 00111010 00110111 11000111
//            01000010 00111010 00111111 11000111
            bytes.forEach(x -> System.out.println(byteToString(x)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String byteToString(byte b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append((b >> i) & 1);
        }
        return sb.reverse().toString();
    }
}

package util;

import com.sun.org.apache.bcel.internal.classfile.Code;
import demo.TestEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/18 19:27
 */

public class CodeCTest {

    public static void main(String[] args) {
        CodecUtil.registerClass(TestEntity.class);
        byte[] bytes = testGetBytes();
        System.out.println(Arrays.toString(bytes));
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
}

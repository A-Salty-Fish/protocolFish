package util;

import demo.TestEntity;
import proto.TestEntityOuterClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/18 19:27
 */

public class CodeCTest {

    public static void main(String[] args) throws IllegalAccessException {
        CodecUtil.registerClass(TestEntity.class);
        long protoLength = 0;
        long testLength = 0;
        for (int i = 0; i < 10000; i++) {
            TestEntity testEntity = getRandomTestEntity();
            testLength += CodecUtil.encode(testEntity).length;
            protoLength += getProtocolEntityFromTestEntity(testEntity).toByteArray().length;
        }
        System.out.println("testLength:" + testLength);
        System.out.println("protoLength:" + protoLength);
        System.out.println("testLength/protoLength: " + (double) testLength / protoLength);
    }

    public static TestEntityOuterClass.TestEntity getProtocolEntity() {
        return TestEntityOuterClass.TestEntity.newBuilder()
                .setDoubleNum(1.1)
                .setDoubleNum2(1111111.1111111111)
                .setIntNum(1)
                .setIntNum2(111111111)
                .setLongNum(1L)
                .setLongNum(1111111111111111111L)
                .setName("1")
                .setName2("111111111111111111111111")
                .setLocalDate(System.currentTimeMillis())
                .setLocalDateTime(System.currentTimeMillis())
                .build();
    }

    public static TestEntity getEntity() {
        return TestEntity.builder()
                .setDoubleNum(1.1)
                .setDoubleNum2(1111111.1111111111)
                .setIntNum(1)
                .setIntNum2(1111111111)
                .setLongNum(1L)
                .setLongNum2(1111111111111111111L)
                .setName("1")
                .setName2("111111111111111111111111")
                .setLocalDate(LocalDate.now())
                .setLocalDateTime(LocalDateTime.now())
                .build();
    }

    public static TestEntity getRandomTestEntity() {
        Random random = new Random();
        return TestEntity.builder()
                .setDoubleNum(random.nextDouble() * random.nextInt())
                .setDoubleNum2(random.nextDouble() * random.nextLong())
                .setIntNum(random.nextInt(256))
                .setIntNum2(random.nextInt(256 * 128) + 256)
                .setLongNum(random.nextLong())
                .setLongNum2((long) random.nextInt(256 * 256 * 256))
                .setName("test" + random.nextLong())
                .setName2("111111111111111111111111" + random.nextLong() + " " + random.nextLong())
                .setLocalDate(LocalDate.now())
                .setLocalDateTime(LocalDateTime.now())
                .build();
    }

    public static TestEntityOuterClass.TestEntity getProtocolEntityFromTestEntity(TestEntity testEntity) {
        return TestEntityOuterClass.TestEntity.newBuilder()
                .setDoubleNum(testEntity.getDoubleNum())
                .setDoubleNum2(testEntity.getDoubleNum2())
                .setIntNum(testEntity.getIntNum())
                .setIntNum2(testEntity.getIntNum2())
                .setLongNum(testEntity.getLongNum())
                .setLongNum2(testEntity.getLongNum2())
                .setName(testEntity.getName())
                .setName2(testEntity.getName2())
                .setLocalDate(testEntity.getLocalDate().toEpochDay())
                .setLocalDateTime(testEntity.getLocalDateTime().toEpochSecond(java.time.ZoneOffset.UTC))
                .build();
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
//            01000010 00111010 00110101 11000111
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

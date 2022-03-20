package util;

import demo.TestEntity;
import org.junit.Assert;
import org.junit.Test;
import proto.TestEntityOuterClass;

import java.nio.charset.StandardCharsets;
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
//        CodecUtil.registerClass(TestEntity.class);
//        long protoLength = 0;
//        long testLength = 0;
//        for (int i = 0; i < 10000; i++) {
//            TestEntity testEntity = getRandomTestEntity();
//            testLength += new CodecUtil("").encode(testEntity).length;
//            protoLength += getProtocolEntityFromTestEntity(testEntity).toByteArray().length;
//        }
//        System.out.println("testLength:" + testLength);
//        System.out.println("protoLength:" + protoLength);
//        System.out.println("testLength/protoLength: " + (double) testLength / protoLength);
    }

    @Test
    public void testGetLengthFromHead() throws Exception {
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 192}, 2, 0), 3);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 0}, 2, 0), 0);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 192}, 2, 1), 2);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 224}, 2, 1), 3);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 224}, 2, 2), 2);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 224}, 3, 0), 7);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 64}, 2, 0), 1);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 1, (byte) 128}, 2, 7), 3);
        Assert.assertEquals(new CodecUtil("").getLengthFromHead(new byte[]{(byte) 3, (byte) 128}, 3, 6), 7);
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
            return new CodecUtil("").getBytes(entity, entity.getClass().getField("intNum"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void testGetBytes2() {
        TestEntity entity = getEntity();
        try {
            List<Byte> bytes = new ArrayList<>();
            int offset = (new CodecUtil("").encodeConstantLengthField(entity, entity.getClass().getField("intNum"), bytes,
                    0));
            offset = new CodecUtil("").encodeConstantLengthField(entity, entity.getClass().getField("intNum2"), bytes, offset);
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

    @Test
    public void testGetValueBytes() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
//        System.out.println(Arrays.toString(codecUtil.getValueBytes(new byte[]{(byte) (0)}, 0, 1)));
//        System.out.println(Arrays.toString(codecUtil.getValueBytes(new byte[]{(byte) (0), (byte) 1}, 0, 2)));
        System.out.println(Arrays.toString(codecUtil.getValueBytes(new byte[]{(byte) (128), (byte) 128}, 1, 1)));
        System.out.println(Arrays.toString(codecUtil.getValueBytes(new byte[]{(byte) (192), (byte) 128}, 1, 1)));
        System.out.println(Arrays.toString(codecUtil.getValueBytes(new byte[]{(byte) (128), (byte) 128}, 2, 1)));
    }

    @Test
    public void testConvertBytesToObjectIntegerAndLong() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11)}, TestEntity.class.getDeclaredField("intNum")), 17);
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("intNum")), ((0x11) << (8)) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("intNum")), ((0x11) << (16)) + ((0x11) << 8) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("intNum")), ((0x11) << 24) + ((0x11) << 16) + ((0x11) << 8) + (0x11));

        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), (long) 17);
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), (long) ((0x11) << (8)) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), (long) ((0x11) << (16)) + ((0x11) << 8) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), (long) ((0x11) << 24) + ((0x11) << 16) + ((0x11) << 8) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), ((long) 0x11 << 32) + (long) ((0x11) << 24) + ((0x11) << 16) + ((0x11) << 8) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), ((long) 0x11 << 40) + ((long) 0x11 << 32) + (long) ((0x11) << 24) + ((0x11) << 16) + ((0x11) << 8) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), ((long) 0x11 << 48) + ((long) 0x11 << 40) + ((long) 0x11 << 32) + (long) ((0x11) << 24) + ((0x11) << 16) + ((0x11) << 8) + (0x11));
        Assert.assertEquals(codecUtil.convertBytesToObject(new byte[]{(byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11), (byte) (0x11)}, TestEntity.class.getDeclaredField("longNum")), ((long) 0x11 << 56) + ((long) 0x11 << 48) + ((long) 0x11 << 40) + ((long) 0x11 << 32) + (long) ((0x11) << 24) + ((0x11) << 16) + ((0x11) << 8) + (0x11));
//        System.out.println());
    }

    @Test
    public void testConvertBytesToObjectDouble() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
        Double a = 1.1111111;
        Double b = 0.11;
        Double c = 0.5;
        Double d = 1.0;
        byte[] aBytes = DoubleToBytes(a);
        double aC = (double) codecUtil.convertBytesToObject(aBytes, TestEntity.class.getDeclaredField("doubleNum"));
        Assert.assertEquals(a, aC, 0.000001);

        byte[] bBytes = DoubleToBytes(b);
        double bC = (double) codecUtil.convertBytesToObject(bBytes, TestEntity.class.getDeclaredField("doubleNum"));
        Assert.assertEquals(b, bC, 0.000001);

        byte[] cBytes = DoubleToBytes(c);
        double cC = (double) codecUtil.convertBytesToObject(cBytes, TestEntity.class.getDeclaredField("doubleNum"));
        Assert.assertEquals(c, cC, 0.000001);

        byte[] dBytes = DoubleToBytes(d);
        double dC = (double) codecUtil.convertBytesToObject(dBytes, TestEntity.class.getDeclaredField("doubleNum"));
        Assert.assertEquals(d, dC, 0.000001);
    }

    public byte[] DoubleToBytes(double value) {
        long doubleBits = Double.doubleToLongBits(value);
        return new byte[]{(byte) (doubleBits >> 56), (byte) (doubleBits >> 48), (byte) (doubleBits >> 40), (byte) (doubleBits >> 32), (byte) (doubleBits >> 24), (byte) (doubleBits >> 16), (byte) (doubleBits >> 8), (byte) doubleBits};
    }

    @Test
    public void testConvertBytesToObjectString() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
        String a = "";
        String b = "a";
        String c = "hello world this day is a good day";
        String d = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccc";

        String ac = (String) codecUtil.convertBytesToObject(a.getBytes(StandardCharsets.UTF_8), TestEntity.class.getDeclaredField("name"));
        Assert.assertEquals(a, ac);

        String bc = (String) codecUtil.convertBytesToObject(b.getBytes(StandardCharsets.UTF_8), TestEntity.class.getDeclaredField("name"));
        Assert.assertEquals(b, bc);

        String cc = (String) codecUtil.convertBytesToObject(c.getBytes(StandardCharsets.UTF_8), TestEntity.class.getDeclaredField("name"));
        Assert.assertEquals(c, cc);

        String dc = (String) codecUtil.convertBytesToObject(d.getBytes(StandardCharsets.UTF_8), TestEntity.class.getDeclaredField("name"));
        Assert.assertEquals(d, dc);
    }

    @Test
    public void getVariableLength() {
        CodecUtil codecUtil = new CodecUtil("");
        int length =  codecUtil.getVariableLength(new byte[]{0,1,1}, 1);
        Assert.assertEquals(length, 257);
        length =  codecUtil.getVariableLength(new byte[]{0,1,1,1}, 1);
        Assert.assertEquals(length, 257);
        length =  codecUtil.getVariableLength(new byte[]{0,1}, 0);
        Assert.assertEquals(length, 1);
        length =  codecUtil.getVariableLength(new byte[]{1,1,1}, 0);
        Assert.assertEquals(length, 257);
    }
}

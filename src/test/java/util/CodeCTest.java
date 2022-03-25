package util;

import com.google.gson.Gson;
import demo.IotSimpleEntity;
import demo.TestEntity;
import demo.TestXmlEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import proto.TestEntityOuterClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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

    @Before
    public void register() {
        CodecUtil.registerClass(TestEntity.class);
    }

    public static void main(String[] args) throws IllegalAccessException {
        CodecUtil.registerClass(TestEntity.class);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setVariableHeadByteLength(1);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        long protoLength1 = 0;
        long testLength1 = 0;
        for (int i = 0; i < 10000; i++) {
            TestEntity testEntity = TestEntity.getRandomTestEntity();
            testLength1 += codecUtil.encode(testEntity).length;
            protoLength1 += TestEntity.getProtocolEntityFromTestEntity(testEntity).toByteArray().length;
        }
        System.out.println("enable variable length:");
        System.out.println("testLength:" + testLength1);
        System.out.println("protoLength:" + protoLength1);
        System.out.println("testLength/protoLength: " + (double) testLength1 / protoLength1);

        CodecUtil codecUtil1 = new CodecUtil("");
        long protoLength2 = 0;
        long testLength2 = 0;
        for (int i = 0; i < 10000; i++) {
            TestEntity testEntity = TestEntity.getRandomTestEntity();
            testLength2 += codecUtil1.encode(testEntity).length;
            protoLength2 += TestEntity.getProtocolEntityFromTestEntity(testEntity).toByteArray().length;
        }
        System.out.println("disable variable length:");
        System.out.println("testLength:" + testLength2);
        System.out.println("protoLength:" + protoLength2);
        System.out.println("testLength/protoLength: " + (double) testLength2 / protoLength2);

        System.out.println("\n" + ((double) testLength2 / protoLength2 - (double) testLength1 / protoLength1));
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

    public static byte[] testGetBytes() {
        TestEntity entity = TestEntity.getRandomTestEntity();
        try {
            return new CodecUtil("").getBytes(entity, entity.getClass().getField("intNum"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void testGetBytes2() {
        TestEntity entity = TestEntity.getRandomTestEntity();
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

    public static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToString(bytes[i]));
            sb.append(" ");
        }
        return sb.toString();
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
        int length = codecUtil.getVariableLength(new byte[]{0, 1, 1}, 1);
        Assert.assertEquals(length, 257);
        length = codecUtil.getVariableLength(new byte[]{0, 1, 1, 1}, 1);
        Assert.assertEquals(length, 257);
        length = codecUtil.getVariableLength(new byte[]{0, 1}, 0);
        Assert.assertEquals(length, 1);
        length = codecUtil.getVariableLength(new byte[]{1, 1, 1}, 0);
        Assert.assertEquals(length, 257);
    }

    @Test
    public void decodeVariableBytes() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");

        String testString = "hello world";
        byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
        byte[] testBytes2 = new byte[testBytes.length + 2];
        testBytes2[0] = (byte) (testBytes.length >> 8);
        testBytes2[1] = (byte) testBytes.length;
        System.arraycopy(testBytes, 0, testBytes2, 2, testBytes.length);
        TestEntity testEntity = new TestEntity();
        Assert.assertEquals(testBytes2.length, 13);
        Assert.assertEquals(codecUtil.decodeVariableBytes(testBytes2, testEntity.getClass().getField("name"), 0, testEntity), testBytes2.length * 8);
        Assert.assertEquals(testEntity.getName(), testString);

        String testString2 = " ";
        byte[] testBytes3 = testString2.getBytes(StandardCharsets.UTF_8);
        byte[] testBytes4 = new byte[testBytes3.length + 2];
        testBytes4[0] = (byte) (testBytes3.length >> 8);
        testBytes4[1] = (byte) testBytes3.length;
        System.arraycopy(testBytes3, 0, testBytes4, 2, testBytes3.length);
        TestEntity testEntity2 = new TestEntity();
        Assert.assertEquals(testBytes4.length, testString2.length() + 2);
        Assert.assertEquals(codecUtil.decodeVariableBytes(testBytes4, testEntity2.getClass().getField("name"), 0, testEntity2), testBytes4.length * 8);
        Assert.assertEquals(testEntity2.getName(), testString2);

        String testString3 = "hello this world";
        byte[] testBytes5 = testString3.getBytes(StandardCharsets.UTF_8);
        byte[] testBytes6 = new byte[testBytes5.length + 3];
        testBytes6[0] = (byte) (testBytes5.length >> 16);
        testBytes6[1] = (byte) (testBytes5.length >> 8);
        testBytes6[2] = (byte) testBytes5.length;
        System.arraycopy(testBytes5, 0, testBytes6, 3, testBytes5.length);
        TestEntity testEntity3 = new TestEntity();
        Assert.assertEquals(testBytes6.length, testString3.length() + 3);
        Assert.assertEquals(codecUtil.decodeVariableBytes(testBytes6, testEntity3.getClass().getField("name"), 2, testEntity3), testBytes6.length * 8 - 2);
        Assert.assertEquals(testEntity3.getName(), testString3);
    }

    @Test
    public void testEncodeAndDecode() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");

        CodecUtil.registerClass(TestEntity.class);
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));
        byte[] bytes = codecUtil.encode(testEntity);
//        System.out.println(bytes.length);
        TestEntity testEntity2 = codecUtil.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
        Assert.assertEquals(testEntity.getName(), testEntity2.getName());
        Assert.assertEquals(testEntity.getName2(), testEntity2.getName2());
        Assert.assertEquals(testEntity.getDoubleNum(), testEntity2.getDoubleNum(), 0.00001);
        Assert.assertEquals(testEntity.getDoubleNum2(), testEntity2.getDoubleNum2(), 0.00001);
        Assert.assertEquals(testEntity.getLongNum(), testEntity2.getLongNum());
        Assert.assertEquals(testEntity.getLongNum2(), testEntity2.getLongNum2());
        Assert.assertEquals(testEntity.getIntNum(), testEntity2.getIntNum());
        Assert.assertEquals(testEntity.getIntNum2(), testEntity2.getIntNum2());
//        Assert.assertEquals(testEntity.getLocalDate(), testEntity2.getLocalDate());
        Assert.assertEquals(testEntity.getLocalDateTime(), testEntity2.getLocalDateTime());

//
//        CodecUtil.registerClass(TestEntity1.class);
//        TestEntity1 testEntity1 = new TestEntity1();
//        testEntity1.num1 = 111111111;
//        testEntity1.num2 = 222222222;
//        testEntity1.longNum = 33333333344L;
//        testEntity1.longNum2 = 2222222222444444L;
//        testEntity1.xDouble = 1.111;
//        testEntity1.xDouble2 = 1111111.11111111;
//        testEntity1.zname = "hello world";
//        testEntity1.zname2 = "hello this world";
//        testEntity1.ylocalDateTime = LocalDateTime.now();
//        testEntity1.ylocalDate = LocalDate.now();
//        System.out.println("row data:" + new Gson().toJson(testEntity1));
//        byte[] bytes = codecUtil.encode(testEntity1);
//        TestEntity1 testEntity2 = codecUtil.decode(bytes, TestEntity1.class);
//        System.out.println("decode data:" + new Gson().toJson(testEntity2));
//        //        System.out.println(Arrays.toString(bytes));
////        System.out.println(bytes.length);
//        for (int i = 0; i < bytes.length; i++) {
//            System.out.println(CodeCTest.byteToString(bytes[i]));
//        }
    }

    @Test
    public void testVariableLengthHeader() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setVariableHeadByteLength(1);
        CodecUtil codecUtil1 = new CodecUtil(protocolConfig);
        CodecUtil codecUtil2 = new CodecUtil("");
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));

        byte[] bytes1 = codecUtil1.encode(testEntity);
        System.out.println(bytes1.length);
        TestEntity testEntity1 = codecUtil1.decode(bytes1, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity1));
        byte[] bytes2 = codecUtil2.encode(testEntity);
        System.out.println(bytes2.length);
        TestEntity testEntity2 = codecUtil2.decode(bytes2, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));

    }

    public static byte[] convertIntegerToBytes(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    @Test
    public void testCompressDouble() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
        double d = 1.1111111;
        long ld = codecUtil.compressDoubleToLong(d);
        System.out.println(ld);
        double dd = codecUtil.deCompressDoubleFromLong(ld);
        System.out.println(dd);
    }

    @Test
    public void testEnableDoubleCompression() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        testEntity.setDoubleNum(1.1111111);
        testEntity.setDoubleNum2(1.1111111);
        System.out.println(new Gson().toJson(testEntity));
        byte[] bytes = codecUtil.encode(testEntity);
        System.out.println(bytes.length);
        TestEntity testEntity2 = codecUtil.decode(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
        System.out.println(new CodecUtil("").encode(testEntity).length);
    }

    @Test
    public void testCompressionRateWithDoubleCompression() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setVariableHeadByteLength(1);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);

        long myLength = 0;
        long protocolLength = 0;
        long jsonLength = 0;
        long xmlLength = 0;
        for (int i = 0; i < 10000; i++) {
            TestEntity testEntity = TestEntity.getRandomTestEntity(255.0);
            myLength += codecUtil.encode(testEntity).length;
            protocolLength += TestEntity.getProtocolEntityFromTestEntity(testEntity).toByteArray().length;
            jsonLength += new Gson().toJson(testEntity).getBytes().length;
            TestXmlEntity testXmlEntity = TestXmlEntity.TestXmlEntityFromTestEntity(testEntity);
            xmlLength += XmlUtil.convertToXml(testXmlEntity).getBytes().length;
        }

        System.out.println("myLength:" + myLength);
        System.out.println("protocolLength:" + protocolLength);
        System.out.println("jsonLength:" + jsonLength);
        System.out.println("xmlLength:" + xmlLength);
//        System.out.println("compressionRate:" + (double) myLength / protocolLength);
    }

    @Test
    public void testXorDouble() throws Exception {
        double d = 1.1111111;
        long ld = Double.doubleToLongBits(d);
        double c = 1.111111111;
        long lc = Double.doubleToLongBits(c);
        long ll = ld ^ lc;
        System.out.println(ll);
        System.out.println(Double.longBitsToDouble(ll));
        System.out.println(Double.longBitsToDouble(ll ^ ld));
        System.out.println(Double.longBitsToDouble(ll ^ lc));
    }

    @Test
    public void testBaseLine() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
//        protocolConfig.setEnableDoubleCompression(true);
//        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setVariableHeadByteLength(1);
        protocolConfig.setEnableBaseLineCompression(true);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0, 1);
        protocolConfig.setBaseLine(testEntity);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);

        TestEntity testEntity2 = TestEntity.getNextNearRandomTestEntity(testEntity);
        System.out.println(new Gson().toJson(testEntity2));
        byte[] bytes = codecUtil.encode(testEntity2);
        System.out.println(bytes.length);
        System.out.println(new Gson().toJson(codecUtil.decode(bytes, TestEntity.class)));

        ProtocolConfig protocolConfig2 = ProtocolConfig.defaultConfig();
//        protocolConfig.setEnableDoubleCompression(true);
//        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setVariableHeadByteLength(1);
        protocolConfig.setEnableBaseLineCompression(false);
        CodecUtil codecUtil2 = new CodecUtil(protocolConfig2);
        byte[] bytes2 = codecUtil2.encode(testEntity2);
        System.out.println(bytes2.length);
        System.out.println(new Gson().toJson(codecUtil2.decode(bytes2, TestEntity.class)));
    }

    @Test
    public void testCompressionRateWithBaseLineCompression() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableBaseLineCompression(true);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0, 7);
        protocolConfig.setBaseLine(testEntity);
        protocolConfig.setVariableHeadByteLength(1);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        Random random = new Random();

        long myLength = 0;
        long protocolLength = 0;
        long jsonLength = 0;
        long xmlLength = 0;
        for (int i = 0; i < 1000; i++) {
            TestEntity nextNearRandomTestEntity = TestEntity.getNextNearRandomTestEntity(testEntity);
            myLength += codecUtil.encode(nextNearRandomTestEntity).length;
            protocolLength += TestEntity.getProtocolEntityFromTestEntity(nextNearRandomTestEntity).toByteArray().length;
            jsonLength += new Gson().toJson(nextNearRandomTestEntity).getBytes().length;
            TestXmlEntity testXmlEntity = TestXmlEntity.TestXmlEntityFromTestEntity(nextNearRandomTestEntity);
            xmlLength += XmlUtil.convertToXml(testXmlEntity).getBytes().length;
        }

        System.out.println("myLength:" + myLength);
        System.out.println("protocolLength:" + protocolLength);
        System.out.println("jsonLength:" + jsonLength);
        System.out.println("xmlLength:" + xmlLength);
//        System.out.println("compressionRate:" + (double) myLength / protocolLength);
    }

    @Test
    public void testProtobufUtil() throws IllegalAccessException {
        long exactLength = 0;
        long myLength = 0;
        for (int i = 0; i < 100000; i++) {
            TestEntity testEntity = TestEntity.getRandomTestEntity();
            TestEntityOuterClass.TestEntity testEntityOuterClass = TestEntity.getProtocolEntityFromTestEntity(testEntity);
            exactLength += testEntityOuterClass.toByteArray().length;
            myLength += ProtobufCountUtil.countBytes(testEntity);
//            System.out.println(testEntityOuterClass.toByteArray().length + ":" +ProtobufUtil.countBytes(testEntity));
        }
        System.out.println( (double)exactLength / myLength);
    }

    @Test
    public void testCodec2() throws Exception {
        CodecUtil.registerClass(IotSimpleEntity.class);
        IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
        System.out.println(new Gson().toJson(iotSimpleEntity));
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        byte[] bytes = codecUtil.encode2(iotSimpleEntity);
        System.out.println(bytes.length);
        System.out.println(ProtobufCountUtil.countBytes(iotSimpleEntity));
        System.out.println(new Gson().toJson(codecUtil.decode2(bytes, IotSimpleEntity.class)));
    }

    @Test
    public void testCodec2WithDoubleCompression() throws Exception {
        CodecUtil.registerClass(IotSimpleEntity.class);
        IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity(255.0);
        System.out.println(new Gson().toJson(iotSimpleEntity));
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        byte[] bytes = codecUtil.encode2(iotSimpleEntity);
        System.out.println(bytes.length);
        System.out.println(ProtobufCountUtil.countBytes(iotSimpleEntity));
        System.out.println(new Gson().toJson(codecUtil.decode2(bytes, IotSimpleEntity.class)));
    }

    @Test
    public void testCodec2WithLombok() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0);
        System.out.println(new Gson().toJson(testEntity));
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        byte[] bytes = codecUtil.encode2(testEntity);
        System.out.println(bytes.length);
        System.out.println(ProtobufCountUtil.countBytes(testEntity));
        System.out.println(new Gson().toJson(codecUtil.decode2(bytes, TestEntity.class)));
    }

    @Test
    public void testCodec2WithBaseLine() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0);
        System.out.println(new Gson().toJson(testEntity));
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setBaseLine(testEntity);
        protocolConfig.setEnableBaseLineCompression(true);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        byte[] bytes = codecUtil.encode2(testEntity);
        System.out.println(bytes.length);
        System.out.println(ProtobufCountUtil.countBytes(testEntity));
        System.out.println(new Gson().toJson(codecUtil.decode2(bytes, TestEntity.class)));
    }

    @Test
    public void testEncode2() throws Exception {
//         codecUtil = new CodecUtil(ProtocolConfig.defaultConfig());
        CodecUtil codecUtil = FunctionMetricProxy.getProxy(CodecUtil.class);
        codecUtil.registerClass(IotSimpleEntity.class);
        for (int i=0;i < 100000;i++) {
            IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
            byte[] bytes = codecUtil.encode2(iotSimpleEntity);
        }
        FunctionMetricProxy.logDurations();
    }

    @Test
    public void testDecode2() throws Exception {
        CodecUtil codecUtil = FunctionMetricProxy.getProxy(CodecUtil.class);
        codecUtil.registerClass(IotSimpleEntity.class);
        IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
        byte[] bytes = new CodecUtil().encode2(iotSimpleEntity);
        for (int i=0;i < 100000;i++) {
            codecUtil.decode2(bytes, IotSimpleEntity.class);
        }
        FunctionMetricProxy.logDurations();
    }

    @Test
    public void testDecode3LongAndInt() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");

        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), 1).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), -1).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), 100000).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), -100000).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), 10000000).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), -10000000).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), Integer.MAX_VALUE).toByteArray())));
        System.out.println(codecUtil.decodeInt3(new ByteArrayInputStream(codecUtil.encodeInt3(new ByteArrayOutputStream(), Integer.MIN_VALUE).toByteArray())));

        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), 1).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), -1).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), 100000).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), -100000).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), 10000000).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), -10000000).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Integer.MAX_VALUE).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Long.MAX_VALUE).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Integer.MAX_VALUE * 1000L).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Integer.MAX_VALUE * 1000L).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Integer.MAX_VALUE * 1000000L).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Integer.MIN_VALUE * 1000L).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Long.MIN_VALUE).toByteArray())));
        System.out.println(codecUtil.decodeLong3(new ByteArrayInputStream(codecUtil.encodeLong3(new ByteArrayOutputStream(), Integer.MIN_VALUE * 1000000L).toByteArray())));

    }

    @Test
    public void testEncodeAndDecodeString() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
        System.out.println(codecUtil.decodeString3(new ByteArrayInputStream(codecUtil.encodeString3(new ByteArrayOutputStream(), "hello").toByteArray())));
        System.out.println(codecUtil.decodeString3(new ByteArrayInputStream(codecUtil.encodeString3(new ByteArrayOutputStream(), "hello world").toByteArray())));
        System.out.println(codecUtil.decodeString3(new ByteArrayInputStream(codecUtil.encodeString3(new ByteArrayOutputStream(), "").toByteArray())));
        System.out.println(codecUtil.decodeString3(new ByteArrayInputStream(codecUtil.encodeString3(new ByteArrayOutputStream(), "\t\n").toByteArray())));
        System.out.println(codecUtil.decodeString3(new ByteArrayInputStream(codecUtil.encodeString3(new ByteArrayOutputStream(), "0").toByteArray())));
        System.out.println(codecUtil.decodeString3(new ByteArrayInputStream(codecUtil.encodeString3(new ByteArrayOutputStream(), "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh").toByteArray())));
    }

    @Test
    public void testDecodeAndEncode3() throws Exception {
        CodecUtil codecUtil = new CodecUtil("");
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        System.out.println(new Gson().toJson(testEntity));
        byte[] bytes = codecUtil.encode3(testEntity);
        TestEntity testEntity2 = codecUtil.decode3(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
    }

    @Test
    public void testDecodeAndEncode3WithDoubleCompression() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0);
        System.out.println(new Gson().toJson(testEntity));
        byte[] bytes = codecUtil.encode3(testEntity);
        TestEntity testEntity2 = codecUtil.decode3(bytes, TestEntity.class);
        System.out.println(new Gson().toJson(testEntity2));
    }

    @Test
    public void DecodeAndEncode3WithBaseLine() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setEnableDoubleCompression(true);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0);
        protocolConfig.setBaseLine(testEntity);

        TestEntity testEntity2 = TestEntity.getNextNearRandomTestEntity(testEntity);
        System.out.println(new Gson().toJson(testEntity2));
        byte[] bytes = codecUtil.encode3(testEntity2);
        System.out.println(bytes.length);
        System.out.println(new Gson().toJson(codecUtil.decode3(bytes, TestEntity.class)));
    }

    @Test
    public void TestEncode3AndDecode2() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setDoubleCompressionAccuracy(2);
        protocolConfig.setEnableDoubleCompression(true);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        TestEntity testEntity = TestEntity.getRandomTestEntity(255.0);
        protocolConfig.setBaseLine(testEntity);

        TestEntity testEntity2 = TestEntity.getNextNearRandomTestEntity(testEntity);
        System.out.println(new Gson().toJson(testEntity2));
        byte[] bytes = codecUtil.encode3(testEntity2);
        System.out.println(new Gson().toJson(codecUtil.decode2(bytes, TestEntity.class)));
    }
}

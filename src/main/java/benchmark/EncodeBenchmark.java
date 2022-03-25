package benchmark;

import client.TestUdpClient;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import demo.TestEntity;
import demo.TestXmlEntity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import proto.TestEntityOuterClass;
import util.CodecUtil;
import util.ProtocolConfig;
import util.XmlUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/19 13:41
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class EncodeBenchmark {

    TestEntity entity;

    TestEntityOuterClass.TestEntity protoEntity;

    byte[] bytes;

    byte[] bytes2;

    byte[] bytes3;

    byte[] protoBytes;

    CodecUtil codecUtil;

    ThreadLocal<Gson> gson = ThreadLocal.withInitial(Gson::new);

    String json;

    String xml;

    TestXmlEntity xmlEntity;

    @Setup(Level.Trial)
    public void init() throws Exception {
        CodecUtil.registerClass(TestEntity.class);
        entity = TestEntity.getRandomTestEntity(255.0,7);
        protoEntity = getProtocolEntityFromTestEntity(entity);
        xmlEntity = TestXmlEntity.TestXmlEntityFromTestEntity(entity);
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setVariableHeadByteLength(1);
        protocolConfig.setEnableBaseLineCompression(true);
        protocolConfig.setBaseLine(entity);
        codecUtil = new CodecUtil(protocolConfig);
        bytes = codecUtil.encode(entity);
        bytes2 = codecUtil.encode2(entity);
        bytes3 = codecUtil.encode3(entity);
        protoBytes = protoEntity.toByteArray();

        json = gson.get().toJson(entity);
        xml = XmlUtil.convertToXml(xmlEntity);
    }

    @Benchmark
    public void protobufEncode(Blackhole bh) {
        bh.consume(protoEntity.toByteArray());
    }

    @Benchmark
    public void protobufDecode(Blackhole bh) throws InvalidProtocolBufferException {
        bh.consume(TestEntityOuterClass.TestEntity.parseFrom(protoBytes));
    }

    @Benchmark
    public void myEncode1(Blackhole bh) throws IllegalAccessException {
        bh.consume(codecUtil.encode(entity));
    }

    @Benchmark
    public void myDecode1(Blackhole bh) throws Exception {
        bh.consume(codecUtil.decode(bytes, TestEntity.class));
    }

    @Benchmark
    public void myEncode2(Blackhole bh) throws Exception {
        bh.consume(codecUtil.encode2(entity));
    }

    @Benchmark
    public void myDecode2(Blackhole bh) throws Exception {
        bh.consume(codecUtil.decode2(bytes2, TestEntity.class));
    }

    @Benchmark
    public void myEncode3(Blackhole bh) throws Exception {
        bh.consume(codecUtil.encode3(entity));
    }

    @Benchmark
    public void myDecode3(Blackhole bh) throws Exception {
        bh.consume(codecUtil.decode3(bytes3, TestEntity.class));
    }

    @Benchmark
    public void jsonEncode(Blackhole bh) {
        bh.consume(gson.get().toJson(entity));
    }

    @Benchmark
    public void jsonDecode(Blackhole bh) {
        bh.consume(gson.get().fromJson(json, TestEntity.class));
    }

    @Benchmark
    public void xmlEncode(Blackhole bh) throws Exception {
        bh.consume(XmlUtil.convertToXml(xmlEntity));
    }

    @Benchmark
    public void xmlDecode(Blackhole bh) throws Exception {
        bh.consume(XmlUtil.convertToJava(xml, TestXmlEntity.class));
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
//                .setLocalDate(testEntity.getLocalDate().toEpochDay())
                .setLocalDateTime(testEntity.getLocalDateTime())
                .build();
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EncodeBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("jmh-encode-decode.json")
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(30))
                .threads(12)
                .warmupForks(0)
                .warmupIterations(0)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}

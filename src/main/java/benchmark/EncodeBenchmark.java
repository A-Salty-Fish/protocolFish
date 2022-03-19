package benchmark;

import client.TestUdpClient;
import demo.TestEntity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import proto.TestEntityOuterClass;
import util.CodecUtil;

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

    @Setup(Level.Trial)
    public void init() throws InterruptedException {
        CodecUtil.registerClass(TestEntity.class);
        entity = getRandomTestEntity();
        protoEntity = getProtocolEntityFromTestEntity(entity);
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

    @Benchmark
    public void proto(Blackhole bh) {
        bh.consume(protoEntity.toByteArray());
    }

    @Benchmark
    public void test(Blackhole bh) throws IllegalAccessException {
        bh.consume(CodecUtil.encode(entity));
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

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EncodeBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("jmh-encode.json")
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(10))
                .threads(12)
                .warmupForks(0)
                .warmupIterations(0)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}

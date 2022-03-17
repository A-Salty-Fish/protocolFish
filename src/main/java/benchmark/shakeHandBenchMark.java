package benchmark;

import client.TestUdpClient;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/17 18:01
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class shakeHandBenchMark {

    @Benchmark
    public void test() throws InterruptedException {
        TestUdpClient.shakeHand();
    }

    @Setup(Level.Trial)
    public void init() throws InterruptedException {
        TestUdpClient.run();
    }

    @TearDown
    public void tearDown() {
        TestUdpClient.shutDown();
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(shakeHandBenchMark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(5))
                .threads(12)
                .warmupForks(0)
                .warmupIterations(0)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}

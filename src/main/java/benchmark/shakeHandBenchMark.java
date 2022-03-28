package benchmark;

import client.TestUdpClient;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import server.TestUdpServer;

import java.util.concurrent.TimeUnit;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/17 18:01
 */
@Slf4j
public class shakeHandBenchMark {

    public static void main(String[] args) throws Exception {
        TestUdpServer.run();
        TestUdpClient.run();

        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < 10000) {
            TestUdpClient.shakeHand();
            TestUdpClient.counts.incrementAndGet();
        }

        log.info("server counts:" + TestUdpServer.counts.longValue());
        log.info("client counts:" + TestUdpClient.counts.longValue());
        TestUdpClient.shutDown();
        TestUdpServer.shutDown();
    }
}

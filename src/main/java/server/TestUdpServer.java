package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author 13090
 * @version 1.0
 * @description: server
 * @date 2022/3/13 16:35
 */
@Slf4j
public class TestUdpServer {

    public static int serverPort = 7397;

    public static void main(String[] args) throws InterruptedException {
        run();
    }

    static EventLoopGroup group = new NioEventLoopGroup();

    static ChannelFuture channelFuture;

    public static Channel getChannel() throws Exception {
        return channelFuture.channel();
    }

    public static void run() throws InterruptedException {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
//                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
//                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                    .handler(new TestServerChannelInitializer());
            channelFuture = b.bind(serverPort).sync();
            log.info("server start done.");
//            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutDown() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        group.shutdownGracefully().await();
        group.shutdownNow();
        channelFuture.channel().closeFuture().sync();
        log.info("server shutdown done.");
//        throw new RuntimeException("server shutdown done.");
    }
}

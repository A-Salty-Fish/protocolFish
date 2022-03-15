package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

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
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
//                    .option(ChannelOption.SO_BROADCAST, true)
//                    .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
//                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                    .handler(new TestServerChannelInitializer());

            ChannelFuture f = b.bind(serverPort).sync();
            log.info("server start done.");
            f.channel().closeFuture().sync();
        } finally {
            //优雅的关闭释放内存
            group.shutdownGracefully();
        }

    }

}

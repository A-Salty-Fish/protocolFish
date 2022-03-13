import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/13 16:35
 */
@Slf4j
public class TestUdpClient {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .handler(new TestClientChannelInitializer());
            Channel ch = b.bind(7398).sync().channel();

            for (int i = 0; i < 10; i++) {
                ch.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer("test client", Charset.forName("GBK")),
                        new InetSocketAddress("127.0.0.1", 7397))).sync();
                TimeUnit.SECONDS.sleep(1);
            }
            ch.closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

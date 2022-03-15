package client;

import header.ShakeHandHeader;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import util.CodecUtil;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 13090
 * @version 1.0
 * @description: client
 * @date 2022/3/13 16:35
 */
@Slf4j
public class TestUdpClient {

    public static int serverPort = 7397;

    public static String serverHostName = "127.0.0.1";

    public static int clientPort = 7398;

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .handler(new TestClientChannelInitializer());
            Channel ch = b.bind(clientPort).sync().channel();
            log.info("client start down.");
            shakeHand(ch);
            ch.closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void shakeHand(Channel ch) throws InterruptedException {
        ByteBuf buf = ShakeHandHeader.getShakeHandHeader(ch);
        log.info(buf.toString());
        ch.writeAndFlush(new DatagramPacket(
                buf,
                new InetSocketAddress(serverHostName, serverPort))).sync();
    }
}

package client;

import handler.header.PlainBodyHeader;
import handler.header.ShakeHandHeader;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

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

    static EventLoopGroup group = new NioEventLoopGroup();

    static Channel channel;

    public static void main(String[] args) throws InterruptedException {
        run();
        for (int i = 0; i < 10; i++) {
            shakeHand();
        }
//        shutDown();
    }

    public static void run() throws InterruptedException {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .handler(new TestClientChannelInitializer());
            channel = b.bind(clientPort).await().channel();
            log.info("client start down.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutDown() {
        channel.close();
        group.shutdownGracefully();
        log.info("client shut down.");
    }

    public static void sendPlainBody() throws InterruptedException {
        channel.writeAndFlush(new DatagramPacket(
                PlainBodyHeader.getPlainBodyHeader(channel),
                new InetSocketAddress(serverHostName, serverPort))).sync();
    }

    public static void shakeHand() throws InterruptedException {
//        log.info("client shake hand.");
        ByteBuf buf = ShakeHandHeader.getShakeHandHeader(channel);
        channel.writeAndFlush(new DatagramPacket(
                buf,
                new InetSocketAddress(serverHostName, serverPort))).sync();
    }
}

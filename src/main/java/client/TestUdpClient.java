package client;

import com.google.gson.Gson;
import demo.TestEntity;
import handler.body.PlainBody;
import handler.header.PlainBodyHeader;
import handler.header.ShakeHandHeader;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import util.CodecUtil;
import util.ProtocolConfig;

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
        shakeHand();
        sendBody();
//        shutDown();
    }

    public static void run() throws InterruptedException {
        CodecUtil.registerClass(TestEntity.class);
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
                PlainBodyHeader.getEmptyPlainBodyHeader(channel),
                new InetSocketAddress(serverHostName, serverPort))).sync();
    }

    public static void shakeHand() throws InterruptedException {
        PlainBodyHeader.clearSequence();
        ByteBuf buf = ShakeHandHeader.getShakeHandHeader(channel, getClientProtocolConfig());
        channel.writeAndFlush(new DatagramPacket(
                buf,
                new InetSocketAddress(serverHostName, serverPort))).sync();
    }

    public static void sendBody() throws InterruptedException {
        ByteBuf bodyHeader = PlainBodyHeader.getPlainBodyHeader(channel, true, TestEntity.class);
        TestEntity testEntity = TestEntity.getRandomTestEntity();
        ByteBuf body = PlainBody.getPlainBodyFromObject(channel, testEntity, getClientProtocolConfig());
        log.info("send body: {}", new Gson().toJson(testEntity));
        ByteBuf buf = channel.alloc().buffer(bodyHeader.readableBytes() + body.readableBytes());
        buf.writeBytes(bodyHeader);
        buf.writeBytes(body);
        channel.writeAndFlush(new DatagramPacket(
                buf,
                new InetSocketAddress(serverHostName, serverPort))).sync();
    }

    public static ProtocolConfig getClientProtocolConfig() {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setVariableHeadByteLength(4);
        protocolConfig.setEnableBaseLineCompression(false);
        protocolConfig.setEnableDoubleCompression(false);
        protocolConfig.setDoubleCompressionAccuracy(12);
        return protocolConfig;
    }
}

package handler.header.handler;

import com.google.gson.Gson;
import handler.RequestHandler;
import handler.header.PlainHeader;
import handler.header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import server.TestUdpServer;
import util.CodecUtil;
import util.ProtocolConfig;
import util.ServerStoreConfig;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 13090
 * @version 1.0
 * @description: header handler for shake hand
 * @date 2022/3/15 15:23
 */
@Slf4j
public class ShakeHandHeaderHandler implements HeaderHandler {

    private boolean isClient = false;

    public ShakeHandHeaderHandler(boolean isClient) {
        this.isClient = isClient;
    }

    static final ConcurrentHashMap<InetSocketAddress, Boolean> InetSocketAddresses = new ConcurrentHashMap<>();

    static AtomicBoolean isShakeHand = new AtomicBoolean(false);

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf byteBuffer = packet.content();
        if (!ShakeHandHeader.validShakeHandHeader(byteBuffer)) {
            return new ExceptionHeaderHandler("shake hand header error");
        } else {
            handler(ctx, packet);
        }
        return null;
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
//        log.info(new Date() + ": shake hand success");
        if (!isClient) {
            int label = packet.content().readInt();
            ProtocolConfig protocolConfig = ProtocolConfig.getProtocolFromLabel(label);
            if (PlainHeader.isPlainBodyHead(label) && protocolConfig.getEnableBaseLineCompression()) {
                byte[] bytes = new byte[2];
                packet.content().readBytes(bytes);
                int classIdentity = (bytes[0] & 0xff) << 8 | (bytes[1] & 0xff) ;
                Class<?> clazz = CodecUtil.getClassByIdentity(classIdentity);
                if (clazz != null) {
                    byte[] body = new byte[packet.content().readableBytes()];
                    packet.content().readBytes(body);
                    protocolConfig.setEnableBaseLineCompression(false);
                    CodecUtil codecUtil = new CodecUtil(protocolConfig);
                    try {
                        Object baseLineObj = codecUtil.decode3(body, clazz);
                        protocolConfig.setBaseLine(baseLineObj);
                        protocolConfig.setEnableDoubleCompression(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            InetSocketAddress address = packet.sender();
            ServerStoreConfig.put(ServerStoreConfig.getKey(address.getAddress().getHostAddress()), protocolConfig);
            log.info(address.getAddress().getHostAddress() + " protocol:" + new Gson().toJson(protocolConfig));;
            InetSocketAddresses.put(packet.sender(), true);
            DatagramPacket data = new DatagramPacket(ShakeHandHeader.getShakeHandHeader(ctx.channel()), packet.sender());
            ctx.writeAndFlush(data);
            log.info(new Date() + ":server shake hand success");
            TestUdpServer.counts.incrementAndGet();
        } else {
            isShakeHand.set(true);
            log.info(new Date() + ":client shake hand success");
        }
    }
}

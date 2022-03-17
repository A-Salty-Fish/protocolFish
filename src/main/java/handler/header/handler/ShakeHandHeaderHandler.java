package handler.header.handler;

import handler.RequestHandler;
import handler.header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 13090
 * @version 1.0
 * @description: header handler for shake hand
 * @date 2022/3/15 15:23
 */
@Slf4j
public class ShakeHandHeaderHandler implements HeaderHandler {

    static AtomicLong atomicLong = new AtomicLong(0L);

    private boolean isClient;

    public ShakeHandHeaderHandler(boolean isClient) {
        this.isClient = isClient;
    }

    static final ConcurrentHashMap<InetSocketAddress, Boolean> InetSocketAddresses = new ConcurrentHashMap<>();

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
            InetSocketAddress address = packet.sender();
            if (!InetSocketAddresses.containsKey(address)) {
                InetSocketAddresses.put(packet.sender(), true);
                DatagramPacket data = new DatagramPacket(ShakeHandHeader.getShakeHandHeader(ctx.channel()), packet.sender());
                ctx.writeAndFlush(data);
            }
            atomicLong.incrementAndGet();
            log.info(new Date() + ":shake hand success, total count: " + atomicLong.get());
        }

    }
}

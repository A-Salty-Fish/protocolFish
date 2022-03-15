package header.handler;

import header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/15 15:23
 */
@Slf4j
public class ShakeHandHeaderHandler implements HeaderHandler {

    private boolean isClient;

    public ShakeHandHeaderHandler(boolean isClient) {
        this.isClient = isClient;
    }

    static final ConcurrentHashMap<InetSocketAddress, Boolean> InetSocketAddresses = new ConcurrentHashMap<>();

    @Override
    public HeaderHandler getNextHeadHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf byteBuffer = packet.content();
        if (byteBuffer.readableBytes() != ShakeHandHeader.length - 1) {
            return new ExceptionHeaderHandler("shake hand header length error");
        } else {
            if (byteBuffer.readInt() != ShakeHandHeader.magicNum) {
                return new ExceptionHeaderHandler("shake hand header magic num error");
            }
            handler(ctx, packet);
        }
        return null;
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        log.info(new Date() + ": shake hand success");
        if (!isClient) {
            InetSocketAddress address = packet.sender();
            if (!InetSocketAddresses.containsKey(address)) {
                InetSocketAddresses.put(packet.sender(), true);
                DatagramPacket data = new DatagramPacket(ShakeHandHeader.getShakeHandHeader(ctx.channel()), packet.sender());
                ctx.writeAndFlush(data);
            }
        }
    }
}

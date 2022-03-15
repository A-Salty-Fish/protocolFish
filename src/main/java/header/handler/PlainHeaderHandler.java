package header.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author 13090
 * @version 1.0
 * @description: header handler for plainHeader
 * @date 2022/3/15 15:20
 */

public class PlainHeaderHandler implements HeaderHandler {

    @Override
    public HeaderHandler getNextHeadHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf byteBuffer = packet.content();
        if (byteBuffer.getBoolean(0)) {
            handler(ctx, packet);
            return new ShakeHandHeaderHandler();
        } else {
            return null;
        }
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        packet.content().readBoolean();
    }
}

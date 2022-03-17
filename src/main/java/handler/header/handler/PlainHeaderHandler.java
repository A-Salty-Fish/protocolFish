package handler.header.handler;

import handler.RequestHandler;
import handler.header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author 13090
 * @version 1.0
 * @description: header handler for plainHeader
 * @date 2022/3/15 15:20
 */

public class PlainHeaderHandler implements HeaderHandler {

    boolean isClient;

    public PlainHeaderHandler(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf byteBuffer = packet.content();
        if (ShakeHandHeader.isShakeHandHeader(byteBuffer)) {
            handler(ctx, packet);
            return new ShakeHandHeaderHandler(isClient);
        } else {
            return new PlainBodyHeaderHandler();
        }
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
//        packet.content().readBoolean();
    }
}

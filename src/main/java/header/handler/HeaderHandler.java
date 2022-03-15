package header.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public interface HeaderHandler {
    /**
     * get next header handler
     * @return
     */
    public HeaderHandler getNextHeadHandler(ChannelHandlerContext ctx, DatagramPacket packet);

    /**
     * handle this part of header
     * @param byteBuffer
     */
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet);
}

package util;

import handler.header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 16:44
 */

public class DatagramUtil {
    public static void sendMsg(ChannelHandlerContext ctx, DatagramPacket packet, ByteBuf byteBuf) {
        DatagramPacket data = new DatagramPacket(byteBuf, packet.sender());
        ctx.writeAndFlush(data);
    }
}

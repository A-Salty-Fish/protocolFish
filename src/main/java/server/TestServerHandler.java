package server;

import handler.RequestHandler;
import handler.header.handler.HeaderHandler;
import handler.header.handler.PlainHeaderHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 13090
 * @version 1.0
 * @description: server handler
 * @date 2022/3/13 16:39
 */
@Slf4j
public class TestServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        RequestHandler headerHandler = new PlainHeaderHandler(false);
        while (headerHandler != null) {
            headerHandler = headerHandler.getNextHandler(ctx, packet);
        }
    }

}
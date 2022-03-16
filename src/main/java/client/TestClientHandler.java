package client;

import handler.RequestHandler;
import handler.header.handler.ExceptionHeaderHandler;
import handler.header.handler.HeaderHandler;
import handler.header.handler.PlainHeaderHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 13090
 * @version 1.0
 * @description: client handler
 * @date 2022/3/13 16:42
 */
@Slf4j
public class TestClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    AtomicBoolean shakeHand = new AtomicBoolean(false);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        RequestHandler headerHandler = new PlainHeaderHandler(true);
        boolean hasException = false;
        while (headerHandler != null) {
            headerHandler = headerHandler.getNextHandler(ctx, packet);
            if (headerHandler instanceof ExceptionHeaderHandler) {
                hasException = true;
            }
        }
        if (!hasException) {
            shakeHand.set(true);
        } else {
            shakeHand.set(false);
        }
    }

}

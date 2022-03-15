package client;

import header.ShakeHandHeader;
import header.handler.ExceptionHeaderHandler;
import header.handler.HeaderHandler;
import header.handler.PlainHeaderHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/13 16:42
 */
@Slf4j
public class TestClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    AtomicBoolean shakeHand = new AtomicBoolean(false);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        HeaderHandler headerHandler = new PlainHeaderHandler();
        boolean hasException = false;
        while (headerHandler != null) {
            headerHandler = headerHandler.getNextHeadHandler(ctx, packet);
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

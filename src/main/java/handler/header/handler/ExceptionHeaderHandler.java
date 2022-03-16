package handler.header.handler;

import handler.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author 13090
 * @version 1.0
 * @description: exception handler for header
 * @date 2022/3/15 15:29
 */
@Slf4j
public class ExceptionHeaderHandler implements HeaderHandler {

    private final String errorMessage;

    public ExceptionHeaderHandler(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        handler(ctx, packet);
        return null;
    }

    @Override
    public void handler(ChannelHandlerContext channel, DatagramPacket packet) {
        log.error("{}: error message:{}, byteBuffer: {}", new Date(), errorMessage, packet.content().toString());
    }
}

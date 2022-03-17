package handler.body.handler;

import handler.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/16 11:03
 */
@Slf4j
public class PlainBodyHandler implements RequestHandler {
    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        log.info("handle plain body");
        return null;
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        return;
    }
}

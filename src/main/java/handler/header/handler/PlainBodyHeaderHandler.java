package handler.header.handler;

import handler.RequestHandler;
import handler.body.handler.PlainBodyHandler;
import handler.header.PlainBodyHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author 13090
 * @version 1.0
 * @description: handler for plain body header
 * @date 2022/3/16 11:00
 */
@Slf4j
public class PlainBodyHeaderHandler implements HeaderHandler {

    boolean isClient;

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        if (PlainBodyHeader.isPlainBodyHeader(packet.content())) {
            log.info(new Date() + ": " + "plain body header");
            return new PlainBodyHandler();
        } else {
            return null;
        }
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        packet.content().readInt();
    }
}

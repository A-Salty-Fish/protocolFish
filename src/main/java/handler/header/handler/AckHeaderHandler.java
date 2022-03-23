package handler.header.handler;

import handler.RequestHandler;
import handler.header.AckHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 16:27
 */
@Slf4j
public class AckHeaderHandler implements HeaderHandler {

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        handler(ctx, packet);
        return null;
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        int label = packet.content().readInt();
        log.info("accept ack，sequence number = {}", AckHeader.getSequenceNumberFromLabel(label));
    }
}

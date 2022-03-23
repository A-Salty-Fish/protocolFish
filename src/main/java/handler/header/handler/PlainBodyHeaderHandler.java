package handler.header.handler;

import handler.RequestHandler;
import handler.body.handler.PlainBodyHandler;
import handler.header.AckHeader;
import handler.header.PlainBodyHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import util.CodecUtil;
import util.DatagramUtil;

import java.util.Date;

/**
 * @author 13090
 * @version 1.0
 * @description: handler for plain body header
 * @date 2022/3/16 11:00
 */
@Slf4j
public class PlainBodyHeaderHandler implements HeaderHandler {

    Boolean isClient;

    Class<?> entityClass;

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        log.info(new Date() + ": " + "plain body header");
        handler(ctx, packet);
        return new PlainBodyHandler(entityClass);
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        int label = packet.content().readInt();
        int identity = PlainBodyHeader.getClassIdentityFromLabel(label);
        entityClass = CodecUtil.getClassByIdentity(identity);
        if (PlainBodyHeader.needAck(label)) {
            int sequenceNumber = PlainBodyHeader.getSequenceNumberFromLabel(label);
            ByteBuf ackBuf = AckHeader.getAckHeader(ctx.channel(), sequenceNumber);
            DatagramUtil.sendMsg(ctx, packet, ackBuf);
        }
    }
}

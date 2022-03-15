package server;

import header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/13 16:39
 */
@Slf4j
public class TestServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf byteBuf = packet.content();
        if (packet.content().getBoolean(0)) {
            if (ShakeHandHeader.isShakeHandHeader(byteBuf)) {
                log.info("shake hand success:" + new Date());
                DatagramPacket data = new DatagramPacket(ShakeHandHeader.getShakeHandHeader(ctx.channel()), packet.sender());
                ctx.writeAndFlush(data);
            } else {
                log.info("shake hand fail");
            }
        }
//        String msg = CodecUtil.deCode(packet.content());
//        log.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " UDP server receive：" + msg);
//        String json = "server reply";
//        byte[] bytes = json.getBytes(Charset.forName("GBK"));
//        DatagramPacket data = new DatagramPacket(Unpooled.copiedBuffer(bytes), packet.sender());
//        ctx.writeAndFlush(data);
    }

}
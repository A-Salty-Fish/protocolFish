package handler;

import handler.header.handler.HeaderHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/16 10:37
 */

public interface RequestHandler {
    /**
     * get next Handler
     * @param ctx
     * @param packet
     * @return
     */
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet);

    /**
     * handle body
     * @param ctx
     * @param packet
     */
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet);
}

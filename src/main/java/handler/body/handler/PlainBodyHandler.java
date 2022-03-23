package handler.body.handler;

import com.google.gson.Gson;
import handler.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import util.CodecUtil;
import util.ProtocolConfig;
import util.ServerStoreConfig;

import java.net.InetSocketAddress;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/16 11:03
 */
@Slf4j
public class PlainBodyHandler implements RequestHandler {

    Class<?> entityClass;

    public PlainBodyHandler(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public RequestHandler getNextHandler(ChannelHandlerContext ctx, DatagramPacket packet) {
        handler(ctx, packet);
        return null;
    }

    @Override
    public void handler(ChannelHandlerContext ctx, DatagramPacket packet) {
        byte[] body = packet.content().readBytes(packet.content().readableBytes()).array();
        InetSocketAddress address = packet.sender();
        ProtocolConfig protocolConfig = ServerStoreConfig.get(ServerStoreConfig.getKey(address.getAddress().getHostAddress()));
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        try {
            Object entity = codecUtil.decode(body, entityClass);
            log.info("收到消息实体：{}", new Gson().toJson(entity));
        } catch (Exception e) {
            log.error("解析数据异常:", e);
        }
    }
}

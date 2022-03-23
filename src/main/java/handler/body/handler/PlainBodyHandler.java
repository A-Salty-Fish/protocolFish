package handler.body.handler;

import com.google.gson.Gson;
import handler.RequestHandler;
import handler.body.PlainBody;
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
        InetSocketAddress address = packet.sender();
        ProtocolConfig protocolConfig = ServerStoreConfig.get(ServerStoreConfig.getKey(address.getAddress().getHostAddress()));
        Object obj = PlainBody.getObjectFromPlainBody(packet.content(), protocolConfig, entityClass);
        log.info("accept data：{}", new Gson().toJson(obj));
    }
}

package handler.body;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import util.CodecUtil;
import util.ProtocolConfig;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/15 14:26
 */
@Slf4j
public class PlainBody {
    public static ByteBuf getPlainBodyFromObject(Channel ch, Object entity, ProtocolConfig protocolConfig) {
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        try {
            byte[] bytes = codecUtil.encode(entity);
            ByteBuf byteBuf = ch.alloc().buffer(bytes.length);
            byteBuf.writeBytes(bytes);
            return byteBuf;
        } catch (Exception e) {
            log.error("encode error: {}", e.getMessage());
        }
        return null;
    }

    public static Object getObjectFromPlainBody(ByteBuf byteBuf, ProtocolConfig protocolConfig, Class<?> entityClass) {
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        int bytesNum = byteBuf.readableBytes();
        byte[] bytes = new byte[bytesNum];
        byteBuf.readBytes(bytes);
        try {
            return codecUtil.decode(bytes, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("decode error: {}", e.getMessage());
        }
        return null;
    }

}

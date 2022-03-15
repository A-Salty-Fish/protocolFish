package util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/14 16:28
 */
@Slf4j
public class CodecUtil {

    public final static int MAGIC_NUM = 0x114514;

    public static ByteBuf encode(String msg) {
        log.info("row msg:{}", msg);
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer(4 + 4 + bytes.length);
        buf.writeInt(MAGIC_NUM);
        buf.writeInt(msg.length());
        buf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        log.info("encoded msg:{}", buf);
        return buf;
    }

    public static String deCode(ByteBuf byteBuf) {
        log.info("decode msg:{}", byteBuf.toString());
        int magicNum = byteBuf.readInt();
        if (magicNum != MAGIC_NUM) {
            return null;
        }
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        String msg = new String(bytes, StandardCharsets.UTF_8);
        log.info("row msg:{}", msg);
        return msg;
    }

}

package header;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author 13090
 * @version 1.0
 * @description: header for shake hand
 * @date 2022/3/15 14:34
 */

public class ShakeHandHeader extends PlainHeader {

    public static int magicNum = 0x114514;

    public static int length = 5;

    public static ByteBuf getShakeHandHeader(Channel ch) {
        ByteBuf byteBuf = ch.alloc().buffer(length);
        byteBuf.writeBoolean(true);
        byteBuf.writeInt(magicNum);
        return byteBuf;
    }

    public static boolean isShakeHandHeader(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() != length) {
            return false;
        }
        boolean flag = byteBuf.readBoolean();
        int magicNum = byteBuf.readInt();
        return flag && (magicNum == ShakeHandHeader.magicNum);
    }
}

package handler.header;

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

    public static int length = 4;

    public static ByteBuf getShakeHandHeader(Channel ch) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        byteBuf.writeInt(magicNum | (1 << LabelPosition.IS_SHAKE_HAND_HEAD.value()));
        return byteBuf;
    }

    public static boolean isShakeHandHeader(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() != length) {
            return false;
        }
        int magicNum = byteBuf.getInt(0);
        return (magicNum & (1 << LabelPosition.IS_SHAKE_HAND_HEAD.value())) != 0;
    }

    public static boolean validShakeHandHeader(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() != length) {
            return false;
        }
        int magicNum = byteBuf.readInt();
//        boolean isShakeHand = (magicNum & (1 << 31)) != 0;
        magicNum = magicNum & ~(1 << LabelPosition.IS_SHAKE_HAND_HEAD.value());
        return magicNum == ShakeHandHeader.magicNum;
    }
}

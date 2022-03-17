package handler.header;

import handler.header.PlainHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/16 11:04
 */

public class PlainBodyHeader extends PlainHeader {

    public static int length = 4;

    public static ByteBuf getPlainBodyHeader(Channel ch) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        byteBuf.writeInt(1 << LabelPosition.IS_PLAIN_BODY_HEAD.value());
        return byteBuf;
    }

    public static boolean isPlainBodyHeader(ByteBuf byteBuf) {
        return (byteBuf.getInt(0) & (1 << LabelPosition.IS_PLAIN_BODY_HEAD.value())) != 0;
    }

}

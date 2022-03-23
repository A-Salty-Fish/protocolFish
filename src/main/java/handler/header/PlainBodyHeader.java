package handler.header;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import util.ByteUtil;
import util.CodecUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/16 11:04
 */

public class PlainBodyHeader extends PlainHeader {

    public static int length = 4;

    static int count = 0;

    public static ByteBuf getEmptyPlainBodyHeader(Channel ch) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        byteBuf.writeInt(1 << PlainHeader.LabelPosition.IS_PLAIN_BODY_HEAD.value());
        return byteBuf;
    }

    public static ByteBuf getPlainBodyHeader(Channel ch) {
        return getPlainBodyHeader(ch, true);
    }

    public static ByteBuf getPlainBodyHeader(Channel ch, boolean needAck) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        int plainBodyLabel = 0;
        plainBodyLabel |= 1 << PlainHeader.LabelPosition.IS_PLAIN_BODY_HEAD.value();
        if (needAck) {
            plainBodyLabel |= 1 << PlainBodyLabelPosition.NEED_ACK.value();
            plainBodyLabel |= getSequenceNumber();
        }
        byteBuf.writeInt(plainBodyLabel);
        return byteBuf;
    }

    public static ByteBuf getPlainBodyHeader(Channel ch, boolean needAck, Class<?> entity) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        int plainBodyLabel = 0;
        plainBodyLabel |= 1 << PlainHeader.LabelPosition.IS_PLAIN_BODY_HEAD.value();
        if (needAck) {
            plainBodyLabel |= 1 << PlainBodyLabelPosition.NEED_ACK.value();
            plainBodyLabel |= getSequenceNumber();
        }
        Integer identity = CodecUtil.getIdentityByClass(entity);
        if (identity == null) {
            identity = 0;
        }
        plainBodyLabel |= identity << PlainBodyLabelPosition.SEQUENCE_NUMBER.value();
        byteBuf.writeInt(plainBodyLabel);
        return byteBuf;
    }

    public static boolean isPlainBodyHeader(ByteBuf byteBuf) {
        return (byteBuf.getInt(0) & (1 << PlainHeader.LabelPosition.IS_PLAIN_BODY_HEAD.value())) != 0;
    }

    public static boolean needAck(int plainBodyLabel) {
//        int plainBodyLabel = byteBuf.getInt(0);
        return (plainBodyLabel & (1 << PlainBodyLabelPosition.NEED_ACK.value())) != 0;
    }

    public static int getSequenceNumberFromLabel(int label) {
        return label & ByteUtil.getMask(PlainBodyLabelPosition.SEQUENCE_NUMBER.value());
    }

    public synchronized static int getSequenceNumber() {
        if (count == Short.MAX_VALUE) {
            count = 0;
            return 0;
        } else {
            return count++;
        }
    }

    public static int getClassIdentityFromLabel(int label) {
        return (label >> PlainBodyLabelPosition.SEQUENCE_NUMBER.value()) & ByteUtil.getMask(PlainBodyLabelPosition.CLASS_IDENTITY.value());
    }

    public static synchronized void clearSequence() {
        count = 0;
    }

    public enum PlainBodyLabelPosition {

        /**
         * 1 bit: check if the request is plain body
         */
        IS_PLAIN_BODY_HEAD(30),

        /**
         * 1 bit: check if the request need ack
         */
        NEED_ACK(29),

        /**
         * 10 bits: class Identity number
         */
        CLASS_IDENTITY(10),

        /**
         * 16 bit : sequence number
         */
        SEQUENCE_NUMBER(16),
        ;

        private int value = 0;

        private PlainBodyLabelPosition(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

}

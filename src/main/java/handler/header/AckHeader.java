package handler.header;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import util.ByteUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 14:05
 */

public class AckHeader {

    static final ConcurrentHashMap<String, Integer> sequenceNumberMap = new ConcurrentHashMap<String, Integer>();

    public final static int length = 4;

    public static ByteBuf getAckHeader(Channel ch, int sequenceNumber) {
        int ackLabel = 0;
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        ackLabel |= 1 << AckLabelPosition.IS_PLAIN_BODY_ACK.value();
        ackLabel |= (sequenceNumber & ByteUtil.getMask(AckLabelPosition.SEQUENCE_NUMBER.value()));
        byteBuf.writeInt(ackLabel);
        return byteBuf;
    }

    public static int getSequenceNumberFromLabel(int label) {
        return label & ByteUtil.getMask(AckLabelPosition.SEQUENCE_NUMBER.value());
    }

    enum AckLabelPosition {

        /**
         * 1 bit: ack
         */
        IS_PLAIN_BODY_ACK(29),

        /**
         * 16 bit : sequence number
         */
        SEQUENCE_NUMBER(16),
        ;
        ;
        private int value = 0;

        private AckLabelPosition(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}

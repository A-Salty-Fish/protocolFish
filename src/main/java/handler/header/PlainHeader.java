package handler.header;

/**
 * @author 13090
 * @version 1.0
 * @description: public header
 * @date 2022/3/15 14:24
 */

public class PlainHeader {
    /**
     * first 1 bit : check if the request is for shaking hand
     * middle remain 14 bits : reserved
     * 17 bit left : magic number 11011111101010010
     */
    public int label;

    public Integer entityId;

    public Object baseEntity;

    public enum LabelPosition {

        /**
         * first 1 bit : check if the request is for shaking hand
         */
        IS_SHAKE_HAND_HEAD(31),

        /**
         * 1 bit: check if the request is plain body
         */
        IS_PLAIN_BODY_HEAD(30),

        /**
         * 1 bit: ack
         */
        IS_PLAIN_BODY_ACK(29),

        /**
         * 1 bit: double compression enable
         */
        ENABLE_DOUBLE_COMPRESSION(28),

        /**
         * 4 bit: double compression accuracy
         */
        DOUBLE_COMPRESSION_ACCURACY(24),

        /**
         * 2 bit : variable byte length
         */
        VARIABLE_BYTE_LENGTH(22),

        /**
         * 3 bit : charset
         */
        CHARSET(19),

        /**
         * 1 bit : time compressed enable
         */
        ENABLE_BASELINE_COMPRESSION(18),

        /**
         * 17 bit : magic number for handshake 11011111101010010
         */
        MAGIC_NUMBER_FOR_HANDSHAKE(17)
        ;

        private int value = 0;

        private LabelPosition(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public static boolean isShakeHandHead(int label) {
        return (label & (1 << LabelPosition.IS_SHAKE_HAND_HEAD.value())) != 0;
    }

    public static boolean isPlainBodyHead(int label) {
        return (label & (1 << LabelPosition.IS_PLAIN_BODY_HEAD.value())) != 0;
    }
}

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

    public enum LabelPosition {

        /**
         * first 1 bit : check if the request is for shaking hand
         */
        IS_SHAKE_HAND_HEAD(31),

        /**
         * 1 bit: check if the request is plain body
         */
        IS_PLAIN_BODY_HEAD(30),
        ;

        private int value = 0;

        private LabelPosition(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
        }
}

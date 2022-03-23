package util;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 15:10
 */

public class ByteUtil {
    public static int getMask(int i) {
        switch (i) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 7;
            case 4:
                return 0xf;
            case 5:
                return 0x1f;
            case 6:
                return 0x3f;
            case 7:
                return 0x7f;
            case 8:
                return 0xff;
            case 9:
                return 0x1ff;
            case 10:
                return 0x3ff;
            case 11:
                return 0x7ff;
            case 12:
                return 0xfff;
            case 13:
                return 0x1fff;
            case 14:
                return 0x3fff;
            case 15:
                return 0x7fff;
            case 16:
                return 0xffff;
            default:
                return 0;
        }
    }
}

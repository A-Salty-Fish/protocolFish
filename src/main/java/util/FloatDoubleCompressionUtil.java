package util;

import java.io.ByteArrayOutputStream;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/29 14:41
 */

public class FloatDoubleCompressionUtil {

    public static byte[] convertDoubleToBytes(double d) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(8);
        long l = Double.doubleToLongBits(d);
        long tl = l;
        int bitPos = 1;
        for (int i = 0; i < 64; i++) {
            if ((tl & 1) == 0) {
                tl >>= 1;
            } else {
                bitPos = i + 1;
                break;
            }
        }
        int bytePos = (bitPos - 1) / 8;
        for (int i = 7; i >= bytePos; i--) {
            bytes.write((int) (l >>> (i * 8)));
        }
        return bytes.toByteArray();
    }

    public static double convertBytesToDouble(byte[] bytes) {
        long l = 0;
        int bytesPos = 0;
        for (int i = 7; bytesPos < bytes.length; i--,bytesPos++) {
            l |= ((long) bytes[bytesPos] & 0xff) << (i * 8);
        }
        return Double.longBitsToDouble(l);
    }

    public static byte[] convertFloatToBytes(float f) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(4);
        int l = Float.floatToIntBits(f);
        int tl = l;
        int bitPos = 1;
        for (int i = 0; i < 32; i++) {
            if ((tl & 1) == 0) {
                tl >>= 1;
            } else {
                bitPos = i + 1;
                break;
            }
        }
        int bytePos = (bitPos - 1) / 8;
        for (int i = 3; i >= bytePos; i--) {
            bytes.write(l >>> (i * 8));
        }
        return bytes.toByteArray();
    }

    public static float convertBytesToFloat(byte[] bytes) {
        int l = 0;
        int bytesPos = 0;
        for (int i = 3; bytesPos < bytes.length; i--,bytesPos++) {
            l |= (bytes[bytesPos] & 0xff) << (i * 8);
        }
        return Float.intBitsToFloat(l);
    }

    public static long compressDoubleToLong(double value, int accuracy) {
        int power = (int) Math.pow(10, accuracy);
        if (Double.MAX_VALUE / power < value) {
            return Long.MAX_VALUE;
        }
        return Math.round(value * power);
    }



}

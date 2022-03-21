package util;

import handler.header.PlainHeader;
import lombok.Builder;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/19 17:06
 */
@Data
@Builder
public class ProtocolConfig {

    /**
     * use double compression with loss result, need 1 bit;
     */
    private Boolean enableDoubleCompression;

    /**
     * The number of decimal places to round to when converting to and from, need 4 bit;
     */
    private int doubleCompressionAccuracy;

    /**
     * the head length for the string, need 2 bit; the max length is 4, min is 1;
     */
    private int variableHeadByteLength;

    /**
     * the charset for the string, need 3 bit;
     */
    private Charset charset;

    /**
     * use time compression without lose, need 1 bit
     */
    private Boolean enableTimeCompression;

    /**
     * for the time compression, the base timeline, need 64 bit;
     */
    private Long timeCompressionBaseline;

    public static ProtocolConfig defaultConfig() {
        return ProtocolConfig.builder()
                .enableDoubleCompression(false)
                .doubleCompressionAccuracy(10)
                .enableTimeCompression(false)
                .timeCompressionBaseline(0L)
                .variableHeadByteLength(2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

    public static Charset convertByteToCharset(byte b) {
        if (b == 1) {
            return StandardCharsets.UTF_8;
        } else if (b == 2) {
            return StandardCharsets.UTF_16;
        } else if (b == 3) {
            return StandardCharsets.UTF_16BE;
        } else if (b == 4) {
            return StandardCharsets.UTF_16LE;
        } else if (b == 5) {
            return StandardCharsets.ISO_8859_1;
        } else if (b == 6) {
            return StandardCharsets.US_ASCII;
        }
        return StandardCharsets.UTF_8;
    }

    public static byte convertCharSetToByte(Charset charset) {
        if (charset.equals(StandardCharsets.UTF_8)) {
            return 1;
        } else if (charset.equals(StandardCharsets.UTF_16)) {
            return 2;
        } else if (charset.equals(StandardCharsets.UTF_16BE)) {
            return 3;
        } else if (charset.equals(StandardCharsets.UTF_16LE)) {
            return 4;
        } else if (charset.equals(StandardCharsets.ISO_8859_1)) {
            return 5;
        } else if (charset.equals(StandardCharsets.US_ASCII)) {
            return 6;
        }
        return 1;
    }

    public static ProtocolConfig getProtocolFromLabel(int label) {
        ProtocolConfig config = ProtocolConfig.defaultConfig();
        config.setEnableDoubleCompression(((label >> PlainHeader.LabelPosition.ENABLE_DOUBLE_COMPRESSION.value()) & 1) != 0);
        config.setDoubleCompressionAccuracy((label >> PlainHeader.LabelPosition.DOUBLE_COMPRESSION_ACCURACY.value()) & 0x0f);
        config.setVariableHeadByteLength(((label >> PlainHeader.LabelPosition.VARIABLE_BYTE_LENGTH.value()) & 0x03) + 1);
        config.setCharset(convertByteToCharset((byte) ((label >> PlainHeader.LabelPosition.CHARSET.value()) & 0x07)));
        config.setEnableTimeCompression(((label >> PlainHeader.LabelPosition.ENABLE_TIME_COMPRESSION.value()) & 1) != 0);
        return config;
    }
}

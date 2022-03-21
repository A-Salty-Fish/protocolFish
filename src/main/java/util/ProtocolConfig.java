package util;

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
    private Byte doubleCompressionAccuracy;

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
                .doubleCompressionAccuracy((byte) 3)
                .enableTimeCompression(false)
                .timeCompressionBaseline(0L)
                .variableHeadByteLength(2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

}

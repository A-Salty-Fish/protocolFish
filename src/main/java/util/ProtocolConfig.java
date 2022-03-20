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
     * use double compression with loss result
     */
    private Boolean enableDoubleCompression;

    /**
     * The number of decimal places to round to when converting to and from
     */
    private Byte doubleCompressionAccuracy;

    /**
     * constrain the head length for string arg
     */
    private Byte stringHeadLength;

    /**
     * use time compression without lose
     */
    private Boolean enableTimeCompression;

    /**
     * for the time compression, the base time line
     */
    private Long timeCompressionBaseline;

    /**
     * the head length for the string
     */
    private int variableHeadByteLength;

    /**
     * the charset for the string
     */
    private Charset charset;

    public static ProtocolConfig defaultConfig() {
        return ProtocolConfig.builder()
                .enableDoubleCompression(false)
                .doubleCompressionAccuracy((byte) 3)
                .stringHeadLength((byte) 2)
                .enableTimeCompression(false)
                .timeCompressionBaseline(0L)
                .variableHeadByteLength(2)
                .charset(StandardCharsets.UTF_8)
                .build();
    }

}

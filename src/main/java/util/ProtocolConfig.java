package util;

import lombok.Builder;
import lombok.Data;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/19 17:06
 */
@Data
@Builder(setterPrefix = "set")
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

}

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
     * constrain the max size of string args
     */
    private Boolean stringHeadLength;

    /**
     * use time compression without lose
     */
    private Boolean enableTimeCompression;

    /**
     * for the time compression, the base time line
     */
    private Long timeCompressionBaseline;

}

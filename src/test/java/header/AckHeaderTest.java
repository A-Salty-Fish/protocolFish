package header;

import handler.header.AckHeader;
import handler.header.PlainBodyHeader;
import io.netty.buffer.ByteBuf;
import org.junit.Before;
import org.junit.Test;
import server.TestUdpServer;
import util.ByteUtil;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 16:12
 */

public class AckHeaderTest {

    @Before
    public void setUp() throws Exception {
        TestUdpServer.run();
    }

    @Test
    public void testAckHeader() throws Exception {
        for (int i = 0; i < 20; i++) {
            ByteBuf bodyBuf = PlainBodyHeader.getPlainBodyHeader(TestUdpServer.getChannel());
            int bodyLabel = bodyBuf.readInt();
            System.out.println("body head:\t"+ ByteUtil.convertIntegerToBytesString(bodyLabel));
            int sequenceNumber = PlainBodyHeader.getSequenceNumberFromLabel(bodyLabel);
            ByteBuf ackBuf = AckHeader.getAckHeader(TestUdpServer.getChannel(), sequenceNumber);
            int ackLabel = ackBuf.readInt();
            System.out.println("ack head:\t"+ ByteUtil.convertIntegerToBytesString(ackLabel));
            int ackSequenceNumber = AckHeader.getSequenceNumberFromLabel(ackLabel);
            System.out.println("head sequence number:" + sequenceNumber + "\tack sequence number:" + ackSequenceNumber);
        }
    }
}

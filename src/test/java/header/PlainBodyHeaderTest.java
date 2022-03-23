package header;

import handler.header.PlainBodyHeader;
import io.netty.buffer.ByteBuf;
import org.junit.Before;
import org.junit.Test;
import server.TestUdpServer;
import util.CodeCTest;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 15:14
 */

public class PlainBodyHeaderTest {

    @Before
    public void setUp() throws Exception {
        TestUdpServer.run();
    }

    @Test
    public void testPlainBody() throws Exception {
        for (int i = 0; i < 20; i++) {
            ByteBuf byteBuf = PlainBodyHeader.getPlainBodyHeader(TestUdpServer.getChannel());
            int label = byteBuf.readInt();
            System.out.println(CodeCTest.bytesToString(CodeCTest.convertIntegerToBytes(label)) + " :" + PlainBodyHeader.getSequenceNumberFromLabel(label));
        }
    }
}

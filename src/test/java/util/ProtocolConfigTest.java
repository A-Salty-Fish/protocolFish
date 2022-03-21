package util;

import com.google.gson.Gson;
import handler.header.ShakeHandHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.TestUdpServer;

import java.nio.charset.StandardCharsets;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/21 14:37
 */

public class ProtocolConfigTest {

    Channel ch;

    @Before
    public void initClient() throws Exception {
        TestUdpServer.run();
        ch = TestUdpServer.getChannel();
    }

    @Test
    public void testGetProtocolHeader() throws Exception {
        ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        protocolConfig.setEnableDoubleCompression(true);
        protocolConfig.setVariableHeadByteLength(4);
        System.out.println(ProtocolConfig.convertCharSetToByte(StandardCharsets.UTF_8));
        ByteBuf buf = ShakeHandHeader.getShakeHandHeader(ch, protocolConfig);
        System.out.println(new Gson().toJson(ProtocolConfig.defaultConfig()));
        byte[] bytes = new byte[4];
        buf.readBytes(bytes);
        System.out.println(CodeCTest.bytesToString(bytes));
    }

}

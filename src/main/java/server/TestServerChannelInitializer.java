package server;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/13 16:38
 */

public class TestServerChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

    private EventLoopGroup group = new NioEventLoopGroup();

    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //pipeline.addLast("stringDecoder", new StringDecoder(Charset.forName("GBK")));
        pipeline.addLast(group, new TestServerHandler());
    }
}
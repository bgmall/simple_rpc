package net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import simple.net.NettyClient;
import simple.net.NettyClientOptions;
import simple.net.NettyServer;
import simple.net.NettyServerOptions;

import java.util.List;

public class NettyClientTest {

    private static NettyClient<SimpleMessage> nettyClient;
    private static NettyServer<SimpleMessage> nettyServer;

    @BeforeClass
    public static void setUp() {
        NettyClientOptions clientOptions = new NettyClientOptions();
        clientOptions.setConnectTimeout(1000);
        clientOptions.setIdleTimeoutSeconds(60);
        clientOptions.setKeepAlive(true);
        clientOptions.setMaxFrameLength(100);
        clientOptions.setReceiveBufferSize(64 * 1024);
        clientOptions.setSendBufferSize(64 * 1024);
        clientOptions.setWorkThreads(2);
        nettyClient = new NettyClient<>(clientOptions);
        nettyClient.encoder(new SimpleMessageEncoder());
        nettyClient.decoder(new SimpleMessageDecoder());
        nettyClient.messageHandler(new MessageHandler());
        nettyClient.channelIdleHandler(new ChannelIdleHandler());
        nettyClient.start();

        NettyServerOptions serverOptions = new NettyServerOptions();
        serverOptions.setBacklog(100);
        serverOptions.setConnectTimeout(1000);
        serverOptions.setIdleTimeoutSeconds(60);
        serverOptions.setKeepAlive(true);
        serverOptions.setMaxFrameLength(100);
        serverOptions.setReceiveBufferSize(64 * 1024);
        serverOptions.setSendBufferSize(64 * 1024);
        serverOptions.setWorkThreads(2);
        nettyServer = new NettyServer<>(serverOptions);
        nettyServer.encoder(new SimpleMessageEncoder());
        nettyServer.decoder(new SimpleMessageDecoder());
        nettyServer.messageHandler(new MessageHandler());
        nettyServer.channelIdleHandler(new ChannelIdleHandler());
        nettyServer.start(4000);
    }

    @AfterClass
    public static void tearDown() {
        nettyClient.shutdown();
        nettyServer.shutdown();
    }

    private static class SimpleMessageEncoder extends MessageToByteEncoder<SimpleMessage> {

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, SimpleMessage simpleMessage, ByteBuf byteBuf) throws Exception {
            System.out.println("encode: msgId=" + simpleMessage.getId() + ", data=" + simpleMessage.getStr());
            byte[] data = simpleMessage.getStr().getBytes();
            byteBuf.writeInt(simpleMessage.getId());
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }

    private static class SimpleMessageDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
                int msgId = byteBuf.readInt();
                int bodylength = byteBuf.readInt();
                byte[] data = new byte[bodylength];
                byteBuf.readBytes(data);
                String str = new String(data);
                System.out.println("decode: msgId=" + msgId + ", data=" + str);
                SimpleMessage simpleMessage = new SimpleMessage();
                simpleMessage.setId(msgId);
                simpleMessage.setStr(str);
                list.add(simpleMessage);
        }
    }

    private static class MessageHandler extends SimpleChannelInboundHandler<SimpleMessage> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleMessage simpleMessage) throws Exception {
            System.out.println(simpleMessage);
            if (simpleMessage.getStr().equals("hello")) {
                SimpleMessage returnMessage = new SimpleMessage();
                returnMessage.setId(2);
                returnMessage.setStr("world");
                channelHandlerContext.writeAndFlush(returnMessage);
            }
        }
    }

    private static class ChannelIdleHandler extends ChannelDuplexHandler {


        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.ALL_IDLE) {
                    // if no read and write for period time, close current channel
                    System.out.println("channel:" + ctx.channel()
                            + " ip=" + ctx.channel().remoteAddress() + " is idle for period time. close now.");
                    ctx.close();
                } else {
                    System.out.println("idle on channel[" + e.state() + "]:" + ctx.channel());
                }
            }
        }
    }

    @Test
    public void testHelloWorld() throws InterruptedException {
        ChannelFuture channelFuture = nettyClient.connect("127.0.0.1", 4000);
        Channel channel = channelFuture.sync().channel();
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setId(1);
        simpleMessage.setStr("hello");
        channel.writeAndFlush(simpleMessage);
        Thread.sleep(2000);
    }
}

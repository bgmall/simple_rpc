package simple.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.message.HeartBeatMessage;
import simple.net.protocol.message.NetMessage;

@ChannelHandler.Sharable
public class HeartBeatMessageServerHandler extends SimpleChannelInboundHandler<NetMessage> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HeartBeatMessageServerHandler.class);

    private static final HeartBeatMessage heartbeatMessage = new HeartBeatMessage();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {
        if (msg instanceof HeartBeatMessage) {
            logger.debug("recv heartbeat message and return");
            ctx.writeAndFlush(heartbeatMessage);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}

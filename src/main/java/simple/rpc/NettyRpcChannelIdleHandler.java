package simple.rpc;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class NettyRpcChannelIdleHandler extends ChannelDuplexHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyRpcChannelIdleHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                // if no read and write for period time, close current channel
                logger.debug("channel[{}], ip[{}] is idle for period time. close now.", ctx.channel(), ctx.channel().remoteAddress());
                ctx.close();
            } else {
                logger.debug("idle on channel[" + e.state() + "]:" + ctx.channel());
            }
        }
    }
}

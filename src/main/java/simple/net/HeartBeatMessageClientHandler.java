package simple.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.message.HeartBeatMessage;
import simple.net.protocol.message.NetMessage;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeartBeatMessageClientHandler extends SimpleChannelInboundHandler<NetMessage> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HeartBeatMessageClientHandler.class);

    private static final HeartBeatMessage heartBeatMessage = new HeartBeatMessage();

    private volatile ScheduledFuture<?> heartBeat;

    private int heartBeatIntervalMills;

    public HeartBeatMessageClientHandler(int heartBeatIntervalMills) {
        this.heartBeatIntervalMills = heartBeatIntervalMills;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (heartBeat == null) {
            logger.info("start heartbeat schedule task");
            heartBeat = ctx.executor().scheduleAtFixedRate(
                    new HeartBeatMessageClientHandler.HeartBeatTask(ctx), 0, heartBeatIntervalMills,
                    TimeUnit.MILLISECONDS);
        }
        ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (heartBeat != null) {
            logger.info("stop heartbeat schedule task");
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {
        if (!(msg instanceof HeartBeatMessage)) {
            ctx.fireChannelRead(msg);
        }
    }


    private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            logger.debug("send heartbeat message");
            ctx.writeAndFlush(heartBeatMessage);
        }
    }

}

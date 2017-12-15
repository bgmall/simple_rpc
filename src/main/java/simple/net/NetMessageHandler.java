package simple.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.handler.MessageDispatcher;
import simple.net.handler.MessageHandlerDesc;
import simple.net.handler.MessageHandlerManager;
import simple.net.protocol.message.NetMessage;

@ChannelHandler.Sharable
public class NetMessageHandler extends SimpleChannelInboundHandler<NetMessage> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetMessageHandler.class);

    private MessageDispatcher messageDispatcher;

    public NetMessageHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        messageDispatcher.channelOpened(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        messageDispatcher.channelClosed(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        messageDispatcher.exceptionCaught(ctx.channel(), cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {
        MessageHandlerDesc messageHandlerDesc = MessageHandlerManager.getInstance().getMessageHandler(msg.getMsgId());
        if (messageHandlerDesc == null) {
            logger.error("msgId[{}] can't find msg handler", msg.getMsgId());
            return;
        }

        messageDispatcher.messageReceived(ctx.channel(), msg, messageHandlerDesc);
    }
}

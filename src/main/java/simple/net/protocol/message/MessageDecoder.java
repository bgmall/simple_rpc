package simple.net.protocol.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.ProtocolFactory;
import simple.net.protocol.ProtocolFactoryManager;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageDecoder.class);

    private int maxFrameLength;

    public MessageDecoder(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < MessageHead.HEAD_LENGTH) {
            return;
        }

        in.markReaderIndex();
        int msgId = in.readInt();
        int length = in.readInt();

        if (length < 0 || length > maxFrameLength) {
            logger.error("package illegal length[{}], force closing channel[{}]", length, ctx.channel());
            ctx.close();
            return;
        }

        if (length > 0) {
            if (in.readableBytes() < length) {
                in.resetReaderIndex();
                return;
            }
        }

        in.resetReaderIndex();
        int protocolType = MessageManager.getInstance().getProtocolType(msgId);
        ProtocolFactory protocolFactory = ProtocolFactoryManager.getInstance().select(protocolType);
        if (protocolFactory == null) {
            logger.error("can't find protocol factory for msgId[{}], force closing channel[{}]", msgId, ctx.channel());
            ctx.close();
            return;
        }

        protocolFactory.decode(ctx, in, out);
    }
}

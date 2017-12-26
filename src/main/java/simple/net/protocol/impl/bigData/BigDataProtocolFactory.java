package simple.net.protocol.impl.bigData;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.ProtocolFactory;
import simple.net.protocol.ProtocolType;
import simple.net.protocol.codec.CodecFactory;
import simple.net.protocol.codec.CodecFactoryManager;
import simple.net.protocol.compress.CompressFactory;
import simple.net.protocol.compress.CompressFactoryManager;
import simple.net.protocol.compress.CompressType;
import simple.net.protocol.message.MessageManager;
import simple.net.protocol.message.NetMessage;

import java.util.List;

public class BigDataProtocolFactory implements ProtocolFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(BigDataProtocolFactory.class);

    private static final byte NO_COMPRESS = 0;

    private static final byte COMPRESSED = 1;

    @Override
    public int getProtocolType() {
        return ProtocolType.BIGDATA;
    }

    @Override
    public boolean checkValidMessage(Class<?> msgClass) {
        return BigDataMessage.class.isAssignableFrom(msgClass);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int msgId = in.readInt();
        int length = in.readInt();

        if (length < 5) {
            logger.error("invalid length for msgId[{}], force closing channel[{}]", msgId, ctx.channel());
            ctx.close();
            return;
        }

        byte isCompressed = in.readByte();
        int simpleDataLength = in.readInt();
        byte[] simpleData = null;

        if (simpleDataLength > 0) {
            simpleData = new byte[simpleDataLength];
            in.readBytes(simpleData);
            if (COMPRESSED == isCompressed) {
                int compressType = MessageManager.getInstance().getCompressType(msgId);
                CompressFactory compressFactory = CompressFactoryManager.getInstance().select(compressType);
                if (compressFactory == null) {
                    logger.error("can't find compress factory for msgId[{}], compressType[{}], force closing channel[{}]", msgId, compressType, ctx.channel());
                    ctx.close();
                    return;
                }

                simpleData = compressFactory.decompress(simpleData);
            }
        }

        int codecType = MessageManager.getInstance().getCodecType(msgId);
        CodecFactory codecFactory = CodecFactoryManager.getInstance().select(codecType);
        if (codecFactory == null) {
            logger.error("can't find codec factory for msgId[{}], codecType[{}], force closing channel[{}]", msgId, codecType, ctx.channel());
            ctx.close();
            return;
        }

        Object msg = codecFactory.decode(msgId, simpleData);
        if (msg == null) {
            logger.error("invalid message, message deserialize null object, msgId[{}], force closing channel[{}]", msgId, ctx.channel());
            ctx.close();
            return;
        }

        byte[] bigData = null;
        int bigDataLength = length - simpleDataLength - 5;
        if (bigDataLength > 0) {
            bigData = new byte[bigDataLength];
            in.readBytes(bigData);
            ((BigDataMessage) msg).setData(bigData);
        }

        out.add(msg);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception {
        int msgId = msg.getMsgId();
        int codecType = MessageManager.getInstance().getCodecType(msgId);
        CodecFactory codecFactory = CodecFactoryManager.getInstance().select(codecType);
        if (codecFactory == null) {
            logger.error("can't find codec factory for msgId[{}], codecType[{}], force closing channel[{}]", msgId, codecType, ctx.channel());
            ctx.close();
            return;
        }

        out.writeInt(msg.getMsgId());

        int simpleDataLength = 0;
        byte compressed = NO_COMPRESS;
        byte[] bigData = ((BigDataMessage) msg).getData();
        byte[] simpleData = codecFactory.encode(msg);
        if (simpleData != null && simpleData.length != 0) {
            int compressType = MessageManager.getInstance().getCompressType(msgId);
            int compressRequiredLength = MessageManager.getInstance().getCompressRequiredLength(msgId);
            if (compressType != CompressType.COMPRESS_NO && simpleData.length >= compressRequiredLength) {
                CompressFactory compressFactory = CompressFactoryManager.getInstance().select(compressType);
                if (compressFactory == null) {
                    logger.error("can't find compress factory for msgId[{}], compressType[{}], force closing channel[{}]", msgId, compressType, ctx.channel());
                    ctx.close();
                    return;
                }
                compressed = COMPRESSED;
                simpleData = compressFactory.compress(simpleData);
            }

            simpleDataLength = simpleData.length;
        }

        out.writeInt(simpleDataLength + bigData.length + 5);
        out.writeByte(compressed);
        out.writeInt(simpleDataLength);
        if (simpleDataLength > 0) {
            out.writeBytes(simpleData);
        }
        if (bigData.length > 0) {
            out.writeBytes(bigData);
        }
    }
}

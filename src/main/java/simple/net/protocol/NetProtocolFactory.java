package simple.net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.codec.CodecFactory;
import simple.net.protocol.codec.CodecFactoryManager;
import simple.net.protocol.compress.CompressFactory;
import simple.net.protocol.compress.CompressFactoryManager;
import simple.net.protocol.compress.CompressType;
import simple.net.protocol.message.MessageManager;
import simple.net.protocol.message.NetMessage;

import java.util.List;

public class NetProtocolFactory implements ProtocolFactory {

    private static final byte NO_COMPRESS = 0;

    private static final byte COMPRESSED = 1;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetProtocolFactory.class);

    @Override
    public int getProtocolType() {
        return ProtocolType.NET;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int msgId = in.readInt();
        int length = in.readInt();

        byte[] data = null;
        if (length > 0) {
            byte compress = in.readByte();
            int dataLength = length - 1;
            data = new byte[dataLength];
            in.readBytes(data);

            if (COMPRESSED == compress) {
                int compressType = MessageManager.getInstance().getCompressType(msgId);
                CompressFactory compressFactory = CompressFactoryManager.getInstance().select(compressType);
                if (compressFactory == null) {
                    logger.error("can't find compress factory for msgId[{}], compressType[{}], force closing channel[{}]", msgId, compressType, ctx.channel());
                    ctx.close();
                    return;
                }

                data = compressFactory.decompress(data);
//            // 预估压缩率50%
//            ByteBuf uncompressed = ctx.alloc().buffer((int) (length * 1.5f));
//            try {
//                int oldWriterIndex = in.writerIndex();
//                try {
//                    in.writerIndex(in.readerIndex() + length);
//                    compressFactory.decompress(in, length, uncompressed);
//                    data = new byte[uncompressed.readableBytes()];
//                    uncompressed.getBytes(0, data);
//                } finally {
//                    in.writerIndex(oldWriterIndex);
//                }
//            } finally {
//                if (uncompressed != null) {
//                    uncompressed.release();
//                }
//            }
            }
        }

        int codecType = MessageManager.getInstance().getCodecType(msgId);
        CodecFactory codecFactory = CodecFactoryManager.getInstance().select(codecType);
        if (codecFactory == null) {
            logger.error("can't find codec factory for msgId[{}], codecType[{}], force closing channel[{}]", msgId, codecType, ctx.channel());
            ctx.close();
            return;
        }

        Object decode = codecFactory.decode(msgId, data);
        out.add(decode);
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

        byte[] data = codecFactory.encode(msg);
        if (data == null || data.length == 0) {
            out.writeInt(0);
            return;
        }

        int compressType = MessageManager.getInstance().getCompressType(msgId);
        int compressRequiredLength = MessageManager.getInstance().getCompressRequiredLength(msgId);
        if (compressType == CompressType.COMPRESS_NO || data.length < compressRequiredLength) {
            out.writeInt(data.length + 1);
            out.writeByte(NO_COMPRESS);
            out.writeBytes(data);
        } else {
            CompressFactory compressFactory = CompressFactoryManager.getInstance().select(compressType);
            if (compressFactory == null) {
                logger.error("can't find compress factory for msgId[{}], compressType[{}], force closing channel[{}]", msgId, compressType, ctx.channel());
                ctx.close();
                return;
            }

            byte[] compress = compressFactory.compress(data);
            out.writeInt(compress.length + 1);
            out.writeByte(COMPRESSED);
            out.writeBytes(compress);
        }

//        ProtocolFactory protocolFactory = ProtocolFactoryManager.getInstance().select(msg.getCodeType());
//        out.writeInt(msg.getMsgId());
//        byte[] data = protocolFactory.encode(msg);
//        if (data == null || data.length == 0) {
//            out.writeByte(CompressType.COMPRESS_NO);
//            out.writeInt(0);
//        } else {
//            if (msg.getCompressType() == CompressType.COMPRESS_NO || data.length < requiredCompressLength) {
//                out.writeByte(CompressType.COMPRESS_NO);
//                out.writeInt(data.length);
//                out.writeBytes(data);
//            } else {
//                out.writeByte(msg.getCompressType());
//                int compressType = MessageManager.getInstance().getCompressType(msg.getMsgId());
//                CompressFactory compressFactory = CompressFactoryManager.getInstance().select(msg.getCompressType());
//                byte[] compress = compressFactory.compress(data);
//                out.writeInt(compress.length);
//                out.writeBytes(compress);
////                out.writeByte(msg.getCompressType());
////                int oldWriterIndex = out.writerIndex();
////                out.writeInt(data.length);
////                int compressType = MessageManager.getInstance().getCompressType(msg.getMsgId());
////                CompressFactory compressFactory = CompressFactoryManager.getInstance().select(msg.getCompressType());
////                compressFactory.compress(data, out);
////                // 实际压缩后的长度
////                int length = out.writerIndex() - oldWriterIndex - 4;
////                out.setInt(oldWriterIndex, length);
//            }
//        }
    }
}

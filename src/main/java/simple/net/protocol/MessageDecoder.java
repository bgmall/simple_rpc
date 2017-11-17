package simple.net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageDecoder.class);
    // 压缩标记占位高8位, 也就是说消息最大长度是2^23长度字节大小
    private static final int COMPRESS_BIT = 1 << 24;

    private int maxFrameLength;

    public MessageDecoder(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 8) {
            return;
        }

        in.markReaderIndex();
        int length = in.readInt();
        // 读取是否压缩以及真实长度
        int compress = length & COMPRESS_BIT;
        if (COMPRESS_BIT == compress) {
            length = length & (~COMPRESS_BIT);
        }

        if (length < 8 || length > maxFrameLength) {
            logger.error("package illegal length[{}], force closing channel[{}]", length, ctx.channel());
            ctx.close();
            return;
        }

        // length < msgId + dataBytes
        if (in.readableBytes() < length + 4) {
            in.resetReaderIndex();
            return;
        }

        int msgId = in.readInt();
        byte[] data = new byte[length];
        in.readBytes(data);

//        Object obj = SerializationUtil.deserialize(data, genericClass);
//        //Object obj = JsonUtil.deserialize(data,genericClass); // Not use this, have some bugs
//        out.add(obj);
    }
}
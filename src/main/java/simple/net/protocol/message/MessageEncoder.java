package simple.net.protocol.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import simple.net.protocol.ProtocolFactory;
import simple.net.protocol.ProtocolFactoryManager;

public class MessageEncoder extends MessageToByteEncoder<NetMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception {
        int protocolType = MessageManager.getInstance().getProtocolType(msg.getMsgId());
        ProtocolFactory protocolFactory = ProtocolFactoryManager.getInstance().select(protocolType);
        protocolFactory.encode(ctx, msg, out);
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

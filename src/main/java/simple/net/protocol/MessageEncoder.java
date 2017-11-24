package simple.net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    private ProtocolFactoryManager protocolFactoryManager;

    public MessageEncoder(ProtocolFactoryManager protocolFactoryManager) {
        this.protocolFactoryManager = protocolFactoryManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        ProtocolFactory protocolFactory = protocolFactoryManager.select(msg.getProtocolCode());
        out.writeByte(msg.getProtocolCode());
        out.writeInt(msg.getMsgId());
        byte[] data = protocolFactory.encode(msg);
        if (data == null || data.length == 0) {
            out.writeInt(0);
        } else {
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}

package simple.net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import simple.net.manager.ProtocolFactoryManager;

public class MessageEncoder extends MessageToByteEncoder<NetMessage> {

    private ProtocolFactorySelector protocolFactorySelector;

    public MessageEncoder(ProtocolFactoryManager protocolFactoryManager) {
        this.protocolFactorySelector = protocolFactoryManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception {
        ProtocolFactory protocolFactory = protocolFactorySelector.select(msg.getProtocolCode());
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

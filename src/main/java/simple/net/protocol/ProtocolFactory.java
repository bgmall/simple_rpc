package simple.net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import simple.net.protocol.message.NetMessage;

import java.util.List;

public interface ProtocolFactory {

    int getProtocolType();

    boolean checkValidMessage(Class<?> msgClass);

    void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception;
}

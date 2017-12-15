package simple.net.handler;

import io.netty.channel.Channel;
import simple.net.protocol.message.NetMessage;


public interface MessageDispatcher {

    void channelOpened(Channel channel) throws Exception;

    void channelClosed(Channel channel) throws Exception;

    void exceptionCaught(Channel channel, Throwable cause) throws Exception;

    void messageReceived(Channel channel, NetMessage message, MessageHandlerDesc handlerDesc) throws Exception;
}

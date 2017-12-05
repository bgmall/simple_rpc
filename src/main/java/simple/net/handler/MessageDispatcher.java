package simple.net.handler;

import simple.net.protocol.NetMessage;

public interface MessageDispatcher {

    void dispatch(NetMessage message, MessageHandlerDesc messageHandlerDesc);
}

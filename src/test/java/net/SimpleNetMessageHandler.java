package net;

import io.netty.channel.Channel;
import simple.net.handler.annotation.NetMessageHandler;
import simple.net.handler.annotation.NetMessageInvoke;

@NetMessageHandler
public class SimpleNetMessageHandler {

    @NetMessageInvoke(msgId = 1)
    public void handleSimpleMessage(Channel channel, SimpleNetMessage message) {
        System.out.println(message.getMsg());
        channel.writeAndFlush(new SimpleNetReturnMessage());
    }

    @NetMessageInvoke(msgId = 2)
    public void handleSimpleMessageReturn(SimpleNetReturnMessage returnMessage) {
        System.out.println(returnMessage.getReturnMsg());
    }
}

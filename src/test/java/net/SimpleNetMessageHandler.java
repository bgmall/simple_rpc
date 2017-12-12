package net;

import io.netty.channel.Channel;
import simple.net.handler.annotation.NetMessageHandler;
import simple.net.handler.annotation.NetMessageInvoke;

@NetMessageHandler
public class SimpleNetMessageHandler {

    @NetMessageInvoke(msgId = 1)
    public void handleSimpleMessage(Channel channel, SimpleNetMessage message) {
        System.out.println(channel);
        System.out.println(message.getMsg());
    }

}

package net;

import simple.net.handler.annotation.NetMessageHandler;
import simple.net.handler.annotation.NetMessageInvoke;

@NetMessageHandler
public class SimpleNetMessageHandler {

    @NetMessageInvoke(msgId = 1)
    public void handleSimpleMessage(SimpleNetMessage message) {
        System.out.println(message.getMsg());
    }

}

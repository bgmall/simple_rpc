package net;

import org.springframework.stereotype.Component;
import simple.net.handler.annotation.NetMessageHandler;
import simple.net.handler.annotation.NetMessageInvoke;

@Component
@NetMessageHandler
public class SimpleNetMessageHandler {

    @NetMessageInvoke(msgId = 1)
    public void handleSimpleMessage(SimpleNetMessage message) {
        System.out.println(message);
    }

}

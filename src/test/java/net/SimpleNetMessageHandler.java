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
        channel.writeAndFlush(new SimpleNetReturnMessage());
    }

    @NetMessageInvoke(msgId = 2)
    public void handleSimpleMessageReturn(SimpleNetReturnMessage returnMessage) {
        System.out.println(returnMessage.getReturnMsg());
    }

    @NetMessageInvoke(msgId = 3)
    public void handleBigDataMessage(Channel channel, SimpleBigDataMessage bigDataMessage) {
        if (bigDataMessage.getData() != null && bigDataMessage.getData().length != 0) {
            String str = new String(bigDataMessage.getData());
            System.out.println(str);
        }
        SimpleBigDataReturnMessage simpleBigDataReturnMessage = new SimpleBigDataReturnMessage();
        String strRes = "world, bigData";
        simpleBigDataReturnMessage.setData(strRes.getBytes());
        channel.writeAndFlush(simpleBigDataReturnMessage);
    }

    @NetMessageInvoke(msgId = 4)
    public void handleBigDataMessageReturn(SimpleBigDataReturnMessage returnMessage) {
        String str = new String(returnMessage.getData());
        System.out.println(str);
    }
}

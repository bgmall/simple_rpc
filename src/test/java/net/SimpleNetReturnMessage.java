package net;

import simple.net.protocol.annotation.NetProtocol;
import simple.net.protocol.message.NetMessage;

@NetProtocol(msgId = 2)
public class SimpleNetReturnMessage implements NetMessage {

    private String returnMsg = "world";

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}

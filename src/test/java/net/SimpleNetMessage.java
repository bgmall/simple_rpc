package net;

import simple.net.protocol.annotation.NetProtocol;
import simple.net.protocol.message.NetMessage;

@NetProtocol(msgId = 1)
public class SimpleNetMessage implements NetMessage {

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

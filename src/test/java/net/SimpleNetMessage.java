package net;

import simple.net.protocol.NetMessage;
import simple.net.protocol.annotation.NetProtocol;

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

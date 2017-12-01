package net;

import simple.net.protocol.AnnotationNetMessage;
import simple.net.protocol.annotation.NetProtocol;

@NetProtocol(msgId = 1, codec = 1)
public class SimpleNetMessage extends AnnotationNetMessage {

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

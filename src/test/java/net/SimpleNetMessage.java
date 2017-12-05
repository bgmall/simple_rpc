package net;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import simple.net.protocol.NetMessage;
import simple.net.protocol.annotation.NetProtocol;

@Component
@Scope("prototype")
@NetProtocol(msgId = 1, codec = 1)
public class SimpleNetMessage implements NetMessage {

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

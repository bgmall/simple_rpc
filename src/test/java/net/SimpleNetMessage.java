package net;

import simple.net.protocol.annotation.NetProtocol;
import simple.net.protocol.message.NetMessage;

import java.util.ArrayList;
import java.util.List;

@NetProtocol(msgId = 1)
public class SimpleNetMessage implements NetMessage {

    private String msg;

    //    @Morph
    private List<Integer> list = new ArrayList<>();

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Integer> getList() {
        return list;
    }
}

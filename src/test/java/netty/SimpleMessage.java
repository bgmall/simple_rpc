package netty;

import simple.netty.Message;

public class SimpleMessage implements Message {

    private int id;
    private String str;

    public int getMsgId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "SimpleMessage{" +
                "id=" + id +
                ", str='" + str + '\'' +
                '}';
    }
}

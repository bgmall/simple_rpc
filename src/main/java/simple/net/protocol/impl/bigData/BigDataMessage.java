package simple.net.protocol.impl.bigData;

import simple.net.protocol.message.NetMessage;

public abstract class BigDataMessage implements NetMessage {

    private transient byte[] data;

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

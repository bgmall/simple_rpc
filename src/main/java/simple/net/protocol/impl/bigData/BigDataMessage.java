package simple.net.protocol.impl.bigData;

import simple.net.protocol.message.NetMessage;

public abstract class BigDataMessage implements NetMessage {

    private static final byte[] EMPTY_BYTES = new byte[0];

    private transient byte[] data = EMPTY_BYTES;

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

package simple.rpc;

import simple.net.Message;

public class RpcMessage implements Message {

    private byte flag;
    // 唯一标识msg
    private long uuid;

    private int id;

    private byte[] data;

    @Override
    public int getId() {
        return id;
    }
}

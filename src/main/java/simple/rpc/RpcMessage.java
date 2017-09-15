package simple.rpc;

import simple.net.Message;

public class RpcMessage implements Message {

    private byte flag;

    private int id;

    private byte[] data;

    @Override
    public int getId() {
        return id;
    }
}

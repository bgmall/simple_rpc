package simple.rpc;

public enum TransportType {

    // 0
    MESSAGE((byte) 0),
    // 11
    RPC_ONEWAY((byte) (1 & 0x3)),
    // 101
    RPC_ASYNC((byte) (1 & 0x5)),
    // 1001
    RPC_SYNC((byte) (1 & 0x9));

    private byte flag;

    private TransportType(byte flag) {
        this.flag = flag;
    }

    public byte getFlag() {
        return this.flag;
    }
}

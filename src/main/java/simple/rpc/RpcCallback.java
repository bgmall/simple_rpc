package simple.rpc;

public interface RpcCallback {

    void result(RpcMessage response);

    void exceptionCaught(RuntimeException ex);
}

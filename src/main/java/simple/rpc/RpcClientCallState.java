package simple.rpc;

import io.netty.util.Timeout;

public class RpcClientCallState {

    private long correlationId;

    private RpcCallback callback;

    private Timeout timeout;

    public RpcClientCallState(RpcCallback callback) {
        this.callback = callback;
    }

    public void handleTimeout() {

    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public long getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(long correlationId) {
        this.correlationId = correlationId;
    }
}

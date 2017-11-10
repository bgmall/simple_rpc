package simple.rpc;

import io.netty.util.Timeout;

public class RpcClientCallState {

    private long correlationId;

    private RpcCallback callback;

    private Timeout timeout;

    public void handleException(RuntimeException ex) {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
        if (callback != null) {
            callback.exceptionCaught(ex);
        }
    }

    public void handleResponse(RpcMessage response) {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
        if (callback != null) {
            callback.result(response);
        }
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

    public RpcCallback getCallback() {
        return callback;
    }

    public void setCallback(RpcCallback callback) {
        this.callback = callback;
    }
}

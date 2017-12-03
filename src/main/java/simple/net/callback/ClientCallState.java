package simple.net.callback;

import io.netty.util.Timeout;
import simple.net.protocol.NetMessage;
import simple.rpc.RpcMessage;

/**
 * Created by Administrator on 2017/12/3.
 */
public class ClientCallState {

    private long callbackId;

    private MessageCallback callback;

    private Timeout timeout;

    public long getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(long callbackId) {
        this.callbackId = callbackId;
    }

    public MessageCallback getCallback() {
        return callback;
    }

    public void setCallback(MessageCallback callback) {
        this.callback = callback;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void handleException(RuntimeException ex) {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
        if (callback != null) {
            callback.exceptionCaught(ex);
        }
    }

    public void handleResponse(NetMessage response) {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
        if (callback != null) {
            callback.result(response);
        }
    }
}

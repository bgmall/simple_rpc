package simple.rpc.exception;

public class RpcSendTimeoutException extends RuntimeException {

    public RpcSendTimeoutException(String message) {
        super(message);
    }

    public RpcSendTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcSendTimeoutException(Throwable cause) {
        super(cause);
    }
}

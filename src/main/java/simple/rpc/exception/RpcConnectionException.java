package simple.rpc.exception;

public class RpcConnectionException extends RuntimeException {

    public RpcConnectionException(String message) {
        super(message);
    }

    public RpcConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcConnectionException(Throwable cause) {
        super(cause);
    }
}

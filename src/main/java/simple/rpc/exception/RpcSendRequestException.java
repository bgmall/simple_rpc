package simple.rpc.exception;

public class RpcSendRequestException extends RuntimeException {

    public RpcSendRequestException(String message) {
        super(message);
    }

    public RpcSendRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcSendRequestException(Throwable cause) {
        super(cause);
    }
}

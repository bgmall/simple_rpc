package simple.net.exception;

/**
 * Created by Administrator on 2017/12/3.
 */
public class SendTimeoutException extends RuntimeException {

    public SendTimeoutException(String message) {
        super(message);
    }

    public SendTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendTimeoutException(Throwable cause) {
        super(cause);
    }
}

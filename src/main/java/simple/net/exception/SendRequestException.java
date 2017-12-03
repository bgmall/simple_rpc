package simple.net.exception;

/**
 * Created by Administrator on 2017/12/3.
 */
public class SendRequestException extends RuntimeException {

    public SendRequestException(String message) {
        super(message);
    }

    public SendRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendRequestException(Throwable cause) {
        super(cause);
    }
}

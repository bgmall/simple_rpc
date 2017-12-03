package simple.net.protocol;

import simple.net.callback.MessageCallback;

/**
 * Created by Administrator on 2017/12/2.
 */
public interface CallbackMessage {

    long getCallbackId();

    long getTimeoutMills();

    MessageCallback getCallback();

}

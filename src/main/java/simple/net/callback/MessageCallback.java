package simple.net.callback;

import simple.net.protocol.NetMessage;

/**
 * Created by Administrator on 2017/12/2.
 */
public interface MessageCallback {

    void result(NetMessage message);

    void exceptionCaught(RuntimeException ex);
}

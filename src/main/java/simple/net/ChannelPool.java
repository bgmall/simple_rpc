package simple.net;

import simple.net.protocol.NetMessage;
import simple.netty.Message;

/**
 * Created by Administrator on 2017/12/2.
 */
public interface ChannelPool {

    NetConnection choose(NetMessage message);

    void close();

}

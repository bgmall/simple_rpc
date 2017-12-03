package simple.net;

import simple.net.protocol.NetMessage;

/**
 * Created by Administrator on 2017/12/2.
 */
public class SingleChannelPool implements ChannelPool {

    private NetConnection connection;

    private long reconnectIntervalMills;

    private long lastReconnectTimeMills;

    public SingleChannelPool(NetClient client) {
        this.reconnectIntervalMills = client.getClientOptions().getReconnectIntervalMills();
        connection = new NetConnection(client);
        connection.connect();
    }

    @Override
    public NetConnection choose(NetMessage message) {
        if (this.connection.invalidChannel()) {
            long now = System.currentTimeMillis();
            if (now - lastReconnectTimeMills >= reconnectIntervalMills) {
                lastReconnectTimeMills = now;
                this.connection.connect();
            }
        }
        return this.connection;
    }

    @Override
    public void close() {
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }
}

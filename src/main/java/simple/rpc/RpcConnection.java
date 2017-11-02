package simple.rpc;

import io.netty.channel.ChannelFuture;
import simple.net.NettyClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RpcConnection {

    /**
     * The future.
     */
    private ChannelFuture future;

    /**
     * The is connected.
     */
    private AtomicBoolean isConnected = new AtomicBoolean();

    /**
     * The request queue.
     */
    private BlockingQueue<RpcClientCallState> requestQueue;

    private NettyClient<RpcMessage> nettyClient;


}

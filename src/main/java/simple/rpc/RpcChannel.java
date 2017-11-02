package simple.rpc;

import io.netty.channel.ChannelFuture;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import simple.net.NettyClient;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RpcChannel {

    private String host;

    private int port;
    /**
     * The channelFuture.
     */
    private volatile ChannelFuture channelFuture;
    /**
     * The is connected.
     */
    private AtomicBoolean isConnected = new AtomicBoolean();

    private NettyClient<RpcMessage> nettyClient;

    private final Map<Long, RpcClientCallState> requestMap = new ConcurrentHashMap<>();
    /**
     * The request queue.
     */
    private BlockingQueue<RpcClientCallState> requestQueue;

    private AtomicLong correlationId = new AtomicLong(1);

    public RpcClientCallState removePendingRequest(long seqId) {
        return requestMap.remove(seqId);
    }

    public void registerPendingRequest(long seqId, RpcClientCallState state) {
        if (requestMap.containsKey(seqId)) {
            throw new IllegalArgumentException("State already registered");
        }
        requestMap.put(seqId, state);
    }

    public long getNextCorrelationId() {
        return correlationId.getAndIncrement();
    }

    public void doTransport(RpcConnection connection, RpcMessage rpcMessage, RpcCallback callback, long timeoutMills) {
        if (callback != null) {
            RpcClientCallState rpcClientCallState = new RpcClientCallState(callback);
            if (timeoutMills > 0) {
                Timeout timeout = createTimeout(rpcClientCallState, timeoutMills);
                rpcClientCallState.setTimeout(timeout);
            }
        }
    }

    private Timeout createTimeout(RpcClientCallState rpcClientCallState, long timeoutMills) {
        return nettyClient.getTimer().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                RpcClientCallState callState = removePendingRequest(rpcClientCallState.getCorrelationId());
                if (callState != null) {
                    rpcClientCallState.handleTimeout();
                }
            }
        }, timeoutMills, TimeUnit.MILLISECONDS);
    }

    private ChannelFuture getAndCreateChannel() {
        if (channelFuture != null && channelFuture.isSuccess()) {
            return channelFuture;
        }
        return null;
    }


}

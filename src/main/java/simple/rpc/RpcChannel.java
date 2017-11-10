package simple.rpc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.rpc.exception.RpcConnectionException;
import simple.rpc.exception.RpcSendRequestException;
import simple.rpc.exception.RpcSendTimeoutException;

import java.util.concurrent.TimeUnit;

public class RpcChannel {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RpcChannel.class);

    private volatile Channel channel;

    private RpcClient rpcClient;

    public RpcChannel(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public RpcMessage invokeSync(RpcMessage request, long timeoutMills) {
        Channel channel = getOrCreateChannel();
        if (invalidChannel(channel)) {
            throw createRpcConnectionException();
        }

        final long correlationId = request.getId();
        BlockingRpcCallback blockingRpcCallback = new BlockingRpcCallback();
        RpcClientCallState rpcClientCallState = new RpcClientCallState();
        rpcClientCallState.setCorrelationId(correlationId);
        rpcClientCallState.setCallback(blockingRpcCallback);
        rpcClient.registerPendingRequest(correlationId, rpcClientCallState);

        try {
            writeAndFlush(channel, request);

            blockingRpcCallback.waitFor(timeoutMills);
            if (blockingRpcCallback.getRuntimeException() != null) {
                throw blockingRpcCallback.getRuntimeException();
            } else if (blockingRpcCallback.getResponse() == null) {
                throw createRpcSendTimeoutException(request.getMsgId());
            }
            return blockingRpcCallback.getResponse();
        } finally {
            rpcClient.removePendingRequest(correlationId);
        }
    }

    public void invokeAsync(RpcMessage request, long timeoutMills, RpcCallback callback) {
        Channel channel = getOrCreateChannel();
        if (invalidChannel(channel)) {
            logger.error("channel to remote address[{}] is closed", rpcClient.getAddress());
            if (callback != null) {
                callback.exceptionCaught(createRpcConnectionException());
            }
            return;
        }

        final long correlationId = request.getId();
        RpcClientCallState rpcClientCallState = new RpcClientCallState();
        rpcClientCallState.setCorrelationId(correlationId);
        rpcClientCallState.setCallback(callback);
        rpcClient.registerPendingRequest(correlationId, rpcClientCallState);
        if (timeoutMills > 0) {
            Timeout timeout = createTimeout(rpcClientCallState, timeoutMills, request.getMsgId());
            rpcClientCallState.setTimeout(timeout);
        }

        writeAndFlush(channel, request);
    }

    public void invokeOneway(RpcMessage request) {
        Channel channel = getOrCreateChannel();
        if (invalidChannel(channel)) {
            throw createRpcConnectionException();
        }
        final int msgId = request.getMsgId();
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    throw createRpcSendRequestException(msgId, future.cause());
                }
            }
        });
    }

    private void writeAndFlush(Channel channel, RpcMessage request) {
        final long correlationId = request.getId();
        final int msgId = request.getMsgId();
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    RpcClientCallState callState = rpcClient.removePendingRequest(correlationId);
                    if (callState != null) {
                        logger.error("send msg[{}] to remote address[{}] exception", msgId, rpcClient.getAddress());
                        callState.handleException(createRpcSendRequestException(msgId, future.cause()));
                    }
                }
            }
        });
    }

    private Timeout createTimeout(RpcClientCallState rpcClientCallState, long timeoutMills, int msgId) {
        return rpcClient.getTimer().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                RpcClientCallState callState = rpcClient.removePendingRequest(rpcClientCallState.getCorrelationId());
                if (callState != null) {
                    logger.error("send msg[{}] to remote address[{}] timeout", msgId, rpcClient.getAddress());
                    rpcClientCallState.handleException(createRpcSendTimeoutException(msgId));
                }
            }
        }, timeoutMills, TimeUnit.MILLISECONDS);
    }

    private boolean invalidChannel(Channel channel) {
        return channel == null || !channel.isActive();
    }

    private RpcConnectionException createRpcConnectionException() {
        return new RpcConnectionException("channel to remote address[" + rpcClient.getAddress() + "] is closed");
    }

    private RpcSendRequestException createRpcSendRequestException(int msgId, Throwable cause) {
        return new RpcSendRequestException("send msg[" + msgId + "] to remote address[" + rpcClient.getAddress() + "] exception", cause);
    }

    private RpcSendTimeoutException createRpcSendTimeoutException(int msgId) {
        return new RpcSendTimeoutException("send msg[" + msgId + "] to remote address[" + rpcClient.getAddress() + "] timeout");
    }

    public synchronized Channel getOrCreateChannel() {
        if (this.channel != null && this.channel.isActive()) {
            return this.channel;
        }

        ChannelFuture channelFuture = rpcClient.connect();
        if (channelFuture.awaitUninterruptibly(rpcClient.getRpcClientOptions().getConnectTimeout())) {
            Channel returnChannel = channelFuture.channel();
            if (returnChannel != null && returnChannel.isActive()) {
                logger.info("open the channel[{}]", channel);
                this.channel = returnChannel;
            }
        } else {
            // connect timeout
            logger.error("connect to remote address[{}] timeout", rpcClient.getAddress());
        }
        return this.channel;
    }

    public synchronized void close() {
        if (channel != null) {
            channel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("close the channel[{}]", channel);
                }
            });
            this.channel = null;
        }
    }
}

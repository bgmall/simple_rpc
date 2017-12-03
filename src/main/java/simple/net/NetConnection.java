package simple.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.callback.ClientCallState;
import simple.net.exception.ConnectionException;
import simple.net.exception.SendRequestException;
import simple.net.protocol.CallbackMessage;
import simple.net.callback.MessageCallback;
import simple.net.protocol.NetMessage;
import simple.net.protocol.RetryMessage;
import simple.rpc.RpcClientCallState;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetConnection {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetConnection.class);

    private volatile Channel channel;

    private NetClient netClient;
    /**
     * The retry request queue.
     */
    private BlockingQueue<NetMessage> retryRequestQueue = new LinkedBlockingQueue<>();

    NetConnection(NetClient netClient) {
        this.netClient = netClient;
    }

    synchronized Channel connect() {
        if (!invalidChannel(this.channel)) {
            return this.channel;
        }

        ChannelFuture channelFuture = netClient.connect();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (channelFuture.isSuccess()) {
                    // 连接成功后，优先发送队列里面的消息
                    NetMessage requestMessage;
                    while (null != (requestMessage = retryRequestQueue.poll())) {
                        logger.debug("retry send msg[{}] to remote address[{}]", requestMessage.getMsgId(), netClient.getRemoteAddress());
                        writeAndFlush(future.channel(), requestMessage);
                    }
                }
            }
        });

        if (channelFuture.awaitUninterruptibly(netClient.getClientOptions().getConnectTimeout())) {
            Channel returnChannel = channelFuture.channel();
            if (returnChannel != null && returnChannel.isActive()) {
                logger.info("open the channel[{}]", channel);
                this.channel = returnChannel;
            }
        } else {
            // connect timeout
            logger.error("connect to remote address[{}] timeout", netClient.getRemoteAddress());
        }
        return this.channel;
    }

    synchronized void close() {
        if (channel != null) {
            retryRequestQueue.clear();
            channel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("close the channel[{}]", channel);
                }
            });
            this.channel = null;
        }
    }

    private void writeAndFlush(Channel channel, NetMessage message) {
        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    logger.error("send msg[{}] to remote address[{}] exception[{}]", message.getMsgId(), netClient.getRemoteAddress(), future.cause());
                    if (message instanceof RetryMessage) {
                        retryRequestQueue.add(message);
                    } else if (message instanceof CallbackMessage) {
                        CallbackMessage callbackMessage = (CallbackMessage) message;
                        ClientCallState callState = netClient.removePendingRequest(callbackMessage.getCallbackId());
                        if (callState != null) {
                            callState.handleException(createSendRequestException(message.getMsgId(), future.cause()));
                        }
                    }
                }
            }
        });
    }

    void writeAndFlush(NetMessage message) {
        Channel channel = this.channel;
        if (invalidChannel(channel)) {
            if (message instanceof RetryMessage) {
                retryRequestQueue.add(message);
            }
            return;
        }

        writeAndFlush(channel, message);
    }

    Channel getChannel() {
        return this.channel;
    }

    private boolean invalidChannel(Channel channel) {
        return channel == null || !channel.isActive();
    }

    public boolean invalidChannel() {
        return invalidChannel(this.channel);
    }

    private SendRequestException createSendRequestException(int msgId, Throwable cause) {
        return new SendRequestException("send msg[" + msgId + "] to remote address[" + netClient.getRemoteAddress() + "] exception", cause);
    }

}

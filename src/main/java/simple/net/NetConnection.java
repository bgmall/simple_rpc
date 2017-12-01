package simple.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.NetMessage;

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
                        writeAndFlush(future.channel(), requestMessage, true);
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

    private void writeAndFlush(Channel channel, NetMessage message, boolean retry) {
        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    logger.error("send msg[{}] to remote address[{}] exception[{}]", message.getMsgId(), netClient.getRemoteAddress(), future.cause());
                    if (retry) {
                        retryRequestQueue.add(message);
                    }
                }
            }
        });
    }

    void writeAndFlush(NetMessage message, boolean retry) {
        Channel channel = this.channel;
        if (invalidChannel(channel)) {
            if (retry) {
                retryRequestQueue.add(message);
            }
            return;
        }

        writeAndFlush(channel, message, retry);
    }

    Channel getChannel() {
        return this.channel;
    }

    boolean invalidChannel(Channel channel) {
        return channel == null || !channel.isActive();
    }
}

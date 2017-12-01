package simple.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import simple.net.manager.ProtocolFactoryManager;
import simple.net.protocol.MessageDecoder;
import simple.net.protocol.MessageEncoder;
import simple.net.protocol.NetMessage;
import simple.net.protocol.RetryMessage;
import simple.util.NettyUtil;

import java.util.concurrent.ThreadFactory;

public class NetClient extends Bootstrap {

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_STATE_HANDLER = "channle_state_handler";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HANDLER = "message_handler";

    private EventLoopGroup workerGroup;

    private NetClientOptions clientOptions;

    private String remoteAddress;

    private ProtocolFactoryManager protocolFactoryManager;

    private SimpleChannelInboundHandler<NetMessage> messageHandler;

    private NetConnection connection = new NetConnection(this);

    public static EventLoopGroup createWorkerGroup(int eventLoopThreads, ThreadFactory threadFactory) {
        if (NettyUtil.isLinuxPlatform()) {
            if (threadFactory != null) {
                return new EpollEventLoopGroup(eventLoopThreads, threadFactory);
            } else {
                return new EpollEventLoopGroup(eventLoopThreads);
            }
        } else {
            if (threadFactory != null) {
                return new NioEventLoopGroup(eventLoopThreads, threadFactory);
            } else {
                return new NioEventLoopGroup(eventLoopThreads);
            }
        }
    }

    public NetClient(NetClientOptions clientOptions, String host, int port) {
        this.remoteAddress = host + ":" + port;
        this.remoteAddress(host, port);
        this.clientOptions = clientOptions;
    }

    public void start() {
        EventLoopGroup workerGroup = createWorkerGroup(clientOptions.getEventLoopThreads(), null);
        this.workerGroup = workerGroup;
        start(workerGroup);
    }

    public void start(EventLoopGroup workerGroup) {
        Class<? extends Channel> channelClass;
        if (workerGroup instanceof EpollEventLoopGroup) {
            channelClass = EpollSocketChannel.class;
        } else {
            channelClass = NioSocketChannel.class;
        }

        this.group(workerGroup);
        this.channel(channelClass);
        this.handler(createNetClientChannelInitializer());
        this.option(ChannelOption.SO_REUSEADDR, clientOptions.isReuseAddress());
        this.option(ChannelOption.SO_SNDBUF, clientOptions.getSendBufferSize());
        this.option(ChannelOption.SO_RCVBUF, clientOptions.getReceiveBufferSize());
        this.option(ChannelOption.SO_KEEPALIVE, clientOptions.isKeepAlive());
        this.option(ChannelOption.TCP_NODELAY, clientOptions.isTcpNoDelay());
        this.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOptions.getConnectTimeout());
    }

    public void shutdown() {
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
    }

    public NetClientOptions getClientOptions() {
        return clientOptions;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public ProtocolFactoryManager getProtocolFactoryManager() {
        return protocolFactoryManager;
    }

    public void setProtocolFactoryManager(ProtocolFactoryManager protocolFactoryManager) {
        this.protocolFactoryManager = protocolFactoryManager;
    }

    public SimpleChannelInboundHandler<NetMessage> getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(SimpleChannelInboundHandler<NetMessage> messageHandler) {
        this.messageHandler = messageHandler;
    }

    private ChannelInitializer<Channel> createNetClientChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline channelPipe = ch.pipeline();

                if (protocolFactoryManager == null) {
                    throw new RuntimeException("protocol factory manager is null");
                }

                channelPipe.addLast(MESSAGE_ENCODER, new MessageEncoder(protocolFactoryManager));

                int idleTimeoutSeconds = getClientOptions().getIdleTimeoutSeconds();
                channelPipe.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
                channelPipe.addLast(CHANNEL_STATE_HANDLER, new NetChannelStateHandler());

                // 这边假设client可以不监听message，统一由server监听处理
                if (getMessageHandler() != null) {
                    int maxFrameLength = getClientOptions().getMaxFrameLength();
                    channelPipe.addLast(MESSAGE_DECODER, new MessageDecoder(maxFrameLength, protocolFactoryManager));
                    channelPipe.addLast(MESSAGE_HANDLER, getMessageHandler());
                }
            }
        };
    }

    public void sendMessage(NetMessage message) {
        boolean retry = message instanceof RetryMessage;
        connection.writeAndFlush(message, retry);
    }

    public boolean isConnected() {
        return !connection.invalidChannel(connection.getChannel());
    }

    public boolean tryToConnect() {
        if (!isConnected()) {
            Channel channel = connection.connect();
            return connection.invalidChannel(channel);
        }
        return true;
    }
}

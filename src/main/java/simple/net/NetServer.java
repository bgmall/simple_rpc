package simple.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.ProtocolFactoryManager;
import simple.net.protocol.MessageDecoder;
import simple.net.protocol.MessageEncoder;
import simple.net.protocol.NetMessage;
import simple.util.NettyUtil;

import java.net.InetSocketAddress;

public class NetServer extends ServerBootstrap {

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_STATE_HANDLER = "channle_state_handler";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HANDLER = "message_handler";

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetServer.class);

    private NetServerOptions serverOptions;
    /**
     * The boss group.
     */
    private EventLoopGroup bossGroup;
    /**
     * The worker group.
     */
    private EventLoopGroup workerGroup;
    /**
     * The channel.
     */
    private Channel channel;

    private ProtocolFactoryManager protocolFactoryManager;

    private SimpleChannelInboundHandler<NetMessage> messageHandler;

    public NetServer(NetServerOptions serverOptions) {
        this.serverOptions = serverOptions;

        Class<? extends ServerChannel> serverChannel;
        if (NettyUtil.isLinuxPlatform()) {
            this.bossGroup = new EpollEventLoopGroup(serverOptions.getAcceptorThreads());
            this.workerGroup = new EpollEventLoopGroup(serverOptions.getWorkThreads());
            serverChannel = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(serverOptions.getAcceptorThreads());
            this.workerGroup = new NioEventLoopGroup(serverOptions.getWorkThreads());
            serverChannel = NioServerSocketChannel.class;
        }

        this.group(this.bossGroup, this.workerGroup);
        this.channel(serverChannel);
        this.option(ChannelOption.SO_BACKLOG, serverOptions.getBacklog());

        this.childOption(ChannelOption.SO_KEEPALIVE, serverOptions.isKeepAlive());
        this.childOption(ChannelOption.SO_REUSEADDR, true);
        this.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        this.childOption(ChannelOption.TCP_NODELAY, serverOptions.isTcpNoDelay());
        this.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverOptions.getConnectTimeout());
        this.childOption(ChannelOption.SO_RCVBUF, serverOptions.getReceiveBufferSize());
        this.childOption(ChannelOption.SO_SNDBUF, serverOptions.getSendBufferSize());
        this.childHandler(createNetServerChannelInitializer());
    }

    public void start(int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        try {
            ChannelFuture channelFuture = this.bind(inetSocketAddress).sync();
            if (channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                logger.info("server address[{}] started", channel);
            }
        } catch (Throwable e) {
            shutdown();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void startAsync(int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        this.bind(inetSocketAddress).addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channel = future.channel();
                    logger.info("server address[{}] started", channel);
                } else {
                    shutdown();
                    throw new Exception("bind port failed:" + inetSocketAddress.toString() + " message:" + future.toString());
                }
            }
        });
    }

    public void shutdown() {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

    public NetServerOptions getServerOptions() {
        return serverOptions;
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

    private ChannelInitializer<Channel> createNetServerChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (protocolFactoryManager == null) {
                    throw new RuntimeException("protocol factory manager is null");
                }

                pipeline.addFirst(MESSAGE_ENCODER, new MessageEncoder(protocolFactoryManager));

                int idleTimeoutSeconds = getServerOptions().getIdleTimeoutSeconds();
                pipeline.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
                pipeline.addLast(CHANNEL_STATE_HANDLER, new NetChannelStateHandler());

                if (getMessageHandler() == null) {
                    throw new RuntimeException("message handler is null");
                }

                int maxFrameLength = getServerOptions().getMaxFrameLength();
                pipeline.addLast(MESSAGE_DECODER, new MessageDecoder(maxFrameLength, protocolFactoryManager));
                pipeline.addLast(MESSAGE_HANDLER, getMessageHandler());
            }
        };
    }
}

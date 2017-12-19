package simple.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.protocol.message.MessageDecoder;
import simple.net.protocol.message.MessageEncoder;
import simple.net.protocol.message.NetMessage;
import simple.util.NettyUtil;

import java.net.InetSocketAddress;

public class NetServer {

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_STATE_HANDLER = "channle_state_handler";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HEARTBEAT = "message_heartbeat";
    private static final String MESSAGE_HANDLER = "message_handler";

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetServer.class);

    private NetServerOptions serverOptions;

    private ServerBootstrap serverBootstrap;
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

    private SimpleChannelInboundHandler<NetMessage> messageHandler;

    public NetServer(NetServerOptions serverOptions) {
        this.serverOptions = serverOptions;
    }

    public void start() {
        if (getMessageHandler() == null) {
            throw new RuntimeException("message handler is null");
        }

        Class<? extends ServerChannel> serverChannel;
        if (NettyUtil.isLinuxPlatform()) {
            this.bossGroup = new EpollEventLoopGroup(serverOptions.getAcceptorThreads(), new DefaultThreadFactory("NetServerAcceptorIoThread"));
            this.workerGroup = new EpollEventLoopGroup(serverOptions.getWorkThreads(), new DefaultThreadFactory("NetServerWorkerIoThread"));
            serverChannel = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(serverOptions.getAcceptorThreads(), new DefaultThreadFactory("NetServerAcceptorIoThread"));
            this.workerGroup = new NioEventLoopGroup(serverOptions.getWorkThreads(), new DefaultThreadFactory("NetServerWorkerIoThread"));
            serverChannel = NioServerSocketChannel.class;
        }

        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(this.bossGroup, this.workerGroup);
        serverBootstrap.channel(serverChannel);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, serverOptions.getBacklog());

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, serverOptions.isKeepAlive());
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, serverOptions.isTcpNoDelay());
        serverBootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverOptions.getConnectTimeout());
        serverBootstrap.childOption(ChannelOption.SO_RCVBUF, serverOptions.getReceiveBufferSize());
        serverBootstrap.childOption(ChannelOption.SO_SNDBUF, serverOptions.getSendBufferSize());
        serverBootstrap.childHandler(createNetServerChannelInitializer());

        listen(serverOptions.getListenPort());
    }

    private void listen(int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(inetSocketAddress).sync();
            if (channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                logger.info("server address[{}] started", channel);
            }
        } catch (Throwable e) {
            shutdown();
            throw new RuntimeException(e.getMessage(), e);
        }
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

                pipeline.addFirst(MESSAGE_ENCODER, new MessageEncoder());

                int idleTimeoutSeconds = getServerOptions().getIdleTimeoutSeconds();
                pipeline.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, 0, 0));
                pipeline.addLast(CHANNEL_STATE_HANDLER, new NetChannelStateHandler());

                int maxFrameLength = getServerOptions().getMaxFrameLength();
                pipeline.addLast(MESSAGE_DECODER, new MessageDecoder(maxFrameLength));
                pipeline.addLast(MESSAGE_HEARTBEAT, new HeartBeatMessageServerHandler());
                pipeline.addLast(MESSAGE_HANDLER, getMessageHandler());
            }
        };
    }
}

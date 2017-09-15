package simple.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;

public class NettyServer<M extends Message> extends ServerBootstrap implements NettyService {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyServer.class);

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_IDLE_HANDLER = "channle_idle_handler";
    private static final String FRAME_DECODER = "frame_decoder";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HANDLER = "message_handler";

    private Channel serverChannel;
    private InetSocketAddress serverAddress;
    private ByteToMessageDecoder decoder;
    private MessageToByteEncoder<M> encoder;
    private SimpleChannelInboundHandler<M> messageHandler;
    private ChannelDuplexHandler channelIdleHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int idleTimeoutSeconds;
    private int maxFrameLength;
    private int listenPort;

    public NettyServer() {}

    public NettyServer(NettyServerOptions serverOptions) {
        Class<? extends ServerChannel> serverChannelClass;
        if ("Linux".equals(System.getProperty("os.name"))) {
            // required lib : netty-transport-native-epoll
            this.bossGroup = new EpollEventLoopGroup(2);
            this.workerGroup = new EpollEventLoopGroup(serverOptions.getWorkThreads());
            serverChannelClass = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(2);
            this.workerGroup = new NioEventLoopGroup(serverOptions.getWorkThreads());
            serverChannelClass = NioServerSocketChannel.class;
        }

        this.group(bossGroup, workerGroup);
        this.channel(serverChannelClass);
        this.option(ChannelOption.SO_BACKLOG, serverOptions.getBacklog());
        this.option(ChannelOption.SO_REUSEADDR, serverOptions.isReuseAddress());

        this.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        this.childOption(ChannelOption.SO_REUSEADDR, serverOptions.isReuseAddress());
        this.childOption(ChannelOption.SO_SNDBUF, serverOptions.getSendBufferSize());
        this.childOption(ChannelOption.SO_RCVBUF, serverOptions.getReceiveBufferSize());
        this.childOption(ChannelOption.SO_KEEPALIVE, serverOptions.isKeepAlive());
        this.childOption(ChannelOption.TCP_NODELAY, serverOptions.isTcpNoDelay());
        this.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverOptions.getConnectTimeout());
        maxFrameLength(serverOptions.getMaxFrameLength());
        idleTimeoutSeconds(serverOptions.getIdleTimeoutSeconds());
    }

    public NettyServer<M> decoder(ByteToMessageDecoder decoder) {
        this.decoder = decoder;
        return this;
    }

    public NettyServer<M> encoder(MessageToByteEncoder<M> encoder) {
        this.encoder = encoder;
        return this;
    }

    public NettyServer<M> messageHandler(SimpleChannelInboundHandler<M> handler) {
        this.messageHandler = handler;
        return this;
    }

    public NettyServer<M> channelIdleHandler(ChannelDuplexHandler channelIdleHandler) {
        this.channelIdleHandler = channelIdleHandler;
        return this;
    }

    public NettyServer<M> idleTimeoutSeconds(int idleSeconds) {
        this.idleTimeoutSeconds = idleSeconds;
        return this;
    }

    public NettyServer<M> maxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
        return this;
    }

    public NettyServer<M> listenPort(int listenPort) {
        this.listenPort = listenPort;
        return this;
    }

    private void verify() {
        if (this.messageHandler == null) {
            throw new IllegalStateException("message handler not set");
        }
        if (this.channelIdleHandler == null) {
            throw new IllegalStateException("channel idle handler not set");
        }
        if (encoder == null) {
            throw new IllegalStateException("encoder not set");
        }
        if (decoder == null) {
            throw new IllegalStateException("decoder not set");
        }
        if (listenPort == 0) {
            throw new IllegalStateException("listen port not set");
        }
    }

    public void start(int port) {
        listenPort(port);
        start();
    }

    public void start() {
        this.verify();
        this.childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, idleTimeoutSeconds, idleTimeoutSeconds));
                pipeline.addLast(CHANNEL_IDLE_HANDLER, channelIdleHandler);
                pipeline.addLast(FRAME_DECODER, new LengthFieldBasedFrameDecoder(maxFrameLength, 4, 4));
                pipeline.addLast(MESSAGE_DECODER, decoder);
                pipeline.addLast(MESSAGE_HANDLER, messageHandler);
                pipeline.addFirst(MESSAGE_ENCODER, encoder);
            }
        });
        this.validate();

        ChannelFuture sync = null;
        try {
            sync = this.bind(listenPort).sync();
            this.serverChannel = sync.channel();
            this.serverAddress = (InetSocketAddress) this.serverChannel.localAddress();
            logger.debug("server start, address={}", this.serverAddress.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException("ServerBootstrap.bind().sync() InterruptedException", e);
        }
    }

    public void shutdown() {
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.debug("server shutdown finish");
    }
}
